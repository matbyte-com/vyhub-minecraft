package net.vyhub.VyHubMinecraft.server;

import net.vyhub.VyHubMinecraft.Entity.VyHubUser;
import net.vyhub.VyHubMinecraft.lib.Types;
import net.vyhub.VyHubMinecraft.lib.Utility;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.data.DataMutateResult;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.types.InheritanceNode;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SvGroups implements Listener {

    @EventHandler
    public static void syncGroups(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        JSONParser jsonParser = new JSONParser();
        VyHubUser user = SvUser.getUser(player.getUniqueId().toString());

        if (user == null) {
            return;
        }

        HttpResponse<String> response = Utility.sendRequest("/user/" + user.getId() + "/group?serverbundle_id=" + SvServer.serverbundleID,
                Types.GET);

        if (response == null  || response.statusCode() != 200) {
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

            addUserToGroup(allGroups, player);

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public static void addUserToGroup(List<String> allGroups, Player player) {
        RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        if (provider != null) {
            LuckPerms api = provider.getProvider();
            User user = api.getUserManager().getUser(player.getUniqueId());
            List<Node> nodes = new ArrayList<>();
            Collection<Node> currentNodes = user.getNodes();

            for (String groupName : allGroups) {
                if (api.getGroupManager().getGroup(groupName) != null) {
                    InheritanceNode node = InheritanceNode.builder(groupName).value(true).build();
                    nodes.add(node);

                    if (!currentNodes.contains(node)) {
                        DataMutateResult result = user.data().add(node);
                        player.sendMessage(ChatColor.GOLD + "You are added to: " + groupName);
                    }
                }
            }
            for (Node n : currentNodes) {
                if (!nodes.contains(n) && n.getKey().charAt(0) != 'l') {
                    user.data().remove(n);
                }
            }
            api.getUserManager().saveUser(user);
        }
    }
}
