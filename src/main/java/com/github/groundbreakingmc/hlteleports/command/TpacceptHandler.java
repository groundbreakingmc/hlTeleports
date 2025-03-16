package com.github.groundbreakingmc.hlteleports.command;

import com.github.groundbreakingmc.hlteleports.Teleports;
import com.github.groundbreakingmc.hlteleports.collections.Cooldowns;
import com.github.groundbreakingmc.hlteleports.events.AcceptTpaRequestEvent;
import com.github.groundbreakingmc.hlteleports.utils.config.ConfigValues;
import org.bukkit.Bukkit;
import org.bukkit.boss.BossBar;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class TpacceptHandler implements CommandExecutor {

    private final Teleports plugin;
    private final ConfigValues configValues;
    private final Cooldowns cooldowns;

    public TpacceptHandler(Teleports plugin) {
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
            if (players.isEmpty()) {
                sender.sendMessage(this.configValues.getNoRequestsMessage());
                return true;
            }

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

        final var event = new AcceptTpaRequestEvent(playerSender, target);
        if (!event.callEvent()) {
            return true;
        }

        this.process(playerSender, target, groupName);
        this.cooldowns.add(playerSender, duration);

        return true;
    }

    private void process(final Player who, final Player target, final String groupName) {
        who.sendMessage(this.configValues.getAcceptedMessage().replace("{player}", target.getName()));
        target.sendMessage(TpacceptHandler.this.configValues.getAcceptedSenderMessage().replace("{player}", who.getName()));

        final int time = TpacceptHandler.this.configValues.getTeleportCooldown(groupName);

        if (time > 0) {
            final BossBar bossBar = Bukkit.createBossBar(
                    this.configValues.getBossBarText().replace("{time}", Integer.toString(time)),
                    this.configValues.getBossBarColor(),
                    this.configValues.getBossBarStyle()
            );

            bossBar.addPlayer(target);

            final BukkitRunnable task = new BukkitRunnable() {

                private int timeLeft = time - 1;
                private final double step = 1.0 / time;

                @Override
                public void run() {
                    bossBar.setTitle(
                            TpacceptHandler.this.configValues.getBossBarText().replace("{time}", Integer.toString(this.timeLeft))
                    );

                    final double newProgress = bossBar.getProgress() - this.step;
                    bossBar.setProgress(newProgress < 0 ? 0 : newProgress);

                    if (this.timeLeft < 1) {
                        Bukkit.getScheduler().runTaskLater(TpacceptHandler.this.plugin, () -> {
                            bossBar.removePlayer(target);
                            target.teleport(who.getLocation());
                        }, 5L);
                        cancel();
                        return;
                    }

                    this.timeLeft--;
                }

                @Override
                public synchronized void cancel() throws IllegalStateException {
                    super.cancel();
                    bossBar.removePlayer(target);
                }
            };

            task.runTaskTimerAsynchronously(this.plugin, 20L, 20L);

            Teleports.addToTeleporting(target, task);
        } else {
            target.teleport(who.getLocation());
        }
    }
}
