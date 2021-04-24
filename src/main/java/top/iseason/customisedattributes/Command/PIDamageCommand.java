package top.iseason.customisedattributes.Command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import top.iseason.customisedattributes.Listener.PIDamageListener;
import top.iseason.customisedattributes.Util.ColorTranslator;


public class PIDamageCommand implements CommandExecutor {
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
        PIDamageListener.iDList.put(player, percentage);
        player.sendMessage(ColorTranslator.toColor(PIDamageListener.IDCTip.replace("[data]", String.valueOf(percentage))));
        return true;
    }
}
