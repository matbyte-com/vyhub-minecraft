package net.vyhub.VyHubMinecraft.server;


import net.vyhub.VyHubMinecraft.Entity.VyHubUser;
import net.vyhub.VyHubMinecraft.VyHub;
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

    public static HttpResponse<String> getServerInformation() {
        HttpResponse<String> response = Utility.sendRequest("/server/?type=MINECRAFT", Types.GET);

        if (response == null || response.statusCode() != 200) {
            new BukkitRunnable() {
                public void run() {
                   getServerInformation();
                }
            }.runTaskLater(VyHub.getPlugin(VyHub.class), 20L*60L);
            return null;
        }


        try (FileWriter fileWr = new FileWriter("plugins/VyHub/serverInformation.json")) {
            fileWr.write(response.body());
            fileWr.flush();

        } catch (IOException e) {
            Bukkit.getServer().getLogger().severe("VyHub API is not reachable");
        }
        return response;
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

        Utility.getServerInformationObject();

        HashMap<String, Object> values = new HashMap<>() {{
            put("users_max", String.valueOf(Bukkit.getMaxPlayers()));
            put("users_current", String.valueOf(Bukkit.getOnlinePlayers().size()));
            put("user_activities", user_activities);
            put("is_alive", "true");
        }};

        Utility.sendRequestBody("/server/" + VyHub.checkConfig().get("serverId"), Types.PATCH, Utility.createRequestBody(values));
    }
}
