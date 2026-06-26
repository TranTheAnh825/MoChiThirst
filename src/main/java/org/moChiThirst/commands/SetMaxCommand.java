package org.moChiThirst.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.moChiThirst.managers.ConfigManager;
import org.moChiThirst.managers.ThirstManager;
import org.moChiThirst.utils.Color;

import java.util.ArrayList;
import java.util.List;

public class SetMaxCommand implements SubCommand {

    private final ThirstManager thirstManager;

    public SetMaxCommand(ThirstManager thirstManager) {
        this.thirstManager = thirstManager;
    }

    @Override
    public String getName() { return "maxset"; }

    @Override
    public String getPermission() { return "mochithirst.maxset"; }

    @Override
    public void execute(CommandSender sender, String[] args) {
        String prefix = ConfigManager.getPrefix() + " ";

        // /thirst maxset <player> <amount>
        if (args.length < 3) {
            sender.sendMessage(Color.translate(prefix + "&cCách dùng: /thirst maxset <player> <amount>"));
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

        if (amount <= 0) {
            sender.sendMessage(Color.translate(prefix + "&cGiá trị phải lớn hơn 0."));
            return;
        }

        thirstManager.setMaxThirst(target, amount);

        String msg = ConfigManager.get("messages").getString("maxset",
                "&aĐã đặt độ khát tối đa của &e{target} &athành &e{amount}");
        msg = msg.replace("{target}", target.getName())
                .replace("{amount}", String.valueOf(amount));
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
            return List.of("20", "30", "50");
        }

        return List.of();
    }
}
