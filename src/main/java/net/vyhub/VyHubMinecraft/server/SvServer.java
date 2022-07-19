package net.vyhub.VyHubMinecraft.server;


import com.google.gson.Gson;
import net.vyhub.VyHubMinecraft.Entity.VyHubServer;
import net.vyhub.VyHubMinecraft.Entity.VyHubUser;
import net.vyhub.VyHubMinecraft.VyHub;
import net.vyhub.VyHubMinecraft.lib.Cache;
import net.vyhub.VyHubMinecraft.lib.Types;
import net.vyhub.VyHubMinecraft.lib.Utility;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.FileWriter;
import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SvServer {

    private static Cache<VyHubServer> serverCache = new Cache<>("server", VyHubServer.class);
    public static String serverbundleID = null;

    private static Gson gson = new Gson();

    public static VyHubServer getServerInformation() {
        String serverID = VyHub.config.get("serverID");

        if (serverID == null) {
            return null;
        }

        HttpResponse<String> response = Utility.sendRequest(String.format("/server/%s", serverID), Types.GET);

        VyHubServer server;

        if (response == null || response.statusCode() != 200) {
            server = serverCache.load();
        } else {
            server = gson.fromJson(response.body(), VyHubServer.class);
        }

        if (server == null) {
            return null;
        }

        serverCache.save(server);

        serverbundleID = server.getServerbundle_id();

        return server;
    }

    public static void patchServer() {
        List<Map<String, Object>> user_activities = new LinkedList<>();

        for (Player player : Bukkit.getOnlinePlayers()) {
            VyHubUser user = SvUser.getUser(player.getUniqueId().toString());

            if (user != null) {
                HashMap<String, Object> map = new HashMap<>();
                HashMap<String, String> extra = new HashMap<>();
                extra.put("Ping", player.getPing() + " ms");
                // extra.put("World", player.getWorld().getName());

                map.put("user_id", user.getId());
                map.put("extra", extra);

                user_activities.add(map);
            }
        }

        HashMap<String, Object> values = new HashMap<>() {{
            put("users_max", String.valueOf(Bukkit.getMaxPlayers()));
            put("users_current", String.valueOf(Bukkit.getOnlinePlayers().size()));
            put("user_activities", user_activities);
            put("is_alive", "true");
        }};

        Utility.sendRequestBody("/server/" + VyHub.config.get("serverID"), Types.PATCH, Utility.createRequestBody(values));
    }
}
