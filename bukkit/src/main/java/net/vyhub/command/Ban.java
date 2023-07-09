package net.vyhub.command;

import net.vyhub.VyHubPlatform;
import net.vyhub.abstractClasses.AGroups;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.Calendar;
import java.util.Date;

public class Ban implements CommandExecutor {
    private final VyHubPlatform platform;
    private final AGroups aGroups;

    public Ban(final VyHubPlatform platform, final AGroups aGroups) {
        this.platform = platform;
        this.aGroups = aGroups;
    }

    // timeban <player> <time> <reason>
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

        if (sender.isOp() || (aGroups != null && aGroups.checkProperty(processor.getUniqueId().toString(), "ban_edit"))) {
            if (args.length == 0) {
                return false;
            }

            //args[0] = Player, args[1] = time, args[2] =reason
            Player p = Bukkit.getPlayer(args[0]);
            if (p != null) {
                long minutes;

                try {
                    minutes = Long.parseLong(args[1]);

                    if (minutes < 1) {
                        throw new NumberFormatException();
                    }
                } catch (NumberFormatException nfe) {
                    sender.sendMessage(platform.getI18n().get("invalidNumberOfMinutes"));
                    return false;
                }

                String reason = args.length == 3 ? args[2] : null;
                p.kickPlayer(String.format(platform.getI18n().get("youGotTimeBanned"), minutes, reason));
                Bukkit.getBanList(BanList.Type.NAME).addBan(p.getUniqueId().toString(), reason, new Date(Calendar.getInstance().getTimeInMillis() + (minutes * 60 * 1000)), sender.getName());
            } else {
                sender.sendMessage(platform.getI18n().get("playerMustBeOnline"));
            }
            return true;
        }
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', platform.getI18n().get("banNoPermissions")));
        return true;
    }
}
