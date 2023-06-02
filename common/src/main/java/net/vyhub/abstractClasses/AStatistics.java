package net.vyhub.abstractClasses;

import com.google.gson.reflect.TypeToken;
import net.vyhub.VyHubPlatform;
import net.vyhub.entity.Definition;
import net.vyhub.lib.Cache;
import net.vyhub.lib.Utility;
import retrofit2.Response;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static java.util.logging.Level.INFO;
import static java.util.logging.Level.SEVERE;

public abstract class AStatistics {
    public static Map<String, Double> playerTime = new HashMap<>();
    private static String definitionID;

    private final VyHubPlatform platform;
    private final AUser aUser;

    public static Cache<Map<String, Double>> statisticCache = new Cache<>(
            "statistics",
            new TypeToken<HashMap<String, Double>>() {
            }.getType()
    );

    public AStatistics(VyHubPlatform platform, AUser aUser) {
        this.platform = platform;
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
            response = platform.getApiClient().getPlaytimeDefinition().execute();
            definition = response.body();
        } catch (IOException e) {
            platform.log(SEVERE, "Failed to get playtime definition: " + e.getMessage());
            return null;
        }

        if (response.isSuccessful()) {
            definitionID = definition.getId();
            return definitionID;
        }

        if (response.code() == 404) {
            platform.log(INFO, "Playtime definition not found. Creating...");
            HashMap<String, Object> values = new HashMap<>() {{
                put("name", "playtime");
                put("title", "Play Time");
                put("unit", "HOURS");
                put("type", "ACCUMULATED");
                put("accumulation_interval", "day");
                put("unspecific", true);
            }};

            try {
                response = platform.getApiClient().createPlaytimeDefinition(Utility.createRequestBody(values)).execute();
                definition = response.body();
            } catch (IOException e) {
                platform.log(SEVERE, "Failed to create playtime definition: " + e.getMessage());
                return null;
            }

            if (response.isSuccessful()) {
                definitionID = definition.getId();
                return definitionID;
            }
        }

        return null;
    }

    public abstract void sendPlayerTime();

    public void resetPlayerTime(String playerID) {
        playerTime.replace(playerID, 0.0);
    }

    public abstract void playerTime();

    public Cache<Map<String, Double>> getStatisticCache() {
        return statisticCache;
    }

    public VyHubPlatform getPlatform() {
        return platform;
    }
}
