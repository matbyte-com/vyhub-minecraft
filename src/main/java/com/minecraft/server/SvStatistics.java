package com.minecraft.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bukkit.Bukkit;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Objects;

public class SvStatistics {

    public static void sendPlayerTime() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.vyhub.app/myinstance/v1/user/attribute/definition?name=playtime"))
                .method("GET", HttpRequest.BodyPublishers.noBody())
                .build();
        HttpResponse<String> response = null;
        try {
            response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }


        if (Objects.equals(response.body(), "[]")) {
            System.out.println("ALLLLLTERRRRRR");
            var values = new HashMap<String, Object>() {{
                put("name", "playtime");
                put("title", "Play Time");
                put("unit", "HOURS");
                put("type", "ACCUMULATED");
                put("accumulation_interval", "day");
                put("unspecific", true);
            }};

            var objectMapper = new ObjectMapper();
            String requestBody = null;

            try {
                requestBody = objectMapper
                        .writeValueAsString(values);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }


            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.vyhub.app/myinstance/v1/user/attribute/definition"))
                    .setHeader("Authorization", "Bearer " + "RX0E5fAb9VMrFJbnETjLbGAXeCMEoPnUwjocWXtfY0V3lDObZWIehQKpQ4Kv5jWt")
                    .method("POST", HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();
            HttpResponse<String> resp = null;
            try {
                resp = HttpClient.newHttpClient().send(req, HttpResponse.BodyHandlers.ofString());
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
}
