package net.vyhub.VyHubMinecraft;

import com.google.common.reflect.TypeToken;
import net.luckperms.api.LuckPerms;
import net.vyhub.VyHubMinecraft.lib.Cache;
import net.vyhub.VyHubMinecraft.lib.PlayerGivenPermissionListener;
import net.vyhub.VyHubMinecraft.lib.Types;
import net.vyhub.VyHubMinecraft.lib.Utility;
import net.vyhub.VyHubMinecraft.server.*;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;


public class VyHub extends JavaPlugin {

    private LuckPerms luckPerms;

    public static BukkitScheduler scheduler = Bukkit.getScheduler();

    private static int readyCheckTaskID;
    private static int playerTimeID;

    public static Map<String, String> config = new HashMap<>();

    private static Logger logger = Bukkit.getServer().getLogger();

    private static Cache<Map<String, String>> configCache = new Cache<>(
            "config",
            new TypeToken<HashMap<String, String>>() {
            }.getType()
    );

    public static JavaPlugin plugin;

    @Override
    public void onEnable() {
        plugin = this;

        // Plugin startup logic
        this.luckPerms = getServer().getServicesManager().load(LuckPerms.class);
        new PlayerGivenPermissionListener(this, this.luckPerms).register();

        readyCheckTaskID = scheduler.runTaskTimer(this, VyHub::checkReady, 0, 20L * 60L).getTaskId();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static void onReady() {
        scheduler.cancelTask(readyCheckTaskID);
        scheduler.cancelTask(playerTimeID);

        listenerRegistration();
        commandRegistration();
        SvRewards.loadExecuted();

        scheduler.runTaskTimerAsynchronously(plugin, SvServer::patchServer, 20L * 1L, 20L * 60L);
        scheduler.runTaskTimerAsynchronously(plugin, SvBans::syncBans, 20L * 1L, 20L * 60L);
        scheduler.runTaskTimerAsynchronously(plugin, SvStatistics::playerTime, 20L * 1L, 20L * 60L);
        scheduler.runTaskTimerAsynchronously(plugin, SvRewards::getRewards, 20L * 5L, 20L * 60L);
        scheduler.runTaskTimer(plugin, SvRewards::runDirectRewards, 20L * 1L, 20L * 60L);
        scheduler.runTaskTimerAsynchronously(plugin, SvStatistics::sendPlayerTime, 20L * 5L, 20L * 60L * 30L);
        scheduler.runTaskTimerAsynchronously(plugin, SvAdverts::loadAdverts, 20L * 1L, 20L * 60L * 5L);
        scheduler.runTaskTimerAsynchronously(plugin, SvAdverts::nextAdvert, 20L * 5L, 20L * Integer.parseInt(VyHub.config.getOrDefault("advertInterval", "180")));
    }


    private static void listenerRegistration() {
        PluginManager pluginManager = Bukkit.getPluginManager();
        pluginManager.registerEvents(new SvUser(), plugin);
        pluginManager.registerEvents(new SvGroups(), plugin);
        pluginManager.registerEvents(new SvRewards(), plugin);
    }

    private static void commandRegistration() {
        plugin.getCommand("login").setExecutor(new SvLogin());
        plugin.getCommand("timeban").setExecutor(new SvBans());
        plugin.getCommand("warn").setExecutor(new SvWarning());
    }


    public static Map<String, String> loadConfig() {
        Map<String, String> configMap = configCache.load();

        if (configMap == null) {
            logger.log(Level.WARNING, "Config File does not exist. Please update config.json File");

            config = new HashMap<>();
            config.put("apiURL", "");
            config.put("apiKey", "");
            config.put("serverID", "");
            config.put("advertPrefix", "[★] ");
            config.put("advertInterval", "180");

            configCache.save(config);
        } else {
            config = configMap;
        }

        return config;
    }

    public static void checkReady() {
        Logger logger = plugin.getLogger();

        Map<String, String> config = loadConfig();

        if (config.get("apiURL").isEmpty() || config.get("apiKey").isEmpty() || config.get("serverID").isEmpty()) {
            logger.warning("VyHub config is missing values! Please follow the installation instructions.");
            return;
        }

        HttpResponse<String> response = Utility.sendRequest("/user/current", Types.GET);

        if (response == null || (response.statusCode() != 200 && response.statusCode() != 307)) {
            playerTimeID = scheduler.runTaskTimer(plugin, SvStatistics::playerTime, 20L * 1L, 20L * 60L).getTaskId();
            commandRegistration();
            logger.warning("Cannot connect to VyHub API! Please follow the installation instructions.");
            return;
        }

        SvServer.getServerInformation();

        if (SvServer.serverbundleID == null || SvServer.serverbundleID.isEmpty()) {
            logger.warning("Cannot fetch serverbundle id from VyHub API! Please follow the installation instructions.");
            return;
        }

        logger.info("Successfully connected to VyHub API.");

        VyHub.onReady();
    }
}
