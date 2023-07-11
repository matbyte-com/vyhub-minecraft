package net.vyhub.abstractClasses;

import com.google.gson.reflect.TypeToken;
import net.vyhub.VyHubPlatform;
import net.vyhub.entity.Ban;
import net.vyhub.entity.MinecraftBan;
import net.vyhub.entity.VyHubUser;
import net.vyhub.lib.Cache;
import net.vyhub.lib.Utility;
import retrofit2.Response;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static java.util.logging.Level.*;
import static net.vyhub.VyHubAPI.gson;

public abstract class ABans extends VyHubAbstractBase{
    public static DateTimeFormatter isoDateFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
    public static DateTimeFormatter mcDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss ZZZ");
    private static Set<String> processedPlayers = new HashSet<>();
    private static Map<String, MinecraftBan> minecraftBans = null;
    private static Map<String, List<Ban>> vyhubBans = null;
    private final AUser aUser;
    private final AGroups aGroups;

    public ABans(VyHubPlatform platform, AUser aUser, AGroups aGroups) {
        super(platform);
        this.aUser = aUser;
        this.aGroups = aGroups;
    }

    private static Cache<Set<String>> banCache = new Cache<>(
            "banned_players",
            new TypeToken<HashSet<String>>() {
            }.getType()
    );

    public void fetchMinecraftBans() {
        try {
            String bansJson = new String(Files.readAllBytes(Paths.get("banned-players.json")), StandardCharsets.UTF_8);

            Type minecraftBansType = new TypeToken<List<MinecraftBan>>() {
            }.getType();
            List<MinecraftBan> mcBansList = gson.fromJson(bansJson, minecraftBansType);
            Map<String, MinecraftBan> mcBansMap = new HashMap<>();

            for (MinecraftBan mcBan : mcBansList) {
                mcBansMap.put(mcBan.getUuid(), mcBan);
            }

            minecraftBans = mcBansMap;
        } catch (IOException e) {
            minecraftBans = null;
            throw new RuntimeException(e);
        }
    }

    public void fetchVyHubBans() {
        Response<Map<String, List<Ban>>> response = null;

        try {
            response = platform.getApiClient().getBans(AServer.serverbundleID).execute();
        } catch (IOException e) {
            vyhubBans = null;
            platform.log(SEVERE, "Failed to fetch Bans from VyHub API: " + e.getMessage());
        }

        if (response == null || response.code() != 200) {
            vyhubBans = null;
            platform.log(WARNING, "Bans could not be fetched from VyHub API.");
            return;
        }

        vyhubBans = response.body();
    }

    private void loadProcessedPlayers() {
        processedPlayers = banCache.load();

        if (processedPlayers == null) {
            platform.log(WARNING, "Missing VyHub banned_players.json, defaulting to empty list.");
            processedPlayers = new HashSet<>();
        }
    }

    private static void saveProcessedPlayers() {
        banCache.save(processedPlayers);
    }

    public synchronized void syncBans() {
        fetchMinecraftBans();
        fetchVyHubBans();

        if (minecraftBans == null || vyhubBans == null) {
            return;
        }

        compareAndHandleDiffs();
    }

    private synchronized void compareAndHandleDiffs() {
        loadProcessedPlayers();

        Set<String> bannedMinecraftPlayers = minecraftBans.keySet();

        Set<String> bannedVyHubPlayers = vyhubBans.keySet();

        // All minecraft bans, that do not exist on VyHub
        Set<String> bannedMinecraftPlayersDiff = new HashSet<>(bannedMinecraftPlayers);
        bannedMinecraftPlayersDiff.removeAll(bannedVyHubPlayers);

        // All VyHub bans, that do not exist on minecraft server
        Set<String> bannedVyHubPlayersDiff = new HashSet<>(bannedVyHubPlayers);
        bannedVyHubPlayersDiff.removeAll(bannedMinecraftPlayers);

        // All bans that minecraft server and VyHub have in common
        Set<String> bannedPlayersIntersect = new HashSet<>(bannedVyHubPlayers);
        bannedPlayersIntersect.retainAll(bannedMinecraftPlayers);

        // Check for bans missing on VyHub
        for (String playerID : bannedMinecraftPlayersDiff) {
            if (processedPlayers.contains(playerID)) {
                // Unbanned on VyHub
                platform.log(INFO, String.format("Unbanning minecraft ban for player %s. (Unbanned on VyHub)", playerID));

                if (unbanMinecraftBan(playerID)) {
                    processedPlayers.remove(playerID);
                }
            } else {
                // Missing on VyHub
                platform.log(INFO, String.format("Adding VyHub ban for player %s from minecraft. (Banned on minecraft server)", playerID));

                if (addVyHubBan(playerID, minecraftBans.get(playerID))) {
                    processedPlayers.add(playerID);
                }
            }
        }

        // Checks for bans missing on minecraft server
        for (String playerID : bannedVyHubPlayersDiff) {
            if (processedPlayers.contains(playerID)) {
                // Unbanned on Minecraft Server
                platform.log(INFO, String.format("Unbanning VyHub ban for player %s. (Unbanned on minecraft server)", playerID));

                if (unbanVyHubBan(playerID)) {
                    processedPlayers.remove(playerID);
                }
            } else {
                // Missing on Minecraft Server
                platform.log(INFO, String.format("Adding minecraft ban for player %s from VyHub. (Banned on VyHub)", playerID));
                if (addMinecraftBan(playerID, vyhubBans.get(playerID).get(0))) {
                    processedPlayers.add(playerID);
                }
            }
        }

        processedPlayers.addAll(bannedPlayersIntersect);

        saveProcessedPlayers();
    }

    public abstract boolean addMinecraftBan(String playerID, Ban vyhubBan);

    public abstract boolean unbanMinecraftBan(String playerID);

    public abstract String getPlayerIdentifier(String playerName);

    public boolean addVyHubBan(String playerID, MinecraftBan minecraftBan) {
        VyHubUser user = aUser.getUser(playerID);
        if (user == null) {
            return false;
        }

        Long time = null;
        ZonedDateTime createdDate = ZonedDateTime.parse(minecraftBan.getCreated(), mcDateFormatter);

        if (!minecraftBan.getExpires().equals("forever")) {
            ZonedDateTime expiresDate = ZonedDateTime.parse(minecraftBan.getExpires(), mcDateFormatter);
            time = expiresDate.toEpochSecond() - createdDate.toEpochSecond();
        }

        String vyHubAdminUserID = null;
        if (!minecraftBan.getSource().equals("CONSOLE") && !minecraftBan.getSource().equals("Server")) {
            try {
                String sourcePlayerId = getPlayerIdentifier(minecraftBan.getSource());

                if (sourcePlayerId != null) {
                    VyHubUser admin = aUser.getUser(sourcePlayerId);

                    if (admin != null) {
                        vyHubAdminUserID = admin.getId();
                    }
                }
            } catch (IllegalArgumentException ignored) {

            }
        }

        Long finalTime = time;
        HashMap<String, Object> values = new HashMap<String, Object>() {{
            put("length", finalTime);
            put("reason", minecraftBan.getReason());
            put("serverbundle_id", AServer.serverbundleID);
            put("user_id", user.getId());
            put("created_on", createdDate.format(isoDateFormatter));
        }};

        Response<Ban> response = null;

        if (vyHubAdminUserID != null) {
            try {
                response = platform.getApiClient().createBan(vyHubAdminUserID, Utility.createRequestBody(values)).execute();
            } catch (IOException e) {
                platform.log(SEVERE, "Failed to create ban" + e.getMessage());
            }
        } else {
            try {
                response = platform.getApiClient().createBanWithoutCreator(Utility.createRequestBody(values)).execute();
            } catch (IOException e) {
                platform.log(SEVERE, "Failed to create ban" + e.getMessage());
            }
        }

        return response != null && response.isSuccessful();
    }

    public boolean unbanVyHubBan(String playerID) {
        VyHubUser user = aUser.getUser(playerID);
        if (user == null) {
            return false;
        }

        Response<Ban> response;
        try {
           response = platform.getApiClient().unbanUser(user.getId(), AServer.serverbundleID).execute();
        } catch (IOException e) {
            platform.log(SEVERE, "Failed to unban user" + e.getMessage());
            return false;
        }

        return response.isSuccessful();
    }
}
