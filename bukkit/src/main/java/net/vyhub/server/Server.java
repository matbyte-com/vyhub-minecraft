package net.vyhub.server;

import net.vyhub.Entity.VyHubUser;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Server {
    public static void patchServer() {
        List<Map<String, Object>> user_activities = new LinkedList<>();

        for (Player player : Bukkit.getOnlinePlayers()) {
            VyHubUser user = SvUser.getUser(player.getUniqueId().toString());

            if (user != null) {
                HashMap<String, Object> map = new HashMap<>();
                HashMap<String, String> extra = new HashMap<>();
                // extra.put("Ping", player.getPing() + " ms");
                // extra.put("World", player.getWorld().getName());

                map.put("user_id", user.getId());
                map.put("extra", extra);

                user_activities.add(map);
            }
        }
        SvServer.patchServerRequest(String.valueOf(Bukkit.getMaxPlayers()), String.valueOf(Bukkit.getOnlinePlayers().size()), user_activities, "true");
    }
}
