package com.github.groundbreakingmc.hlteleports.command;

import com.github.groundbreakingmc.hlteleports.Teleports;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public final class TpaTabCompleter implements TabCompleter {

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        final List<Player> waiting;
        if (args.length != 1
                || !(sender instanceof final Player playerSender)
                || (waiting = Teleports.getRequestsWhere(playerSender)).isEmpty()) {
            return List.of();
        }

        final List<String> completions = new ArrayList<>();
        final String input = args[0];

        for (final Player player : waiting) {
            if (StringUtil.startsWithIgnoreCase(player.getName(), input)) {
                completions.add(player.getName());
            }
        }

        return completions;
    }
}
