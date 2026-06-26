package org.moChiThirst.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.bukkit.plugin.java.JavaPlugin;
import org.moChiThirst.data.PlayerData;

import java.io.*;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

public class DataManager {

    private static final String FILE_NAME = "data.json";
    private static final Type   DATA_TYPE = new TypeToken<Map<UUID, PlayerData>>() {}.getType();

    private final File   file;
    private final Gson   gson;
    private final Logger logger;

    private Map<UUID, PlayerData> dataMap = new HashMap<>();

    public DataManager(JavaPlugin plugin) {
        this.logger = plugin.getLogger();
        this.gson   = new GsonBuilder().setPrettyPrinting().create();
        this.file   = new File(plugin.getDataFolder(), FILE_NAME);

        plugin.getDataFolder().mkdirs();
        load();
    }

    // -------------------------------------------------------------------------
    // Load / Save
    // -------------------------------------------------------------------------

    public void load() {
        if (!file.exists()) return;

        try (Reader reader = new FileReader(file)) {
            Map<UUID, PlayerData> loaded = gson.fromJson(reader, DATA_TYPE);
            if (loaded != null) dataMap = loaded;
        } catch (IOException e) {
            logger.severe("Không thể đọc " + FILE_NAME + ": " + e.getMessage());
        }
    }

    public void save() {
        try (Writer writer = new FileWriter(file)) {
            gson.toJson(dataMap, writer);
        } catch (IOException e) {
            logger.severe("Không thể lưu " + FILE_NAME + ": " + e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // CRUD
    // -------------------------------------------------------------------------

    public void set(UUID uuid, int thirstLevel, long lastUpdated) {
        dataMap.put(uuid, new PlayerData(thirstLevel, lastUpdated));
    }

    public PlayerData get(UUID uuid) {
        return dataMap.get(uuid);
    }

    public void remove(UUID uuid) {
        dataMap.remove(uuid);
    }
}
