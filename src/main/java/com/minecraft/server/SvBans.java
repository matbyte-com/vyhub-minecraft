package com.minecraft.server;

import com.google.common.primitives.Bytes;
import org.bukkit.BanEntry;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.util.*;

public class SvBans {
    private static List<MessageDigest> hashList = new ArrayList<>();

    public static void getBans() {
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
        try {
            jsonObject = (JSONObject) jsonParser.parse(response.body());
            Collection keys =  jsonObject.keySet();
            for (Object v : keys) {
              String playerUUID = v.toString();

              JSONArray arr = (JSONArray) jsonObject.get(playerUUID);


              JSONObject banObject = (JSONObject) arr.get(0);

              String reason = banObject.get("reason").toString();
              OffsetDateTime dateTime = OffsetDateTime.parse(banObject.get("ends_on").toString());
              Date date = Date.from(dateTime.toInstant());

              //TODO wenn Player online kicken
              Bukkit.getBanList(BanList.Type.NAME).addBan(playerUUID, reason, date, "VyHub");

              MessageDigest messageDigest = MessageDigest.getInstance("MD5");
              messageDigest.update(Bytes.concat(playerUUID.getBytes(), date.toString().getBytes()));
              hashList.add(messageDigest);

            }

            unban(keys);
        } catch (ParseException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

       /* for (BanEntry ban : Bukkit.getBanList(BanList.Type.NAME).getBanEntries()) {
            System.out.println(ban.getExpiration());
        }
        */

    }

    private static void unban(Collection keys) {
        try (FileReader reader = new FileReader("banned-players.json")) {
            JSONParser jsonParser = new JSONParser();
            JSONArray jsonArray = (JSONArray) jsonParser.parse(reader);

            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject object = (JSONObject) jsonArray.get(i);
                String uuid = object.get("uuid").toString();

                if (!keys.contains(uuid)) {
                    Bukkit.getBanList(BanList.Type.NAME).pardon(uuid);
                }
            }

        } catch (ParseException | IOException fileNotFoundException) {
            fileNotFoundException.printStackTrace();
        }


    }
}
