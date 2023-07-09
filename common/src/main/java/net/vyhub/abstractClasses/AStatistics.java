package net.vyhub.abstractClasses;

import com.google.gson.reflect.TypeToken;
import net.vyhub.VyHubPlatform;
import net.vyhub.entity.Definition;
import net.vyhub.entity.VyHubUser;
import net.vyhub.lib.Cache;
import net.vyhub.lib.Utility;
import retrofit2.Response;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import static java.util.logging.Level.INFO;
import static java.util.logging.Level.SEVERE;
import static net.vyhub.lib.Utility.checkResponse;

public abstract class AStatistics extends SuperClass {
    public static Map<String, Double> playerTime = new HashMap<>();
    private static String definitionID;
    private final AUser aUser;
    public static Cache<Map<String, Double>> statisticCache = new Cache<>(
            "statistics",
            new TypeToken<HashMap<String, Double>>() {
            }.getType()
    );

    public AStatistics(VyHubPlatform platform, AUser aUser) {
        super(platform);
        this.aUser = aUser;
    }

    public AUser getAUser() {
        return aUser;
    }

    public String checkDefinition() {
        if (definitionID != null) {
            return definitionID;
        }

        Response<Definition> response;
        Definition definition;
        try {
            response = getPlatform().getApiClient().getPlaytimeDefinition().execute();
            definition = response.body();
        } catch (IOException e) {
            getPlatform().log(SEVERE, "Failed to get playtime definition: " + e.getMessage());
            return null;
        }

        if (response.isSuccessful()) {
            definitionID = definition.getId();
            return definitionID;
        }

        if (response.code() == 404) {
            getPlatform().log(INFO, "Playtime definition not found. Creating...");
            HashMap<String, Object> values = new HashMap<String, Object>() {{
                put("name", "playtime");
                put("title", "Play Time");
                put("unit", "HOURS");
                put("type", "ACCUMULATED");
                put("accumulation_interval", "day");
                put("unspecific", true);
            }};

            try {
                response = getPlatform().getApiClient().createPlaytimeDefinition(Utility.createRequestBody(values)).execute();
                definition = response.body();
            } catch (IOException e) {
                getPlatform().log(SEVERE, "Failed to create playtime definition: " + e.getMessage());
                return null;
            }

            if (response.isSuccessful()) {
                definitionID = definition.getId();
                return definitionID;
            }
        }

        return null;
    }

    public void sendPlayerTime() {
        super.getPlatform().log(INFO, "Sending playertime to API");

        String definitionID = checkDefinition();

        if (definitionID != null) {
            for (Map.Entry<String, Double> entry : playerTime.entrySet()) {
                String playerID = entry.getKey();
                VyHubUser user = getAUser().getUser(playerID);

                if (user != null) {
                    double hours = Math.round((entry.getValue() / 60) * 100.0) / 100.0;

                    if (hours < 0.1) {
                        continue;
                    }

                    HashMap<String, Object> values = new HashMap<String, Object>() {{
                        put("definition_id", definitionID);
                        put("user_id", user.getId());
                        put("serverbundle_id", AServer.serverbundleID);
                        put("value", hours);
                    }};

                    Response response = null;
                    try {
                        response = super.getPlatform().getApiClient().sendPlayerTime(Utility.createRequestBody(values)).execute();
                    } catch (IOException e) {
                        super.getPlatform().log(Level.SEVERE, "Failed to send player time to VyHub API" + e.getMessage());
                    }

                    if (!checkResponse(getPlatform(), response, "Send playtime statistic to API")) {
                        continue;
                    }

                    resetPlayerTime(playerID);
                }
            }
        }

        statisticCache.save(playerTime);
    }

    public void resetPlayerTime(String playerID) {
        playerTime.replace(playerID, 0.0);
    }

    public abstract void collectPlayerTime();

    public Cache<Map<String, Double>> getStatisticCache() {
        return statisticCache;
    }
}
