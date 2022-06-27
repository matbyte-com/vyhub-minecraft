package com.minecraft.server;

import com.minecraft.Entity.VyHubPlayer;
import com.minecraft.lib.Types;
import com.minecraft.lib.Utility;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class SvWarning implements CommandExecutor {

    public static void createWarning(Player player, String reason) {
        String vyHubPlayerUUID = "";
        for (VyHubPlayer vyHubPlayer : SvUser.vyHubPlayers) {
            if (vyHubPlayer.getIdentifier().equals(player.getUniqueId().toString())) {
                vyHubPlayerUUID = vyHubPlayer.getId();
            }
        }

        String finalVyHubPlayerUUID = vyHubPlayerUUID;
        HashMap<String, Object> values = new HashMap<>() {{
            put("reason", reason);
            put("serverbundle_id", Utility.serverbundleID);
            put("user_id", finalVyHubPlayerUUID);
        }};

        Utility.sendRequestBody("/warning/?morph_user_id=" + vyHubPlayerUUID, Types.POST, Utility.createRequestBody(values));
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
                }
            }
            return false;
        }
        return true;
    }
}
