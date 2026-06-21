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

    private final JavaPlugin plugin;
    private final Map<String, SubCommand> subCommands = new HashMap<>();
    private String noPermissionMessage = ConfigManager.getNoPermissionMessage();
    private String unknownCommandMessage;

    public CommandManager(JavaPlugin plugin, String command) {
        this.plugin = plugin;
        plugin.getCommand(command).setExecutor(this);
        plugin.getCommand(command).setTabCompleter(this);
    }

    public void register(SubCommand subCommand) {
        subCommands.put(subCommand.getName().toLowerCase(), subCommand);
    }

    public void setUnknownCommandMessage(String message) {
        this.unknownCommandMessage = message;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (unknownCommandMessage != null)
                sender.sendMessage(unknownCommandMessage);
            return true;
        }

        SubCommand sub = subCommands.get(args[0].toLowerCase());

        if (sub == null) {
            if (unknownCommandMessage != null)
                sender.sendMessage(unknownCommandMessage);
            return true;
        }

        String prefix = ConfigManager.getPrefix() + " ";
        if (sub.getPermission() != null && !sender.hasPermission(sub.getPermission())) {
            if (noPermissionMessage != null)
                sender.sendMessage(Color.translate(prefix + noPermissionMessage));
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
            if (sub != null) {
                List<String> subCompletions = sub.onTabComplete(sender, args);
                if (subCompletions != null) completions.addAll(subCompletions);
            }
        }

        return completions;
    }
}