package org.moChiThirst.managers;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ThirstManager {

    public  static final Map<UUID, Integer> thirstMap  = new HashMap<>();
    public  static final Map<UUID, Long>    timeMap    = new HashMap<>();
    private static final String             CAPACITY   = "thirst.capacity";
    private static       int                defaultMax;

    public static void reload() { defaultMax = getDefaultMax(); }

    private void saveData(Player player) {
        FileConfiguration data = ConfigManager.get("data");
        String uuid = player.getUniqueId().toString();

        data.set(uuid + ".thirst", thirstMap.get(player.getUniqueId()));
        data.set(uuid + ".time", timeMap.get(player.getUniqueId()));
        ConfigManager.save("data");
    }

    private void loadData(Player player, String path) {
        FileConfiguration data = ConfigManager.get("data");
        UUID uuid = player.getUniqueId();
        String uuidStr = uuid.toString();

        if (data.contains(uuid + ".thirst")) {
            thirstMap.put(uuid, (data.getInt(uuidStr + "." + path)));
        } else {
            thirstMap.put(uuid, defaultMax);
        }

        if (data.contains(uuid + ".time")) {
            timeMap.put(uuid, (data.getLong(uuidStr + ".time")));
        } else {
            timeMap.put(uuid, System.currentTimeMillis());
        }
    }

    public static void increaseThirst(Player player, int amount) {
        int increase = getCurrentThirst(player) + amount;
        int max      = getMaxThirst(player);
        thirstMap.put(player.getUniqueId(), Math.min(increase, max));
    }

    public static void decreaseThirst(Player player, int amount) {
        int decrease = getCurrentThirst(player) - amount;
        thirstMap.put(player.getUniqueId(), Math.max(decrease, 0));
    }

    public static void setThirst(Player player, int amount) {
        thirstMap.put(player.getUniqueId(), Math.max(getMaxThirst(player), amount));
    }

    public static void resetThirst(Player player) {
        thirstMap.put(player.getUniqueId(), getMaxThirst(player));
    }

    public static int getMaxThirst(Player player) {
        int max = defaultMax;

        for (PermissionAttachmentInfo info : player.getEffectivePermissions()) {
            if (!info.getValue()) continue;

            String perm = info.getPermission();
            if (!perm.startsWith(CAPACITY)) continue;

            int value = Integer.parseInt(perm.substring(CAPACITY.length() + 1));
            if (value > defaultMax) max = value;
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

    private static Integer getDefaultMax() { return ConfigManager.get("config").getInt("thirst.max"); }
}
