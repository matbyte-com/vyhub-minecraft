package net.vyhub.VyHubMinecraft.server;


import com.google.common.reflect.TypeToken;
import net.vyhub.VyHubMinecraft.Entity.VyHubUser;
import net.vyhub.VyHubMinecraft.lib.Cache;
import net.vyhub.VyHubMinecraft.lib.Types;
import net.vyhub.VyHubMinecraft.lib.Utility;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

public class SvStatistics {
    public static Map<String, Double> playerTime = new HashMap<>();
    private static String definitionID;

    private static Cache<Map<String, Double>> statisticCache = new Cache<>(
            "statistics",
            new TypeToken<HashMap<String, Double>>() {}.getType()
    );

    public static String checkDefinition(){
        if (definitionID != null) {
            return definitionID;
        }

        HttpResponse<String> response = Utility.sendRequest("/user/attribute/definition/playtime", Types.GET);

        if (response != null) {
            if (response.statusCode() == 200) {
                JSONParser jsonParser = new JSONParser();
                JSONObject object = null;
                try {
                    object = (JSONObject) jsonParser.parse(response.body());
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
                definitionID =  object.get("id").toString();
                return definitionID;
            } else if (response.statusCode() == 404) {
                HashMap<String, Object> values = new HashMap<>() {{
                    put("name", "playtime");
                    put("title", "Play Time");
                    put("unit", "HOURS");
                    put("type", "ACCUMULATED");
                    put("accumulation_interval", "day");
                    put("unspecific", true);
                }};

                HttpResponse<String> resp = Utility.sendRequestBody("/user/attribute/definition", Types.POST, Utility.createRequestBody(values));

                if (resp != null && resp.statusCode() == 200) {
                    JSONParser jsonParser = new JSONParser();
                    JSONObject object = null;
                    try {
                        object = (JSONObject) jsonParser.parse(resp.body());
                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }
                    definitionID =  object.get("id").toString();
                    return definitionID;
                }
            }
        }

        return null;
    }

    public static void sendPlayerTime() {
        String definitionID = checkDefinition();

        if (definitionID != null) {
            for (Map.Entry<String, Double> entry : playerTime.entrySet()) {
                String playerID = entry.getKey();
                VyHubUser user = SvUser.getUser(playerID);

                if (user != null) {
                    double hours = Math.round((entry.getValue() / 60) * 100.0) / 100.0;

                    if (hours < 0.1 ) {
                        continue;
                    }

                    HashMap<String, Object> values = new HashMap<>() {{
                        put("definition_id", definitionID);
                        put("user_id", user.getId());
                        put("serverbundle_id", SvServer.serverbundleID);
                        put("value", hours);
                    }};

                    Utility.sendRequestBody("/user/attribute/", Types.POST, Utility.createRequestBody(values));
                    resetPlayerTime(playerID);
                }
            }
        }

        statisticCache.save(playerTime);
    }

    public static void resetPlayerTime(String playerID) {
        playerTime.replace(playerID, 0.0);
    }

    public static void playerTime() {
        playerTime = statisticCache.load();
        if (playerTime == null) {
            playerTime = new HashMap<>();
        }

        for (Player player: Bukkit.getOnlinePlayers()) {
            if (playerTime.containsKey(player.getUniqueId().toString())) {
                double oldTime = playerTime.get(player.getUniqueId().toString());
                playerTime.replace(player.getUniqueId().toString(), oldTime + 1);
            } else {
                playerTime.put(player.getUniqueId().toString(), 1.0);
            }
        }

        statisticCache.save(playerTime);
    }
}
