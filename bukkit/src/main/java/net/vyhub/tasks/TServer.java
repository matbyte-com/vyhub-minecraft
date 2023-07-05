package net.vyhub.tasks;

import net.vyhub.VyHubPlatform;
import net.vyhub.abstractClasses.AServer;
import net.vyhub.abstractClasses.AUser;
import net.vyhub.config.VyHubConfiguration;
import net.vyhub.entity.VyHubUser;
import net.vyhub.lib.Utility;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import retrofit2.Response;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static net.vyhub.lib.Utility.checkResponse;

public class TServer extends AServer {
    public TServer(VyHubPlatform platform, AUser aUser) {
        super(platform, aUser);
    }
    public void patchServer() {
        List<Map<String, Object>> user_activities = new LinkedList<>();

        for (Player player : Bukkit.getOnlinePlayers()) {
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

        HashMap<String, Object> values = new HashMap<>() {{
            put("users_max", String.valueOf(Bukkit.getMaxPlayers()));
            put("users_current", String.valueOf(Bukkit.getOnlinePlayers().size()));
            put("user_activities", user_activities);
            put("is_alive", "true");
        }};

        Response response = null;
        try {
            response = getPlatform().getApiClient().patchServer(VyHubConfiguration.getServerId(), Utility.createRequestBody(values)).execute();
        } catch (IOException e) {
            return;
        }

        checkResponse(getPlatform(), response, "Patch server");
    }
}
