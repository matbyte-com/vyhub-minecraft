package net.vyhub.command;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.vyhub.VyHubPlugin;
import net.vyhub.config.VyHubConfiguration;

public class ConfigCommand implements SimpleCommand {
    private final VyHubPlugin plugin;

    public ConfigCommand(final VyHubPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(final Invocation invocation) {
        CommandSource sender = invocation.source();
        String[] args = invocation.arguments();

        if (args.length != 2) {
            return;
        }

        String key = args[0];
        String value = args[1];

        if (key.isEmpty() || value.isEmpty()) {
            return;
        }

        VyHubConfiguration.setConfigValue(key, value);
        sender.sendMessage(Component.text(plugin.getI18n().get("Config Saved")).color(NamedTextColor.GREEN));
    }


    @Override
    public boolean hasPermission(final Invocation invocation) {
        return invocation.source().hasPermission("vyhub.config");
    }

}
