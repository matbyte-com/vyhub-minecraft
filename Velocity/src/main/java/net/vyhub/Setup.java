package net.vyhub;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;
import net.vyhub.config.VyHubConfiguration;

public class Setup extends Command {
    private final VyHubPlugin plugin;

    public Setup(final VyHubPlugin plugin) {
        super("vh_setup");
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender commandSender, String[] args) {
        // Fast setup /vh_setup <api_key> <api_url> <server_id>
        if (args.length != 3) {
            return;
        }

        String api_key = args[0];
        String api_url = args[1];
        String server_id = args[2];

        if (api_key.isEmpty() || api_url.isEmpty() || server_id.isEmpty()) {
            return;
        }

        VyHubConfiguration.setConfigValue("api_key", api_key);
        VyHubConfiguration.setConfigValue("api_url", api_url);
        VyHubConfiguration.setConfigValue("server_id", server_id);

        commandSender.sendMessage(new TextComponent(ChatColor.GREEN + plugin.getI18n().get("Config Saved")));
    }
}
