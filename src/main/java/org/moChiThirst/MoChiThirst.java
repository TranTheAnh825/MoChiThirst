package org.moChiThirst;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.moChiThirst.commands.ReloadCommand;
import org.moChiThirst.commands.ThirstEditCommand;
import org.moChiThirst.managers.CommandManager;
import org.moChiThirst.managers.ConfigManager;
import org.moChiThirst.managers.ThirstManager;
import org.moChiThirst.utils.ThirstPlaceholder;

public final class MoChiThirst extends JavaPlugin {

    @Override
    public void onEnable() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI").isEnabled()) { new ThirstPlaceholder().register(); }

        ConfigManager.setup(this);

        CommandManager commandManager = new CommandManager(this, "thirst");
        commandManager.register(new ReloadCommand());
        commandManager.register(new ThirstEditCommand());

        ThirstManager.reloadUUID();
    }

    @Override
    public void onDisable() {
        ConfigManager.saveAll();
    }
}
