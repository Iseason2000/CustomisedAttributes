package top.iseason.customisedattributes.Command;


import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import top.iseason.customisedattributes.ConfigManager;

import java.io.File;
import java.io.IOException;

import static top.iseason.customisedattributes.Main.getInstance;

public class BlackListCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!(commandSender instanceof Player)) {
            return true;
        }
        if (!commandSender.isOp()) {
            return true;
        }
        Player player = (Player) commandSender;
        ItemStack[] contents = player.getInventory().getContents();
        File file = new File(getInstance().getDataFolder(), "blackList.yml");
        FileConfiguration yamlConfiguration = YamlConfiguration.loadConfiguration(file);
        for (ItemStack item : contents) {
            if (item == null) {
                continue;
            }
            String name = item.getType().toString();
            ConfigManager.getBlackList().add(name);
            yamlConfiguration.createSection(name);
        }
        try {
            yamlConfiguration.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

}
