package net.vyhub;

import net.md_5.bungee.api.plugin.Event;
import net.vyhub.config.I18n;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class BungeeVyHubPlatform implements VyHubPlatform {
    private final VyHubPlugin plugin;
    protected BungeeVyHubPlatform(final VyHubPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public VyHubAPI getApiClient() {
        return this.plugin.getApiClient();
    }

    @Override
    public I18n getI18n() {
        return this.plugin.getI18n();
    }

    @Override
    public void executeAsync(Runnable runnable) {
        plugin.getProxy().getScheduler().runAsync(plugin, runnable);
    }

    @Override
    public void executeAsyncLater(Runnable runnable, long time, TimeUnit unit) {
        plugin.getProxy().getScheduler().schedule(plugin, runnable, time, unit);
    }

    @Override
    public void executeBlocking(Runnable runnable) {
        // BungeeCord has no concept of blocking tasks
        executeAsync(runnable);
    }

    @Override
    public void executeBlockingLater(Runnable runnable, long time, TimeUnit unit) {
        // BungeeCord has no concept of blocking tasks
        executeAsyncLater(runnable, time, unit);
    }

    @Override
    public void log(Level level, String message) {
        plugin.getLogger().log(level, message);
    }

    @Override
    public void callEvent(Object event) {
        Event bungeeEvent = (Event) event;
        plugin.getProxy().getPluginManager().callEvent(bungeeEvent);
    }
}
