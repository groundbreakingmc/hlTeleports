package com.github.groundbreakingmc.hlteleports;

import com.github.groundbreakingmc.hlteleports.collections.Cooldowns;
import com.github.groundbreakingmc.hlteleports.command.*;
import com.github.groundbreakingmc.hlteleports.database.DatabaseHandler;
import com.github.groundbreakingmc.hlteleports.listener.DamageListener;
import com.github.groundbreakingmc.hlteleports.listener.DataLoader;
import com.github.groundbreakingmc.hlteleports.utils.config.ConfigValues;
import com.github.groundbreakingmc.mylib.utils.vault.VaultUtils;
import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Getter;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings("unused") // unused methods may be needful as api for future
@Getter
public final class Teleports extends JavaPlugin {

    private static final Map<UUID, Player> REQUESTS;
    private static final Map<UUID, BukkitRunnable> TELEPORTING;

    private final ConfigValues configValues;
    private final Cooldowns cooldowns;
    private final DatabaseHandler database;

    private Permission permission;

    static {
        REQUESTS = new Object2ObjectOpenHashMap<>();
        TELEPORTING = new Object2ObjectOpenHashMap<>();
    }

    public Teleports() {
        this.configValues = new ConfigValues(this);
        this.cooldowns = new Cooldowns(this);
        this.database = new DatabaseHandler(this);
    }

    @Override
    public void onEnable() {
        this.configValues.setup();
        this.database.createTable();

        this.permission = VaultUtils.getPermissionProvider();

        final TpaHandler tpaHandler = new TpaHandler(this);
        this.setupCommand("tpa", tpaHandler, tpaHandler);

        final TpaTabCompleter tpaTabCompleter = new TpaTabCompleter();

        final TpacceptHandler tpacceptHandler = new TpacceptHandler(this);
        this.setupCommand("tpaccept", tpacceptHandler, tpaTabCompleter);

        final TpacancelHandler tpacancelHandler = new TpacancelHandler(this);
        this.setupCommand("tpacancel", tpacancelHandler, tpaTabCompleter);

        final TpacancelRequestHandler tpacancelRequestHandler = new TpacancelRequestHandler(this);
        this.setupCommand("tpacancelrequest", tpacancelRequestHandler, tpacancelRequestHandler);

        final TpToggleHandler tpToggleHandler = new TpToggleHandler(this);
        this.setupCommand("tptoggle", tpToggleHandler, tpToggleHandler);

        final PluginManager pluginManager = super.getServer().getPluginManager();
        pluginManager.registerEvents(new DamageListener(this), this);
        pluginManager.registerEvents(new DataLoader(this), this);

        for (final Player target : Bukkit.getOnlinePlayers()) {
            this.database.load(target);
        }
    }

    @Override
    public void onDisable() {
        this.database.closeConnection();
    }

    private void setupCommand(final String textCommand,
                              final CommandExecutor executor,
                              final TabCompleter completer) {
        final PluginCommand command = super.getCommand(textCommand);
        command.setExecutor(executor);
        command.setTabCompleter(completer);
    }

    public static boolean addRequest(final Player sender, final Player target) {
        return REQUESTS.put(sender.getUniqueId(), target) != target;
    }

    public static Player removeRequest(final Player sender) {
        return REQUESTS.remove(sender.getUniqueId());
    }

    public static Player getRequest(final Player whose) {
        return REQUESTS.get(whose.getUniqueId());
    }

    public static List<Player> getRequestsWhere(final Player whose) {
        if (REQUESTS.isEmpty()) {
            return List.of();
        }

        final List<Player> players = new ArrayList<>();
        for (final Map.Entry<UUID, Player> entry : REQUESTS.entrySet()) {
            if (entry.getValue() == whose) {
                players.add(Bukkit.getPlayer(entry.getKey()));
            }
        }

        return ImmutableList.copyOf(players);
    }

    public static boolean clearRequests(final Player whose) {
        return REQUESTS.remove(whose.getUniqueId()) != null;
    }

    public static boolean addToTeleporting(final Player whom, final BukkitRunnable task) {
        return TELEPORTING.put(whom.getUniqueId(), task) == null;
    }

    public static boolean cancelTeleportation(final Player who) {
        final BukkitRunnable task = TELEPORTING.remove(who.getUniqueId());
        if (task == null) {
            return false;
        }

        task.cancel();
        return true;
    }

    public static boolean isTeleporting(final Player who) {
        return TELEPORTING.containsKey(who.getUniqueId());
    }
}
