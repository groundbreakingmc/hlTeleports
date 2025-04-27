package com.github.groundbreakingmc.hlteleports.listener;

import com.github.groundbreakingmc.hlteleports.Teleports;
import com.github.groundbreakingmc.hlteleports.database.Database;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class DataLoader implements Listener {

    private final Teleports plugin;
    private final Database database;

    public DataLoader(Teleports plugin) {
        this.plugin = plugin;
        this.database = plugin.getDatabase();
    }

    @EventHandler
    public void onJoin(final PlayerJoinEvent event) {
        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () ->
                this.database.load(event.getPlayer())
        );
    }

    @EventHandler
    public void onQuit(final PlayerQuitEvent event) {
        this.database.removeFromCache(event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true)
    public void onKick(final PlayerQuitEvent event) {
        this.database.removeFromCache(event.getPlayer());
    }
}
