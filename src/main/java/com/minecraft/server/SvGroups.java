package com.minecraft.server;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.event.LuckPermsEvent;
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
import org.checkerframework.checker.units.qual.A;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class SvGroups implements Listener {

    @EventHandler
    public static void syncGroups(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        JSONParser jsonParser = new JSONParser();
        JSONObject serverInformation = SvServer.getServerInformationObject();

        String  serverBundleId = null;
        try {
            JSONObject serverbundleInformation = (JSONObject) jsonParser.parse(serverInformation.get("serverbundle").toString());
            serverBundleId = serverbundleInformation.get("id").toString();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.vyhub.app/myinstance/v1/user/" + SvUser.getUser(player.getUniqueId().toString()).getId() + "/group?serverbundle_id=" + serverBundleId))
                .method("GET", HttpRequest.BodyPublishers.noBody())
                .build();
        HttpResponse<String> response = null;

        try {
            response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        try {
            JSONArray jsonArray = (JSONArray) jsonParser.parse(response.body());

            List<String> allGroups = new ArrayList<>();

            // TODO: Von allen Gruppen alle mappings bestimmen, wo mapping.serverbundle_id der
            //  Serverbundle ID entspricht oder null ist.
            //  Diese Gruppen in `allGroups` speichern.

            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject jsonObject = (JSONObject) jsonArray.get(i);
                JSONArray mappings = (JSONArray) jsonObject.get("mappings");
                JSONObject groupParameters = (JSONObject) mappings.get(0);

                String groupName =  groupParameters.get("name").toString();
                allGroups.add(groupName);
            }

            // TO DO END

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
            for (String groupName : allGroups) {
                if (api.getGroupManager().getGroup(groupName) != null) {


                    InheritanceNode node = InheritanceNode.builder(groupName).value(true).build();
                    nodes.add(node);
                    DataMutateResult result = user.data().add(node);

                    player.sendMessage(ChatColor.GOLD + "You are added to: " + groupName);


                }
            }
            for (Node n : user.getNodes()) {
                    if (!nodes.contains(n) && n.getKey().charAt(0) != 'l') {
                        user.data().remove(n);
                }
            }
            api.getUserManager().saveUser(user);


        }
    }
}
