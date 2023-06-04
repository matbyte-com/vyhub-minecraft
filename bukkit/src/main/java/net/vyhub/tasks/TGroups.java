package net.vyhub.tasks;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.event.node.NodeAddEvent;
import net.luckperms.api.event.node.NodeClearEvent;
import net.luckperms.api.event.node.NodeMutateEvent;
import net.luckperms.api.event.node.NodeRemoveEvent;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.InheritanceNode;
import net.vyhub.VyHubPlatform;
import net.vyhub.abstractClasses.AGroups;
import net.vyhub.abstractClasses.AUser;
import net.vyhub.entity.VyHubUser;
import net.vyhub.event.VyHubPlayerInitializedEvent;
import net.vyhub.lib.Utility;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TGroups extends AGroups implements Listener {
    public TGroups(VyHubPlatform platform, AUser aUser) {
        super(platform, aUser);
    }

    @EventHandler
    public void onPlayerInit(VyHubPlayerInitializedEvent event) {
        Player player = event.getPlayer();

        getPlatform().executeAsync(() -> {
            syncGroups(player.getUniqueId().toString());
        });
    }

    public void onNodeMutate(NodeMutateEvent event) {
        //logger.info(String.format("Received node mutate event: %s", event.toString()));

        getPlatform().executeAsync(() -> {
            if (!event.isUser()) {
                return;
            }

            String playerID = ((User) event.getTarget()).getUniqueId().toString();
            VyHubUser user = getAUser().getUser(playerID);

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
        });
    }

    public void addPlayerToLuckpermsGroup(List<String> allGroups, String playerId) {
        Player player = Bukkit.getPlayer(playerId);
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
}

