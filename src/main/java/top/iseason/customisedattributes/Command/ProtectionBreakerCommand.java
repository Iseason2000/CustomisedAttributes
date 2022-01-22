package top.iseason.customisedattributes.Command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import top.iseason.customisedattributes.Listener.ProtectionBreakerListener;
import top.iseason.customisedattributes.Main;
import top.iseason.customisedattributes.Util.Binder;
import top.iseason.customisedattributes.Util.ColorTranslator;

import java.util.UUID;

public class ProtectionBreakerCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        }
        if (!sender.isOp()) {
            return true;
        }
        if (args.length < 1) return true;
        Player player = (Player) sender;
        double percentage;
        try {
            percentage = Double.parseDouble(args[0]);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return true;
        }
        int time = 0;
        if (args.length > 1) {
            try {
                time = Integer.parseInt(args[1]);
            } catch (NumberFormatException ignored) {
            }
        }
        UUID uniqueId = player.getUniqueId();
        Binder.bind(player, player.getItemInHand());
        if (time == 0) {
            //负的表示只有一次
            ProtectionBreakerListener.pbList.put(uniqueId, -percentage);
        } else {
            ProtectionBreakerListener.pbList.put(uniqueId, percentage);
            new BukkitRunnable() {
                @Override
                public void run() {
                    ProtectionBreakerListener.pbList.remove(uniqueId);
                    Binder.remove(player);
                }
            }.runTaskLaterAsynchronously(Main.getInstance(), time);
        }
        if (ProtectionBreakerListener.commandMessage != null && !ProtectionBreakerListener.commandMessage.isEmpty()) {
            if (time == 0)
                player.sendMessage(ColorTranslator.toColor(ProtectionBreakerListener.commandMessage.replace("[data]", String.format("%.0f", percentage))));
            else
                player.sendMessage(ColorTranslator.toColor(ProtectionBreakerListener.commandMessage2.replace("[data]", String.format("%.0f", percentage)).replace("[time]", String.valueOf(time / 20.0))));

        }
        return true;
    }
}
