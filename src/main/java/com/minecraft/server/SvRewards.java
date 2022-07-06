package com.minecraft.server;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.minecraft.Entity.AppliedReward;
import com.minecraft.Entity.Reward;
import com.minecraft.Entity.VyHubPlayer;
import com.minecraft.Vyhub;
import com.minecraft.lib.Types;
import com.minecraft.lib.Utility;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerQuitEvent;
import org.json.simple.JSONObject;

import java.io.*;
import java.lang.reflect.Type;
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
    private static List<String> executedAndSentRewards;


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
        Type userRewardType = new TypeToken<Map<String, List<AppliedReward>>>() {}.getType();

        rewards = gson.fromJson(resp.body(), userRewardType);

    }

    public static void executeReward(List<String> events, String playerID) {
        Map<String, List<AppliedReward>> rewardsByPlayer = rewards;

        if (playerID == null) {
            for (String event : events) {
                if (!event.equals("DIRECT") && !event.equals("DISABLE")) {
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
                if (executedRewards.contains(appliedReward.getId()) || executedAndSentRewards.contains(appliedReward.getId())){
                    continue;
                }

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
        List<String> serverID = new ArrayList<>();
        serverID.add(Vyhub.checkConfig().get("serverId"));
        executedAndSentRewards = new ArrayList<>();
        HashMap<String, Object> values = new HashMap<>() {{
            put("executed_on", serverID);
        }};
        for (Iterator<String> it = executedRewards.iterator(); it.hasNext();) {
            String rewardID = it.next();
            HttpResponse<String> response = Utility.sendRequestBody("/packet/reward/applied/" + rewardID, Types.PATCH, Utility.createRequestBody(values));
            if (response.statusCode() == 200) {
                executedAndSentRewards.add(rewardID);
                it.remove();
                saveExecuted();
            }

        }
    }

    public static void loadExecuted() {
        File file = new File("plugins/Vyhub/rewardsQueue.txt");

        if (!file.isFile()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

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
