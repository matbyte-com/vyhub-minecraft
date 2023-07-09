package net.vyhub;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.event.EventBus;
import net.luckperms.api.event.node.NodeAddEvent;
import net.luckperms.api.event.node.NodeClearEvent;
import net.luckperms.api.event.node.NodeRemoveEvent;
import net.vyhub.abstractClasses.AServer;
import net.vyhub.command.*;
import net.vyhub.config.I18n;
import net.vyhub.config.VyHubConfiguration;
import net.vyhub.lib.PlayerGivenPermissionListener;
import net.vyhub.lib.Utility;
import net.vyhub.tasks.*;
import okhttp3.OkHttpClient;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import retrofit2.Response;

import java.io.File;
import java.util.Objects;

import static java.util.logging.Level.INFO;
import static java.util.logging.Level.WARNING;

public class VyHubPlugin extends JavaPlugin {
    public static JavaPlugin plugin;
    private LuckPerms luckPerms;

    private final VyHubConfiguration configuration = new VyHubConfiguration();
    public static BukkitScheduler scheduler = Bukkit.getScheduler();
    private static int readyCheckTaskID;
    private static int playerTimeID;
    private BukkitVyHubPlatform platform;
    private VyHubAPI apiClient;
    private OkHttpClient httpClient;
    private I18n i18n;
    public Ban ban;
    public Config config;
    public Login login;
    public Warn warn;
    public TAdvert tAdvert;
    public TGroups tGroups;
    public TRewards tRewards;
    public TServer tServer;
    public TStatistics tStatistics;
    public TUser tUser;

    public I18n getI18n() {
        return i18n;
    }

    public VyHubAPI getApiClient() {
        return apiClient;
    }
    public BukkitVyHubPlatform getPlatform() {
        return platform;
    }

    @Override
    public void onEnable() {
        plugin = this;
        platform = new BukkitVyHubPlatform(this);
        // Load Command and Task classes
        tUser = new TUser(this.platform);
        config = new Config(this);
        login = new Login(this);
        warn = new Warn(this.platform, this.ban, this.tUser);
        tAdvert = new TAdvert(this.platform);
        tServer = new TServer(this.platform, this.tUser);
        tRewards = new TRewards(this.platform, this.tUser);
        tStatistics = new TStatistics(this.platform, this.tUser);

        // Configuration, I18N and API Client
        VyHubConfiguration.loadConfig();
        i18n = new I18n(VyHubConfiguration.getLocale());
        httpClient = Utility.okhttp(new File(getDataFolder(), "cache"));

        playerTimeID = scheduler.runTaskTimer(plugin, tStatistics::playerTime, 20L * 1L, 20L * 60L).getTaskId();


        // Plugin startup logic
        if (luckPermsInstalled()) {
            tGroups = new TGroups(this.platform, this.tUser);
            this.luckPerms = getServer().getServicesManager().load(LuckPerms.class);
            new PlayerGivenPermissionListener(this, this.luckPerms).register();
        } else {
            this.platform.log(INFO, "LuckPerms not found. Disabling group sync");
        }

        ban = new Ban(this.platform, this.tUser, this.tGroups);


        plugin.getCommand("vh_config").setExecutor(config);
        plugin.getCommand("vh_setup").setExecutor(config);

        readyCheckTaskID = scheduler.runTaskTimer(this, this::checkReady, 0, 20L * 60L).getTaskId();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public void onReady() {
        scheduler.cancelTask(readyCheckTaskID);
        scheduler.cancelTask(playerTimeID);

        listenerRegistration();
        commandRegistration();
        TRewards.loadExecuted();

        scheduler.runTaskTimerAsynchronously(plugin, tServer::patchServer, 20L * 1L, 20L * 60L);
        scheduler.runTaskTimerAsynchronously(plugin, ban::syncBans, 20L * 1L, 20L * 60L);
        scheduler.runTaskTimerAsynchronously(plugin, tStatistics::playerTime, 20L * 1L, 20L * 60L);
        scheduler.runTaskTimerAsynchronously(plugin, tRewards::fetchRewards, 20L * 5L, 20L * 60L);
        scheduler.runTaskTimer(plugin, tRewards::runDirectRewards, 20L * 1L, 20L * 60L);
        scheduler.runTaskTimerAsynchronously(plugin, tStatistics::sendPlayerTime, 20L * 5L, 20L * 60L); // *30L
        scheduler.runTaskTimerAsynchronously(plugin, tAdvert::loadAdverts, 20L * 1L, 20L * 60L * 5L);
        scheduler.runTaskTimerAsynchronously(plugin, tAdvert::nextAdvert, 20L * 5L, 20L * Integer.parseInt(VyHubConfiguration.getAdvertInterval()));

        if (luckPermsInstalled()) {
            scheduler.runTaskTimerAsynchronously(plugin, tGroups::updateGroups, 20L * 1L, 20L * 60L); // *5L
            scheduler.runTaskTimerAsynchronously(plugin, tGroups::syncGroupsForAll, 20L * 60L, 20L * 60L); // *10L und *8L
        }
    }


    private void listenerRegistration() {
        PluginManager pluginManager = Bukkit.getPluginManager();
        pluginManager.registerEvents(tUser, plugin);
        pluginManager.registerEvents(tRewards, plugin);

        if (!luckPermsInstalled()) {
            return;
        }

        pluginManager.registerEvents(tGroups, plugin);

        LuckPerms luckPerms = LuckPermsProvider.get();
        EventBus eventBus = luckPerms.getEventBus();

        eventBus.subscribe(plugin, NodeAddEvent.class, tGroups::onNodeMutate);
        eventBus.subscribe(plugin, NodeRemoveEvent.class, tGroups::onNodeMutate);
        eventBus.subscribe(plugin, NodeClearEvent.class, tGroups::onNodeMutate);
    }

    private void commandRegistration() {
        plugin.getCommand("login").setExecutor(login);
        plugin.getCommand("timeban").setExecutor(ban);
        plugin.getCommand("warn").setExecutor(warn);
    }

    public void checkReady() {
        String apiBaseUrl = VyHubConfiguration.getApiUrl();
        final String apiKey = VyHubConfiguration.getApiKey();

        if (!apiBaseUrl.endsWith("/")) {
            apiBaseUrl = apiBaseUrl + "/";
        }

        if (Objects.equals(apiBaseUrl, "") || Objects.equals(apiKey, "")) {
            getLogger().log(INFO, "Looks like this is a fresh setup. Get started by using 'vh_config' in the console.");
        } else {
            getLogger().info("Validating API credentials...");
            apiClient = VyHubAPI.create(apiBaseUrl, apiKey, httpClient);
        }


        Response response = null;
        try {
            response = apiClient.getServer(VyHubConfiguration.getServerId()).execute();
        } catch (Exception e) {
            this.platform.log(WARNING, "Cannot connect to VyHub API! Please follow the installation instructions.");
            return;
        }

        if (response == null || !response.isSuccessful()) {
            this.platform.log(WARNING, "Cannot connect to VyHub API! Please follow the installation instructions.");
            return;
        }

        tServer.getServerInformation();

        if (AServer.serverbundleID == null || AServer.serverbundleID.isEmpty()) {
            this.platform.log(WARNING, "Cannot fetch serverbundle id from VyHub API! Please follow the installation instructions.");
            return;
        }

        this.platform.log(INFO, "Successfully connected to VyHub API.");
        this.onReady();
    }

    public boolean luckPermsInstalled() {
        return Bukkit.getPluginManager().isPluginEnabled("LuckPerms");
    }
}
