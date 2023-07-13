package net.vyhub.command;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.vyhub.VyHubPlugin;
import net.vyhub.config.VyHubConfiguration;

public class SetupCommand implements SimpleCommand {
    private final VyHubPlugin plugin;

    public SetupCommand(final VyHubPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(final SimpleCommand.Invocation invocation) {
        CommandSource sender = invocation.source();
        String[] args = invocation.arguments();

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

        sender.sendMessage(Component.text(plugin.getI18n().get("Config Saved")).color(NamedTextColor.GREEN));
    }


    @Override
    public boolean hasPermission(final SimpleCommand.Invocation invocation) {
        return invocation.source().hasPermission("vyhub.config");
    }

}
