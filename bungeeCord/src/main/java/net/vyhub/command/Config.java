package net.vyhub.command;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;
import net.vyhub.VyHubPlugin;
import net.vyhub.config.VyHubConfiguration;

public class Config extends Command {
    private final VyHubPlugin plugin;

    public Config(final VyHubPlugin plugin) {
        super("vh_config");
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length != 2) {
            return;
        }

        String key = args[0];
        String value = args[1];

        if (key.isEmpty() || value.isEmpty()) {
            return;
        }

        VyHubConfiguration.setConfigValue(key, value);
        sender.sendMessage(new TextComponent(ChatColor.GREEN + plugin.getI18n().get("Config Saved")));

    }
}
