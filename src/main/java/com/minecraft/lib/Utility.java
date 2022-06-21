package com.minecraft.lib;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minecraft.Vyhub;
import com.minecraft.server.SvServer;
import org.bukkit.command.CommandSender;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;

public class Utility {

    public static String serverbundleID = "";

    public static void sendUsage(CommandSender sender, String message) {
        sender.sendMessage("§cUsage: §9" + message);
    }

    public static String createRequestBody(HashMap<String, Object> values) {
        ObjectMapper objectMapper = new ObjectMapper();
        String requestBody = null;
        try {
            requestBody = objectMapper.writeValueAsString(values);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return requestBody;
    }

    public static HttpResponse<String> sendRequestBody(String endpoint, Types type, String body) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(Vyhub.checkConfig().get("apiUrl") + endpoint))
                .setHeader("Authorization", "Bearer " + Vyhub.checkConfig().get("apiKey"))
                .method(type.name(), HttpRequest.BodyPublishers.ofString(body))
                .build();
        HttpResponse<String> response = null;
        try {
            response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return response;
    }

    public static HttpResponse<String> sendRequest(String endpoint, Types type) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(Vyhub.checkConfig().get("apiUrl") + endpoint))
                .setHeader("Authorization", "Bearer " + Vyhub.checkConfig().get("apiKey"))
                .method(type.name(), HttpRequest.BodyPublishers.noBody())
                .build();
        HttpResponse<String> response = null;
        try {
            response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return response;
    }

    public static JSONObject getServerInformationObject() {
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObj = null;
        if (SvServer.getServerInformation().statusCode() != 200) {
            try (FileReader reader = new FileReader("plugins/Vyhub/serverInformation.json")) {

                JSONArray jsonArray = (JSONArray) jsonParser.parse(reader);
                jsonObj = (JSONObject) jsonArray.get(0);

            } catch (IOException | ParseException e) {
                e.printStackTrace();
            }
        } else {
            try {
                JSONArray jsonArray = (JSONArray) jsonParser.parse(SvServer.getServerInformation().body());
                jsonObj = (JSONObject) jsonArray.get(0);

            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        JSONObject serverbundleObject = (JSONObject) jsonObj.get("serverbundle");
        serverbundleID = serverbundleObject.get("id").toString();
        return jsonObj;
    }
}
