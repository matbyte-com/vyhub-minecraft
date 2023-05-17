package net.vyhub;

import java.util.logging.Level;

public interface VyHubPlatform {
    VyHubAPI getApiClient();
    void executeAsync(Runnable runnable);
    void log(Level level, String message);

}
