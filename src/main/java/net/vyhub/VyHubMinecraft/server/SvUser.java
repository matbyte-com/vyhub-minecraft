package net.vyhub.VyHubMinecraft.server;

import com.google.gson.Gson;
import net.vyhub.VyHubMinecraft.Entity.VyHubPlayer;
import net.vyhub.VyHubMinecraft.lib.Types;
import net.vyhub.VyHubMinecraft.lib.Utility;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SvUser implements Listener {

    public static List<VyHubPlayer> vyHubPlayers = new ArrayList<>();

    @EventHandler
    public static void checkUserExists(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        getUser(player.getUniqueId().toString());
    }

    public static VyHubPlayer getUser(String UUID) {
        if (vyHubPlayers != null) {
            for (VyHubPlayer player : vyHubPlayers) {
                if (player.getIdentifier().equals(UUID)) {
                    return player;
                }
            }
        }

        String userInformation = "";

        HttpResponse<String> response = Utility.sendRequest("/user/" + UUID + "?type=MINECRAFT", Types.GET);

        if (response.statusCode() == 404) {
            userInformation = createUser(UUID);
        } else {
            userInformation = response.body();
        }

        Gson gson = new Gson();
        VyHubPlayer vyHubPlayer = gson.fromJson(userInformation, VyHubPlayer.class);

        vyHubPlayers.add(vyHubPlayer);
        return vyHubPlayer;
    }

    public static String createUser(String UUID) {
        int statusCode = 500;
        HttpResponse<String> response = null;

        while (statusCode != 200) {
            HashMap<String, Object> values = new HashMap<>() {{
                put("type", "MINECRAFT");
                put("identifier", UUID);
            }};

            response = Utility.sendRequestBody("/user/", Types.POST, Utility.createRequestBody(values));
            statusCode = response.statusCode();
        }
        return response.body();
    }
}
