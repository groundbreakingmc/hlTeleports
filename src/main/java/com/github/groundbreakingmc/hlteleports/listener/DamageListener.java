package com.github.groundbreakingmc.hlteleports.listener;

import com.github.groundbreakingmc.hlteleports.Teleports;
import com.github.groundbreakingmc.hlteleports.utils.config.ConfigValues;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public final class DamageListener implements Listener {

    private final ConfigValues configValues;

    public DamageListener(Teleports plugin) {
        this.configValues = plugin.getConfigValues();
    }

    @EventHandler(ignoreCancelled = true)
    public void onDamage(final EntityDamageEvent event) {
        if (event.getEntity() instanceof final Player damager) {
            if (!Teleports.cancelTeleportation(damager)) {
                return;
            }

            damager.sendMessage(this.configValues.getDamagedMessage());
        }
    }
}
