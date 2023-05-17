package net.vyhub.server;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;

import static net.vyhub.server.SvStatistics.playerTime;
import static net.vyhub.server.SvStatistics.statisticCache;

public class Statistics {
    public static void playerTime() {
        playerTime = statisticCache.load();
        if (playerTime == null) {
            playerTime = new HashMap<>();
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (playerTime.containsKey(player.getUniqueId().toString())) {
                double oldTime = playerTime.get(player.getUniqueId().toString());
                playerTime.replace(player.getUniqueId().toString(), oldTime + 1);
            } else {
                playerTime.put(player.getUniqueId().toString(), 1.0);
            }
        }

        statisticCache.save(playerTime);
    }

}
