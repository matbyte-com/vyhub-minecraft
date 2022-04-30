package com.minecraft.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bukkit.Bukkit;
import org.bukkit.generator.WorldInfo;
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
import java.util.HashMap;

public class SvServer {

    //TODO URL austauschen für endgültige Anwendung
    public static HttpResponse<String> getServerInformation() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.vyhub.app/myinstance/v1/server/?type=MINECRAFT"))
                .method("GET", HttpRequest.BodyPublishers.noBody())
                .build();
        HttpResponse<String> response = null;
        try {
            response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        try (FileWriter file = new FileWriter("serverInformation.json")) {
            file.write(response.body());
            file.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }

    //TODO getServerInformationObject().get("id") ServerID aus Config datei lesen
    public static void patchServer() {
        var values = new HashMap<String, Object>() {{
            put("users_max", String.valueOf(Bukkit.getMaxPlayers()));
            put("users_current", String.valueOf(Bukkit.getOnlinePlayers().size()));
            put("is_alive", "true");
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
                .uri(URI.create("https://api.vyhub.app/myinstance/v1/server/" + getServerInformationObject().get("id")))
                .setHeader("Authorization", "Bearer " + "coXfmc7Uuf08poaxwsOWOQK7zwke9xQodhqL1iDmD4WPC2iuIa2gkOKdXEyZleRX")
                .method("PATCH", HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        try {
           HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }


    public static JSONObject getServerInformationObject() {
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObj = null;
        if (getServerInformation().statusCode() != 200) {
            try (FileReader reader = new FileReader("serverInformation.json")) {

                JSONArray jsonArray = (JSONArray) jsonParser.parse(reader);
                jsonObj = (JSONObject) jsonArray.get(0);

            } catch (IOException | ParseException e) {
                e.printStackTrace();
            }
        } else {
            try {
                JSONArray jsonArray = (JSONArray) jsonParser.parse(getServerInformation().body());
                jsonObj = (JSONObject) jsonArray.get(0);

            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return jsonObj;
    }
}

