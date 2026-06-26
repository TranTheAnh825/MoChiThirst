package org.moChiThirst.listener;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Levelled;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionType;
import org.moChiThirst.managers.ThirstManager;

public class ThirstListener implements Listener {

    private final JavaPlugin    plugin;
    private final ThirstManager thirstManager;

    public ThirstListener(JavaPlugin plugin, ThirstManager thirstManager) {
        this.plugin        = plugin;
        this.thirstManager = thirstManager;
    }

    // -------------------------------------------------------------------------
    // Join / Quit
    // -------------------------------------------------------------------------

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(PlayerJoinEvent event) {
        thirstManager.handleJoin(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerQuit(PlayerQuitEvent event) {
        thirstManager.handleQuit(event.getPlayer());
    }

    // -------------------------------------------------------------------------
    // Uống nước từ water block / cauldron (right-click)
    // -------------------------------------------------------------------------

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Chỉ xử lý main hand để tránh trigger 2 lần
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Block block = event.getClickedBlock();
        if (block == null) return;

        Player player = event.getPlayer();
        if (player.hasPermission("mochithirst.bypass")) return;

        Material type = block.getType();

        // Water block (source hoặc flowing)
        if (type == Material.WATER) {
            thirstManager.applyDrink(player, thirstManager.getWaterConfig());
            return;
        }

        // Cauldron có nước: WATER_CAULDRON, kiểm tra level > 0
        if (type == Material.WATER_CAULDRON) {
            Levelled levelled = (Levelled) block.getBlockData();
            if (levelled.getLevel() > 0) {
                thirstManager.applyDrink(player, thirstManager.getWaterConfig());
            }
        }
    }

    // -------------------------------------------------------------------------
    // Uống water bottle / potion (consume item)
    // -------------------------------------------------------------------------

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerConsume(PlayerItemConsumeEvent event) {
        Player    player = event.getPlayer();
        ItemStack item   = event.getItem();

        if (player.hasPermission("mochithirst.bypass")) return;
        if (item == null) return;

        Material mat = item.getType();

        if (mat == Material.MILK_BUCKET) {
            thirstManager.applyDrink(player, thirstManager.getWaterConfig());
        }

        if (mat == Material.POTION) {
            PotionMeta meta = (PotionMeta) item.getItemMeta();
            // Water bottle = WATER type
            if (meta != null && meta.getBasePotionType() == PotionType.WATER) {
                thirstManager.applyDrink(player, thirstManager.getWaterConfig());
            } else {
                thirstManager.applyDrink(player, thirstManager.getPotionConfig());
            }
        }
    }
}
