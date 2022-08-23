package net.vyhub.VyHubMinecraft.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class VyHubPlayerInitializedEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Player player;

    public VyHubPlayerInitializedEvent(Player player) {
        this.player = player;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public Player getPlayer() {
        return player;
    }
}