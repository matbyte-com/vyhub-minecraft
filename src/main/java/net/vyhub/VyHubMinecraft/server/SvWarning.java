package net.vyhub.VyHubMinecraft.server;

import net.vyhub.VyHubMinecraft.Entity.VyHubUser;
import net.vyhub.VyHubMinecraft.VyHub;
import net.vyhub.VyHubMinecraft.lib.Types;
import net.vyhub.VyHubMinecraft.lib.Utility;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.net.http.HttpResponse;
import java.util.HashMap;

public class SvWarning implements CommandExecutor {
    public static void createWarning(Player player, String reason, Player adminPlayer) {
        VyHubUser vyHubAdminUser = SvUser.getUser(adminPlayer.getUniqueId().toString());
        VyHubUser vyHubUser = SvUser.getUser(player.getUniqueId().toString());

        if (vyHubAdminUser == null || vyHubUser == null) {
            adminPlayer.sendMessage("§c[WARN] §9Error while warning player. Please try again later.");
        }

        String finalVyHubPlayerUUID = vyHubUser.getId();
        HashMap<String, Object> values = new HashMap<>() {{
            put("reason", reason);
            put("serverbundle_id", SvServer.serverbundleID);
            put("user_id", finalVyHubPlayerUUID);
        }};

        HttpResponse<String> response = Utility.sendRequestBody("/warning/?morph_user_id=" + vyHubAdminUser.getId(), Types.POST, Utility.createRequestBody(values));

        if (response == null || response.statusCode() != 200) {
            adminPlayer.sendMessage("§c[WARN] §9Error while warning player. Please try again later.");
        } else {
            player.sendMessage(String.format("§c[WARN] §9You have received a warning:§6 %s", reason));
            player.sendMessage("§c[WARN] §9Too many warnings will result in a ban.");
            adminPlayer.sendMessage(String.format("§c[WARN] §9Warned user %s:§6 %s", player.getName(), reason));
        }

    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = (Player) sender;
        if (player.isOp()) {
            if (args.length == 0) {
                Utility.sendUsage(sender, "/warn <Player> <reason>");
                return true;
            }

            //args[0] = Player, args[1] =reason
            Player p = Bukkit.getPlayer(args[0]);

            if (p != null) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        createWarning(p, args[1], player);
                        SvBans.syncBans();
                    }
                }.runTaskAsynchronously(VyHub.plugin);
            }

            return false;
        }
        return true;
    }
}
