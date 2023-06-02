package net.vyhub.tasks;

import net.vyhub.VyHubPlatform;
import net.vyhub.abstractClasses.AServer;
import net.vyhub.abstractClasses.AStatistics;
import net.vyhub.abstractClasses.AUser;
import net.vyhub.entity.VyHubUser;
import net.vyhub.lib.Utility;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class TStatistics extends AStatistics {
    public TStatistics(VyHubPlatform platform, AUser aUser) {
        super(platform, aUser);
    }

    public void sendPlayerTime() {
        String definitionID = checkDefinition();

        if (definitionID != null) {
            for (Map.Entry<String, Double> entry : playerTime.entrySet()) {
                String playerID = entry.getKey();
                VyHubUser user = getAUser().getUser(playerID);

                if (user != null) {
                    double hours = Math.round((entry.getValue() / 60) * 100.0) / 100.0;

                    if (hours < 0.1) {
                        continue;
                    }

                    HashMap<String, Object> values = new HashMap<>() {{
                        put("definition_id", definitionID);
                        put("user_id", user.getId());
                        put("serverbundle_id", AServer.serverbundleID);
                        put("value", hours);
                    }};

                    try {
                        super.getPlatform().getApiClient().sendPlayerTime(Utility.createRequestBody(values)).execute();
                    } catch (IOException e) {
                        super.getPlatform().log(Level.SEVERE, "Failed to send player time to VyHub API" + e.getMessage());
                    }
                    resetPlayerTime(playerID);
                }
            }
        }

        statisticCache.save(playerTime);
    }

    public void playerTime() {
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
