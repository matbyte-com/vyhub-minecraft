package net.vyhub.command;

import net.vyhub.VyHubPlatform;
import net.vyhub.abstractClasses.ABans;
import net.vyhub.abstractClasses.AUser;
import net.vyhub.abstractClasses.AWarning;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;


public class Warn extends AWarning implements CommandExecutor {
    public Warn(VyHubPlatform platform, ABans aBans, AUser aUser) {
        super(platform, aBans, aUser);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = null;

        if (sender instanceof ConsoleCommandSender) {

        } else if (sender instanceof Player) {
            player = (Player) sender;
        } else {
            return false;
        }

        if (args.length == 0) {
            return false;
        }

        //args[0] = Player, args[1] =reason
        Player p = Bukkit.getPlayer(args[0]);

        if (p != null) {
            Player finalPlayer = player;

            getPlatform().executeAsync(() -> {
                createWarning(p.getUniqueId().toString(), p.getName(), args[1], finalPlayer.getUniqueId().toString(), finalPlayer.getName());
                aBans.syncBans();
            });

            return true;
        }

        return false;
    }
}
