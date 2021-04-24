package top.iseason.customisedattributes.Util;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public class LogSender {
    public static void sendLog(String message){
        Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW+"自定义"+ChatColor.GOLD+"属性插件"+ChatColor.GREEN+"| "+ChatColor.RESET+message);
    }
}

