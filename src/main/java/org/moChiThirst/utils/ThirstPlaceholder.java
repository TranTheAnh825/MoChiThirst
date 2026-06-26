package org.moChiThirst.utils;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.moChiThirst.managers.ThirstManager;

/**
 * %thirst_current%      → độ khát hiện tại  (vd: 10)
 * %thirst_max%          → độ khát tối đa    (vd: 20)
 * %thirst_percent%      → phần trăm độ khát (vd: 50)
 * %thirst_time%         → ms còn lại đến lần giảm tiếp theo (debug)
 * %thirst_interval%     → effective interval tính bằng ms (debug)
 */
public class ThirstPlaceholder extends PlaceholderExpansion {

    private final ThirstManager thirstManager;

    public ThirstPlaceholder(ThirstManager thirstManager) {
        this.thirstManager = thirstManager;
    }

    @Override public @NotNull String getIdentifier() { return "thirst"; }
    @Override public @NotNull String getAuthor()     { return "MoChiMC"; }
    @Override public @NotNull String getVersion()    { return "1.0"; }
    @Override public boolean persist()               { return true; }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) return "";

        return switch (params.toLowerCase()) {
            case "current"   -> String.valueOf(thirstManager.getCurrentThirst(player));
            case "max"       -> String.valueOf(thirstManager.getMaxThirst(player));
            case "percent"   -> String.valueOf(thirstManager.getThirstPercentage(player));
            case "interval"  -> thirstManager.getEffectiveIntervalMs(player) + "ms";
            case "time"      -> thirstManager.getTimeUntilNextDecrease(player) + "ms";
            default          -> null;
        };
    }
}
