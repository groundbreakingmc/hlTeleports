package com.github.groundbreakingmc.hlteleports.command;

import com.github.groundbreakingmc.hlteleports.Teleports;
import com.github.groundbreakingmc.hlteleports.utils.config.ConfigValues;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public final class TpacancelRequestHandler implements TabExecutor {

    private final ConfigValues configValues;

    public TpacancelRequestHandler(Teleports plugin) {
        this.configValues = plugin.getConfigValues();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof final Player playerSender)) {
            sender.sendMessage("§cCommand can be executed only by the player!");
            return true;
        }

        final Player target = Teleports.removeRequest(playerSender);
        if (target == null) {
            sender.sendMessage(this.configValues.getNoRequestMessage());
            return true;
        }

        sender.sendMessage(this.configValues.getCanceledMessage().replace("{player}", target.getName()));

        return false;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return List.of();
    }
}
