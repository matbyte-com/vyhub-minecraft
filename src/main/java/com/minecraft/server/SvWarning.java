package com.minecraft.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minecraft.Entity.VyHubPlayer;
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
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class SvWarning implements CommandExecutor {

    public static void createWarning(Player player, String reason) {


        String vyHubPlayerUUID = "";
        for (VyHubPlayer vyHubPlayer : SvUser.vyHubPlayers) {
            if (vyHubPlayer.getIdentifier().equals(player.getUniqueId().toString())) {
                vyHubPlayerUUID = vyHubPlayer.getId();
            }
        }

        String finalVyHubPlayerUUID = vyHubPlayerUUID;
        var values = new HashMap<String, Object>() {{
            put("reason", reason);
            put("serverbundle_id", "30034cb5-582f-404b-b6d9-d5f4cb7eaa8b");
            put("user_id", finalVyHubPlayerUUID);
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
                .uri(URI.create("https://api.vyhub.app/myinstance/v1/warning/?morph_user_id="+ vyHubPlayerUUID ))
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

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = (Player) sender;
        if (player.isOp()) {
            if (args.length == 0) {
                sendUsage(sender);
                return true;
            }

            //args[0] = Player, args[1] =reason

            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.getName().equals(args[0])) {
                    createWarning(p, args[1]);
                }
            }

            return false;
        }
        return true;
    }

    private void sendUsage(CommandSender sender) {
        sender.sendMessage("§cUsage: §9/warning <Player> <reason>");
    }
}
