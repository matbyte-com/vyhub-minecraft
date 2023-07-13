package net.vyhub;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.event.EventBus;
import net.luckperms.api.event.node.NodeAddEvent;
import net.luckperms.api.event.node.NodeClearEvent;
import net.luckperms.api.event.node.NodeRemoveEvent;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.scheduler.TaskScheduler;
import net.vyhub.abstractClasses.AServer;
import net.vyhub.command.Config;
import net.vyhub.command.Setup;
import net.vyhub.config.I18n;
import net.vyhub.config.VyHubConfiguration;
import net.vyhub.lib.Utility;
import net.vyhub.tasks.TServer;
import net.vyhub.tasks.TUser;
import okhttp3.OkHttpClient;
import retrofit2.Response;

import java.io.File;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static java.util.logging.Level.INFO;
import static java.util.logging.Level.WARNING;

public class VyHubPlugin extends Plugin {
    public static Plugin plugin;
    private static int readyCheckTaskID;
    private BungeeVyHubPlatform platform;
    private TaskScheduler scheduler;
    private VyHubAPI apiClient;
    private OkHttpClient httpClient;
    private I18n i18n;
    public Config config;
    public TUser tUser;
    public TServer tServer;

    public I18n getI18n() {
        return i18n;
    }

    public VyHubAPI getApiClient() {
        return apiClient;
    }
    public BungeeVyHubPlatform getPlatform() {
        return platform;
    }

    @Override
    public void onEnable() {
        plugin = this;
        platform = new BungeeVyHubPlatform(this);
        scheduler = getProxy().getScheduler();

        config = new Config(this);
        tUser = new TUser(this.platform);
        tServer = new TServer(this.platform, this.tUser);

        // Configuration, I18N and API Client
        VyHubConfiguration.setPlatform(platform);
        VyHubConfiguration.loadConfig();
        i18n = new I18n(VyHubConfiguration.getLocale());
        httpClient = Utility.okhttp(new File(getDataFolder(), "cache"));

        sendStartupMessage();

        getProxy().getPluginManager().registerCommand(this, new Config(this));
        getProxy().getPluginManager().registerCommand(this, new Setup(this));
        // TODO ADD other Config Command
        //plugin.getCommand("vh_setup").setExecutor(config);

        readyCheckTaskID = scheduler.schedule(plugin, this::checkReady, 0, 1, TimeUnit.MINUTES).getId();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public void onReady() {
        scheduler.cancel(readyCheckTaskID);
        scheduler.schedule(plugin, tServer::patchServer, 0, 1, TimeUnit.MINUTES);
    }

    public void checkReady() {
        String apiBaseUrl = VyHubConfiguration.getApiUrl();
        final String apiKey = VyHubConfiguration.getApiKey();

        if (!apiBaseUrl.endsWith("/")) {
            apiBaseUrl = apiBaseUrl + "/";
        }

        if (Objects.equals(apiBaseUrl, "") || Objects.equals(apiKey, "")) {
            this.platform.log(INFO, "Looks like this is a fresh setup. Get started by creating a server and generating the correct command in your VyHub server settings.");
        } else {
            this.platform.log(INFO, "Validating API credentials...");
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

    public void sendStartupMessage() {
        ProxyServer.getInstance().getConsole().sendMessage(new TextComponent(""));
        ProxyServer.getInstance().getConsole().sendMessage(new TextComponent("  \\  / |__|   " + ChatColor.DARK_RED + "VyHub" + ChatColor.AQUA + " v" + plugin.getDescription().getVersion()));
        ProxyServer.getInstance().getConsole().sendMessage(new TextComponent("   \\/  |  |   " + ChatColor.DARK_GRAY + "Running on BungeeCord"));
        ProxyServer.getInstance().getConsole().sendMessage(new TextComponent(""));
    }
}
