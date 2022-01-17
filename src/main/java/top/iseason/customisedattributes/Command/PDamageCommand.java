package top.iseason.customisedattributes.Command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import top.iseason.customisedattributes.Listener.PercentageDamageListener;
import top.iseason.customisedattributes.Main;
import top.iseason.customisedattributes.Util.Binder;
import top.iseason.customisedattributes.Util.ColorTranslator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class PDamageCommand implements CommandExecutor, TabExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        }
        if (!sender.isOp()) {
            return true;
        }
        if ("set".equals(args[0])) {
            if (args.length <= 2) {
                return true;
            }
            Player player = (Player) sender;
            double percentage;
            try {
                percentage = Double.parseDouble(args[1]);
            } catch (NumberFormatException e) {
                return true;
            }
            int time = 0;
            if (args.length > 3) {
                try {
                    time = Integer.parseInt(args[2]);
                } catch (NumberFormatException ignored) {
                }
            }
            UUID uniqueId = player.getUniqueId();
            if (time == 0) {
                //负的表示只有一次
                PercentageDamageListener.attackList.put(uniqueId, -percentage);
            } else {
                PercentageDamageListener.attackList.put(uniqueId, percentage);
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        PercentageDamageListener.attackList.remove(uniqueId);
                        Binder.remove(player);
                    }
                }.runTaskLaterAsynchronously(Main.getInstance(), time);
            }
            Binder.bind(player, player.getItemInHand());
            if (PercentageDamageListener.PDCTip != null && !PercentageDamageListener.PDCTip.isEmpty()) {
                if (time == 0)
                    player.sendMessage(ColorTranslator.toColor(PercentageDamageListener.PDCTip.replace("[data]", String.valueOf(percentage))));
                else
                    player.sendMessage(ColorTranslator.toColor(PercentageDamageListener.PDCTip2.replace("[data]", String.valueOf(percentage)).replace("[time]", String.valueOf(time / 20.0))));
            }
            return true;
        } else if ("me".equals(args[0])) {
            Player me = (Player) sender;
            if (args.length != 2) {
                me.sendMessage(ColorTranslator.toColor("&4请输入值"));
                return true;
            }
            double per = Double.parseDouble(args[1]);
            me.damage(0);
            double health = me.getHealth() - me.getMaxHealth() * (per / 100);
            if (health < 0) {
                health = 0;
            }
            me.setHealth(health);
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            List<String> list = new ArrayList<>(Arrays.asList("set", "me"));
            list.removeIf(s -> !s.startsWith(args[0].toLowerCase()));
            return list;
        } else {
            return null;
        }
    }
}
