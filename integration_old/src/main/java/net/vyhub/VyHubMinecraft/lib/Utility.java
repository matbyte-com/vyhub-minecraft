package net.vyhub.VyHubMinecraft.lib;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.vyhub.VyHubMinecraft.VyHub;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

public class Utility {

    public static Logger logger = Bukkit.getServer().getLogger();

    public static void sendUsage(CommandSender sender, String message) {
        sender.sendMessage("§cUsage: §9" + message);
    }

    public static String createRequestBody(HashMap<String, Object> values) {
        ObjectMapper objectMapper = new ObjectMapper();
        String requestBody = null;
        try {
            requestBody = objectMapper.writeValueAsString(values);
        } catch (JsonProcessingException e) {
            logger.severe("Can't create Request Body");
        }

        return requestBody;
    }

    public static HttpResponse<String> sendRequestBody(String endpoint, Types type, String body, List<Integer> ignoreCodes) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(VyHub.config.get("api_url") + endpoint))
                .setHeader("Authorization", "Bearer " + VyHub.config.get("api_key"))
                .method(type.name(), (body != null ? HttpRequest.BodyPublishers.ofString(body) : HttpRequest.BodyPublishers.noBody()))
                .timeout(Duration.ofSeconds(5))
                .build();

        HttpResponse<String> response = null;
        try {
            if (body != null) {
                logger.fine(String.format("%s %s with body: %s", type.name(), endpoint, body));
            } else {
                logger.fine(String.format("%s %s", type.name(), endpoint));
            }

            response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() < 200 || response.statusCode() > 399) {
                if (ignoreCodes == null || !ignoreCodes.contains(response.statusCode())) {
                    logger.severe(String.format("Error %d when accessing %s:", response.statusCode(), endpoint));

                    if (response.statusCode() != 502) {
                        logger.severe(String.format("Error Message: %s", response.body()));
                    }
                }
            }
        } catch (IOException | InterruptedException e) {
            logger.severe(String.format("VyHub API is not reachable: %s", e.getMessage()));
        }
        return response;
    }

    public static HttpResponse<String> sendRequestBody(String endpoint, Types type, String body) {
        return sendRequestBody(endpoint, type, body, null);
    }

    public static HttpResponse<String> sendRequest(String endpoint, Types type) {
        return sendRequestBody(endpoint, type, null, null);
    }

    public static HttpResponse<String> sendRequest(String endpoint, Types type, List<Integer> ignoreCodes) {
        return sendRequestBody(endpoint, type, null, ignoreCodes);
    }
}
