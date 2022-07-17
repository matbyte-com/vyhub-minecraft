package net.vyhub.VyHubMinecraft.server;


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
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class SvBans implements CommandExecutor {

    private static Map<String, Boolean> banPlayer = new HashMap<>();

    private static DateTimeFormatter isoDateFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
    private static DateTimeFormatter mcDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss Z");


    public static void getVyHubBans() {

        HttpResponse<String> response = Utility.sendRequest("/server/bundle/" + Utility.serverbundleID + "/ban?active=true", Types.GET);

        if (response == null || response.statusCode() !=  200) {
            return;
        }

        try (FileWriter file = new FileWriter("plugins/VyHub/banList.json")) {
            file.write(response.body());
            file.flush();

        } catch (IOException e) {
            Bukkit.getServer().getLogger().warning("VyHub API is not reachable");
        }

        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject;
        Map<String, String> uuidIDMap = new HashMap<>();
        try {
            jsonObject = (JSONObject) jsonParser.parse(response.body());
            Collection keys =  jsonObject.keySet();


            for (Object v : keys) {
                String playerUUID = v.toString();

                JSONArray arr = (JSONArray) jsonObject.get(playerUUID);
                JSONObject banObject = (JSONObject) arr.get(0);
                String reason = banObject.get("reason").toString();
                uuidIDMap.put(playerUUID, banObject.get("id").toString());


                Date date = null;
                if (banObject.get("ends_on") != null) {
                    OffsetDateTime dateTime = OffsetDateTime.parse(banObject.get("ends_on").toString());
                    date = Date.from(dateTime.toInstant());
                }

                if (!banPlayer.containsKey(playerUUID)) {
                    for (Player banPlayer : Bukkit.getServer().getOnlinePlayers()) {
                        if (banPlayer.getUniqueId().toString().equals(playerUUID)) {
                            banPlayer.kickPlayer(reason);
                        }
                    }
                    Bukkit.getBanList(BanList.Type.NAME).addBan(playerUUID, reason, date, "VyHub");
                    banPlayer.put(playerUUID, false);
                } else if (banPlayer.get(playerUUID)) {
                    banPlayer.put(playerUUID, false);
                }
            }
            unban(uuidIDMap);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }


    public static void getMinecraftBans() {
        try {
            FileReader reader = new FileReader("banned-players.json");
            JSONParser jsonParser = new JSONParser();
            JSONArray jsonArray = (JSONArray) jsonParser.parse(reader);
            for (Object ban : jsonArray) {
                JSONObject object = (JSONObject) ban;

                String uuid = object.get("uuid").toString();
                String expires = object.get("expires").toString();
                String reason = object.get("reason").toString();
                String created = object.get("created").toString();
                String source = object.get("source").toString();

                Long time = null;
                ZonedDateTime createdDate = ZonedDateTime.parse(created, mcDateFormatter);

                if (!expires.equals("forever")) {
                    ZonedDateTime expiresDate = ZonedDateTime.parse(expires, mcDateFormatter);
                    time = expiresDate.toEpochSecond() - createdDate.toEpochSecond();
                }

                if (!banPlayer.containsKey(uuid)) {
                    banPlayer.put(uuid, true);

                    String createdPlayerUUID = "";
                    for (Player sourcePlayer : Bukkit.getServer().getOnlinePlayers()) {
                        if (sourcePlayer.getName().equals(source) && !source.equals("CONSOLE") && !source.equals("Server")) {
                            createdPlayerUUID = sourcePlayer.getUniqueId().toString();
                        }
                    }

                    String vyHubPlayerUUID = SvUser.getUser(uuid).getId();
                    String vyHubAdminPlayerUUID = "";
                    if (!source.equals("CONSOLE") && !source.equals("Server")) {
                        vyHubAdminPlayerUUID = SvUser.getUser(createdPlayerUUID).getId();
                    }

                    Long finalTime = time;
                    String finalCreated = createdDate.format(isoDateFormatter);
                    HashMap<String, Object> values = new HashMap<>() {{
                        put("length", finalTime);
                        put("reason", reason);
                        put("serverbundle_id", Utility.serverbundleID);
                        put("user_id", vyHubPlayerUUID);
                        put("created_on", finalCreated);
                    }};
                    if (!source.equals("CONSOLE") && !source.equals("Server")) {
                        Utility.sendRequestBody("/ban/?morph_user_id=" + vyHubAdminPlayerUUID, Types.POST, Utility.createRequestBody(values));
                    } else {
                        Utility.sendRequestBody("/ban/", Types.POST, Utility.createRequestBody(values));
                    }
                }
            }
        } catch (ParseException | IOException e) {
            throw new RuntimeException(e);
        }
        getVyHubBans();
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

            for (String uuidVy : uuidIdMap.keySet()) {
                if (!uuidMcList.contains(uuidVy) && !banPlayer.get(uuidVy)) {
                    banPlayer.remove(uuidVy);

                    HashMap<String, Object> values = new HashMap<>();
                    values.put("status", "UNBANNED");

                    Utility.sendRequestBody("/ban/"+ uuidIdMap.get(uuidVy), Types.PATCH, Utility.createRequestBody(values));
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
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.getName().equals(args[0])) {
                    p.kickPlayer(args[2]);
                    Bukkit.getBanList(BanList.Type.NAME).addBan(p.getUniqueId().toString(), args[2], new Date(Calendar.getInstance().getTimeInMillis() + (Long.parseLong(args[1]) * 60 * 1000)), sender.getName());
                }
            }
            return false;
        }
        return true;
    }
}
