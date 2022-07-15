package net.vyhub.VyHubMinecraft;


import net.vyhub.VyHubMinecraft.lib.PlayerGivenPermissionListener;
import net.vyhub.VyHubMinecraft.lib.Types;
import net.vyhub.VyHubMinecraft.lib.Utility;
import net.vyhub.VyHubMinecraft.server.*;
import net.luckperms.api.LuckPerms;
import net.vyhub.VyHubMinecraft.server.*;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.scheduler.BukkitScheduler;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public class VyHub extends JavaPlugin {

    private LuckPerms luckPerms;

    private static BukkitScheduler scheduler = Bukkit.getScheduler();

    private static int readyCheckTaskID;

    @Override
    public void onEnable() {
        // Plugin startup logic
        checkConfig();

        this.luckPerms = getServer().getServicesManager().load(LuckPerms.class);
        new PlayerGivenPermissionListener(this, this.luckPerms).register();

        readyCheckTaskID = scheduler.runTaskTimer(this, VyHub::checkReady, 0, 20L*60L).getTaskId();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static void onReady() {
        JavaPlugin plugin = getPlugin(VyHub.class);

        scheduler.cancelTask(readyCheckTaskID);

        listenerRegistration();
        commandRegistration();
        SvRewards.loadExecuted();

        scheduler.runTaskTimer(plugin, SvServer::patchServer, 20L*1L, 20L*60L);
        scheduler.runTaskTimer(plugin, SvBans::getVyHubBans, 20L*1L, 20L*60L);
        scheduler.runTaskTimer(plugin, SvStatistics::playerTime, 20L*1L, 20L*60L);
        scheduler.runTaskTimer(plugin, SvRewards::getRewards, 20L*1L, 20L*60L);
        scheduler.runTaskTimer(plugin, SvRewards::runDirectRewards, 20L*1L, 20L*60L);
        scheduler.runTaskTimer(plugin, SvStatistics::sendPlayerTime, 20L*1L, 20L*60L*60L);
    }


    private static void listenerRegistration() {
        JavaPlugin plugin = getPlugin(VyHub.class);

        PluginManager pluginManager = Bukkit.getPluginManager();
        pluginManager.registerEvents(new SvUser(), plugin);
        pluginManager.registerEvents(new SvGroups(), plugin);
        pluginManager.registerEvents(new SvRewards(), plugin);
    }

    private static void commandRegistration() {
        JavaPlugin plugin = getPlugin(VyHub.class);

        plugin.getCommand("login").setExecutor(new SvLogin());
        plugin.getCommand("timeban").setExecutor(new SvBans());
        plugin.getCommand("warn").setExecutor(new SvWarning());
    }


    public static Map<String, String> checkConfig() {
        JSONParser jsonParser = new JSONParser();
        Map<String, String> configMap = new HashMap<>();

        try (FileReader reader = new FileReader("plugins/VyHub/config.json"))
        {
            JSONObject jsonObj = (JSONObject) jsonParser.parse(reader);

            configMap.put("apiUrl", (String) jsonObj.get("API-URL"));
            configMap.put("apiKey", (String) jsonObj.get("API-Key"));
            configMap.put("serverId", (String) jsonObj.get("Server-ID"));

        } catch (FileNotFoundException e) {
            Bukkit.getServer().getLogger().log(Level.WARNING, "Config File does not exist. Please update config.json File");
            createJsonFile();
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        return configMap;
    }

    private static void createJsonFile() {;
        JSONObject configDetails = new JSONObject();
        configDetails.put("API-URL","");
        configDetails.put("API-Key","");
        configDetails.put("Server-ID","");

        getPlugin(VyHub.class).getDataFolder().mkdir();
        try (FileWriter fileWr = new FileWriter("plugins/VyHub/config.json")) {
            fileWr.write(configDetails.toJSONString());
            fileWr.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void checkReady() {
        JavaPlugin plugin = getPlugin(VyHub.class);
        Logger logger = plugin.getLogger();

        Map<String, String> config = checkConfig();

        if (config.get("apiUrl").isEmpty() || config.get("apiKey").isEmpty() || config.get("serverId").isEmpty()) {
            logger.warning("VyHub config is missing values! Please follow the installation instructions.");
            return;
        }

        HttpResponse<String> response = Utility.sendRequest("/user/current", Types.GET);

        if (response.statusCode() != 200 && response.statusCode() != 307) {
            logger.warning("Cannot connect to VyHub API! Please follow the installation instructions.");
            return;
        }

        logger.info("Successfully connected to VyHub API.");

        VyHub.onReady();
    }
}
