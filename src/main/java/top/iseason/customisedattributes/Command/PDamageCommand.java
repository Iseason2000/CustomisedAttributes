package top.iseason.customisedattributes.Command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import top.iseason.customisedattributes.Listener.PercentageDamageListener;
import top.iseason.customisedattributes.Util.Binder;
import top.iseason.customisedattributes.Util.ColorTranslator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
            if (args.length != 2) {
                return true;
            }
            Player player = (Player) sender;
            double percentage;
            try {
                percentage = Double.parseDouble(args[1]);
            } catch (NumberFormatException e) {
                e.printStackTrace();
                return true;
            }
            PercentageDamageListener.attackList.put(player, percentage);
            Binder.bind(player, player.getItemInHand());
            if (PercentageDamageListener.PDCTip != null && !PercentageDamageListener.PDCTip.isEmpty()) {
                player.sendMessage(ColorTranslator.toColor(PercentageDamageListener.PDCTip.replace("[data]", String.valueOf(percentage))));
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
