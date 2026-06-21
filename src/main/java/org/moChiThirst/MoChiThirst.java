package org.moChiThirst;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.moChiThirst.commands.ReloadCommand;
import org.moChiThirst.commands.SetThirstCommand;
import org.moChiThirst.managers.CommandManager;
import org.moChiThirst.managers.ConfigManager;
import org.moChiThirst.managers.ThirstManager;
import org.moChiThirst.utils.ThirstPlaceholder;

public final class MoChiThirst extends JavaPlugin {

    @Override
    public void onEnable() {
        ConfigManager.setup(this);

        CommandManager commandManager = new CommandManager(this, "thirst");
        commandManager.register(new ReloadCommand());
        commandManager.register(new SetThirstCommand());

        ThirstManager.reloadUUID();

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI").isEnabled()) {
            new ThirstPlaceholder().register();
        }
    }

    @Override
    public void onDisable() {
        ConfigManager.saveAll();
    }
}
