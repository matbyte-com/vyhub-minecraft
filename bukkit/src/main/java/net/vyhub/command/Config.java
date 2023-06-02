package net.vyhub.command;

import net.vyhub.BukkitVyHubPlugin;
import net.vyhub.config.VyHubConfiguration;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class Config implements CommandExecutor {
    private final BukkitVyHubPlugin plugin;

    public Config(final BukkitVyHubPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.isOp()) {
            sender.sendMessage(ChatColor.DARK_RED + plugin.getI18n().get("commandOnlyForAdmins"));
            return true;
        }

        if (command.getName().equals("vh_config")) {
            if (args.length != 2) {
                return false;
            }

            String key = args[0];
            String value = args[1];

            if (key.isEmpty() || value.isEmpty()) {
                return false;
            }

            VyHubConfiguration.setConfigValue(key, value);
            return true;
        }

        return false;
    }
}
