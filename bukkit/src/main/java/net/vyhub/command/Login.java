package net.vyhub.command;

import net.vyhub.VyHubPlugin;
import net.vyhub.lib.Utility;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import retrofit2.Response;

import java.io.IOException;
import java.util.HashMap;

public class Login implements CommandExecutor {
    private final VyHubPlugin plugin;

    public Login(final VyHubPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof ConsoleCommandSender) {
            return false;
        }

        if (args.length == 0 || args[0].length() != 36) {
            return false;
        }

        Player player = (Player) sender;

        plugin.getPlatform().executeAsync(() -> {
            HashMap<String, Object> values = new HashMap<>() {{
                put("user_type", "MINECRAFT");
                put("identifier", player.getUniqueId());
            }};

            Response<Object> response;
            try {
                response = plugin.getApiClient().confirmAuth(args[0], Utility.createRequestBody(values)).execute();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            if (response.code() == 400) {
                sender.sendMessage(ChatColor.DARK_RED + plugin.getI18n().get("invalidLoginUUID"));
            } else if (response.code() != 200) {
                sender.sendMessage(ChatColor.DARK_RED + plugin.getI18n().get("apiNotAvailable"));
            } else {
                sender.sendMessage(ChatColor.GREEN + plugin.getI18n().get("loginSuccess"));
            }
        });

        return true;
    }
}
