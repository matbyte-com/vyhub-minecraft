package com.minecraft.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minecraft.Entity.VyHubPlayer;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.util.*;

public class SvBans {
    private static Map<String, Boolean> banPlayer = new HashMap<>();

    public static void getVyHubBans() {
        JSONObject serverbundleObject = (JSONObject) SvServer.getServerInformationObject().get("serverbundle");
        String serverBundleId = serverbundleObject.get("id").toString();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.vyhub.app/myinstance/v1/server/bundle/" + serverBundleId + "/ban?active=true"))
                .setHeader("Authorization", "Bearer " + "RX0E5fAb9VMrFJbnETjLbGAXeCMEoPnUwjocWXtfY0V3lDObZWIehQKpQ4Kv5jWt")
                .method("GET", HttpRequest.BodyPublishers.noBody())
                .build();
        HttpResponse<String> response = null;
        try {
            response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        try (FileWriter file = new FileWriter("banList.json")) {
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
              String dateCalc = "";
              if (banObject.get("ends_on") != null) {
                  OffsetDateTime dateTime = OffsetDateTime.parse(banObject.get("ends_on").toString());
                  date = Date.from(dateTime.toInstant());
                  dateCalc = date.toString();
              } else {
                  dateCalc = "forever";
                }

              for (Player banPlayer : Bukkit.getServer().getOnlinePlayers()) {
                  if (banPlayer.getUniqueId().toString().equals(playerUUID)) {
                      banPlayer.kickPlayer(reason);
                  }
              }

                if (!banPlayer.containsKey(playerUUID)) {
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

                    time = String.valueOf((expiresDate.getTime() - createdDate.getTime())/60);
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
                        }
                        if (player.getIdentifier().equals(createdPlayerUUID)) {
                            vyHubAdminPlayerUUID = player.getId();
                        }
                    }

                    JSONObject serverbundleObject = (JSONObject) SvServer.getServerInformationObject().get("serverbundle");
                    String serverBundleId = serverbundleObject.get("id").toString();

                    String finalVyHubPlayerUUID = vyHubPlayerUUID;
                    String finalTime = time;
                    var values = new HashMap<String, Object>() {{
                        put("length", finalTime);
                        put("reason", reason);
                        put("serverbundle_id", serverBundleId);
                        put("user_id", finalVyHubPlayerUUID);
                        put("created_on", created.replaceFirst(" ","T").replaceAll("[ ].*$", ".000Z"));
                    }};

                    var objectMapper = new ObjectMapper();
                    String requestBody = null;

                    try {
                        requestBody = objectMapper
                                .writeValueAsString(values);
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }



                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create("https://api.vyhub.app/myinstance/v1/ban/?morph_user_id="+ vyHubAdminPlayerUUID ))
                            .setHeader("Authorization", "Bearer " + "RX0E5fAb9VMrFJbnETjLbGAXeCMEoPnUwjocWXtfY0V3lDObZWIehQKpQ4Kv5jWt")
                            .method("POST",  HttpRequest.BodyPublishers.ofString(requestBody))
                            .build();
                    HttpResponse<String> response = null;
                    try {
                        response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                    }


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
                uuidMcList.add(uuid);

                if (!uuidIdMap.containsKey(uuid) && !banPlayer.get(uuid)) {
                    Bukkit.getBanList(BanList.Type.NAME).pardon(uuid);
                    banPlayer.remove(uuid);
                }
            }

            for (String uuidVy : uuidIdMap.keySet()) {
                if (!uuidMcList.contains(uuidVy) && !banPlayer.get(uuidVy)) {
                    banPlayer.remove(uuidVy);



                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create("https://api.vyhub.app/myinstance/v1/ban/"+ uuidIdMap.get(uuidVy)))
                            .setHeader("Authorization", "Bearer " + "RX0E5fAb9VMrFJbnETjLbGAXeCMEoPnUwjocWXtfY0V3lDObZWIehQKpQ4Kv5jWt")
                            .method("DELETE",  HttpRequest.BodyPublishers.noBody())
                            .build();
                    HttpResponse<String> response = null;
                    try {
                        response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (ParseException | IOException fileNotFoundException) {
            fileNotFoundException.printStackTrace();
        }
    }
}
