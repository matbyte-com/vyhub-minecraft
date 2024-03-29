package net.vyhub.tasks;

import net.vyhub.VyHubPlatform;
import net.vyhub.abstractClasses.AUser;
import net.vyhub.event.VyHubPlayerInitializedEvent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import static org.bukkit.Bukkit.getServer;

public class TUser extends AUser implements Listener {
    public TUser(VyHubPlatform platform) {
        super(platform);
    }

    public void callVyHubPlayerInitializedEvent(String playerId, String playerName) {
        Player player = getServer().getPlayer(playerName);
        getPlatform().callEvent(new VyHubPlayerInitializedEvent(player));
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        getPlatform().executeAsync(() -> {
            checkPlayerExists(player.getUniqueId().toString(), player.getName());
        });
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        getPlatform().executeAsync(() -> {
            onPlayerDisconnect(player.getUniqueId().toString());
        });
    }

    public void sendMessage(String playerName, String message) {
        Player player = getServer().getPlayer(playerName);

        if (player == null) {
            return;
        }
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }

}
