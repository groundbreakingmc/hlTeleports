package com.github.groundbreakingmc.hlteleports.command;

import com.github.groundbreakingmc.hlteleports.Teleports;
import com.github.groundbreakingmc.hlteleports.cooldowns.Cooldowns;
import com.github.groundbreakingmc.hlteleports.config.ConfigValues;
import com.github.groundbreakingmc.hlteleports.database.Database;
import com.github.groundbreakingmc.hlteleports.events.SentTpaRequestEvent;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public final class TpaHandler implements TabExecutor {

    private final Teleports plugin;
    private final ConfigValues configValues;
    private final Cooldowns cooldowns;
    private final Database database;

    public TpaHandler(Teleports plugin) {
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

        if (args.length < 1) {
            sender.sendMessage(this.configValues.getUsageMessage());
            return true;
        }

        final var target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(this.configValues.getNotFoundMessage().replace("{player}", args[1]));
            return true;
        }

        if (target.getUniqueId().equals(playerSender.getUniqueId())) {
            sender.sendMessage(this.configValues.getSelfMessage());
            return true;
        }

        final String groupName = this.plugin.getPermission().getPrimaryGroup(playerSender);
        final int duration = this.configValues.getCommandCooldown(groupName);
        if (this.cooldowns.check(playerSender, duration)) {
            return true;
        }

        if (this.database.hasTpToggled(target)) {
            sender.sendMessage(this.configValues.getTptoggledMessage().replace("{player}", target.getName()));
            return true;
        }

        if (!Teleports.addRequest(playerSender, target)) {
            sender.sendMessage(this.configValues.getAlreadySentMessage().replace("{player}", target.getName()));
            return true;
        }

        final var event = new SentTpaRequestEvent(playerSender, target);
        if (!event.callEvent()) {
            return true;
        }

        sender.sendMessage(this.getForSender(target));
        target.sendMessage(this.getForTarget(playerSender));

        this.cooldowns.add(playerSender, duration);

        Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
            if (Teleports.removeRequest(playerSender) != null) {
                sender.sendMessage(this.configValues.getTimeLeftMessage().replace("{player}", target.getName()));
            }
        }, this.configValues.getRequestTime());

        return true;
    }

    private BaseComponent[] getForSender(final Player target) {
        final var declineButton = TextComponent.fromLegacyText(this.configValues.getDeclineButton())[0];
        declineButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpacancelrequest " + target.getName()));
        declineButton.setHoverEvent(this.configValues.getDeclineButtonHoverEvent());

        final String[] message = this.configValues.getSentMessage()
                .replace("{player}", target.getName())
                .split("\\{decline}");

        final BaseComponent[] textComponent = TextComponent.fromLegacyText(message[0]);
        final BaseComponent lastComponent = textComponent[textComponent.length - 1];
        lastComponent.addExtra(declineButton);
        if (message.length > 1) {
            lastComponent.addExtra(message[1]);
        }

        return textComponent;
    }

    private BaseComponent[] getForTarget(final Player sender) {
        final BaseComponent acceptButton = this.getButton(
                this.configValues.getAcceptButton(),
                "/tpaccept " + sender.getName(),
                this.configValues.getAcceptButtonHoverEvent()
        )[0];

        final BaseComponent declineButton = this.getButton(
                this.configValues.getDeclineButton(),
                "/tpacancel " + sender.getName(),
                this.configValues.getDeclineButtonHoverEvent()
        )[0];

        final String message = this.configValues.getRequestMessage().replace("{player}", sender.getName());

        final int acceptIndex = message.indexOf("{accept}");
        final int declineIndex = message.indexOf("{decline}");

        final int min = Math.min(acceptIndex, declineIndex);
        final int max = Math.max(acceptIndex, declineIndex);

        final String start = message.substring(0, min);
        final String middle = message.substring(min + 8, max);
        final String last = message.substring(max + 9);

        final BaseComponent[] textComponent = TextComponent.fromLegacyText(start);
        final BaseComponent lastComponent = textComponent[textComponent.length - 1];
        lastComponent.addExtra(min == acceptIndex ? acceptButton : declineButton);
        for (final BaseComponent component : TextComponent.fromLegacyText(middle)) {
            lastComponent.addExtra(component);
        }
        lastComponent.addExtra(max == acceptIndex ? acceptButton : declineButton);
        for (final BaseComponent component : TextComponent.fromLegacyText(last)) {
            lastComponent.addExtra(component);
        }

        return textComponent;
    }

    private BaseComponent[] getButton(final String text,
                                      final String clickCommand,
                                      final HoverEvent hoverEvent) {
        final BaseComponent[] button = TextComponent.fromLegacyText(text);
        for (final BaseComponent component : button) {
            component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, clickCommand));
            component.setHoverEvent(hoverEvent);
        }
        return button;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length != 1 || !(sender instanceof Player)) {
            return List.of();
        }

        final List<String> completions = new ArrayList<>();
        final String input = args[0];

        for (final Player target : Bukkit.getOnlinePlayers()) {
            if (StringUtil.startsWithIgnoreCase(target.getName(), input)) {
                completions.add(target.getName());
            }
        }

        return completions;
    }
}
