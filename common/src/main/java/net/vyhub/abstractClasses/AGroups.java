package net.vyhub.abstractClasses;

import net.vyhub.VyHubPlatform;
import net.vyhub.entity.*;
import net.vyhub.lib.Utility;
import retrofit2.Response;

import java.io.IOException;
import java.util.*;

import static java.util.logging.Level.*;
import static net.vyhub.lib.Utility.checkResponse;

public abstract class AGroups extends VyHubAbstractBase {
    private static List<Group> groups;
    private static Map<String, Group> mappedGroups;
    public static List<String> groupChangeBacklog = new ArrayList<>();
    private final AUser aUser;

    public AGroups(VyHubPlatform platform, AUser aUser) {
        super(platform);
        this.aUser = aUser;
    }

    public AUser getAUser() {
        return aUser;
    }

    public Map<String, Group> getMappedGroups() {
        return mappedGroups;
    }

    public void updateGroups() {
        Response<List<Group>> response = null;
        try {
            response = platform.getApiClient().getGroups().execute();
        } catch (IOException e) {
            platform.log(SEVERE, "Failed to fetch groups from VyHub API: " + e.getMessage());
            return;
        }

        if (!checkResponse(platform, response, "Fetch Groups")) {
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

    public abstract void syncGroupsForAll();

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

        if (!checkResponse(platform, response, "Fetch Memberships")) {
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

        platform.executeAsync(() -> {
            addPlayerToLuckpermsGroups(allGroups, user.getIdentifier());
        });
    }

    public abstract void addPlayerToLuckpermsGroups(List<String> allGroups, UUID playerId);


    public void addUserToVyHubGroup(VyHubUser user, String groupName) {
        Group group = mappedGroups.getOrDefault(groupName, null);

        if (group == null) {
            platform.log(WARNING, String.format("Could not find group mapping for %s.", groupName));
            return;
        }

        HashMap<String, Object> values = new HashMap<String, Object>() {{
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

    public abstract List<String> getGroupsForPlayer(String playerId);

    public boolean checkProperty(String playerId, String property) {
        List<String> playerGroups = getGroupsForPlayer(playerId);
        for (String group : playerGroups) {
            if (mappedGroups.get(group) == null) {
                continue;
            }
            Map<String, Property> properties = mappedGroups.get(group).getProperties();
            for (Property prop : properties.values()) {
                if (prop.getName().equals(property) && prop.isGranted()) {
                    return true;
                }
            }
        }
        return false;
    }
}
