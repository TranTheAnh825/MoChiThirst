package org.moChiThirst.managers;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.block.Biome;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.moChiThirst.data.PlayerData;

import java.util.*;
import java.util.logging.Logger;

public class ThirstManager {

    private static final String CAPACITY_PREFIX = "thirst.capacity.";

    private final JavaPlugin  plugin;
    private final Logger      logger;
    private final DataManager dataManager;

    private final Map<UUID, Integer> thirstMap    = new HashMap<>();
    private final Map<UUID, Long>    timeMap      = new HashMap<>();
    private final Map<UUID, Integer> maxThirstMap = new HashMap<>();

    // Config values
    private int defaultMax;
    private int thirstInterval;
    private int damageAmount;
    private int damageInterval;
    private int thirstEffectLevel;

    private Map<Biome, Double> hotBiomes     = new HashMap<>();
    private List<PotionEffect> thirstEffects = new ArrayList<>();
    private DrinkConfig        waterConfig;
    private DrinkConfig        potionConfig;

    private BukkitTask thirstTask;
    private BukkitTask damageTask;

    // -------------------------------------------------------------------------
    // Inner: DrinkConfig
    // -------------------------------------------------------------------------

    public static class DrinkConfig {
        public final int recovery;
        public final List<EffectEntry> effects;

        public DrinkConfig(int recovery, List<EffectEntry> effects) {
            this.recovery = recovery;
            this.effects        = effects;
        }
    }

    public static class EffectEntry {
        public final PotionEffectType type;
        public final int              amplifier;
        public final int              duration;
        public final double           chance;

        public EffectEntry(PotionEffectType type, int amplifier, int duration, double chance) {
            this.type      = type;
            this.amplifier = amplifier;
            this.duration  = duration;
            this.chance    = chance;
        }
    }

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    public ThirstManager(JavaPlugin plugin) {
        this.plugin      = plugin;
        this.logger      = plugin.getLogger();
        this.dataManager = new DataManager(plugin);
        loadConfig();
        startTimers();
    }

    // -------------------------------------------------------------------------
    // Config
    // -------------------------------------------------------------------------

    public void loadConfig() {
        FileConfiguration cfg = ConfigManager.get("config");

        defaultMax      = cfg.getInt("thirst.max", 20);
        thirstInterval  = cfg.getInt("thirst.interval", 600);
        damageAmount    = cfg.getInt("damage.amount", 1);
        damageInterval  = cfg.getInt("damage.interval", 100);
        thirstEffectLevel = cfg.getInt("thirstEffect.thirstLevel", 5);

        // Hot biomes
        hotBiomes.clear();
        for (String entry : cfg.getStringList("hotBiome")) {
            String[] parts = entry.trim().split("\\s+");
            if (parts.length < 2) continue;
            try {
                Biome  biome      = Biome.valueOf(parts[0].toUpperCase());
                double multiplier = Double.parseDouble(parts[1]);
                hotBiomes.put(biome, multiplier);
            } catch (Exception e) {
                logger.warning("hotBiome không hợp lệ: " + entry);
            }
        }

        // Thirst effects
        thirstEffects.clear();
        for (String entry : cfg.getStringList("thirstEffect.effects")) {
            PotionEffect effect = parseSimpleEffect(entry);
            if (effect != null) thirstEffects.add(effect);
        }

        // Drink configs
        waterConfig  = parseDrinkConfig(cfg, "drink.water");
        potionConfig = parseDrinkConfig(cfg, "drink.potion");
    }

    private DrinkConfig parseDrinkConfig(FileConfiguration cfg, String path) {
        int recovery = cfg.getInt(path + ".recovery", 3);
        List<EffectEntry> entries = new ArrayList<>();

        for (String line : cfg.getStringList(path + ".effects")) {
            String[] parts = line.trim().split("\\s+");
            if (parts.length < 4) continue;
            try {
                PotionEffectType type      = PotionEffectType.getByName(parts[0].toUpperCase());
                int              amplifier = Integer.parseInt(parts[1]);
                int              duration  = Integer.parseInt(parts[2]);
                double           chance    = Double.parseDouble(parts[3]);
                if (type != null) entries.add(new EffectEntry(type, amplifier, duration, chance));
                else logger.warning("Hiệu ứng không tồn tại: " + parts[0]);
            } catch (Exception e) {
                logger.warning("Effect entry không hợp lệ: " + line);
            }
        }
        return new DrinkConfig(recovery, entries);
    }

    private PotionEffect parseSimpleEffect(String line) {
        String[] parts = line.trim().split("\\s+");
        if (parts.length < 2) return null;
        try {
            PotionEffectType type      = PotionEffectType.getByName(parts[0].toUpperCase());
            int              amplifier = Integer.parseInt(parts[1]);
            if (type == null) { logger.warning("Hiệu ứng không tồn tại: " + parts[0]); return null; }
            return new PotionEffect(type, Integer.MAX_VALUE, amplifier, true, false, false);
        } catch (Exception e) {
            logger.warning("thirstEffect entry không hợp lệ: " + line);
            return null;
        }
    }

    public void reload() {
        loadConfig();
        stopTimers();
        startTimers();
        // Xóa effect cũ cho toàn bộ player online
        for (Player p : Bukkit.getOnlinePlayers()) removeThirstEffects(p);
    }

    // -------------------------------------------------------------------------
    // Timers
    // -------------------------------------------------------------------------

    private void startTimers() {
        // Timer giảm khát — mỗi thirstInterval ticks chạy 1 lần, nhưng check biome multiplier
        thirstTask = Bukkit.getScheduler().runTaskTimer(plugin, this::tickThirst, 0L, 1L);
        // Timer damage khi thirst = 0
        damageTask = Bukkit.getScheduler().runTaskTimer(plugin, this::tickDamage, 0L, damageInterval);
    }

    private void stopTimers() {
        if (thirstTask != null && !thirstTask.isCancelled()) thirstTask.cancel();
        if (damageTask != null && !damageTask.isCancelled()) damageTask.cancel();
    }

    /**
     * Chạy mỗi tick, nhưng mỗi player có interval riêng tùy biome.
     * Dùng timeMap để track lần cuối giảm khát (ms).
     */
    private void tickThirst() {
        long now = System.currentTimeMillis();

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission("mochithirst.bypass")
                    || player.getGameMode().equals(GameMode.CREATIVE)
                    || player.getGameMode().equals(GameMode.SPECTATOR)) continue;

            UUID uuid = player.getUniqueId();

            // Default = 0L để lần đầu tiên luôn trigger ngay
            long   last        = timeMap.getOrDefault(uuid, 0L);
            double multiplier  = getBiomeMultiplier(player);
            long   effectiveMs = (long) ((thirstInterval / 20.0) * 1000.0 / multiplier);

            if (now - last >= effectiveMs) {
                timeMap.put(uuid, now);
                decreaseThirst(player, 1);
            }

            applyThirstEffects(player);
        }
    }

    private void tickDamage() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission("mochithirst.bypass")) continue;
            if (getCurrentThirst(player) <= 0) {
                player.damage(damageAmount);
            }
        }
    }

    // -------------------------------------------------------------------------
    // Biome
    // -------------------------------------------------------------------------

    private double getBiomeMultiplier(Player player) {
        Biome biome = player.getLocation().getBlock().getBiome();
        return hotBiomes.getOrDefault(biome, 1.0);
    }

    // -------------------------------------------------------------------------
    // Thirst effects
    // -------------------------------------------------------------------------

    private void applyThirstEffects(Player player) {
        if (thirstEffects.isEmpty()) return;

        if (getCurrentThirst(player) <= thirstEffectLevel) {
            for (PotionEffect effect : thirstEffects) {
                if (!player.hasPotionEffect(effect.getType())) {
                    player.addPotionEffect(effect);
                }
            }
        } else {
            removeThirstEffects(player);
        }
    }

    private void removeThirstEffects(Player player) {
        for (PotionEffect effect : thirstEffects) {
            player.removePotionEffect(effect.getType());
        }
    }

    // -------------------------------------------------------------------------
    // Drink
    // -------------------------------------------------------------------------

    public void applyDrink(Player player, DrinkConfig config) {
        increaseThirst(player, config.recovery);

        Random random = new Random();
        for (EffectEntry entry : config.effects) {
            if (random.nextDouble() < entry.chance) {
                player.addPotionEffect(new PotionEffect(entry.type, entry.duration, entry.amplifier));
            }
        }
    }

    public DrinkConfig getWaterConfig()  { return waterConfig; }
    public DrinkConfig getPotionConfig() { return potionConfig; }

    // -------------------------------------------------------------------------
    // Player join / quit
    // -------------------------------------------------------------------------

    public void handleJoin(Player player) {
        UUID       uuid = player.getUniqueId();
        PlayerData data = dataManager.get(uuid);

        if (data != null) {
            thirstMap.put(uuid, Math.min(data.getThirstLevel(), getMaxThirst(player)));
            timeMap.put(uuid, data.getLastUpdated());
        } else {
            thirstMap.put(uuid, getMaxThirst(player));
            timeMap.put(uuid, System.currentTimeMillis());
        }
    }

    public void handleQuit(Player player) {
        UUID uuid = player.getUniqueId();
        removeThirstEffects(player);
        persist(uuid);
        thirstMap.remove(uuid);
        timeMap.remove(uuid);
    }

    // -------------------------------------------------------------------------
    // Persistence
    // -------------------------------------------------------------------------

    private void persist(UUID uuid) {
        dataManager.set(
                uuid,
                thirstMap.getOrDefault(uuid, defaultMax),
                timeMap.getOrDefault(uuid, System.currentTimeMillis())
        );
        dataManager.save();
    }

    public void saveAll() {
        for (UUID uuid : thirstMap.keySet()) {
            dataManager.set(
                    uuid,
                    thirstMap.getOrDefault(uuid, defaultMax),
                    timeMap.getOrDefault(uuid, System.currentTimeMillis())
            );
        }
        dataManager.save();
        stopTimers();
    }

    public void loadOnlinePlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!thirstMap.containsKey(player.getUniqueId())) handleJoin(player);
        }
    }

    // -------------------------------------------------------------------------
    // Thirst operations
    // -------------------------------------------------------------------------

    public void increaseThirst(Player player, int amount) {
        thirstMap.put(player.getUniqueId(), Math.min(getCurrentThirst(player) + amount, getMaxThirst(player)));
    }

    public void decreaseThirst(Player player, int amount) {
        thirstMap.put(player.getUniqueId(), Math.max(getCurrentThirst(player) - amount, 0));
    }

    public void setThirst(Player player, int amount) {
        int clamped = Math.max(0, Math.min(amount, getMaxThirst(player)));
        thirstMap.put(player.getUniqueId(), clamped);
        // Reset timer để bắt đầu đếm lại từ thời điểm set
        timeMap.put(player.getUniqueId(), System.currentTimeMillis());
    }

    public void resetThirst(Player player) {
        thirstMap.put(player.getUniqueId(), getMaxThirst(player));
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    public int getMaxThirst(Player player) {
        UUID uuid = player.getUniqueId();

        // Override từ lệnh maxset
        if (maxThirstMap.containsKey(uuid)) {
            return maxThirstMap.get(uuid);
        }

        // Override từ permission node (thirst.capacity.<value>)
        int max = defaultMax;
        for (PermissionAttachmentInfo info : player.getEffectivePermissions()) {
            if (!info.getValue()) continue;
            String perm = info.getPermission();
            if (!perm.startsWith(CAPACITY_PREFIX)) continue;
            try {
                int value = Integer.parseInt(perm.substring(CAPACITY_PREFIX.length()));
                if (value > max) max = value;
            } catch (NumberFormatException ignored) {}
        }
        return max;
    }

    public void setMaxThirst(Player player, int amount) {
        UUID uuid = player.getUniqueId();
        maxThirstMap.put(uuid, amount);
        // Clamp current thirst nếu vượt max mới
        int current = getCurrentThirst(player);
        if (current > amount) thirstMap.put(uuid, amount);
    }

    public int getCurrentThirst(Player player) {
        return thirstMap.getOrDefault(player.getUniqueId(), defaultMax);
    }

    public int getThirstPercentage(Player player) {
        int max = getMaxThirst(player);
        if (max <= 0) return 0;
        return (int) ((getCurrentThirst(player) / (double) max) * 100);
    }

    /** Effective interval tính bằng ms cho player (có tính biome multiplier) — dùng để debug */
    public long getEffectiveIntervalMs(Player player) {
        double multiplier = getBiomeMultiplier(player);
        return (long) ((thirstInterval / 20.0) * 1000.0 / multiplier);
    }

    /** Ms còn lại đến lần giảm khát tiếp theo — dùng để debug */
    public long getTimeUntilNextDecrease(Player player) {
        long last        = timeMap.getOrDefault(player.getUniqueId(), 0L);
        long elapsed     = System.currentTimeMillis() - last;
        long remaining   = getEffectiveIntervalMs(player) - elapsed;
        return Math.max(0, remaining);
    }
}
