package top.iseason.customisedattributes.Command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import top.iseason.customisedattributes.Listener.HealListener;
import top.iseason.customisedattributes.Main;
import top.iseason.customisedattributes.Util.Binder;
import top.iseason.customisedattributes.Util.ColorTranslator;
import top.iseason.customisedattributes.Util.PercentageGetter;

import java.util.UUID;

public class HealCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!commandSender.isOp()) return true;
        if (!(commandSender instanceof Player)) return true;
        onCommand((Player) commandSender, strings);
        return true;
    }

    private void onCommand(Player player, String[] args) {
        if (args.length < 1) return;
        double second = PercentageGetter.formatString(args[0]);
        UUID uniqueId = player.getUniqueId();
        if (second == 0.0) return;
        double health = 1;
        if (args.length > 1) {
            health = PercentageGetter.formatString(args[1]);
        }
        HealListener.map.put(uniqueId, health);
        new BukkitRunnable() {
            @Override
            public void run() {
                HealListener.map.remove(uniqueId);
                Binder.remove(player);
            }
        }.runTaskLaterAsynchronously(Main.getInstance(), new Double(second * 20).longValue());
        Binder.bind(player, player.getItemInHand());
        if (HealListener.tip1 != null && !HealListener.tip1.isEmpty()) {
            player.sendMessage(ColorTranslator.toColor(HealListener.tip1.replace("[time]", String.valueOf(second)).replace("[data]", String.valueOf(health))));
        }

    }
}
