package com.github.groundbreakingmc.hlteleports.command;

import com.github.groundbreakingmc.hlteleports.Teleports;
import com.github.groundbreakingmc.hlteleports.collections.Cooldowns;
import com.github.groundbreakingmc.hlteleports.database.DatabaseHandler;
import com.github.groundbreakingmc.hlteleports.utils.config.ConfigValues;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class TpToggleHandler implements TabExecutor {


    private final Teleports plugin;
    private final ConfigValues configValues;
    private final Cooldowns cooldowns;
    private final DatabaseHandler database;

    public TpToggleHandler(Teleports plugin) {
        this.plugin = plugin;
        this.configValues = plugin.getConfigValues();
        this.cooldowns = plugin.getCooldowns();
        this.database = plugin.getDatabase();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof final Player playerSender)) {
            sender.sendMessage("§cCommand can be executed only by the player!");
            return true;
        }

        final String groupName = this.plugin.getPermission().getPrimaryGroup(playerSender);
        final int duration = this.configValues.getCommandCooldown(groupName);
        if (this.cooldowns.check(playerSender, duration)) {
            return true;
        }

        if (this.database.hasTpToggled(playerSender)) {
            Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () ->
                    this.database.removeFromTpToggled(playerSender)
            );
            this.database.removeFromCache(playerSender);
            sender.sendMessage(this.configValues.getTptoggleOffMessage());
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () ->
                    this.database.addToTpToggled(playerSender)
            );
            this.database.addToCache(playerSender);
            sender.sendMessage(this.configValues.getTptoggleOnMessage());
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return List.of();
    }
}
