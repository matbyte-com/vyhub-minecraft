package net.vyhub.command;

import net.vyhub.lib.Utility;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.logging.Logger;

public class Config implements CommandExecutor {
    private static Logger logger = Bukkit.getServer().getLogger();

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

            Utility.setConfigValue(key, value);
            return true;
        }

        return false;
    }
}
