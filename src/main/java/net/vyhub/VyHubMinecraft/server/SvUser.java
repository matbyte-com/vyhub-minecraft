package net.vyhub.VyHubMinecraft.server;

import com.google.gson.Gson;
import net.vyhub.VyHubMinecraft.Entity.VyHubUser;
import net.vyhub.VyHubMinecraft.VyHub;
import net.vyhub.VyHubMinecraft.lib.Types;
import net.vyhub.VyHubMinecraft.lib.Utility;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class SvUser implements Listener {

    public static Map<String, VyHubUser> vyHubPlayers = new HashMap<>();
    private static Logger logger = Bukkit.getServer().getLogger();

    @EventHandler
    public static void checkUserExists(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        VyHubUser user = getUser(player.getUniqueId().toString());

        if (user == null) {
            logger.warning(String.format("Could not register player %s, trying again in a minute..", player.getName()));

            VyHub.scheduler.scheduleSyncDelayedTask(VyHub.getPlugin(VyHub.class), new Runnable(){
                @Override
                public void run(){
                    checkUserExists(event);
                }
            }, 20L * 60L);
        }
    }

    public static VyHubUser getUser(String UUID) {
        if (UUID == null || UUID.isEmpty()) {
            throw new IllegalArgumentException("UUID may not be empty or null.");
        }

        if (vyHubPlayers != null) {
            if (vyHubPlayers.containsKey(UUID)) {
                return vyHubPlayers.get(UUID);
            }
        }

        String userInformation = "";

        HttpResponse<String> response = Utility.sendRequest("/user/" + UUID + "?type=MINECRAFT", Types.GET);

        if (response != null) {
            if (response.statusCode() == 404) {
                userInformation = createUser(UUID);

                if (userInformation == null) {
                    return null;
                }
            } else if (response.statusCode() == 200) {
                userInformation = response.body();
            } else {
                return null;
            }

            Gson gson = new Gson();
            VyHubUser vyHubUser = gson.fromJson(userInformation, VyHubUser.class);

            vyHubPlayers.put(UUID, vyHubUser);
            return vyHubUser;
        } else {
            return null;
        }
    }

    public static String createUser(String UUID) {
        HashMap<String, Object> values = new HashMap<>() {{
            put("type", "MINECRAFT");
            put("identifier", UUID);
        }};

        HttpResponse<String> response = Utility.sendRequestBody("/user/", Types.POST, Utility.createRequestBody(values));

        if (response != null && response.statusCode() == 200) {
            return response.body();
        }

        return null;
    }
}
