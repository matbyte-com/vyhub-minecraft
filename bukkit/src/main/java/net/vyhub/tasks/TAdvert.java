package net.vyhub.tasks;

import net.vyhub.VyHubPlatform;
import net.vyhub.abstractClasses.AAdvert;
import net.vyhub.config.VyHubConfiguration;
import net.vyhub.entity.Advert;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;

public class TAdvert extends AAdvert {
    private static List<Advert> adverts = new ArrayList<>();

    private static int currentAdvert = 0;

    public TAdvert(VyHubPlatform platform) {
        super(platform);
    }

    public String replaceColorTags(String text) {
        text = text.replace("<red>", ChatColor.RED.toString());
        text = text.replace("<green>", ChatColor.GREEN.toString());
        text = text.replace("<blue>", ChatColor.BLUE.toString());
        text = text.replace("<yellow>", ChatColor.YELLOW.toString());
        text = text.replace("<pink>", ChatColor.LIGHT_PURPLE.toString());

        text = text.replaceAll("</[a-z]+>", ChatColor.WHITE.toString());

        return text;
    }

    public void showAdvert(Advert advert) {
        List<String> lines = List.of(advert.getContent().split(System.lineSeparator()));
        String prefix = String.format("%s%s", ChatColor.BLUE, VyHubConfiguration.getAdvertPrefix());

        for (String line : lines) {
            line = replaceColorTags(line);
            String message = String.format("%s%s%s", prefix, ChatColor.WHITE, line);

            Bukkit.getServer().broadcastMessage(message);
        }
    }
}
