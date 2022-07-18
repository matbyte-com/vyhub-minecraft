/*package net.vyhub.VyHubMinecraft.server;


import net.vyhub.VyHubMinecraft.Entity.VyHubUser;
import net.vyhub.VyHubMinecraft.lib.Types;
import net.vyhub.VyHubMinecraft.lib.Utility;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class SvTemp implements CommandExecutor {
    private static Map<String, Boolean> banPlayer = new HashMap<>();

    private static DateTimeFormatter isoDateFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
    private static DateTimeFormatter mcDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss ZZZ");


    public static void getVyHubBans() {

        HttpResponse<String> response = Utility.sendRequest("/server/bundle/" + Utility.serverbundleID + "/ban?active=true", Types.GET);

        if (response == null || response.statusCode() !=  200) {
            return;
        }

        try (FileWriter file = new FileWriter("plugins/VyHub/banList.json")) {
            file.write(response.body());
            file.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        JSONParser jsonParser = new JSONParser();
        Map<String, String> playerIDbanIDMap = new HashMap<>();
        try {
            JSONObject userBansObject = (JSONObject) jsonParser.parse(response.body());
            Collection keys =  userBansObject.keySet();

            for (Object playerID : keys) {
                String playerUUID = playerID.toString();

                JSONArray bansArray = (JSONArray) userBansObject.get(playerUUID);
                JSONObject banObject = (JSONObject) bansArray.get(0);
                String reason = banObject.get("reason").toString();
                playerIDbanIDMap.put(playerUUID, banObject.get("id").toString());
                String endsOn = banObject.get("ends_on").toString();

                Date endDate = null;
                if (banObject.get("ends_on") != null) {
                    ZonedDateTime expiresDate = ZonedDateTime.parse(endsOn, isoDateFormatter);
                    endDate = Date.from(expiresDate.toInstant());
                }

                if (!banPlayer.containsKey(playerUUID)) {
                    Player bannnedPlayer = Bukkit.getPlayer(playerUUID);
                    if (bannnedPlayer != null) {
                        bannnedPlayer.kickPlayer(reason);
                    }

                    Bukkit.getBanList(BanList.Type.NAME).addBan(playerUUID, reason, endDate, "VyHub");
                    banPlayer.put(playerUUID, false);
                } else if (banPlayer.get(playerUUID)) {
                    banPlayer.put(playerUUID, false);
                }
            }
            getMinecraftBans();
            unban(playerIDbanIDMap);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }


    public static void getMinecraftBans() {
        try {
            FileReader reader = new FileReader("banned-players.json");
            JSONParser jsonParser = new JSONParser();
            JSONArray bannedPlayersArray = (JSONArray) jsonParser.parse(reader);
            for (Object ban : bannedPlayersArray) {
                JSONObject object = (JSONObject) ban;

                String uuid = object.get("uuid").toString();
                String expires = object.get("expires").toString();
                String reason = object.get("reason").toString();
                String created = object.get("created").toString();
                String source = object.get("source").toString();

                if (!banPlayer.containsKey(uuid)) {
                    VyHubUser user = SvUser.getUser(uuid);
                    if (user == null) {
                        return;
                    }

                    banPlayer.put(uuid, true);

                    Long time = null;
                    ZonedDateTime createdDate = ZonedDateTime.parse(created, mcDateFormatter);

                    if (!expires.equals("forever")) {
                        ZonedDateTime expiresDate = ZonedDateTime.parse(expires, mcDateFormatter);
                        time = expiresDate.toEpochSecond() - createdDate.toEpochSecond();
                    }

                    String vyHubAdminUserUUID = null;

                    if (!source.equals("CONSOLE") && !source.equals("Server")) {
                        Player sourcePlayer = Bukkit.getPlayer(source);

                        if (sourcePlayer != null) {
                            VyHubUser admin = SvUser.getUser(sourcePlayer.getUniqueId().toString());

                            if (admin != null) {
                                vyHubAdminUserUUID = admin.getId();
                            }
                        }
                    }

                    String vyHubPlayerUUID = user.getId();
                    Long finalTime = time;
                    String finalCreated = createdDate.format(isoDateFormatter);
                    HashMap<String, Object> values = new HashMap<>() {{
                        put("length", finalTime);
                        put("reason", reason);
                        put("serverbundle_id", Utility.serverbundleID);
                        put("user_id", vyHubPlayerUUID);
                        put("created_on", finalCreated);
                    }};

                    if (vyHubAdminUserUUID != null) {
                        Utility.sendRequestBody("/ban/?morph_user_id=" + vyHubAdminUserUUID, Types.POST, Utility.createRequestBody(values));
                    } else {
                        Utility.sendRequestBody("/ban/", Types.POST, Utility.createRequestBody(values));
                    }
                }
            }
        } catch (ParseException | IOException e) {
            throw new RuntimeException(e);
        }

    }

    private static void unban(Map<String, String> uuidIdMap) {
        try (FileReader reader = new FileReader("banned-players.json")) {
            JSONParser jsonParser = new JSONParser();
            JSONArray jsonArray = (JSONArray) jsonParser.parse(reader);
            List<String> uuidMcList = new ArrayList<>();

            for (Object o : jsonArray) {
                JSONObject object = (JSONObject) o;
                String uuid = object.get("uuid").toString();
                String expires = object.get("expires").toString();

                if (!expires.equals("forever")) {
                    ZonedDateTime expiresDate = ZonedDateTime.parse(expires, mcDateFormatter);

                    uuidMcList.add(uuid);

                    if (!uuidIdMap.containsKey(uuid) && !banPlayer.get(uuid)) {
                        Bukkit.getBanList(BanList.Type.NAME).pardon(uuid);
                        banPlayer.remove(uuid);
                    }

                    if (expiresDate.isBefore(ZonedDateTime.now())) {
                        Bukkit.getBanList(BanList.Type.NAME).pardon(uuid);
                        banPlayer.remove(uuid);
                    }
                }
            }

            for (String playerUUID : uuidIdMap.keySet()) {
                if (!uuidMcList.contains(playerUUID) && !banPlayer.get(playerUUID)) {
                    banPlayer.remove(playerUUID);

                    Bukkit.getBanList(BanList.Type.NAME).pardon(playerUUID);

                    HashMap<String, Object> values = new HashMap<>();
                    values.put("status", "UNBANNED");

                    Utility.sendRequestBody("/ban/"+ uuidIdMap.get(playerUUID), Types.PATCH, Utility.createRequestBody(values));
                }
            }
        } catch (ParseException | IOException fileNotFoundException) {
            fileNotFoundException.printStackTrace();
        }
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
                p.kickPlayer(args[2]);
                Bukkit.getBanList(BanList.Type.NAME).addBan(p.getUniqueId().toString(), args[2], new Date(Calendar.getInstance().getTimeInMillis() + (Long.parseLong(args[1]) * 60 * 1000)), sender.getName());
            }
            return false;
        }
        return true;
    }
}
*/