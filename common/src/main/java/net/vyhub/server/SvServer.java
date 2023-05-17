package net.vyhub.server;

import com.google.gson.Gson;
import net.vyhub.Entity.Server;
import net.vyhub.Entity.VyHubUser;
import net.vyhub.lib.Config;
import net.vyhub.lib.Cache;
import net.vyhub.lib.Types;
import net.vyhub.lib.Utility;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class SvServer {

    private static Cache<Server> serverCache = new Cache<>("server", Server.class);
    public static String serverbundleID = null;

    private static Gson gson = new Gson();

    public static Server getServerInformation() {
        String serverID = Config.get("server_id");

        if (serverID == null) {
            return null;
        }

        HttpResponse<String> response = Utility.sendRequest(String.format("/server/%s", serverID), Types.GET);

        Server server;

        if (response == null || response.statusCode() != 200) {
            server = serverCache.load();
        } else {
            server = gson.fromJson(response.body(), Server.class);
        }

        if (server == null) {
            return null;
        }

        serverCache.save(server);

        serverbundleID = server.getServerbundle_id();

        return server;
    }

    public static void patchServerRequest(String users_max, String users_current, List<Map<String, Object>> user_activities, String is_alive) {
        HashMap<String, Object> values = new HashMap<>() {{
            put("users_max", String.valueOf(Bukkit.getMaxPlayers()));
            put("users_current", String.valueOf(Bukkit.getOnlinePlayers().size()));
            put("user_activities", user_activities);
            put("is_alive", "true");
        }};

        Utility.sendRequestBody("/server/" + Config.get("server_id"), Types.PATCH, Utility.createRequestBody(values));
    }
}
