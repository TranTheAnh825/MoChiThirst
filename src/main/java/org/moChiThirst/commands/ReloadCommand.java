package org.moChiThirst.commands;

import org.bukkit.command.CommandSender;
import org.moChiThirst.managers.ConfigManager;
import org.moChiThirst.managers.ThirstManager;
import org.moChiThirst.utils.Color;

import java.util.List;

public class ReloadCommand implements SubCommand {

    private final ThirstManager thirstManager;

    public ReloadCommand(ThirstManager thirstManager) {
        this.thirstManager = thirstManager;
    }

    @Override
    public String getName() { return "reload"; }

    @Override
    public String getPermission() { return "mochithirst.reload"; }

    @Override
    public void execute(CommandSender sender, String[] args) {
        long start = System.currentTimeMillis();

        ConfigManager.reloadAll();
        thirstManager.reload();

        long time = System.currentTimeMillis() - start;

        String prefix = ConfigManager.getPrefix() + " ";
        String template = ConfigManager.get("messages").getString("reload", "&aĐã reload xong trong &f{time}ms&a!");
        sender.sendMessage(Color.translate(prefix + template.replace("{time}", String.valueOf(time))));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
