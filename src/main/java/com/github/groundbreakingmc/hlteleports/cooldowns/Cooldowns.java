package com.github.groundbreakingmc.hlteleports.cooldowns;

import com.github.groundbreakingmc.hlteleports.Teleports;
import com.github.groundbreakingmc.hlteleports.config.ConfigValues;
import com.github.groundbreakingmc.mylib.collections.expiring.SelfExpiringMap;
import com.github.groundbreakingmc.mylib.utils.time.TimeFormatterUtil;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public final class Cooldowns {

    private final ConfigValues configValues;
    private final SelfExpiringMap<UUID, Long> cooldowns;

    public Cooldowns(Teleports plugin) {
        this.configValues = plugin.getConfigValues();
        this.cooldowns = new SelfExpiringMap<>(TimeUnit.MILLISECONDS);
    }

    public boolean check(final Player player, final long duration) {
        final Long cooldown = this.get(player);
        if (cooldown != null) {
            final long timeLeftInMillis = cooldown - System.currentTimeMillis();
            final int result = (int) (duration / 1000 + timeLeftInMillis / 1000);
            final String restTime = TimeFormatterUtil.getTime(result, this.configValues.getTimeValues());
            player.sendMessage(this.configValues.getCooldownMessage().replace("{time}", restTime));
            return true;
        }

        return false;
    }

    public void add(final Player player, final int duration) {
        this.cooldowns.put(player.getUniqueId(), System.currentTimeMillis(), duration);
    }

    @Nullable
    public Long get(final Player player) {
        return this.cooldowns.get(player.getUniqueId());
    }
}
