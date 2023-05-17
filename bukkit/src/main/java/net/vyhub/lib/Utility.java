package net.vyhub.lib;

import org.bukkit.ChatColor;

public class Utility {
    static String replaceColorTags(String text) {
        text = text.replace("<red>", ChatColor.RED.toString());
        text = text.replace("<green>", ChatColor.GREEN.toString());
        text = text.replace("<blue>", ChatColor.BLUE.toString());
        text = text.replace("<yellow>", ChatColor.YELLOW.toString());
        text = text.replace("<pink>", ChatColor.LIGHT_PURPLE.toString());

        text = text.replaceAll("</[a-z]+>", ChatColor.WHITE.toString());

        return text;
    }
}
