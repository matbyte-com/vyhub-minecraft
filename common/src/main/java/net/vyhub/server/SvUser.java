package net.vyhub.server;

import com.google.gson.Gson;
import net.vyhub.Entity.VyHubUser;
import net.vyhub.VyHub;
import net.vyhub.event.VyHubPlayerInitializedEvent;
import net.vyhub.lib.Types;
import net.vyhub.lib.Utility;

import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.logging.Logger;

public class SvUser {

    public static Map<String, VyHubUser> vyHubPlayers = new HashMap<>();
    private static Logger logger = Bukkit.getServer().getLogger();

    private static Semaphore userCreateSem = new Semaphore(1, true);

    public static VyHubUser getUser(String UUID) {
        return getUser(UUID, true);
    }

    public static VyHubUser getUser(String UUID, Boolean create) {
        if (UUID == null || UUID.isEmpty()) {
            throw new IllegalArgumentException("UUID may not be empty or null.");
        }

        if (vyHubPlayers != null) {
            if (vyHubPlayers.containsKey(UUID)) {
                return vyHubPlayers.get(UUID);
            }
        }

        String userInformation = "";

        HttpResponse<String> response = Utility.sendRequest("/user/" + UUID + "?type=MINECRAFT", Types.GET,
                Arrays.asList(404));

        if (response != null && create) {
            try {
                userCreateSem.acquire();
            } catch (InterruptedException e) {
                return null;
            }

            VyHubUser vyHubUser = getUser(UUID, false);

            if (vyHubUser != null) {
                return vyHubUser;
            }

            logger.info(String.format("Could not find VyHub user for player %s, creating..", UUID));

            if (response.statusCode() == 404) {
                userInformation = createUser(UUID);

                if (userInformation == null) {
                    return null;
                }
            } else if (response.statusCode() == 200) {
                userInformation = response.body();
            } else {
                return null;
            }

            Gson gson = new Gson();
            vyHubUser = gson.fromJson(userInformation, VyHubUser.class);

            vyHubPlayers.put(UUID, vyHubUser);

            userCreateSem.release();

            return vyHubUser;
        } else {
            return null;
        }
    }

    public static String createUser(String UUID) {
        HashMap<String, Object> values = new HashMap<>() {{
            put("type", "MINECRAFT");
            put("identifier", UUID);
        }};

        HttpResponse<String> response = Utility.sendRequestBody("/user/", Types.POST, Utility.createRequestBody(values));

        if (response != null && response.statusCode() == 200) {
            return response.body();
        }

        return null;
    }
}
