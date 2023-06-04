package net.vyhub;

import net.vyhub.config.I18n;
import net.vyhub.config.VyHubConfiguration;
import org.bukkit.Bukkit;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class BukkitVyHubPlatform implements VyHubPlatform {
    private final VyHubPlugin plugin;
    protected BukkitVyHubPlatform(final VyHubPlugin plugin) {
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
    public VyHubConfiguration getConfiguration() {
        return null;
    }

    @Override
    public void executeAsync(Runnable runnable) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, runnable);
    }

    @Override
    public void executeAsyncLater(Runnable runnable, long time, TimeUnit unit) {
        plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin, runnable, unit.toMillis(time) / 50);
    }

    @Override
    public void executeBlocking(Runnable runnable) {
        plugin.getServer().getScheduler().runTask(plugin, runnable);
    }

    @Override
    public void executeBlockingLater(Runnable runnable, long time, TimeUnit unit) {
        Bukkit.getScheduler().runTaskLater(plugin, runnable, unit.toMillis(time) / 50);
    }

    @Override
    public void log(Level level, String message) {
        plugin.getLogger().log(level, message);
    }

    @Override
    public void callEvent(Object event) {

    }
}
