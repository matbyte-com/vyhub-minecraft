package net.vyhub;

import net.vyhub.config.I18n;
import net.vyhub.config.VyHubConfiguration;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public interface VyHubPlatform {
    VyHubAPI getApiClient();
    I18n getI18n();
    void executeAsync(Runnable runnable);
    void executeAsyncLater(Runnable runnable, long time, TimeUnit unit);
    void executeBlocking(Runnable runnable);
    void executeBlockingLater(Runnable runnable, long time, TimeUnit unit);
    void log(Level level, String message);
    void callEvent(Object event);
}
