package org.moChiThirst.utils;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.moChiThirst.managers.ThirstManager;

/**
 * %thirst_current% -> độ khát hiện tại  | vd: 10
 * %thirst_max%     -> độ khát tối đa    | vd: 20
 * %thirst_percent% -> phần trăm độ khát | vd: 50 [(10 / 20) * 100]
 */

public class ThirstPlaceholder extends PlaceholderExpansion {
    @Override
    public @NotNull String getIdentifier() { return "thirst"; }

    @Override
    public @NotNull String getAuthor() { return "MoChiMC"; }

    @Override
    public @NotNull String getVersion() { return "1.0.0"; }

    @Override
    public boolean persist() { return true; }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) return "";

        int current    = ThirstManager.getCurrentThirst(player);
        int max        = ThirstManager.getMaxThirst(player);
        int percentage = ThirstManager.getThirstPercentage(player);

        return switch (params.toLowerCase()) {
            case "current" -> String.valueOf(current);
            case "max"     -> String.valueOf(max);
            case "percent" -> String.valueOf(percentage);
            default        -> null;
        };
    }
}
