package org.moChiThirst.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.moChiThirst.managers.ThirstManager;
import org.moChiThirst.utils.Color;

import java.util.ArrayList;
import java.util.List;

public class ThirstEditCommand implements SubCommand {

    @Override
    public String getName() { return "set"; }

    @Override
    public String getPermission() { return "mochithirst.set"; }

    @Override
    public void execute(CommandSender sender, String[] args) {
        try {
            ThirstManager.setThirst(Bukkit.getPlayer(args[1]), Integer.parseInt(args[2]));
        } catch (NumberFormatException e) {
            sender.sendMessage(Color.translate("&cUsage: /thirst set <player> <amount>"));
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {

        if (args.length == 2) {
            List<String> completions = new ArrayList<>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                completions.add(player.getName());
            }
            return completions;
        }

        if (args.length == 3) {
            return List.of("number");
        }

        return null;
    }
}
