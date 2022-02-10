package top.iseason.customisedattributes;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;
import top.iseason.customisedattributes.Command.ReloadCommand;
import top.iseason.customisedattributes.Util.HealthTimer;
import top.iseason.customisedattributes.Util.LogSender;

public final class Main extends JavaPlugin {
    private static Main plugin;

    @Override
    public void onEnable() {
        plugin = this;
        saveDefaultConfig();
        LogSender.sendLog(ChatColor.GREEN + "插件已启用，作者：" + ChatColor.GOLD + "Iceason");
        Bukkit.getPluginCommand("CustomisedAttributes").setExecutor(new ReloadCommand());
        ConfigManager.reload();
    }

    @Override
    public void onDisable() {
        saveDefaultConfig();
        HealthTimer.reset();
        LogSender.sendLog(ChatColor.RED + "插件已注销!");
    }

    public static Main getInstance() {
        return plugin;
    }


}
