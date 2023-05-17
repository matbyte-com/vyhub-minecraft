package net.vyhub.VyHubMinecraft.server;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.event.node.NodeAddEvent;
import net.luckperms.api.event.node.NodeClearEvent;
import net.luckperms.api.event.node.NodeMutateEvent;
import net.luckperms.api.event.node.NodeRemoveEvent;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.InheritanceNode;
import net.vyhub.VyHubMinecraft.Entity.Group;
import net.vyhub.VyHubMinecraft.Entity.GroupMapping;
import net.vyhub.VyHubMinecraft.Entity.VyHubUser;
import net.vyhub.VyHubMinecraft.VyHub;
import net.vyhub.VyHubMinecraft.event.VyHubPlayerInitializedEvent;
import net.vyhub.VyHubMinecraft.lib.Types;
import net.vyhub.VyHubMinecraft.lib.Utility;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.scheduler.BukkitRunnable;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.net.http.HttpResponse;
import java.util.*;
import java.util.logging.Level;

import static net.vyhub.VyHubMinecraft.VyHub.logger;


public class SvGroups implements Listener {
    private static List<Group> groups;
    private static Map<String, Group> mappedGroups;
    private static Gson gson = new Gson();
    private static List<String> groupChangeBacklog = new ArrayList<>();

    public static void updateGroups() {
        HttpResponse<String> response = Utility.sendRequest("/group/", Types.GET);

        if (response == null || response.statusCode() != 200) {
            return;
        }

        groups = gson.fromJson(response.body(), new TypeToken<ArrayList<Group>>() {
        }.getType());

        Map<String, Group> newMappedGroups = new HashMap<>();

        for (Group group : groups) {
            for (GroupMapping mapping : group.getMappings()) {
                if (mapping.getServerbundle_id() == null || mapping.getServerbundle_id().equals(SvServer.serverbundleID)) {
                    newMappedGroups.put(mapping.getName(), group);
                }
            }
        }

        mappedGroups = new HashMap<>(newMappedGroups);
    }

    @EventHandler
    public static void onPlayerInit(VyHubPlayerInitializedEvent event) {
        Player player = event.getPlayer();
        new BukkitRunnable() {
            @Override
            public void run() {
                syncGroups(player);
            }
        }.runTaskAsynchronously(VyHub.plugin);
    }

    public static void onNodeMutate(NodeMutateEvent event) {
        //logger.info(String.format("Received node mutate event: %s", event.toString()));
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!event.isUser()) {
                    return;
                }

                String playerID = ((User) event.getTarget()).getUniqueId().toString();
                VyHubUser user = SvUser.getUser(playerID);

                if (user == null) {
                    return;
                }

                if (event instanceof NodeAddEvent ) {
                    Node node = ((NodeAddEvent) event).getNode();

                    if (node.getType() != NodeType.INHERITANCE) {
                        return;
                    }

                    InheritanceNode inode = (InheritanceNode) node;
                    String groupName = inode.getGroupName();

                    String backlogKey = getBacklogKey(playerID, groupName, "add");

                    // Only do something if there is no backlog
                    if (!groupChangeBacklog.remove(backlogKey)) {
                        addUserToVyHubGroup(user, groupName);
                    }

                }

                if (event instanceof NodeRemoveEvent) {
                    Node node = ((NodeRemoveEvent) event).getNode();

                    if (node.getType() != NodeType.INHERITANCE) {
                        return;
                    }

                    InheritanceNode inode = (InheritanceNode) node;
                    String groupName = inode.getGroupName();

                    String backlogKey = getBacklogKey(playerID, groupName, "remove");
                    // Only do something if there is no backlog
                    if (!groupChangeBacklog.remove(backlogKey)) {
                        removeUserFromVyHubGroup(user, groupName);
                    }
                }

                if (event instanceof NodeClearEvent) {
                    removeUserFromAllVyHubGroups(user);
                }
            }
        }.runTaskAsynchronously(VyHub.plugin);
    }

    public static void syncGroups(Player player) {
        JSONParser jsonParser = new JSONParser();
        VyHubUser user = SvUser.getUser(player.getUniqueId().toString());

        if (user == null) {
            return;
        }

        HttpResponse<String> response = Utility.sendRequest("/user/" + user.getId() + "/group?serverbundle_id=" + SvServer.serverbundleID,
                Types.GET);

        if (response == null || response.statusCode() != 200) {
            return;
        }

        try {
            JSONArray jsonArray = (JSONArray) jsonParser.parse(response.body());

            List<String> allGroups = new ArrayList<>();

            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject jsonObject = (JSONObject) jsonArray.get(i);
                JSONArray mappings = (JSONArray) jsonObject.get("mappings");
                for (int j = 0; j < mappings.size(); j++) {
                    JSONObject groupParameters = (JSONObject) mappings.get(j);
                    String groupName = groupParameters.get("name").toString();
                    allGroups.add(groupName);
                }
            }


            new BukkitRunnable() {
                @Override
                public void run() {
                    addPlayerToLuckpermsGroup(allGroups, player);
                }
            }.runTask(VyHub.plugin);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public static void addPlayerToLuckpermsGroup(List<String> allGroups, Player player) {
        RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        if (provider != null) {
            LuckPerms api = provider.getProvider();
            User user = api.getUserManager().getUser(player.getUniqueId());
            List<Node> nodes = new ArrayList<>();
            Collection<InheritanceNode> currentNodes = user.getNodes(NodeType.INHERITANCE);
            String playerID = player.getUniqueId().toString();

            for (String groupName : allGroups) {
                if (api.getGroupManager().getGroup(groupName) != null) {
                    InheritanceNode node = InheritanceNode.builder(groupName).value(true).build();
                    nodes.add(node);

                    if (!currentNodes.contains(node)) {
                        groupChangeBacklog.add(getBacklogKey(playerID, groupName, "add"));
                        user.data().add(node);
                        // player.sendMessage(ChatColor.GOLD + "You were added to group " + groupName);
                    }
                }
            }

            for (InheritanceNode n : currentNodes) {
                if (!nodes.contains(n)) {
                    groupChangeBacklog.add(getBacklogKey(playerID, n.getGroupName(), "remove"));
                    user.data().remove(n);
                }
            }
            api.getUserManager().saveUser(user);
        }
    }

    public static void addUserToVyHubGroup(VyHubUser user, String groupName) {
        Group group = mappedGroups.getOrDefault(groupName, null);

        if (group == null) {
            logger.log(Level.WARNING, String.format("Could not find group mapping for %s.", groupName));
            return;
        }

        HashMap<String, Object> data = new HashMap<>() {{
            put("group_id", group.getId());
            put("serverbundle_id", SvServer.serverbundleID);
        }};

        Utility.sendRequestBody(String.format("/user/%s/membership", user.getId()), Types.POST,
                Utility.createRequestBody(data));

        logger.info(String.format("Added VyHub group membership in group %s for player %s.", groupName, user.getUsername()));
    }

    public static void removeUserFromVyHubGroup(VyHubUser user, String groupName) {
        Group group = mappedGroups.getOrDefault(groupName, null);

        if (group == null) {
            logger.log(Level.WARNING, String.format("Could not find group mapping for %s.", groupName));
            return;
        }

        String url = String.format(
                "/user/%s/membership/by-group?group_id=%s&serverbundle_id=%s",
                user.getId(),
                group.getId(),
                SvServer.serverbundleID
        );

        Utility.sendRequest(url, Types.DELETE);

        logger.info(String.format("Ended VyHub group membership in group %s for player %s.", groupName, user.getUsername()));
    }

    public static void removeUserFromAllVyHubGroups(VyHubUser user) {
        String url = String.format(
                "/user/%s/membership?serverbundle_id=%s",
                user.getId(),
                SvServer.serverbundleID
        );

        Utility.sendRequest(url, Types.DELETE);

        logger.info(String.format("Ended all VyHub group memberships for player %s.", user.getUsername()));
    }

    public static String getBacklogKey(String playerID, String groupName, String operation) {
        return String.format("%s_%s_%s", playerID, operation.toUpperCase(), groupName);
    }
}
