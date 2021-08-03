package top.iseason.customisedattributes.Command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import top.iseason.customisedattributes.Listener.ProtectionBreakerListener;
import top.iseason.customisedattributes.Util.ColorTranslator;

public class ProtectionBreakerCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        }
        if (!sender.isOp()) {
            return true;
        }
        if (args.length != 1) return true;
        Player player = (Player) sender;
        double percentage;
        try {
            percentage = Double.parseDouble(args[0]);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return true;
        }
        ProtectionBreakerListener.pbList.put(player, percentage);
        ProtectionBreakerListener.itemSet.add(player.getItemInHand());
        if (ProtectionBreakerListener.commandMessage != null && !ProtectionBreakerListener.commandMessage.isEmpty()) {
            player.sendMessage(ColorTranslator.toColor(ProtectionBreakerListener.commandMessage.replace("[data]", String.valueOf(percentage))));
        }
        return true;
    }
}
