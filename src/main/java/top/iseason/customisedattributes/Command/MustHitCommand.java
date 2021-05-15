package top.iseason.customisedattributes.Command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import top.iseason.customisedattributes.Listener.MustHitListener;
import top.iseason.customisedattributes.Main;
import top.iseason.customisedattributes.Util.ColorTranslator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MustHitCommand implements CommandExecutor, TabExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        }
        Player player = (Player) sender;
        if (!sender.isOp()) {
            return true;
        }
        ItemStack item = player.getEquipment().getItemInHand();
        if (args.length == 1 && "once".equals(args[0])) {
            MustHitListener.mustHitMap.put(player, item);
            player.sendMessage(ColorTranslator.toColor(MustHitListener.mustHitOnceTip));
        } else if (args.length == 2 && "time".equals(args[0])) {
            String arg = args[1];
            int second = 0;
            try {
                second = Integer.parseInt(arg);
            } catch (NumberFormatException ignored) {
            }
            MustHitListener.mustHitTimeMap.put(player, item);
            new BukkitRunnable() {
                @Override
                public void run() {
                    MustHitListener.mustHitTimeMap.remove(player, item);
                }
            }.runTaskLater(Main.getInstance(), second * 20L);
            player.sendMessage(ColorTranslator.toColor(MustHitListener.mustHitTimeTip.replace("[data]", arg)));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            List<String> list = new ArrayList<>(Arrays.asList("once", "time"));
            list.removeIf(s -> !s.startsWith(args[0].toLowerCase()));
            return list;
        } else {
            return null;
        }
    }
}
