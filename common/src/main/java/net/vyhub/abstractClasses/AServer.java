package net.vyhub.abstractClasses;

import net.vyhub.VyHubPlatform;
import net.vyhub.config.VyHubConfiguration;
import net.vyhub.entity.Server;
import net.vyhub.lib.Cache;
import net.vyhub.lib.Utility;
import retrofit2.Response;

import java.io.IOException;
import java.util.HashMap;

import static net.vyhub.lib.Utility.checkResponse;

public abstract class AServer extends VyHubAbstractBase {
    private static Cache<Server> serverCache = new Cache<>("server", Server.class);
    public static String serverbundleID = null;
    private final AUser aUser;

    public AServer(VyHubPlatform platform, AUser aUser) {
        super(platform);
        this.aUser = aUser;
    }

    public AUser getAUser() {
        return aUser;
    }

    public Server getServerInformation() {
        String serverID = VyHubConfiguration.getServerId();

        if (serverID == null) {
            return null;
        }

        Server server;

        Response<Server> response = null;
        try {
            response = platform.getApiClient().getServer(serverID).execute();
        } catch (IOException e) {
            server = serverCache.load();
        }

        if (response.code() != 200) {
            server = serverCache.load();
        }

        server = response.body();

        serverCache.save(server);
        serverbundleID = server.getServerbundle_id();

        return server;
    }

    public abstract HashMap<String, Object> collectServerStatistics();

    public void patchServer() {
        HashMap<String, Object> values = collectServerStatistics();

        Response response = null;
        try {
            response = platform.getApiClient().patchServer(VyHubConfiguration.getServerId(), Utility.createRequestBody(values)).execute();
        } catch (IOException e) {
            return;
        }

        checkResponse(platform, response, "Patch server");

    }
}
