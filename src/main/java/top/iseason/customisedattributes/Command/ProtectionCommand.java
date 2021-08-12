package top.iseason.customisedattributes.Command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import top.iseason.customisedattributes.Listener.PercentageProtectionListener;
import top.iseason.customisedattributes.Main;
import top.iseason.customisedattributes.Util.ColorTranslator;

public class ProtectionCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        }
        if (!sender.isOp()) {
            return true;
        }
        if (args.length != 2) return true;
        double percentage;
        try {
            percentage = Double.parseDouble(args[0]);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return true;
        }
        double second;
        try {
            second = Double.parseDouble(args[1]);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return true;
        }
        Player player = (Player) sender;
        PercentageProtectionListener.playerMap.put(player.getUniqueId(), percentage);
        new BukkitRunnable() {
            @Override
            public void run() {
                PercentageProtectionListener.playerMap.remove(player.getUniqueId(), percentage);
            }
        }.runTaskLaterAsynchronously(Main.getInstance(), (long) (20 * second));
        if (PercentageProtectionListener.commandMessage != null && !PercentageProtectionListener.commandMessage.isEmpty()) {
            player.sendMessage(ColorTranslator.toColor(
                    PercentageProtectionListener.commandMessage.replace("[second]", String.valueOf(second))
                            .replace("[data]", String.valueOf(percentage))));
        }
        return true;
    }
}
