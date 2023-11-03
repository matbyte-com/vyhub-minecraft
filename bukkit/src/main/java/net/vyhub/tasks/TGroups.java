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
import net.vyhub.config.VyHubConfiguration;
import net.vyhub.entity.VyHubUser;
import net.vyhub.event.VyHubPlayerInitializedEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

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

    public void addPlayerToLuckpermsGroups(List<String> allGroups, UUID playerId) {
        Player player = Bukkit.getPlayer(playerId);
        if (player == null) {
            getPlatform().log(Level.WARNING, "Group-Sync: Player with could not be found on server");
            return;
        }
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
                        if (VyHubConfiguration.getSuccesMessage()) {
                        player.sendMessage(ChatColor.YELLOW + String.format(getPlatform().getI18n().get("groupAdded"), groupName));
                        } else {
                            return;
                        }
                    }
                }
            }

            for (InheritanceNode n : currentNodes) {
                // Only remove group if there is a mapping for it. Otherwise, let it there
                if (!nodes.contains(n) && getMappedGroups().containsKey(n.getGroupName())) {
                    String groupName = n.getGroupName();
                    groupChangeBacklog.add(getBacklogKey(playerID, groupName, "remove"));
                    user.data().remove(n);
                    if (VyHubConfiguration.getSuccesMessage()) {
                        player.sendMessage(ChatColor.YELLOW + String.format(getPlatform().getI18n().get("groupRemoved"), groupName));
                    } else {
                        return;
                    }
                }
            }
            api.getUserManager().saveUser(user);
        }
    }

    @Override
    public List<String> getGroupsForPlayer(String playerId) {
        RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        if (provider != null) {
            LuckPerms api = provider.getProvider();
            User user = api.getUserManager().getUser(UUID.fromString(playerId));
            if (user == null) {
                return new ArrayList<>();
            }
            Collection<InheritanceNode> currentNodes = user.getNodes(NodeType.INHERITANCE);
            List<String> res = new ArrayList<>();
            for (InheritanceNode n : currentNodes) {
                res.add(n.getGroupName());
            }
            return res;
        }
        return new ArrayList<>();
    }

    @Override
    public void syncGroupsForAll() {
        Bukkit.getOnlinePlayers().forEach(player -> {
            syncGroups(player.getUniqueId().toString());
        });
    }
}


