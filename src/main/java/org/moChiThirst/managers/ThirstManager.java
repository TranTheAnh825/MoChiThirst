package org.moChiThirst.managers;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ThirstManager {

    public static final Map<UUID, Integer> thirstMap = new HashMap<>();
    private static final String CAPACITY = "thirst.capacity";
    private static int max = getMax();

    public static void increaseThirst(Player player, int amount) {

    }
    public static void decreaseThirst(Player player, int amount) {

    }

    public static void resetThirst(Player player) {

    }

    public static int getMaxThirst(Player player) {

        for (PermissionAttachmentInfo info : player.getEffectivePermissions()) {
            if (!info.getValue()) continue;

            String perm = info.getPermission();
            if (!perm.startsWith(CAPACITY)) continue;

            int value = Integer.parseInt(perm.substring(CAPACITY.length() + 1));
            if (value > max) max = value;

            break;
        }

        return max;
    }

    public static int getCurrentThirst(Player player) {
        return thirstMap.getOrDefault(player.getUniqueId(), getMaxThirst(player));
    }

    public static int getThirstPercentage(Player player) {
        int max = getMaxThirst(player);
        int current = getCurrentThirst(player);
        return max > 0 ? (int) ((current / (double) max) * 100) : 0;
    }

    public static void reloadUUID() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!thirstMap.containsKey(player.getUniqueId())) {
                thirstMap.put(player.getUniqueId(), getMaxThirst(player));
            }
        }
    }

    public static Integer getMax() {
        return ConfigManager.get("config").getInt("thirstMax");
    }
}
