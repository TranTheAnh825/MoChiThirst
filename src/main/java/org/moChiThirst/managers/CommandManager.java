package org.moChiThirst.managers;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.java.JavaPlugin;
import org.moChiThirst.commands.SubCommand;
import org.moChiThirst.utils.Color;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandManager implements CommandExecutor, TabCompleter {

    private final Map<String, SubCommand> subCommands = new HashMap<>();

    public CommandManager(JavaPlugin plugin, String command) {
        plugin.getCommand(command).setExecutor(this);
        plugin.getCommand(command).setTabCompleter(this);
    }

    public void register(SubCommand subCommand) {
        subCommands.put(subCommand.getName().toLowerCase(), subCommand);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String prefix = ConfigManager.getPrefix() + " ";

        // Hiện danh sách subcommand nếu không có args
        if (args.length == 0) {
            sender.sendMessage(Color.translate(prefix + "&7Các lệnh có sẵn: &f" + String.join(", ", subCommands.keySet())));
            return true;
        }

        SubCommand sub = subCommands.get(args[0].toLowerCase());

        if (sub == null) {
            sender.sendMessage(Color.translate(prefix + "&cLệnh không tồn tại. Dùng /thirst để xem danh sách."));
            return true;
        }

        if (sub.getPermission() != null && !sender.hasPermission(sub.getPermission())) {
            String noPermMsg = ConfigManager.getNoPermissionMessage();
            sender.sendMessage(Color.translate(prefix + (noPermMsg != null ? noPermMsg : "&cBạn không có quyền!")));
            return true;
        }

        sub.execute(sender, args);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            for (Map.Entry<String, SubCommand> entry : subCommands.entrySet()) {
                if (entry.getKey().startsWith(args[0].toLowerCase())) {
                    SubCommand sub = entry.getValue();
                    if (sub.getPermission() == null || sender.hasPermission(sub.getPermission())) {
                        completions.add(entry.getKey());
                    }
                }
            }
        } else if (args.length > 1) {
            SubCommand sub = subCommands.get(args[0].toLowerCase());
            if (sub != null && (sub.getPermission() == null || sender.hasPermission(sub.getPermission()))) {
                List<String> subCompletions = sub.onTabComplete(sender, args);
                if (subCompletions != null) completions.addAll(subCompletions);
            }
        }

        return completions;
    }
}
