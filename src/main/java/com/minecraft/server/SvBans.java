package com.minecraft.server;


import com.minecraft.Entity.VyHubPlayer;
import com.minecraft.lib.Types;
import com.minecraft.lib.Utility;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
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
import java.util.*;

public class SvBans implements CommandExecutor {

    private static Map<String, Boolean> banPlayer = new HashMap<>();


    public static void getVyHubBans() {

        HttpResponse<String> response = Utility.sendRequest("/server/bundle/" + Utility.serverbundleID + "/ban?active=true", Types.GET);

        try (FileWriter file = new FileWriter("plugins/Vyhub/banList.json")) {
            file.write(response.body());
            file.flush();

        } catch (IOException e) {
            e.printStackTrace();
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
            getMinecraftBans();
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

                String time = "";
                if (expires.equals("forever")) {
                    time = null;
                } else   {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Date createdDate = null;
                    Date expiresDate = null;

                    createdDate = sdf.parse(created.replaceAll("[+].*$", ""));
                    expiresDate = sdf.parse(expires.replaceAll("[+].*$", ""));

                    time = String.valueOf((expiresDate.getTime() - createdDate.getTime())/1000);
                }


                if (!banPlayer.containsKey(uuid)) {
                    banPlayer.put(uuid, true);

                    String createdPlayerUUID = "";
                    //TODO change to Onlineplayers
                    for (OfflinePlayer sourcePlayer : Bukkit.getServer().getOfflinePlayers()) {
                        if (sourcePlayer.getName().equals(source)) {
                            createdPlayerUUID = sourcePlayer.getUniqueId().toString();
                        }
                    }

                    String vyHubPlayerUUID = "";
                    String vyHubAdminPlayerUUID = "";
                    for (VyHubPlayer player : SvUser.vyHubPlayers) {
                        if (player.getIdentifier().equals(uuid)) {
                            vyHubPlayerUUID = player.getId();
                            System.out.println("UUID: " + player.getId());
                        }
                        if (player.getIdentifier().equals(createdPlayerUUID)) {
                            vyHubAdminPlayerUUID = player.getId();
                        }
                    }

                    String finalVyHubPlayerUUID = vyHubPlayerUUID;
                    String finalTime = time;
                    HashMap<String, Object> values = new HashMap<>() {{
                        put("length", finalTime);
                        put("reason", reason);
                        put("serverbundle_id", Utility.serverbundleID);
                        put("user_id", finalVyHubPlayerUUID);
                        put("created_on", created.replaceFirst(" ","T").replaceAll("[ ].*$", ".000Z"));
                    }};

                    Utility.sendRequestBody("/ban/?morph_user_id="+ vyHubAdminPlayerUUID, Types.POST, Utility.createRequestBody(values));
                }
            }
        } catch (ParseException | IOException | java.text.ParseException e) {
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

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date expiresDate = sdf.parse(expires.replaceAll("[+].*$", ""));

                uuidMcList.add(uuid);

                if (!uuidIdMap.containsKey(uuid) && !banPlayer.get(uuid)) {
                    Bukkit.getBanList(BanList.Type.NAME).pardon(uuid);
                    banPlayer.remove(uuid);
                }

                if (expiresDate.before(new Date())) {
                    Bukkit.getBanList(BanList.Type.NAME).pardon(uuid);
                    banPlayer.remove(uuid);
                }
            }

            for (String uuidVy : uuidIdMap.keySet()) {
                if (!uuidMcList.contains(uuidVy) && !banPlayer.get(uuidVy)) {
                    banPlayer.remove(uuidVy);

                    Utility.sendRequest("/ban/"+ uuidIdMap.get(uuidVy), Types.DELETE);
                }
            }
        } catch (ParseException | IOException fileNotFoundException) {
            fileNotFoundException.printStackTrace();
        } catch (java.text.ParseException e) {
            throw new RuntimeException(e);
        }
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = (Player) sender;
        if (player.isOp()) {
            if (args.length == 0) {
                Utility.sendUsage(sender, "/timeban <Player> <time in minutes> <reason>");
                return true;
            }

            //args[0] = Player, args[1] = time, args[2] =reason
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.getName().equals(args[0])) {
                    player.kickPlayer(args[2]);
                    Bukkit.getBanList(BanList.Type.NAME).addBan(p.getUniqueId().toString(), args[2], new Date(Calendar.getInstance().getTimeInMillis() + (Long.parseLong(args[1]) * 60 * 1000)), player.getName());
                }
            }
            return false;
        }
        return true;
    }
}
