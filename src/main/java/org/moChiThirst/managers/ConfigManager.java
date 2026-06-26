package org.moChiThirst.managers;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ConfigManager {

    private static final Map<String, File>              files   = new HashMap<>();
    private static final Map<String, FileConfiguration> configs = new HashMap<>();

    private static Plugin plugin;

    public static void setup(Plugin p) {
        plugin = p;
        register("config",   "config.yml");
        register("messages", "messages.yml");
    }

    public static void register(String key, String fileName) {
        File file = new File(plugin.getDataFolder(), fileName);

        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        if (!file.exists()) {
            if (plugin.getResource(fileName) != null) {
                plugin.saveResource(fileName, false);
            } else {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    plugin.getLogger().severe("Không thể tạo file: " + fileName);
                }
            }
        }

        files.put(key, file);
        configs.put(key, YamlConfiguration.loadConfiguration(file));
    }

    public static FileConfiguration get(String key) {
        FileConfiguration config = configs.get(key);
        if (config == null) {
            throw new IllegalArgumentException("Không tìm thấy config: '" + key + "'. Hãy register trước.");
        }
        return config;
    }

    public static void save(String key) {
        File file = files.get(key);
        FileConfiguration config = configs.get(key);
        if (file == null || config == null) return;
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Không thể lưu config: " + key);
        }
    }

    public static void saveAll() {
        for (String key : files.keySet()) {
            save(key);
        }
    }

    public static void reload(String key) {
        File file = files.get(key);
        if (file == null) return;
        configs.put(key, YamlConfiguration.loadConfiguration(file));
    }

    public static void reloadAll() {
        for (String key : files.keySet()) {
            reload(key);
        }
    }

    public static String getPrefix() {
        String prefix = get("messages").getString("prefix");
        return prefix != null ? prefix : "&8[&bMoChiThirst&8]";
    }

    public static String getNoPermissionMessage() {
        return get("messages").getString("noPerm", "&cBạn không có quyền thực hiện lệnh này!");
    }
}
