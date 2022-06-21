package com.minecraft;


import com.minecraft.lib.PlayerGivenPermissionListener;
import com.minecraft.server.*;
import net.luckperms.api.LuckPerms;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.scheduler.BukkitScheduler;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public class Vyhub extends JavaPlugin {

    private LuckPerms luckPerms;

    @Override
    public void onEnable() {
        // Plugin startup logic
        checkConfig();
        listenerRegistration();
        commandRegistration();

        BukkitScheduler scheduler = Bukkit.getScheduler();
        scheduler.runTaskTimer(this, SvServer::patchServer, 20L*1L, 20L*60L);
        scheduler.runTaskTimer(this, SvBans::getVyHubBans, 20L*1L, 20L*60L);
        scheduler.runTaskTimer(this, SvStatistics::playerTime, 20L*1L, 20L*60L);
        scheduler.runTaskTimer(this, SvStatistics::sendPlayerTime, 20L*1L, 20L*60L*60L);

        this.luckPerms = getServer().getServicesManager().load(LuckPerms.class);
        new PlayerGivenPermissionListener(this, this.luckPerms).register();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    private void listenerRegistration() {
        PluginManager pluginManager = Bukkit.getPluginManager();
        pluginManager.registerEvents(new SvUser(), this);
        pluginManager.registerEvents(new SvGroups(), this);
    }

    private void commandRegistration() {
        getCommand("login").setExecutor(new SvLogin());
        getCommand("timeban").setExecutor(new SvBans());
        getCommand("warning").setExecutor(new SvWarning());
    }


    public static Map<String, String> checkConfig() {
        JSONParser jsonParser = new JSONParser();
        Map<String, String> configMap = new HashMap<>();

        try (FileReader reader = new FileReader("plugins/Vyhub/config.json"))
        {
            JSONObject jsonObj = (JSONObject) jsonParser.parse(reader);

            configMap.put("apiUrl", (String) jsonObj.get("API-URL"));
            configMap.put("apiKey", (String) jsonObj.get("API-Key"));
            configMap.put("serverId", (String) jsonObj.get("Server-ID"));

        } catch (FileNotFoundException e) {
            Bukkit.getLogger().fine("Config File does not exist. Please update config.json File");
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

        getPlugin(Vyhub.class).getDataFolder().mkdir();
        try (FileWriter fileWr = new FileWriter("plugins/Vyhub/config.json")) {
            fileWr.write(configDetails.toJSONString());
            fileWr.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
