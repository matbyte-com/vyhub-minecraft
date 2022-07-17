package net.vyhub.VyHubMinecraft.lib;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.vyhub.VyHubMinecraft.VyHub;
import net.vyhub.VyHubMinecraft.server.SvServer;
import org.bukkit.Bukkit;
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
            Bukkit.getServer().getLogger().severe("Can't create Request Body");
        }

        return requestBody;
    }

    public static HttpResponse<String> sendRequestBody(String endpoint, Types type, String body) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(VyHub.checkConfig().get("apiUrl") + endpoint))
                .setHeader("Authorization", "Bearer " + VyHub.checkConfig().get("apiKey"))
                .method(type.name(), HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = null;
        try {
            Bukkit.getServer().getLogger().fine(String.format("%s %s with body: %s", type.name(), endpoint, body));

            response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() < 200 || response.statusCode() > 299) {
                Bukkit.getServer().getLogger().severe(String.format("Error %d when accessing %s: %s", response.statusCode(), endpoint, response.body()));
            }
        } catch (IOException | InterruptedException e) {
            Bukkit.getServer().getLogger().severe("VyHub API is not reachable");
        }
        return response;
    }

    public static HttpResponse<String> sendRequest(String endpoint, Types type) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(VyHub.checkConfig().get("apiUrl") + endpoint))
                .setHeader("Authorization", "Bearer " + VyHub.checkConfig().get("apiKey"))
                .method(type.name(), HttpRequest.BodyPublishers.noBody())
                .build();
        HttpResponse<String> response = null;
        try {
            Bukkit.getServer().getLogger().fine(String.format("%s %s", type.name(), endpoint));

            response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() < 200 || response.statusCode() > 299) {
                Bukkit.getServer().getLogger().severe(String.format("Error %d when accessing %s: %s", response.statusCode(), endpoint, response.body()));
            }
        } catch (IOException | InterruptedException e) {
            Bukkit.getServer().getLogger().severe("VyHub API is not reachable");
        }
        return response;
    }

    public static JSONObject getServerInformationObject() {
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObj = null;

        try (FileReader reader = new FileReader("plugins/VyHub/serverInformation.json")) {
            JSONArray jsonArray = (JSONArray) jsonParser.parse(reader);
            jsonObj = (JSONObject) jsonArray.get(0);
        } catch (IOException | ParseException e) {
            Bukkit.getServer().getLogger().severe("ServerInformation doesn't exists");
            SvServer.getServerInformation();

            return null;
        }

        JSONObject serverbundleObject = (JSONObject) jsonObj.get("serverbundle");
        serverbundleID = serverbundleObject.get("id").toString();
        return jsonObj;
    }
}
