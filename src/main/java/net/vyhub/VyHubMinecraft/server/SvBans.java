package net.vyhub.VyHubMinecraft.server;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import net.vyhub.VyHubMinecraft.Entity.MinecraftBan;
import net.vyhub.VyHubMinecraft.Entity.VyHubBan;
import net.vyhub.VyHubMinecraft.Entity.VyHubUser;
import net.vyhub.VyHubMinecraft.lib.Cache;
import net.vyhub.VyHubMinecraft.lib.Types;
import net.vyhub.VyHubMinecraft.lib.Utility;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Logger;

public class SvBans implements CommandExecutor {
    private static DateTimeFormatter isoDateFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
    private static DateTimeFormatter mcDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss ZZZ");

    private static Set<String> processedPlayers = new HashSet<>();

    private static Map<String, MinecraftBan> minecraftBans = null;
    private static Map<String, List<VyHubBan>> vyhubBans = null;

    private static Gson gson = new Gson();

    private static Logger logger = Bukkit.getServer().getLogger();

    private static Cache<Set<String>> banCache = new Cache<>(
            "banned_players",
            new TypeToken<HashSet<String>>() {
            }.getType()
    );

    public static void fetchMinecraftBans() {
        try {
            String bansJson = Files.readString(Paths.get("banned-players.json"), StandardCharsets.UTF_8);

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

    public static void fetchVyHubBans() {
        HttpResponse<String> response = Utility.sendRequest(String.format("/server/bundle/%s/ban?active=true", SvServer.serverbundleID), Types.GET);

        if (response == null || response.statusCode() != 200) {
            vyhubBans = null;
            return;
        }

        Type vyhubBansType = new TypeToken<Map<String, List<VyHubBan>>>() {
        }.getType();
        vyhubBans = gson.fromJson(response.body(), vyhubBansType);
    }

    private static void loadProcessedPlayers() {
        processedPlayers = banCache.load();

        if (processedPlayers == null) {
            logger.warning("Missing VyHub banned_players.json, defaulting to empty list.");
            processedPlayers = new HashSet<>();
        }
    }

    private static void saveProcessedPlayers() {
        banCache.save(processedPlayers);
    }

    public static synchronized void syncBans() {
        fetchMinecraftBans();
        fetchVyHubBans();

        if (minecraftBans == null || vyhubBans == null) {
            return;
        }

        compareAndHandleDiffs();

        //Bukkit.getScheduler().runTask(VyHub.plugin, SvBans::);
    }

    private static synchronized void compareAndHandleDiffs() {
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
                logger.info(String.format("Unbanning minecraft ban for player %s. (Unbanned on VyHub)", playerID));

                if (unbanMinecraftBan(playerID)) {
                    processedPlayers.remove(playerID);
                }
            } else {
                // Missing on VyHub
                logger.info(String.format("Adding VyHub ban for player %s from minecraft. (Banned on minecraft server)", playerID));

                if (addVyHubBan(playerID, minecraftBans.get(playerID))) {
                    processedPlayers.add(playerID);
                }
            }
        }

        // Checks for bans missing on minecraft server
        for (String playerID : bannedVyHubPlayersDiff) {
            if (processedPlayers.contains(playerID)) {
                // Unbanned on Minecraft Server
                logger.info(String.format("Unbanning VyHub ban for player %s. (Unbanned on minecraft server)", playerID));

                if (unbanVyHubBan(playerID)) {
                    processedPlayers.remove(playerID);
                }
            } else {
                // Missing on Minecraft Server
                logger.info(String.format("Adding minecraft ban for player %s from VyHub. (Banned on VyHub)", playerID));
                if (addMinecraftBan(playerID, vyhubBans.get(playerID).get(0))) {
                    processedPlayers.add(playerID);
                }
            }
        }

        processedPlayers.addAll(bannedPlayersIntersect);

        saveProcessedPlayers();
    }

    public static boolean addMinecraftBan(String playerID, VyHubBan vyhubBan) {
        Date endDate = null;
        if (vyhubBan.getEnds_on() != null) {
            ZonedDateTime expiresDate = ZonedDateTime.parse(vyhubBan.getEnds_on(), isoDateFormatter);
            endDate = Date.from(expiresDate.toInstant());
        }

        Bukkit.getBanList(BanList.Type.NAME).addBan(playerID, vyhubBan.getReason(), endDate, "VyHub");

        Player bannnedPlayer = Bukkit.getPlayer(UUID.fromString(playerID));
        if (bannnedPlayer != null) {
            VyHub.scheduler.runTask(VyHub.plugin, new Runnable() {
                @Override
                public void run() {
                    bannnedPlayer.kickPlayer(String.format("You are banned from this server.\nReason: %s", vyhubBan.getReason()));
                }
            });
        }

        return true;
    }

    public static boolean unbanMinecraftBan(String playerID) {
        Bukkit.getBanList(BanList.Type.NAME).pardon(playerID);
        return true;
    }

    public static boolean addVyHubBan(String playerID, MinecraftBan minecraftBan) {
        VyHubUser user = SvUser.getUser(playerID);
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
                Player sourcePlayer = Bukkit.getPlayer(minecraftBan.getSource());

                if (sourcePlayer != null) {
                    VyHubUser admin = SvUser.getUser(sourcePlayer.getUniqueId().toString());

                    if (admin != null) {
                        vyHubAdminUserID = admin.getId();
                    }
                }
            } catch (IllegalArgumentException ignored) {

            }
        }

        Long finalTime = time;
        HashMap<String, Object> values = new HashMap<>() {{
            put("length", finalTime);
            put("reason", minecraftBan.getReason());
            put("serverbundle_id", SvServer.serverbundleID);
            put("user_id", user.getId());
            put("created_on", createdDate.format(isoDateFormatter));
        }};

        HttpResponse<String> response;

        if (vyHubAdminUserID != null) {
            response = Utility.sendRequestBody("/ban/?morph_user_id=" + vyHubAdminUserID, Types.POST, Utility.createRequestBody(values));
        } else {
            response = Utility.sendRequestBody("/ban/", Types.POST, Utility.createRequestBody(values));
        }

        return response != null && response.statusCode() == 200;
    }

    public static boolean unbanVyHubBan(String playerID) {
        VyHubUser user = SvUser.getUser(playerID);
        if (user == null) {
            return false;
        }

        HttpResponse<String> response = Utility.sendRequest(String.format("/user/%s/ban?serverbundle_id=%s", user.getId(), SvServer.serverbundleID), Types.PATCH);

        return response != null && response.statusCode() == 200;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender.isOp()) {
            if (args.length == 0) {
                Utility.sendUsage(sender, "/timeban <player> <time in minutes> <reason>");
                return true;
            }

            //args[0] = Player, args[1] = time, args[2] =reason
            Player p = Bukkit.getPlayer(args[0]);
            if (p != null) {
                long minutes;

                try {
                    minutes = Long.parseLong(args[1]);

                    if (minutes < 1) {
                        throw new NumberFormatException();
                    }
                } catch (NumberFormatException nfe) {
                    Utility.sendUsage(sender, "Invalid number of minutes.");
                    return false;
                }

                p.kickPlayer(String.format("You are banned from this server for %d minutes.\nReason: %s", minutes, args[2]));
                Bukkit.getBanList(BanList.Type.NAME).addBan(p.getUniqueId().toString(), args[2], new Date(Calendar.getInstance().getTimeInMillis() + (minutes * 60 * 1000)), sender.getName());
            } else {
                Utility.sendUsage(sender, "Player must be online.");
            }
            return false;
        }
        return true;
    }
}