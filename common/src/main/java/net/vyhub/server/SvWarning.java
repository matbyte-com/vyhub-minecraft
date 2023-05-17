package net.vyhub.server;

import net.vyhub.Entity.VyHubUser;
import net.vyhub.lib.Types;
import net.vyhub.lib.Utility;

import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.HashMap;

public class SvWarning {
    public static HttpResponse<String> createWarningRequest(String playerId, String reason, String adminPlayerId, VyHubUser vyHubUser) {
        String finalVyHubPlayerUUID = vyHubUser.getId();
        HashMap<String, Object> values = new HashMap<>() {{
            put("reason", reason);
            put("serverbundle_id", SvServer.serverbundleID);
            put("user_id", finalVyHubPlayerUUID);
        }};

        String adminUserID = "";
        if (adminPlayerId != null) {
            VyHubUser vyHubAdminUser = SvUser.getUser(adminPlayerId);
            adminUserID = "?morph_user_id=" + vyHubAdminUser.getId();
        }

        return Utility.sendRequestBody(String.format("/warning/%s", adminUserID), Types.POST, Utility.createRequestBody(values),
                Arrays.asList(403));
    }

}
