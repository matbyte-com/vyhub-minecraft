package net.vyhub.VyHubMinecraft.server;


import net.vyhub.VyHubMinecraft.lib.Types;
import net.vyhub.VyHubMinecraft.lib.Utility;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.net.http.HttpResponse;
import java.util.HashMap;

public class SvStatistics {
    public static HashMap<String, Double> playerTime = new HashMap<>();

    public static String checkDefinition(){
        HttpResponse<String> response = Utility.sendRequest("/user/attribute/definition/playtime", Types.GET);

        if (response.statusCode() == 200) {
            JSONParser jsonParser = new JSONParser();
            JSONObject object = null;
            try {
                object = (JSONObject) jsonParser.parse(response.body());
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
            return object.get("id").toString();
        }

        HashMap<String, Object> values = new HashMap<>() {{
            put("name", "playtime");
            put("title", "Play Time");
            put("unit", "HOURS");
            put("type", "ACCUMULATED");
            put("accumulation_interval", "day");
            put("unspecific", true);
        }};

        HttpResponse<String> resp = Utility.sendRequestBody("/user/attribute/definition", Types.POST, Utility.createRequestBody(values));

        JSONParser jsonParser = new JSONParser();
        JSONObject object = null;
        try {
            object = (JSONObject) jsonParser.parse(resp.body());
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        return object.get("id").toString();
    }

    public static void sendPlayerTime() {
        String definitionID = checkDefinition();


        for (Player player : Bukkit.getOnlinePlayers()) {

            if (Math.round(playerTime.get(player.getUniqueId().toString()) / 60) < 1 ) {
                continue;
            }
            HashMap<String, Object> values = new HashMap<>() {{
                put("definition_id", definitionID);
                put("user_id", SvUser.getUser(player.getUniqueId().toString()).getId());
                put("serverbundle_id", Utility.serverbundleID);
                put("value",String.valueOf(Math.round(playerTime.get(player.getUniqueId().toString()) / 60)));
            }};

            Utility.sendRequestBody("/user/attribute/", Types.POST, Utility.createRequestBody(values));
            resetPlayerTime(player);
        }
    }

    public static void resetPlayerTime(Player player) {
        playerTime.replace(player.getUniqueId().toString(), 0.0);
    }

    public static void playerTime() {
        for (Player player: Bukkit.getOnlinePlayers()) {
            if (playerTime.containsKey(player.getUniqueId().toString())) {
                double oldTime = playerTime.get(player.getUniqueId().toString());
                playerTime.replace(player.getUniqueId().toString(), oldTime + 1);
            } else {
                playerTime.put(player.getUniqueId().toString(), 1.0);
            }
        }
    }
}
