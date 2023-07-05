package net.vyhub.abstractClasses;

import net.vyhub.VyHubPlatform;
import net.vyhub.config.VyHubConfiguration;
import net.vyhub.entity.Server;
import net.vyhub.lib.Cache;
import retrofit2.Response;

import java.io.IOException;

public abstract class AServer {
    private static Cache<Server> serverCache = new Cache<>("server", Server.class);
    public static String serverbundleID = null;

    private final VyHubPlatform platform;
    private final AUser aUser;

    public AServer(VyHubPlatform platform, AUser aUser) {
        this.platform = platform;
        this.aUser = aUser;
    }

    public VyHubPlatform getPlatform() {
        return platform;
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
            // TODO Make Async (maybe? not necessary)
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

    public abstract void patchServer();
}
