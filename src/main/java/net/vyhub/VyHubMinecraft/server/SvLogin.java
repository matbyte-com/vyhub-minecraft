package net.vyhub.VyHubMinecraft.server;

import net.vyhub.VyHubMinecraft.VyHub;
import net.vyhub.VyHubMinecraft.lib.Types;
import net.vyhub.VyHubMinecraft.lib.Utility;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.net.http.HttpResponse;
import java.util.HashMap;

public class SvLogin implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0 || args[0].length() != 36) {
            Utility.sendUsage(sender, "/login <UUID>");
            return true;
        }

        Player player = (Player) sender;

        new BukkitRunnable() {
            @Override
            public void run() {
                HashMap<String, Object> values = new HashMap<>() {{
                    put("user_type", "MINECRAFT");
                    put("identifier", player.getUniqueId());
                }};

                HttpResponse<String> response = Utility.sendRequestBody("/auth/request/" + args[0], Types.PATCH, Utility.createRequestBody(values));

                if (response != null && response.statusCode() != 200) {
                    Utility.sendUsage(sender, "/login <UUID>");
                } else if (response == null) {
                    sender.sendMessage("§aVyHub API is not available. Try it again later!");
                } else {
                    sender.sendMessage("§aSuccessfully logged in!");
                }
            }
        }.runTaskAsynchronously(VyHub.plugin);

        return true;
    }
}
