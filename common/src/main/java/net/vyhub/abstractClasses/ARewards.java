package net.vyhub.abstractClasses;

import com.google.gson.reflect.TypeToken;
import net.vyhub.VyHubPlatform;
import net.vyhub.config.VyHubConfiguration;
import net.vyhub.entity.AppliedReward;
import net.vyhub.entity.VyHubUser;
import net.vyhub.lib.Cache;
import net.vyhub.lib.Utility;
import retrofit2.Response;

import java.io.IOException;
import java.util.*;

import static java.util.logging.Level.SEVERE;

public abstract class ARewards {
    private static Map<String, List<AppliedReward>> rewards;
    private static List<String> executedRewards = new ArrayList<>();
    private static List<String> executedAndSentRewards = new ArrayList<>();

    private final VyHubPlatform platform;
    private final AUser aUser;

    private static Cache<List<String>> rewardCache = new Cache<>(
            "executed_rewards",
            new TypeToken<ArrayList<String>>() {
            }.getType()
    );

    protected ARewards(VyHubPlatform platform, AUser aUser) {
        this.platform = platform;
        this.aUser = aUser;
    }

    public AUser getAUser() {
        return aUser;
    }

    public VyHubPlatform getPlatform() {
        return platform;
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

    public void getRewardsFromApi() {
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
        }
    }

    public abstract void getPlayerReward(Object object);

    public abstract void executeReward(List<String> events, String playerID);

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
        HashMap<String, Object> values = new HashMap<>() {{
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

            if (response != null && response.isSuccessful()) {
                newExecutedAndSentRewards.add(rewardID);
                it.remove();
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


}
