package com.github.groundbreakingmc.hlteleports.command;

import com.github.groundbreakingmc.hlteleports.Teleports;
import com.github.groundbreakingmc.hlteleports.collections.Cooldowns;
import com.github.groundbreakingmc.hlteleports.events.CancellTpaRequestEvent;
import com.github.groundbreakingmc.hlteleports.utils.config.ConfigValues;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class TpacancelHandler implements CommandExecutor {

    private final Teleports plugin;
    private final ConfigValues configValues;
    private final Cooldowns cooldowns;

    public TpacancelHandler(Teleports plugin) {
        this.plugin = plugin;
        this.configValues = plugin.getConfigValues();
        this.cooldowns = plugin.getCooldowns();
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

        final Player target;

        if (args.length == 0) {
            final List<Player> players = Teleports.getRequestsWhere(playerSender);
            if (players.size() != 1) {
                final StringBuilder playerNames = new StringBuilder();
                for (final Player player : players) {
                    playerNames.append(player.getName()).append(", ");
                }
                playerNames.setLength(playerNames.length() - 2);
                sender.sendMessage(this.configValues.getManyRequestsMessage().replace("{players}", playerNames.toString()));
                return true;
            }

            target = players.get(0);
        } else {
            target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage(this.configValues.getNotFoundMessage().replace("{player}", args[1]));
                return true;
            }

            if (target.getUniqueId().equals(playerSender.getUniqueId())) {
                sender.sendMessage(this.configValues.getSelfMessage());
                return true;
            }

            final Player request = Teleports.removeRequest(target);
            if (request == null) {
                sender.sendMessage(this.configValues.getNoRequestMessage().replace("{player}", target.getName()));
                return true;
            }
        }

        final var event = new CancellTpaRequestEvent(playerSender, target);
        if (!event.callEvent()) {
            return true;
        }

        Teleports.removeRequest(target);

        playerSender.sendMessage(this.configValues.getDeclinedMessage().replace("{player}", target.getName()));
        target.sendMessage(this.configValues.getDeclinedSenderMessage().replace("{player}", playerSender.getName()));

        this.cooldowns.add(playerSender, duration);

        return true;
    }
}
