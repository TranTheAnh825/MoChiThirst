package org.moChiThirst;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.moChiThirst.commands.ReloadCommand;
import org.moChiThirst.commands.SetCommand;
import org.moChiThirst.commands.SetMaxCommand;
import org.moChiThirst.listener.ThirstListener;
import org.moChiThirst.managers.CommandManager;
import org.moChiThirst.managers.ConfigManager;
import org.moChiThirst.managers.ThirstManager;
import org.moChiThirst.utils.ThirstPlaceholder;

public final class MoChiThirst extends JavaPlugin {

    private static MoChiThirst instance;
    private ThirstManager thirstManager;
    private CommandManager commandManager;

    @Override
    public void onEnable() {
        instance = this;

        ConfigManager.setup(this);
        thirstManager = new ThirstManager(this);

        // Register PlaceholderAPI expansion nếu có
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new ThirstPlaceholder(thirstManager).register();
            getLogger().info("PlaceholderAPI expansion đã được đăng ký.");
        }

        // Đăng ký listener
        Bukkit.getPluginManager().registerEvents(new ThirstListener(this, thirstManager), this);

        // Đăng ký commands
        commandManager = new CommandManager(this, "thirst");
        commandManager.register(new ReloadCommand(thirstManager));
        commandManager.register(new SetCommand(thirstManager));
        commandManager.register(new SetMaxCommand(thirstManager));

        // Load dữ liệu cho player đang online (trường hợp reload)
        thirstManager.loadOnlinePlayers();

        getLogger().info("MoChiThirst đã được bật!");
    }

    @Override
    public void onDisable() {
        if (thirstManager != null) {
            thirstManager.saveAll();
        }
        getLogger().info("MoChiThirst đã được tắt!");
    }

    public static MoChiThirst getInstance() {
        return instance;
    }

    public ThirstManager getThirstManager() {
        return thirstManager;
    }
}
