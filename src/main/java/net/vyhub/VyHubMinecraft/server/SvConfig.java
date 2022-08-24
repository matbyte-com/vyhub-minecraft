package net.vyhub.VyHubMinecraft.server;

import net.vyhub.VyHubMinecraft.VyHub;
import net.vyhub.VyHubMinecraft.lib.Utility;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.logging.Logger;


public class SvConfig implements CommandExecutor {
    private static Logger logger = Bukkit.getServer().getLogger();

    public void setConfigValue(String key, String value) {
        VyHub.config.put(key, value);
        logger.info(String.format("Set config value %s -> %s.", key, value));
        VyHub.configCache.save(VyHub.config);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.isOp()) {
            sender.sendMessage("§4This command is only for admins.");
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

            setConfigValue(key, value);
            return true;
        }

        return false;
    }
}
