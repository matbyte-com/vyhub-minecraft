package net.vyhub.VyHubMinecraft.server;

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
    public static void createWarning(Player player, String reason) {
        String vyHubPlayerUUID = SvUser.getUser(player.getUniqueId().toString()).getId();

        String finalVyHubPlayerUUID = vyHubPlayerUUID;
        HashMap<String, Object> values = new HashMap<>() {{
            put("reason", reason);
            put("serverbundle_id", Utility.serverbundleID);
            put("user_id", finalVyHubPlayerUUID);
        }};

        HttpResponse<String> response = Utility.sendRequestBody("/warning/?morph_user_id=" + vyHubPlayerUUID, Types.POST, Utility.createRequestBody(values));

        if (response == null) {
            new BukkitRunnable() {
                public void run() {
                    createWarning(player, reason);
                }
            }.runTaskLater(VyHub.getPlugin(VyHub.class), 20L*60L);
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
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.getName().equals(args[0])) {
                    createWarning(p, args[1]);
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(),  String.format("msg %s You have received a warning", p.getName()));
                    SvBans.getVyHubBans();
                }
            }
            return false;
        }
        return true;
    }
}
