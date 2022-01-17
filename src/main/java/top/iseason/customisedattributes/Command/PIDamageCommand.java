package top.iseason.customisedattributes.Command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import top.iseason.customisedattributes.Listener.PIDamageListener;
import top.iseason.customisedattributes.Main;
import top.iseason.customisedattributes.Util.Binder;
import top.iseason.customisedattributes.Util.ColorTranslator;

import java.util.UUID;


public class PIDamageCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        }
        if (!sender.isOp()) {
            return true;
        }
        if (args.length < 1) {
            return true;
        }
        double percentage;
        try {
            percentage = Double.parseDouble(args[0]);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return true;
        }
        Player player = (Player) sender;
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
            PIDamageListener.iDList.put(uniqueId, -percentage);
        } else {
            PIDamageListener.iDList.put(uniqueId, percentage);
            new BukkitRunnable() {
                @Override
                public void run() {
                    PIDamageListener.iDList.remove(uniqueId);
                    Binder.remove(player);
                }
            }.runTaskLaterAsynchronously(Main.getInstance(), time);
        }
        if (PIDamageListener.IDCTip != null && !PIDamageListener.IDCTip.isEmpty()) {
            if (time == 0)
                player.sendMessage(ColorTranslator.toColor(PIDamageListener.IDCTip.replace("[data]", String.valueOf(percentage))));
            else
                player.sendMessage(ColorTranslator.toColor(PIDamageListener.IDCTip2.replace("[data]", String.valueOf(percentage)).replace("[time]", String.valueOf(time / 20.0))));
        }
        return true;
    }
}
