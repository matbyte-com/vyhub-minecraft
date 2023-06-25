package net.vyhub.abstractClasses;

import net.vyhub.VyHubPlatform;
import net.vyhub.entity.VyHubUser;
import net.vyhub.lib.Utility;
import retrofit2.Response;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import static java.util.logging.Level.INFO;

public abstract class AUser {
    public static Map<String, VyHubUser> vyHubPlayers = new HashMap<>();

    private final VyHubPlatform platform;

    private static Semaphore userCreateSem = new Semaphore(1, true);

    public AUser(VyHubPlatform platform) {
        this.platform = platform;
    }

    public VyHubPlatform getPlatform() {
        return platform;
    }

    public void checkPlayerExists(String playerId, String playerName) {
        VyHubUser user = getUser(playerId);

        if (user == null) {
            platform.log(Level.WARNING, String.format("Could not register player %s, trying again in a minute..", playerName));

            platform.executeAsyncLater(() -> checkPlayerExists(playerId, playerName), 1, TimeUnit.MINUTES);
        } else {
            // TODO Check how the event call is working
            platform.executeAsync(() -> callVyHubPlayerInitializedEvent(playerId, playerName));
        }
    };

    public abstract void callVyHubPlayerInitializedEvent(String playerId, String playerName);

    public abstract void sendMessage(String playerName, String message);

    public VyHubUser createUser(String UUID) {
        HashMap<String, Object> values = new HashMap<>() {{
            put("type", "MINECRAFT");
            put("identifier", UUID);
        }};

        Response<VyHubUser> response;
        try {
            response = platform.getApiClient().createUser(Utility.createRequestBody(values)).execute();
        } catch (IOException e) {
            platform.log(Level.SEVERE, "Failed to create user in VyHub API" + e.getMessage());
            return null;
        }

        if (response.isSuccessful()) {
            return response.body();
        }

        return null;
    }

    public VyHubUser getUser(String UUID) {
        return getUser(UUID, true);
    }

    public VyHubUser getUser(String UUID, Boolean create) {
        if (UUID == null || UUID.isEmpty()) {
            throw new IllegalArgumentException("UUID may not be empty or null.");
        }

        if (vyHubPlayers != null) {
            if (vyHubPlayers.containsKey(UUID)) {
                return vyHubPlayers.get(UUID);
            }
        }

        Response<VyHubUser> response;
        try {
            // TODO Does this response returns a List?
            response = platform.getApiClient().getUser(UUID).execute();
        } catch (IOException e) {
            platform.log(Level.SEVERE, "Failed to get user from VyHub API" + e.getMessage());
            return null;
        }

        if (create) {
            try {
                userCreateSem.acquire();
            } catch (InterruptedException e) {
                return null;
            }

            VyHubUser vyHubUser = getUser(UUID, false);

            if (vyHubUser != null) {
                return vyHubUser;
            }

            platform.log(INFO, String.format("Could not find VyHub user for player %s, creating..", UUID));

            if (response.code() == 404) {
                vyHubUser = createUser(UUID);

                if (vyHubUser == null) {
                    return null;
                }
            } else if (response.code() == 200) {
                vyHubUser = response.body();
            } else {
                return null;
            }

            vyHubPlayers.put(UUID, vyHubUser);

            userCreateSem.release();

            return vyHubUser;
        } else {
            return null;
        }
    }
}
