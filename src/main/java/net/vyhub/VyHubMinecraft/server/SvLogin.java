package net.vyhub.VyHubMinecraft.server;

import net.vyhub.VyHubMinecraft.lib.Types;
import net.vyhub.VyHubMinecraft.lib.Utility;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.net.http.HttpResponse;
import java.util.HashMap;

public class SvLogin implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            Utility.sendUsage(sender, "/login <UUID>");
            return true;
        }

        Player player = (Player) sender;

        HashMap<String, Object> values = new HashMap<>(){{
            put("user_type", "MINECRAFT");
            put("identifier", player.getUniqueId());
        }};

        HttpResponse<String> response =  Utility.sendRequestBody("/auth/request/"+ args[0], Types.PATCH, Utility.createRequestBody(values));

        if (response.statusCode() != 200) {
            Utility.sendUsage(sender, "/login <UUID>");
            return true;
        } else if (response == null) {
            sender.sendMessage("§aVyHub API is not available. Try it again later!");
            return  false;
        }
        sender.sendMessage("§aSuccessfully logged in!");
        return false;
    }
}
