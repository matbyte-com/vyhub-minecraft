package net.vyhub.command;

import net.vyhub.VyHubPlatform;
import net.vyhub.abstractClasses.ABans;
import net.vyhub.abstractClasses.AUser;
import net.vyhub.abstractClasses.AWarning;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;


public class Warn extends AWarning implements CommandExecutor {
    public Warn(VyHubPlatform platform, ABans aBans, AUser aUser) {
        super(platform, aBans, aUser);
    }

    // warn <player> <reason>
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player processor;

        if (sender instanceof ConsoleCommandSender) {
            processor = null;
        } else if (sender instanceof Player) {
            processor = (Player) sender;
        } else {
            processor = null;
        }

        if (args.length == 0) {
            return false;
        }

        //args[0] = Player, args[1] =reason
        Player p = Bukkit.getPlayer(args[0]);

        if (p != null) {
            getPlatform().executeAsync(() -> {
                String reason = args.length == 2 ? args[1] : null;
                String processorId = processor!= null ? processor.getUniqueId().toString() : null;
                String processorName = processor!= null ? processor.getName() : null;
                createWarning(p.getUniqueId().toString(), p.getName(), reason, processorId, processorName);
            });

            return true;
        }

        sender.sendMessage(ChatColor.DARK_RED + getPlatform().getI18n().get("playerMustBeOnline"));
        return false;
    }
}
