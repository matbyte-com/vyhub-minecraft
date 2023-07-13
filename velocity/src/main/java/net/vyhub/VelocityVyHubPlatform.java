package net.vyhub;

import net.vyhub.config.I18n;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class VelocityVyHubPlatform implements VyHubPlatform {
    private final VyHubPlugin plugin;
    protected VelocityVyHubPlatform(final VyHubPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public VyHubAPI getApiClient() {
        return plugin.getApiClient();
    }

    @Override
    public I18n getI18n() {
        return this.plugin.getI18n();
    }

    @Override
    public void executeAsync(Runnable runnable) {
        plugin.getServer().getScheduler().buildTask(plugin, runnable);
    }

    @Override
    public void executeAsyncLater(Runnable runnable, long time, TimeUnit unit) {
        plugin.getServer().getScheduler().buildTask(plugin, runnable).delay(time, unit).schedule();
    }

    @Override
    public void executeBlocking(Runnable runnable) {
        // Velocity has no concept of blocking tasks
        executeAsync(runnable);
    }

    @Override
    public void executeBlockingLater(Runnable runnable, long time, TimeUnit unit) {
        // Velocity has no concept of blocking tasks
        executeAsyncLater(runnable, time, unit);
    }

    @Override
    public void log(Level level, String message) {
        plugin.getLogger().log(level, message);
    }

    @Override
    public void callEvent(Object event) {
        plugin.getServer().getEventManager().fire(event);
    }
}
