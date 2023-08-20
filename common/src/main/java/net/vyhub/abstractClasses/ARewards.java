package net.vyhub.abstractClasses;

import com.google.gson.reflect.TypeToken;
import net.vyhub.VyHubPlatform;
import net.vyhub.config.VyHubConfiguration;
import net.vyhub.entity.AppliedReward;
import net.vyhub.entity.Reward;
import net.vyhub.entity.VyHubUser;
import net.vyhub.lib.Cache;
import net.vyhub.lib.Utility;
import retrofit2.Response;

import java.io.IOException;
import java.util.*;

import static java.util.logging.Level.*;

public abstract class ARewards extends VyHubAbstractBase {
    private static Map<String, List<AppliedReward>> rewards;
    private static List<String> executedRewards = new ArrayList<>();
    private static List<String> executedAndSentRewards = new ArrayList<>();
    private final AUser aUser;
    private static Cache<List<String>> rewardCache = new Cache<>(
            "executed_rewards",
            new TypeToken<ArrayList<String>>() {
            }.getType()
    );

    protected ARewards(VyHubPlatform platform, AUser aUser) {
        super(platform);
        this.aUser = aUser;
    }

    public AUser getAUser() {
        return aUser;
    }

    public Map<String, List<AppliedReward>> getRewards() {
        return rewards;
    }

    public List<String> getExecutedRewards() {
        return executedRewards;
    }

    public List<String> getExecutedAndSentRewards() {
        return executedAndSentRewards;
    }

    public abstract List<String> getOnlinePlayerIds();

    public void fetchRewards() {
        StringBuilder stringBuilder = new StringBuilder();

        for (String playerId : getOnlinePlayerIds()) {
            VyHubUser user = aUser.getUser(playerId);

            if (user != null) {
                stringBuilder.append("user_id=").append(user.getId()).append("&");
            }
        }

        if (stringBuilder.toString().length() != 0) {
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        }

        Response<Map<String, List<AppliedReward>>> response = null;

        try {
            response = platform.getApiClient().getRewards(AServer.serverbundleID, VyHubConfiguration.getServerId(), stringBuilder.toString()).execute();
        } catch (IOException e) {
            platform.log(SEVERE, "Failed to get rewards from API." + e.getMessage());
        }

        if (response != null && response.isSuccessful()) {
            rewards = response.body();
            executedRewards = new ArrayList<>();
        }
    }

    public abstract void getPlayerReward(Object object);

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

            Object player = null;
            try {
                player = getPlayer(_playerID);
            } catch (IllegalArgumentException e) {
                platform.log(WARNING, "Error while executing rewards: PlayerID: " + _playerID + " is not a valid UUID");
                continue;
            }

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
                        dispatchCommand(stringReplace(command, player, appliedReward));
                    } else {
                        success = false;

                        platform.log(WARNING, "No implementation for Reward Type: " + reward.getType());
                    }
                    if (reward.getOnce()) {
                        setExecuted(appliedReward.getId());
                    }
                    if (success) {
                        platform.log(INFO, "RewardName: " + appliedReward.getReward().getName() + " Type: " +
                                appliedReward.getReward().getType() + " Player: " + getPlayerName(player) + " executed!");
                    }
                }
            }
        }

        platform.executeAsync(this::sendExecuted);
    }


    public static synchronized void setExecuted(String id) {
        executedRewards.add(id);
        saveExecuted();
    }

    public static synchronized void saveExecuted() {
        rewardCache.save(executedRewards);
    }

    public synchronized void sendExecuted() {
        List<String> serverID = new ArrayList<>();
        serverID.add(VyHubConfiguration.getServerId());

        List<String> newExecutedAndSentRewards = new ArrayList<>();
        HashMap<String, Object> values = new HashMap<String, Object>() {{
            put("executed_on", serverID);
        }};

        for (Iterator<String> it = executedRewards.iterator(); it.hasNext(); ) {
            String rewardID = it.next();

            Response<AppliedReward> response;
            try {
                response = platform.getApiClient().sendExecutedRewards(rewardID, Utility.createRequestBody(values)).execute();
            } catch (IOException e) {
                e.printStackTrace();
                continue;
            }

            if (response.isSuccessful()) {
                newExecutedAndSentRewards.add(rewardID);
                saveExecuted();
            }
        }

        executedAndSentRewards = newExecutedAndSentRewards;
    }

    public static synchronized void loadExecuted() {
        executedRewards = rewardCache.load();

        if (executedRewards == null) {
            executedRewards = new ArrayList<>();
        }
    }

    public void runDirectRewards() {
        List<String> eventList = new LinkedList<>();
        eventList.add("DIRECT");
        eventList.add("DISABLE");

        executeReward(eventList, null);
    }

    public abstract void dispatchCommand(String command);

    public abstract String stringReplace(String command, Object player, AppliedReward appliedReward);

    public abstract Object getPlayer(String playerId);

    public abstract String getPlayerName(Object player);
}
