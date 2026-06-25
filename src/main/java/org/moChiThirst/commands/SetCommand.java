package org.moChiThirst.commands;

import org.bukkit.command.CommandSender;

import java.util.List;

public class SetCommand implements SubCommand{
    @Override
    public String getName() { return "set"; }

    @Override
    public String getPermission() { return "mochithirst.set"; }

    @Override
    public void execute(CommandSender sender, String[] args) {

    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
