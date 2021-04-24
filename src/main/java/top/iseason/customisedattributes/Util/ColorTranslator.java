package top.iseason.customisedattributes.Util;

import org.bukkit.ChatColor;

public class ColorTranslator {
    public static String toColor(String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }
    public static String noColor(String string) {
        return ChatColor.stripColor(string);
    }
}
