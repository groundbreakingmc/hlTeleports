package com.github.groundbreakingmc.hlteleports.config;

import com.github.groundbreakingmc.hlteleports.Teleports;
import com.github.groundbreakingmc.mylib.colorizer.Colorizer;
import com.github.groundbreakingmc.mylib.colorizer.LegacyColorizer;
import com.github.groundbreakingmc.mylib.config.ConfigurateLoader;
import com.github.groundbreakingmc.mylib.logger.console.LoggerFactory;
import com.github.groundbreakingmc.mylib.utils.time.TimeFormatterUtil;
import com.google.common.collect.ImmutableMap;
import lombok.AccessLevel;
import lombok.Getter;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.spongepowered.configurate.ConfigurationNode;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Getter
public final class ConfigValues {

    @Getter(AccessLevel.NONE)
    private final Teleports plugin;
    @Getter(AccessLevel.NONE)
    private final Colorizer colorizer;

    @Getter(AccessLevel.NONE)
    private Map<String, Integer> commandCooldowns;
    @Getter(AccessLevel.NONE)
    private int defaultCommandCooldowns;

    @Getter(AccessLevel.NONE)
    private Map<String, Integer> teleportCooldowns;
    @Getter(AccessLevel.NONE)
    private int defaultTeleportCooldown;

    private String bossBarText;
    private BarColor bossBarColor;
    private BarStyle bossBarStyle;

    private int requestTime;

    private String cooldownMessage;
    private String notFoundMessage;
    private String usageMessage;
    private String noRequestsMessage;
    private String manyRequestsMessage;
    private String noRequestMessage;
    private String alreadySentMessage;
    private String selfMessage;
    private String sentMessage;
    private String requestMessage;
    private String acceptedMessage;
    private String declinedMessage;
    private String acceptedSenderMessage;
    private String declinedSenderMessage;
    private String damagedMessage;
    private String timeLeftMessage;
    private String canceledMessage;
    private String tptoggleOnMessage;
    private String tptoggleOffMessage;
    private String tptoggledMessage;

    private String acceptButton;
    private HoverEvent acceptButtonHoverEvent;
    private String declineButton;
    private HoverEvent declineButtonHoverEvent;

    private TimeFormatterUtil.TimeValues timeValues;

    public ConfigValues(Teleports plugin) {
        this.plugin = plugin;
        this.colorizer = new LegacyColorizer();
    }

    public void setup() {
        final ConfigurationNode config = ConfigurateLoader.loader(this.plugin, LoggerFactory.createLogger(this.plugin))
                .fileName("config.yml")
                .load();

        this.setupCooldowns(config);
        this.setupBossBars(config);
        this.setupMessages(config);

        this.requestTime = config.node("request-time").getInt() * 20;
    }

    private void setupCooldowns(final ConfigurationNode config) {
        final var cooldowns = config.node("group-cooldowns");
        this.setupCommandCooldowns(cooldowns);
        this.setupTeleportCooldowns(cooldowns);
    }

    private void setupCommandCooldowns(final ConfigurationNode cooldowns) {
        final Map<String, Integer> map = this.getCooldownMap(cooldowns.node("command"), (numb) -> numb * 1000);
        this.defaultCommandCooldowns = map.remove("default");
        this.commandCooldowns = ImmutableMap.copyOf(map);
    }

    private void setupTeleportCooldowns(final ConfigurationNode cooldowns) {
        final Map<String, Integer> map = this.getCooldownMap(cooldowns.node("teleport"), (numb) -> numb);
        this.defaultTeleportCooldown = map.remove("default");
        this.teleportCooldowns = ImmutableMap.copyOf(map);
    }

    private Map<String, Integer> getCooldownMap(final ConfigurationNode node, final Function<Integer, Integer> counter) {
        final HashMap<String, Integer> map = new HashMap<>();
        for (final Map.Entry<Object, ? extends ConfigurationNode> entry : node.childrenMap().entrySet()) {
            map.put(entry.getKey().toString(), counter.apply(entry.getValue().getInt()));
        }
        return map;
    }

    private void setupBossBars(final ConfigurationNode config) {
        final var bossbarSettings = config.node("bossbar-settings");

        this.bossBarText = this.getMessage(bossbarSettings, "text");
        this.bossBarColor = BarColor.valueOf(bossbarSettings.node("color").getString());
        this.bossBarStyle = BarStyle.valueOf(bossbarSettings.node("style").getString());
    }

    private void setupMessages(final ConfigurationNode config) {
        final var messages = config.node("messages");
        this.cooldownMessage = this.getMessage(messages, "cooldown");
        this.notFoundMessage = this.getMessage(messages, "not-found");
        this.usageMessage = this.getMessage(messages, "usage");
        this.noRequestsMessage = this.getMessage(messages, "no-requests");
        this.manyRequestsMessage = this.getMessage(messages, "many-requests");
        this.noRequestMessage = this.getMessage(messages, "no-request");
        this.alreadySentMessage = this.getMessage(messages, "already-sent");
        this.selfMessage = this.getMessage(messages, "self");
        this.sentMessage = this.getMessage(messages, "sent");
        this.requestMessage = this.getMessage(messages, "request");
        this.acceptedMessage = this.getMessage(messages, "accepted");
        this.declinedMessage = this.getMessage(messages, "declined");
        this.acceptedSenderMessage = this.getMessage(messages, "accepted-sender");
        this.declinedSenderMessage = this.getMessage(messages, "declined-sender");
        this.damagedMessage = this.getMessage(messages, "damaged");
        this.timeLeftMessage = this.getMessage(messages, "time-left");
        this.canceledMessage = this.getMessage(messages, "canceled");
        this.tptoggleOnMessage = this.getMessage(messages, "tptoggle-on");
        this.tptoggleOffMessage = this.getMessage(messages, "tptoggle-off");
        this.tptoggledMessage = this.getMessage(messages, "tptoggled");

        this.acceptButton = this.getMessage(messages, "accept");
        this.acceptButtonHoverEvent = new HoverEvent(
                HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(
                this.getMessage(messages, "accept-hover")
        ));
        this.declineButton = this.getMessage(messages, "decline");
        this.declineButtonHoverEvent = new HoverEvent(
                HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(
                this.getMessage(messages, "decline-hover")
        ));

        final var time = messages.node("time");
        this.timeValues = new TimeFormatterUtil.TimeValues(
                "",
                time.node("hours").getString(),
                time.node("minutes").getString(),
                time.node("seconds").getString()
        );
    }

    private String getMessage(final ConfigurationNode node, final String path) {
        return this.colorizer.colorize(node.node(path).getString());
    }

    public int getCommandCooldown(final String groupName) {
        return this.commandCooldowns.getOrDefault(groupName, this.defaultCommandCooldowns);
    }

    public int getTeleportCooldown(final String groupName) {
        return this.teleportCooldowns.getOrDefault(groupName, this.defaultTeleportCooldown);
    }
}
