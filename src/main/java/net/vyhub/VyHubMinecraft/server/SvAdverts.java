package net.vyhub.VyHubMinecraft.server;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import net.vyhub.VyHubMinecraft.Entity.Advert;
import net.vyhub.VyHubMinecraft.VyHub;
import net.vyhub.VyHubMinecraft.lib.Types;
import net.vyhub.VyHubMinecraft.lib.Utility;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class SvAdverts {
    private static Gson gson = new Gson();
    private static List<Advert> adverts = new ArrayList<>();

    private static int currentAdvert = 0;

    public static void loadAdverts() {
        HttpResponse<String> response = Utility.sendRequest(String.format("/advert/?active=true&serverbundle_id=%s", SvServer.serverbundleID), Types.GET);

        if (response == null || response.statusCode() != 200) {
            return;
        }

        adverts = gson.fromJson(response.body(), new TypeToken<ArrayList<Advert>>() {
        }.getType());
    }

    public static void nextAdvert() {
        if (adverts.size() < 1) {
            return;
        }

        if (adverts.get(currentAdvert) == null) {
            currentAdvert = 0;
        }

        Advert advert = adverts.get(currentAdvert);
        showAdvert(advert);
    }

    private static String replaceColorTags(String text) {
        text = text.replace("<red>", ChatColor.RED.toString());
        text = text.replace("<green>", ChatColor.GREEN.toString());
        text = text.replace("<blue>", ChatColor.BLUE.toString());
        text = text.replace("<yellow>", ChatColor.YELLOW.toString());
        text = text.replace("<pink>", ChatColor.LIGHT_PURPLE.toString());

        text = text.replaceAll("</[a-z]+>", ChatColor.WHITE.toString());

        return text;
    }

    private static void showAdvert(Advert advert) {
        List<String> lines = List.of(advert.getContent().split(System.lineSeparator()));
        String prefix = String.format("%s%s", ChatColor.BLUE, VyHub.config.getOrDefault("advert_prefix", "[★] "));

        for (String line : lines) {
            line = replaceColorTags(line);
            String message = String.format("%s%s%s", prefix, ChatColor.WHITE, line);

            Bukkit.getServer().broadcastMessage(message);
        }
    }
}