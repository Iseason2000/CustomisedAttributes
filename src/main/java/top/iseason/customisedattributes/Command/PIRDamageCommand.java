package top.iseason.customisedattributes.Command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import top.iseason.customisedattributes.Listener.PIRDamageListener;
import top.iseason.customisedattributes.Util.ColorTranslator;

public class PIRDamageCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        }
        if (!sender.isOp()) {
            return true;
        }
        if (args.length != 1) {
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
        PIRDamageListener.iRDList.put(player, percentage);
        if (PIRDamageListener.IRDCTip != null && !PIRDamageListener.IRDCTip.isEmpty()) {
            player.sendMessage(ColorTranslator.toColor(PIRDamageListener.IRDCTip.replace("[data]", String.valueOf(percentage))));
        }
        return true;
    }
}