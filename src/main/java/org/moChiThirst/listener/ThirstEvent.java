package org.moChiThirst.listener;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.moChiThirst.managers.ConfigManager;
import org.moChiThirst.managers.ThirstManager;

import java.util.UUID;

import static org.moChiThirst.managers.ThirstManager.*;

public class ThirstEvent implements Listener {
    private static BukkitTask task;
    private static int        interval = ConfigManager.get("config").getInt("thirst.interval");
    private static int        damage   = ConfigManager.get("config").getInt("damage.amount");

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID   uuid   = player.getUniqueId();

        thirstMap.put(uuid, getMaxThirst(player));
    }

    private void start(Plugin plugin) {
        task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.hasPermission("thirst.bypass")) continue;

                UUID uuid = player.getUniqueId();
                long last = ThirstManager.timeMap.getOrDefault(player.getUniqueId(), 0L);
                long now  = System.currentTimeMillis();

                // add default
                if (timeMap.get(uuid) == 0L) {
                    timeMap.put(uuid, now);
                    continue;
                }

                // time check
                if (last <= now) {
                    timeMap.put(uuid, now);
                    ThirstManager.decreaseThirst(player, 1);
                }

                //if (thirstMap.get(uuid) <= 0) player.damage(damage);
            }
        },0, interval);

    }
}
