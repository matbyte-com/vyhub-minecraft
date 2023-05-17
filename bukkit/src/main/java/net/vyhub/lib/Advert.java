package net.vyhub.lib;

import net.vyhub.VyHub;
import net.vyhub.server.SvAdverts;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.util.List;

public class Advert {
    public static void showAdvert() {
        net.vyhub.Entity.Advert advert = SvAdverts.nextAdvert();
        if (advert == null) {
            return;
        }
        List<String> lines = List.of(advert.getContent().split(System.lineSeparator()));
        String prefix = String.format("%s%s", ChatColor.BLUE, VyHub.config.getOrDefault("advert_prefix", "[★] "));

        for (String line : lines) {
            line = Utility.replaceColorTags(line);
            String message = String.format("%s%s%s", prefix, ChatColor.WHITE, line);

            Bukkit.getServer().broadcastMessage(message);
        }
    }
}
