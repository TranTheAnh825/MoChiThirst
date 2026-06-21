package org.moChiThirst.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.moChiThirst.managers.ConfigManager;

import java.util.UUID;

import static org.moChiThirst.managers.ThirstManager.getMaxThirst;
import static org.moChiThirst.managers.ThirstManager.thirstMap;

public class ThirstEvent implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = event.getPlayer().getUniqueId();

        thirstMap.put(uuid, getMaxThirst(player));
    }
}
