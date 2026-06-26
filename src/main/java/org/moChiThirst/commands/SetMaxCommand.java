package org.moChiThirst.commands;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.moChiThirst.managers.ConfigManager;
import org.moChiThirst.managers.ThirstManager;
import org.moChiThirst.utils.Color;

import java.util.ArrayList;
import java.util.List;

public class SetMaxCommand implements SubCommand {

    private static final String CAPACITY_PREFIX = "thirst.capacity.";

    private final ThirstManager thirstManager;

    public SetMaxCommand(ThirstManager thirstManager) {
        this.thirstManager = thirstManager;
    }

    @Override
    public String getName() { return "setmax"; }

    @Override
    public String getPermission() { return "mochithirst.setmax"; }

    @Override
    public void execute(CommandSender sender, String[] args) {
        String prefix = ConfigManager.getPrefix() + " ";

        if (args.length < 3) {
            sender.sendMessage(Color.translate(prefix + "&cCách dùng: /thirst setmax <player> <amount>"));
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

        RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        if (provider == null) {
            sender.sendMessage(Color.translate(prefix + "&cLuckPerms không khả dụng!"));
            return;
        }

        LuckPerms luckPerms = provider.getProvider();
        User      user      = luckPerms.getUserManager().getUser(target.getUniqueId());

        if (user == null) {
            sender.sendMessage(Color.translate(prefix + "&cKhông thể load dữ liệu LuckPerms của &f" + target.getName()));
            return;
        }

        // Xóa tất cả capacity node cũ
        user.data().clear(node -> node.getKey().startsWith(CAPACITY_PREFIX));

        // Thêm capacity node mới
        user.data().add(Node.builder(CAPACITY_PREFIX + amount).build());

        luckPerms.getUserManager().saveUser(user).thenRun(() -> {
            target.recalculatePermissions();

            String msg = ConfigManager.get("messages").getString("maxset",
                    "&aĐã đặt độ khát tối đa của &e{target} &athành &e{amount}");
            msg = msg.replace("{target}", target.getName())
                    .replace("{amount}", String.valueOf(amount));

            final String finalMsg = msg;
            Bukkit.getScheduler().runTask(
                    Bukkit.getPluginManager().getPlugin("MoChiThirst"),
                    () -> sender.sendMessage(Color.translate(prefix + finalMsg))
            );
        });
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
