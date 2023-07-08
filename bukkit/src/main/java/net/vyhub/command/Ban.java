package net.vyhub.command;

import net.vyhub.VyHubPlatform;
import net.vyhub.abstractClasses.ABans;
import net.vyhub.abstractClasses.AGroups;
import net.vyhub.abstractClasses.AUser;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

public class Ban extends ABans implements CommandExecutor {
    public Ban(VyHubPlatform platform, AUser aUser, AGroups aGroups) {
        super(platform, aUser, aGroups);
    }

    @Override
    public boolean addMinecraftBan(String playerID, net.vyhub.entity.Ban vyhubBan) {
        Date endDate = null;
        if (vyhubBan.getEnds_on() != null) {
            ZonedDateTime expiresDate = ZonedDateTime.parse(vyhubBan.getEnds_on(), isoDateFormatter);
            endDate = Date.from(expiresDate.toInstant());
        }

        Bukkit.getBanList(BanList.Type.NAME).addBan(playerID, vyhubBan.getReason(), endDate, "VyHub");

        Player bannnedPlayer = Bukkit.getPlayer(UUID.fromString(playerID));
        if (bannnedPlayer != null) {

            getPlatform().executeBlocking(() -> {
                bannnedPlayer.kickPlayer(String.format(getPlatform().getI18n().get("youGotBanned"), vyhubBan.getReason()));
            });
        }

        return true;
    }

    @Override
    public boolean unbanMinecraftBan(String playerID) {
        Bukkit.getBanList(BanList.Type.NAME).pardon(playerID);
        return true;
    }

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

        if (sender.isOp() || getAGroups().checkProperty(processor.getUniqueId().toString(), "ban_edit")) {
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
                    sender.sendMessage(getPlatform().getI18n().get("invalidNumberOfMinutes"));
                    return false;
                }

                p.kickPlayer(String.format(getPlatform().getI18n().get("youGotTimeBanned"), minutes, args[2]));
                Bukkit.getBanList(BanList.Type.NAME).addBan(p.getUniqueId().toString(), args[2], new Date(Calendar.getInstance().getTimeInMillis() + (minutes * 60 * 1000)), sender.getName());
            } else {
                sender.sendMessage(getPlatform().getI18n().get("playerMustBeOnline"));
            }
            return true;
        }
        sender.sendMessage(getPlatform().getI18n().get("noPermission"));
        return true;
    }
}
