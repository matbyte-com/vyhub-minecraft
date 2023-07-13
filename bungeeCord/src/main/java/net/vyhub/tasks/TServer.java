package net.vyhub.tasks;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.vyhub.VyHubPlatform;
import net.vyhub.abstractClasses.AServer;
import net.vyhub.abstractClasses.AUser;
import net.vyhub.entity.VyHubUser;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class TServer extends AServer {
    public TServer(VyHubPlatform platform, AUser aUser) {
        super(platform, aUser);
    }
    public HashMap<String, Object> collectServerStatistics() {
        List<Map<String, Object>> user_activities = new LinkedList<>();

        for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
            VyHubUser user = getAUser().getUser(player.getUniqueId().toString());

            if (user != null) {
                HashMap<String, Object> map = new HashMap<>();
                HashMap<String, String> extra = new HashMap<>();
                // TODO Put these things as well
                // extra.put("Ping", player.getPing() + " ms");
                // extra.put("World", player.getWorld().getName());

                map.put("user_id", user.getId());
                map.put("extra", extra);

                user_activities.add(map);
            }
        }

        HashMap<String, Object> values = new HashMap<String, Object>() {{
            put("users_current", String.valueOf(ProxyServer.getInstance().getOnlineCount()));
            put("user_activities", user_activities);
            put("is_alive", "true");
        }};

        Integer playerLimit = ProxyServer.getInstance().getConfig().getPlayerLimit();
        if (playerLimit >= 1) {
            values.put("users_max", String.valueOf(playerLimit));
        } else {
            values.put("users_max", null);

        }

        return values;
    }
}
