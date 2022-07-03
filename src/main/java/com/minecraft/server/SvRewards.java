package com.minecraft.server;

import com.google.gson.Gson;
import com.minecraft.Entity.AppliedReward;
import com.minecraft.Entity.Reward;
import com.minecraft.Entity.VyHubPlayer;
import com.minecraft.Vyhub;
import com.minecraft.lib.Types;
import com.minecraft.lib.Utility;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.json.simple.JSONObject;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SvRewards {

    private static Map<String, List<AppliedReward>> rewards;
    private static List<String> executedRewards;


    public static void getRewards() {
        HashMap<String, Object> values = new HashMap<>() {{
            put("active", true);
            put("foreign_ids", true);
            put("status", "OPEN");
            put("serverbundle_id", Utility.serverbundleID);
            put("for_server_id", Vyhub.checkConfig().get("serverId"));
        }};

        StringBuilder stringBuilder = new StringBuilder();

        for (Player player : Bukkit.getOnlinePlayers()) {
            String userID = SvUser.getUser(player.getUniqueId().toString()).getId();
            stringBuilder.append("user_id=").append(userID).append("&");
        }
        stringBuilder.deleteCharAt(stringBuilder.length() - 1);

        HttpResponse<String> resp = Utility.sendRequestBody("/packet/reward/applied/user?" + stringBuilder, Types.GET, Utility.createRequestBody(values));

        Gson gson = new Gson();
        rewards = gson.fromJson(resp.body(), HashMap.class);
    }

    public static void executeReward(List<String> events, String playerID) {
        Map<String, List<AppliedReward>> rewardsByPlayer = rewards;

        if (playerID == null) {
            for (String event : events) {
                if (!event.equals("DIRECT") || !event.equals("DISABLE")) {
                    throw new RuntimeException();
                }
            }
        } else {
            rewardsByPlayer.clear();
            if (rewards.containsKey(playerID)) {
                rewardsByPlayer.put(playerID, rewards.get(playerID));
            } else {
                return;
            }
        }

        for (Map.Entry<String, List<AppliedReward>> entry : rewardsByPlayer.entrySet()) {
            String _playerID = entry.getKey();
            List<AppliedReward> appliedRewards = entry.getValue();

            Player player = null;
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.getUniqueId().toString().equals(_playerID)) {
                    player = p;
                    break;
                }
            }

            for (AppliedReward appliedReward : appliedRewards) {
                Reward reward = appliedReward.getReward();
                if (events.contains(reward.getOn_event())) {
                    Map<String, String> data = reward.getData();
                    if (reward.getType().equals("COMMAND")) {
                        String command = data.get("command");
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
                    } else {
                        System.out.println("No implementation for Reward Type: " + reward.getType());
                    }
                    if (reward.getOnce()) {
                        setExecuted(appliedReward.getId());
                    }
                }
            }
        }
        sendExecuted();
    }

    public static void setExecuted(String id) {
        executedRewards.add(id);
        saveExecuted();
    }

    public static void saveExecuted() {
        try (FileWriter fileWr = new FileWriter("plugins/Vyhub/rewardsQueue.txt")) {
            fileWr.write(executedRewards.stream().collect(Collectors.joining("\n")));
            fileWr.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void sendExecuted() {
        List<String> serverID = new LinkedList<>();
        serverID.add(Vyhub.checkConfig().get("serverId"));
        HashMap<String, Object> values = new HashMap<>() {{
            put("executed_on", serverID);
        }};
        for (String rewardID : executedRewards) {
            HttpResponse<String> response = Utility.sendRequestBody("/packet/reward/applied/" + rewardID, Types.PATCH, Utility.createRequestBody(values));

            if (response.statusCode() == 200) {
                executedRewards.remove(rewardID);
                saveExecuted();
            }
        }
    }

    public static void loadExecuted() {
       try (Stream<String> lines = Files.lines(Paths.get("plugins/Vyhub/rewardsQueue.txt"))) {
           executedRewards = lines.collect(Collectors.toList());
       } catch (IOException e) {
           throw new RuntimeException(e);
       }

    }

    public static void runDirectRewards() {
        List<String> eventList = new LinkedList<>();
        eventList.add("DIRECT");
        eventList.add("DISABLE");

        executeReward(eventList, null);
    }
}
