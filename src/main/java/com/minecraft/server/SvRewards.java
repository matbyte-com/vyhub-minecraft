package com.minecraft.server;

import com.minecraft.Vyhub;
import com.minecraft.lib.Types;
import com.minecraft.lib.Utility;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.net.http.HttpResponse;
import java.util.HashMap;

public class SvRewards {

    public static void getRewards() {
        HashMap<String, Object> values = new HashMap<>() {{
            put("active", true);
            put("foreign_ids", true);
            put("status", "OPEN");
            put("serverbundle_id", Utility.serverbundleID);
            put("for_server_id", Vyhub.checkConfig().get("serverId"));
        }};

        StringBuilder stringBuilder = new StringBuilder();

        for (Player player : Bukkit.getOnlinePlayers()) {
            String userID = SvUser.getUser(player.getUniqueId().toString()).getId();
            stringBuilder.append("user_id=").append(userID).append("&");
        }
        stringBuilder.deleteCharAt(stringBuilder.length()-1);

        HttpResponse<String> resp = Utility.sendRequestBody("/packet/reward/applied/user?"+stringBuilder, Types.GET, Utility.createRequestBody(values));

        JSONParser jsonParser = new JSONParser();
        JSONObject object = null;
        try {
            object = (JSONObject) jsonParser.parse(resp.body());

        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

}
