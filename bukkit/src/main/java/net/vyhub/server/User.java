package net.vyhub.server;

import net.vyhub.Entity.VyHubUser;
import net.vyhub.VyHub;
import net.vyhub.event.VyHubPlayerInitializedEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.logging.Logger;

public class User implements Listener {
    private static Logger logger = Bukkit.getServer().getLogger();

    @EventHandler
    public static void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        new BukkitRunnable() {
            @Override
            public void run() {
                checkPlayerExists(player);
            }
        }.runTaskAsynchronously(VyHub.plugin);
    }

    public static void checkPlayerExists(Player player) {
        VyHubUser user = SvUser.getUser(player.getUniqueId().toString());

        if (user == null) {
            logger.warning(String.format("Could not register player %s, trying again in a minute..", player.getName()));

            new BukkitRunnable() {
                @Override
                public void run() {
                    checkPlayerExists(player);
                }
            }.runTaskLaterAsynchronously(VyHub.plugin, 20L * 60L);
        } else {
            new BukkitRunnable() {
                @Override
                public void run() {
                    Bukkit.getPluginManager().callEvent(new VyHubPlayerInitializedEvent(player));
                }
            }.runTask(VyHub.plugin);
        }
    }

}
