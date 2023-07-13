package net.vyhub.tasks;

import net.vyhub.VyHubPlatform;
import net.vyhub.abstractClasses.AUser;

public class TUser extends AUser {
    public TUser(VyHubPlatform platform) {
        super(platform);
    }

    public void callVyHubPlayerInitializedEvent(String playerId, String playerName) {
        throw new UnsupportedOperationException();
    }

    public void sendMessage(String playerName, String message) {
        throw new UnsupportedOperationException();
    }

}
