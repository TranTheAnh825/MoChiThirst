package org.moChiThirst.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.moChiThirst.managers.ConfigManager;
import org.moChiThirst.managers.ThirstManager;
import org.moChiThirst.utils.Color;

import java.util.ArrayList;
import java.util.List;

public class SetCommand implements SubCommand {

    private final ThirstManager thirstManager;

    public SetCommand(ThirstManager thirstManager) {
        this.thirstManager = thirstManager;
    }

    @Override
    public String getName() { return "set"; }
    @Override
    public String getPermission() { return "mochithirst.set"; }

    @Override
    public void execute(CommandSender sender, String[] args) {
        String prefix = ConfigManager.getPrefix() + " ";

        if (args.length < 3) {
            sender.sendMessage(Color.translate(prefix + "&cCách dùng: /thirst set <player> <amount>"));
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(Color.translate(prefix + "&cKhông tìm thấy người chơi: &f" + args[1]));
            return;
        }

        int amount;
        try {
            amount = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage(Color.translate(prefix + "&c'" + args[2] + "' không phải số hợp lệ."));
            return;
        }

        if (amount < 0) {
            sender.sendMessage(Color.translate(prefix + "&cGiá trị không được âm."));
            return;
        }

        thirstManager.setThirst(target, amount);

        String msg = ConfigManager.get("messages").getString("set", "&aĐã chỉnh độ khát của &e{target} &athành &e{amount}");
        msg = msg.replace("{target}", target.getName())
                .replace("{amount}", String.valueOf(thirstManager.getCurrentThirst(target)));
        sender.sendMessage(Color.translate(prefix + msg));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        if (args.length == 2) {
            List<String> completions = new ArrayList<>();
            String input = args[1].toLowerCase();
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getName().toLowerCase().startsWith(input)) {
                    completions.add(player.getName());
                }
            }
            return completions;
        }

        if (args.length == 3) {
            return List.of("1", "5", "10", "20");
        }

        return List.of();
    }
}
