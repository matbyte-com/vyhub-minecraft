package net.vyhub.command;

import net.vyhub.Entity.VyHubUser;
import net.vyhub.VyHub;
import net.vyhub.server.SvUser;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.net.http.HttpResponse;

import static net.vyhub.server.SvWarning.createWarningRequest;


public class Warn implements CommandExecutor {
    public static void createWarning(Player player, String reason, Player adminPlayer) {
        VyHubUser vyHubUser = SvUser.getUser(player.getUniqueId().toString());

        if (adminPlayer != null && vyHubUser == null) {
            adminPlayer.sendMessage("§c[WARN] §9Error while warning player. Please try again later.");
        }

        HttpResponse<String> response = createWarningRequest(
                player.getUniqueId().toString(),
                reason,
                adminPlayer.getUniqueId().toString(),
                vyHubUser);

        if (response == null || response.statusCode() != 200) {
            if (adminPlayer != null) {
                if (response.statusCode() == 403) {
                    adminPlayer.sendMessage("§c[WARN] §9You are not permitted to warn players.");
                } else {
                    adminPlayer.sendMessage(String.format("§c[WARN] §9Error %s while warning player. Please try again later.",
                            response.statusCode()));
                }
            }
        } else {
            player.sendMessage(String.format("§c[WARN] §9You have received a warning:§6 %s", reason));
            player.sendMessage("§c[WARN] §9Too many warnings will result in a ban.");

            String adminMsg = String.format("§c[WARN] §9Warned user %s:§6 %s", player.getName(), reason);

            VyHub.logger.info(adminMsg);
            if (adminPlayer != null) {
                adminPlayer.sendMessage(adminMsg);
            }
        }
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
            new BukkitRunnable() {
                @Override
                public void run() {
                    createWarning(p, args[1], finalPlayer);
                    SvBans.syncBans();
                }
            }.runTaskAsynchronously(VyHub.plugin);
            return true;
        }

        return false;
    }
}
