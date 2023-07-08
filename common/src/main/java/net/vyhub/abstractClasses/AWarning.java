package net.vyhub.abstractClasses;

import net.vyhub.VyHubAPI;
import net.vyhub.VyHubPlatform;
import net.vyhub.entity.VyHubUser;
import net.vyhub.entity.Warn;
import net.vyhub.lib.Utility;
import retrofit2.Response;

import java.io.IOException;
import java.util.HashMap;

import static java.util.logging.Level.INFO;
import static java.util.logging.Level.SEVERE;

public abstract class AWarning {
    private final VyHubPlatform platform;
    public final ABans aBans;

    private final AUser aUser;

    public AWarning(VyHubPlatform platform, ABans aBans, AUser aUser) {
        this.platform = platform;
        this.aBans = aBans;
        this.aUser = aUser;
    }

    public VyHubPlatform getPlatform() {
        return platform;
    }
    public void createWarning(String playerId, String playerName, String reason, String adminPlayerId, String adminPlayerName) {
        VyHubUser vyHubUser = aUser.getUser(playerId);

        if (adminPlayerId != null && vyHubUser == null) {
            aUser.sendMessage(adminPlayerName, platform.getI18n().get("warningUnsuccessful"));
        }

        String finalVyHubPlayerUUID = vyHubUser.getId();
        HashMap<String, Object> values = new HashMap<>() {{
            put("reason", reason);
            put("serverbundle_id", AServer.serverbundleID);
            put("user_id", finalVyHubPlayerUUID);
        }};

        String adminUserID = "";
        if (adminPlayerId != null) {
            VyHubUser vyHubAdminUser = aUser.getUser(adminPlayerId);
            adminUserID = vyHubAdminUser.getId();
        }

        Response<Warn> response;
        try {
            VyHubAPI apiClient = platform.getApiClient();
            if (adminPlayerId == null) response = apiClient.createWarningWithoutCreator(Utility.createRequestBody(values)).execute();
            else response = apiClient.createWarning(adminUserID, Utility.createRequestBody(values)).execute();
        } catch (IOException e) {
            platform.log(SEVERE, "Failed to create warning in VyHub API" + e.getMessage());
            return;
        }

        if (response.code() != 200) {
            if (adminPlayerId != null) {
                if (response.code() == 403) {
                    aUser.sendMessage(adminPlayerName, platform.getI18n().get("warningNoPermissions"));
                } else {
                    aUser.sendMessage(adminPlayerName, platform.getI18n().get("warningUnsuccessful"));
                    try {
                        platform.log(SEVERE,response.errorBody().toString());
                    } catch (Exception e) {
                        return;
                    }
                }
            }
        } else {
            aUser.sendMessage(playerName, String.format(platform.getI18n().get("warningReceived"), reason));
            aUser.sendMessage(playerName, platform.getI18n().get("warningNotice"));

            platform.log(INFO, String.format("§c[WARN] §9Warned user %s:§6 %s", playerName, reason));

            if (adminPlayerId != null) {
                aUser.sendMessage(adminPlayerName, String.format(platform.getI18n().get("warningSuccessful"), playerName, reason));
            }

            aBans.syncBans();
        }
    }
}
