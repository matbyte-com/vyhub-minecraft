package net.vyhub.command;

import net.vyhub.VyHubPlugin;
import net.vyhub.config.VyHubConfiguration;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class Config implements CommandExecutor {
    private final VyHubPlugin plugin;

    public Config(final VyHubPlugin plugin) {
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
            sender.sendMessage(ChatColor.GREEN + plugin.getI18n().get("Config Saved"));

            return true;
        }

        if (command.getName().equals("vh_setup")) {
            // Fast setup /vh_setup <api_key> <api_url> <server_id>
            if (args.length != 3) {
                return false;
            }

            String api_key = args[0];
            String api_url = args[1];
            String server_id = args[2];

            if (api_key.isEmpty() || api_url.isEmpty() || server_id.isEmpty()) {
                return false;
            }

            VyHubConfiguration.setConfigValue("api_key", api_key);
            VyHubConfiguration.setConfigValue("api_url", api_url);
            VyHubConfiguration.setConfigValue("server_id", server_id);

            sender.sendMessage(ChatColor.GREEN + plugin.getI18n().get("Config Saved"));
            return true;
        }

        return false;
    }
}
