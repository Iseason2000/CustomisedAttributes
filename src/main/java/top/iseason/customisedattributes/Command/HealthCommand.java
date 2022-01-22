package top.iseason.customisedattributes.Command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import top.iseason.customisedattributes.Listener.HealthListener;
import top.iseason.customisedattributes.Util.Binder;
import top.iseason.customisedattributes.Util.ColorTranslator;
import top.iseason.customisedattributes.Util.HealthModifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HealthCommand implements CommandExecutor, TabExecutor {
    public static String Tip;

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!commandSender.isOp()) return true;
        if (!(commandSender instanceof Player)) return true;
        onCommand((Player) commandSender, strings);
        return true;
    }

    private void onCommand(Player player, String[] args) {
        if (args.length < 1) return;
        switch (args[0]) {
            case "heal":
                if (args.length != 2) return;
                String arg1 = args[1];
                if (arg1.contains("%")) {
                    String replace = arg1.replace("%", "");
                    HealthModifier.healPercent(player, HealthModifier.toDouble(replace) / 100.0);
                } else {
                    HealthModifier.heal(player, HealthModifier.toDouble(arg1));
                }
                break;
            case "addMaxHealth":
                if (args.length != 3) return;
                String arg2 = args[1];
                new HealthModifier.Timer(player, arg2, HealthModifier.toInt(args[2])).start();
                break;
            case "reduceMaxHealth":
                if (args.length != 3) return;
                String[] data = new String[2];
                data[0] = args[1];
                data[1] = args[2];
                HealthListener.attackMap.put(player.getUniqueId(), data);
                Binder.bind(player, player.getItemInHand());
                if (Tip != null && !Tip.isEmpty()) {
                    player.sendMessage(ColorTranslator.toColor(Tip.replace("[data]", args[1]).replace("[time]", String.format("%.0f", HealthModifier.toDouble(args[2]) / 20))));
                }
                break;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String st, String[] args) {
        if (args.length == 1) {
            List<String> list = new ArrayList<>(Arrays.asList("heal", "addMaxHealth", "reduceMaxHealth"));
            list.removeIf(s -> !s.startsWith(args[0].toLowerCase()));
            return list;
        } else {
            return null;
        }
    }
}
