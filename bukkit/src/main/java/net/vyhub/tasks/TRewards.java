package net.vyhub.tasks;

import net.vyhub.VyHubPlatform;
import net.vyhub.abstractClasses.ARewards;
import net.vyhub.abstractClasses.AUser;
import net.vyhub.entity.AppliedReward;
import net.vyhub.entity.Reward;
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

import static java.util.logging.Level.INFO;
import static java.util.logging.Level.WARNING;

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

    public synchronized void executeReward(List<String> events, String playerID) {
        if (getRewards() == null) {
            return;
        }

        Map<String, List<AppliedReward>> rewardsByPlayer = new HashMap<>(getRewards());

        if (playerID == null) {
            for (String event : events) {
                if (!event.equals("DIRECT") && !event.equals("DISABLE")) {
                    throw new RuntimeException();
                }
            }
        } else {
            rewardsByPlayer.clear();

            if (getRewards().containsKey(playerID)) {
                rewardsByPlayer.put(playerID, getRewards().get(playerID));
            } else {
                return;
            }
        }

        for (Map.Entry<String, List<AppliedReward>> entry : rewardsByPlayer.entrySet()) {
            String _playerID = entry.getKey();
            List<AppliedReward> appliedRewards = entry.getValue();

            Player player = Bukkit.getPlayer(UUID.fromString(_playerID));

            if (player == null) {
                continue;
            }

            for (AppliedReward appliedReward : appliedRewards) {
                if (getExecutedRewards().contains(appliedReward.getId()) || getExecutedAndSentRewards().contains(appliedReward.getId())) {
                    continue;
                }

                Reward reward = appliedReward.getReward();
                if (events.contains(reward.getOn_event())) {
                    Map<String, String> data = reward.getData();
                    boolean success = true;
                    if (reward.getType().equals("COMMAND")) {
                        String command = data.get("command");
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), stringReplace(command, player, appliedReward));
                    } else {
                        success = false;

                        getPlatform().log(WARNING, "No implementation for Reward Type: " + reward.getType());
                    }
                    if (reward.getOnce()) {
                        setExecuted(appliedReward.getId());
                    }
                    if (success) {
                        getPlatform().log(INFO, "RewardName: " + appliedReward.getReward().getName() + " Type: " +
                                appliedReward.getReward().getType() + " Player: " + player.getName() + " executed!");
                    }
                }
            }
        }

        getPlatform().executeAsync(this::sendExecuted);
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

    public String stringReplace(String command, Player player, AppliedReward appliedReward) {
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
}
