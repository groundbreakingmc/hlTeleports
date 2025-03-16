package com.github.groundbreakingmc.hlteleports.events;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

public final class SentTpaRequestEvent extends PlayerEvent implements Cancellable {

    private static final HandlerList HANDLER_LIST;

    @Getter
    private final Player target;

    private boolean cancelled;

    public SentTpaRequestEvent(@NotNull Player who, Player target) {
        super(who);

        this.target = target;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    public static @NotNull HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    static {
        HANDLER_LIST = new HandlerList();
    }
}
