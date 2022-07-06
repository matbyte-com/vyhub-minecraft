package com.minecraft.server;


import com.minecraft.Vyhub;
import com.minecraft.lib.Types;
import com.minecraft.lib.Utility;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

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


        try (FileWriter fileWr = new FileWriter("plugins/Vyhub/serverInformation.json")) {
            fileWr.write(response.body());
            fileWr.flush();


        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }

    public static void patchServer() {
        getServerInformation();

        List<Map<String, Object>> user_activities = new LinkedList<>();

        for (Player player : Bukkit.getOnlinePlayers()) {
            HashMap<String, Object> map = new HashMap<>();
            HashMap<String, String> extra = new HashMap<>();
            extra.put("Ping", player.getPing() + " ms");
           // extra.put("World", player.getWorld().getName());

            map.put("user_id", SvUser.getUser(player.getUniqueId().toString()).getId());
            map.put("extra", extra);

            user_activities.add(map);
        }

        HashMap<String, Object> values = new HashMap<>() {{
            put("users_max", String.valueOf(Bukkit.getMaxPlayers()));
            put("users_current", String.valueOf(Bukkit.getOnlinePlayers().size()));
            put("user_activities", user_activities);
            put("is_alive", "true");
        }};
        Utility.getServerInformationObject();

        Utility.sendRequestBody("/server/" + Vyhub.checkConfig().get("serverId"), Types.PATCH, Utility.createRequestBody(values));
    }
}
