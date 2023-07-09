package net.vyhub.tasks;

import net.vyhub.VyHubPlatform;
import net.vyhub.abstractClasses.ARewards;
import net.vyhub.abstractClasses.AUser;
import net.vyhub.entity.AppliedReward;
import net.vyhub.event.VyHubPlayerInitializedEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class TRewards extends ARewards implements Listener {

    public TRewards(VyHubPlatform platform, AUser aUser) {
        super(platform, aUser);
    }

    @EventHandler
    public void onPlayerInit(VyHubPlayerInitializedEvent event) {
        Player player = event.getPlayer();
        getPlatform().executeAsync(() -> {
            getPlayerReward(player);

            List<String> eventList = new LinkedList<>();
            eventList.add("CONNECT");
            eventList.add("SPAWN");

            getPlatform().executeBlocking(() -> {
                executeReward(eventList, player.getUniqueId().toString());
            });
        });
    }

    @Override
    public void getPlayerReward(Object object) {

    }



    @EventHandler
    public void onSpawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();

        List<String> eventList = new LinkedList<>();
        eventList.add("SPAWN");

        getPlatform().executeBlockingLater(() -> {
            executeReward(eventList, player.getUniqueId().toString());
        }, 500, TimeUnit.MILLISECONDS);
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        List<String> eventList = new LinkedList<>();
        eventList.add("DEATH");

        executeReward(eventList, player.getUniqueId().toString());
    }

    @EventHandler
    public void onDisconnect(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        List<String> eventList = new LinkedList<>();
        eventList.add("DISCONNECT");

        executeReward(eventList, player.getUniqueId().toString());
    }

    public String stringReplace(String command, Object playerObject, AppliedReward appliedReward) {
        if (!(playerObject instanceof Player)) {
            getPlatform().log(Level.SEVERE, "Player object is not a player instance.");
            return null;
        }
        Player player = (Player) playerObject;
        String newString = command;
        newString = newString.replace("%nick%", player.getName());
        newString = newString.replace("%user_id%", getAUser().getUser(player.getUniqueId().toString()).getId());
        newString = newString.replace("%applied_packet_id%", appliedReward.getApplied_packet_id());
        newString = newString.replace("%player_id%", player.getUniqueId().toString());
        newString = newString.replace("%player_ip_address%", player.getAddress().getAddress().toString().replace("/", ""));

        String purchaseAmount = "-";

        if (appliedReward.getApplied_packet().getPurchase() != null) {
            purchaseAmount = appliedReward.getApplied_packet().getPurchase().getAmount_text();
        }

        newString = newString.replace("%purchase_amount%", purchaseAmount);
        newString = newString.replace("%packet_title%", appliedReward.getApplied_packet().getPacket().getTitle());

        return newString;
    }

    public List<String> getOnlinePlayerIds() {
        List<String> playerIds = new LinkedList<>();

        for (Player player : Bukkit.getOnlinePlayers()) {
            playerIds.add(player.getUniqueId().toString());
        }

        return playerIds;
    }

    public void dispatchCommand(String command) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
    }

    public Object getPlayer(String playerId) {
        return Bukkit.getPlayer(UUID.fromString(playerId));
    }

    @Override
    public String getPlayerName(Object playerObject) {
        if (!(playerObject instanceof Player)) {
            getPlatform().log(Level.SEVERE, "Player object is not a player instance.");
            return null;
        }
        Player player = (Player) playerObject;
        return player.getName();
    }
}
