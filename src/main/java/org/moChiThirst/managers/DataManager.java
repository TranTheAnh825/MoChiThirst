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

public class DataManager {

    private final File file;
    private final Gson gson;
    private Map<UUID, PlayerData> dataMap;

    public DataManager(JavaPlugin plugin) {
        this.file = new File(plugin.getDataFolder(), "data.json");
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.dataMap = new HashMap<>();

        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        loadData();
    }

    public void saveData() {
        try (Writer writer = new FileWriter(file)) {
            gson.toJson(dataMap, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadData() {
        if (!file.exists()) return;

        try (Reader reader = new FileReader(file)) {
            Type type = new TypeToken<Map<UUID, PlayerData>>(){}.getType();
            Map<UUID, PlayerData> loadedData = gson.fromJson(reader, type);
            if (loadedData != null) {
                this.dataMap = loadedData;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setPlayerData(UUID uuid, int thirstLevel, double time) {
        dataMap.put(uuid, new PlayerData(thirstLevel, time));
    }

    public PlayerData getPlayerData(UUID uuid) {
        return dataMap.get(uuid);
    }

    public Map<UUID, PlayerData> getDataMap() {
        return dataMap;
    }
}
