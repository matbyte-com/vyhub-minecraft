package net.vyhub.abstractClasses;

import net.vyhub.VyHubPlatform;
import net.vyhub.entity.Group;
import net.vyhub.entity.GroupMapping;
import net.vyhub.entity.Membership;
import net.vyhub.entity.VyHubUser;
import net.vyhub.lib.Utility;
import retrofit2.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.logging.Level.*;

public abstract class AGroups {
    private static List<Group> groups;
    private static Map<String, Group> mappedGroups;
    public static List<String> groupChangeBacklog = new ArrayList<>();

    private final VyHubPlatform platform;
    private final AUser aUser;

    public AGroups(VyHubPlatform platform, AUser aUser) {
        this.platform = platform;
        this.aUser = aUser;
    }

    public AUser getAUser() {
        return aUser;
    }

    public VyHubPlatform getPlatform() {
        return platform;
    }

    public void updateGroups() {
        Response<List<Group>> response = null;
        try {
            response = platform.getApiClient().getGroups().execute();
        } catch (IOException e) {
            platform.log(SEVERE, "Failed to fetch groups from VyHub API: " + e.getMessage());
        }

        if (response == null || response.code() != 200) {
            return;
        }

        groups = response.body();

        Map<String, Group> newMappedGroups = new HashMap<>();

        for (Group group : groups) {
            for (GroupMapping mapping : group.getMappings()) {
                if (mapping.getServerbundle_id() == null || mapping.getServerbundle_id().equals(AServer.serverbundleID)) {
                    newMappedGroups.put(mapping.getName(), group);
                }
            }
        }

        mappedGroups = new HashMap<>(newMappedGroups);
    }


    public void syncGroups(String playerId) {
        VyHubUser user = aUser.getUser(playerId);

        if (user == null) {
            return;
        }

        Response<List<Group>> response = null;

        try {
            response = platform.getApiClient().getUserGroups(user.getId(), AServer.serverbundleID).execute();
        } catch (IOException e) {
            platform.log(SEVERE, "Failed to fetch memberships from VyHub API: " + e.getMessage());
        }

        if (response == null || response.code() != 200) {
            return;
        }

        List<Group> vyhubGroups = response.body();
        List<String> allGroups = new ArrayList<>();

        (vyhubGroups).forEach(group -> {
            group.getMappings().forEach(mapping -> {
                allGroups.add(mapping.getName());
            });
            allGroups.add(group.getName());
        });

        // TODO Thats the old implementation, is replaced through the one above
        /*
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject jsonObject = (JSONObject) jsonArray.get(i);
            JSONArray mappings = (JSONArray) jsonObject.get("mappings");
            for (int j = 0; j < mappings.size(); j++) {
                JSONObject groupParameters = (JSONObject) mappings.get(j);
                String groupName = groupParameters.get("name").toString();
                allGroups.add(groupName);
            }
        }*/


        platform.executeAsync(() -> {
            addPlayerToLuckpermsGroup(allGroups, playerId);
        });
    }

    public abstract void addPlayerToLuckpermsGroup(List<String> allGroups, String playerId);


    public void addUserToVyHubGroup(VyHubUser user, String groupName) {
        Group group = mappedGroups.getOrDefault(groupName, null);

        if (group == null) {
            platform.log(WARNING, String.format("Could not find group mapping for %s.", groupName));
            return;
        }

        HashMap<String, Object> values = new HashMap<>() {{
            put("group_id", group.getId());
            put("serverbundle_id", AServer.serverbundleID);
        }};

        Response<Membership> response;
        try {
            response = platform.getApiClient().createMembership(user.getId(), Utility.createRequestBody(values)).execute();
        } catch (IOException e) {
            platform.log(SEVERE, "Could not add user to groups." + e.getMessage());
            return;
        }

        if (!response.isSuccessful()) {
            platform.log(WARNING, String.format("Could not add user to group %s.", groupName));
        }
        platform.log(INFO, String.format("Added VyHub group membership in group %s for player %s.", groupName, user.getUsername()));
    }

    public void removeUserFromVyHubGroup(VyHubUser user, String groupName) {
        Group group = mappedGroups.getOrDefault(groupName, null);

        if (group == null) {
            platform.log(WARNING, String.format("Could not find group mapping for %s.", groupName));
            return;
        }

        Response<Membership> response;
        try {
            response = platform.getApiClient().deleteMembership(user.getId(), group.getId(), AServer.serverbundleID).execute();
        } catch (IOException e) {
            platform.log(SEVERE, "Could not remove user from groups." + e.getMessage());
            return;
        }

        if (!response.isSuccessful()) {
            platform.log(WARNING, String.format("Could not remove group %s from user %s", groupName, user.getUsername()));
        }
        platform.log(INFO, String.format("Ended VyHub group membership in group %s for player %s.", groupName, user.getUsername()));
    }

    public void removeUserFromAllVyHubGroups(VyHubUser user) {
        Response<Membership> response;
        try {
            response = platform.getApiClient().deleteAllMemberships(user.getId(), AServer.serverbundleID).execute();
        } catch (IOException e) {
            platform.log(SEVERE, "Could not remove user from all VyHub groups." + e.getMessage());
            return;
        }

        platform.log(INFO, String.format("Ended all VyHub group memberships for player %s.", user.getUsername()));
    }

    public static String getBacklogKey(String playerID, String groupName, String operation) {
        return String.format("%s_%s_%s", playerID, operation.toUpperCase(), groupName);
    }
}
