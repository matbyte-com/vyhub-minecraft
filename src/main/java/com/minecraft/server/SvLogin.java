package com.minecraft.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;

public class SvLogin implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length == 0) {
            sendUsage(sender);
            return true;
        }

        Player player = (Player) sender;

        var values = new HashMap<String, Object>() {{
            put("user_type", "MINECRAFT");
            put("identifier", player.getUniqueId());
        }};

        var objectMapper = new ObjectMapper();
        String requestBody = null;

        try {
            requestBody = objectMapper
                    .writeValueAsString(values);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        System.out.println("ARGS: "+ args[0]);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.vyhub.app/myinstance/v1/auth/request/" + args[0]))
                .setHeader("Authorization", "Bearer " + "RX0E5fAb9VMrFJbnETjLbGAXeCMEoPnUwjocWXtfY0V3lDObZWIehQKpQ4Kv5jWt")
                .method("PATCH", HttpRequest.BodyPublishers.ofString(requestBody))
                .build();
        HttpResponse<String> response = null;
        try {
           response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        if (response.statusCode() != 200) {
            sendUsage(sender);
            System.out.println("STATUSCODE " + response.body() + "    " + response.statusCode());
            return true;
        }
        sender.sendMessage("§aSuccessfully logged in!");
        System.out.println("YAAA " + response.body() + "    " + response.statusCode());
        return false;
    }

    private void sendUsage(CommandSender sender) {
        sender.sendMessage("§cUsage: §9/login <UUID>");
    }
}
