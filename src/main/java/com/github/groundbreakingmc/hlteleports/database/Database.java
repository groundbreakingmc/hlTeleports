package com.github.groundbreakingmc.hlteleports.database;

import com.github.groundbreakingmc.hlteleports.Teleports;
import com.github.groundbreakingmc.mylib.database.DatabaseUtils;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Set;
import java.util.UUID;

public final class Database extends com.github.groundbreakingmc.mylib.database.Database {

    private static final String ADD_QUERY = "INSERT OR IGNORE INTO tptoggled(playerUUID) VALUES(?);";
    private static final String REMOVE_QUERY = "DELETE FROM tptoggled WHERE playerUUID = ?;";
    private static final String CHECK_QUERY = "SELECT EXISTS(SELECT 1 FROM tptoggled WHERE playerUUID = ?);";

    private final Teleports plugin;
    private final Set<UUID> cache;

    public Database(Teleports plugin) {
        super(DatabaseUtils.getSQLiteDriverUrl(plugin));
        this.plugin = plugin;
        this.cache = new ObjectArraySet<>();
    }

    public void createTable() {
        final String query = "CREATE TABLE IF NOT EXISTS tptoggled(playerUUID TEXT NOT NULL);";
        try (final Connection connection = super.getConnection()) {
            super.createTables(connection, query);
        } catch (final SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void addToTpToggled(final Player player) {
        this.executeUpdate(ADD_QUERY, player);
    }

    public void removeFromTpToggled(final Player player) {
        this.executeUpdate(REMOVE_QUERY, player);
    }

    private void executeUpdate(final String query, final Player player) {
        try (final Connection connection = super.getConnection()) {
            super.executeUpdateQuery(query, connection, player.getUniqueId());
        } catch (final SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void load(final Player player) {
        try (final Connection connection = super.getConnection()) {
            if (super.containsInTable(CHECK_QUERY, connection, player.getUniqueId())) {
                Bukkit.getScheduler().runTask(this.plugin, () ->
                        this.addToCache(player)
                );
            }
        } catch (final SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void addToCache(final Player player) {
        this.cache.add(player.getUniqueId());
    }

    public void removeFromCache(final Player player) {
        this.cache.remove(player.getUniqueId());
    }

    public boolean hasTpToggled(final Player player) {
        return this.cache.contains(player.getUniqueId());
    }
}
