package net.vyhub.tasks;

import net.vyhub.VyHubPlatform;
import net.vyhub.abstractClasses.AStatistics;
import net.vyhub.abstractClasses.AUser;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import java.util.HashMap;

public class TStatistics extends AStatistics {
    public TStatistics(VyHubPlatform platform, AUser aUser) {
        super(platform, aUser);
    }

    public void collectPlayerTime() {
        playerTime = super.getStatisticCache().load();
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

        super.getStatisticCache().save(playerTime);
    }
}
