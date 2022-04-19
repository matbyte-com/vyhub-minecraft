package com.minecraft.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.minecraft.Entity.VyHubPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SvUser implements Listener {

    public static List<VyHubPlayer> vyHubPlayers = new ArrayList<>();

    @EventHandler
    public static void checkUserExists(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        getUser(player.getUniqueId().toString());
    }


    public static VyHubPlayer getUser(String UUID) {
        if (vyHubPlayers != null) {
            for (VyHubPlayer player : vyHubPlayers) {
                if (player.getIdentifier().equals(UUID)) {
                    return player;
                }
            }
        }

        String userInformation = "";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.vyhub.app/myinstance/v1/user/" + UUID + "?type=MINECRAFT"))
                .method("GET", HttpRequest.BodyPublishers.noBody())
                .build();
        HttpResponse<String> response = null;

        try {
            response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        if (response.statusCode() == 404) {
            userInformation = createUser(UUID);
        } else {
            userInformation = response.body();
        }

        Gson gson = new Gson();
        VyHubPlayer vyHubPlayer = gson.fromJson(userInformation, VyHubPlayer.class);

        vyHubPlayers.add(vyHubPlayer);
        return vyHubPlayer;
    }

    public static String createUser(String UUID) {
        int statusCode = 500;
        HttpResponse<String> response = null;

        while (statusCode != 200) {
            var values = new HashMap<String, Object>() {{
                put("type", "MINECRAFT");
                put("identifier", UUID);
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
                    .uri(URI.create("https://api.vyhub.app/myinstance/v1/user/"))
                    .setHeader("Authorization", "Bearer " + "coXfmc7Uuf08poaxwsOWOQK7zwke9xQodhqL1iDmD4WPC2iuIa2gkOKdXEyZleRX")
                    .method("POST", HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            try {
                response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
            statusCode = response.statusCode();
        }

        return response.body();
    }
}
