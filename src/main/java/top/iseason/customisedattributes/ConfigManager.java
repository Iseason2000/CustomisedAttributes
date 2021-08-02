package top.iseason.customisedattributes;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import top.iseason.customisedattributes.Command.*;
import top.iseason.customisedattributes.Listener.*;
import top.iseason.customisedattributes.Util.LogSender;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import static top.iseason.customisedattributes.Main.getInstance;

public class ConfigManager {
    public static SecureRandom random;
    private static FileConfiguration config;
    private static Set<String> blackList;

    private static PercentageDamageListener percentageDamageListener;
    private static PercentageProtectionListener percentageProtectionListener;
    private static PIDamageListener piDamageListener;
    private static PIRDamageListener pirDamageListener;
    private static MustHitListener mustHitListener;
    private static ProtectionBreakerListener protectionBreakerListener;


    public static void reload() {
        getInstance().reloadConfig();
        config = Main.getInstance().getConfig();
        try {
            random = SecureRandom.getInstance("SHA1PRNG", "SUN");
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            e.printStackTrace();
        }
        setPercentageDamageConfig();
        setPercentageProtectionConfig();
        setProtectionBreakerListenerConfig();
        setPIDConfig();
        setPIRDConfig();
        setMustHitConfig();
        readBlackList();
        Bukkit.getPluginCommand("CustomisedAttributes").setExecutor(new ReloadCommand());
    }

    public static Double getDoubleRandom() {
        return random.nextDouble();
    }

    public static void readBlackList() {
        File file = new File(getInstance().getDataFolder(), "blackList.yml");
        FileConfiguration yamlConfiguration = YamlConfiguration.loadConfiguration(file);
        try {
            yamlConfiguration.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        blackList = new HashSet<>();
        blackList.addAll(yamlConfiguration.getKeys(false));
        Bukkit.getPluginCommand("CustomisedAttributesBlackList").setExecutor(new BlackListCommand());

    }

    private static void setPercentageDamageConfig() {
        PercentageDamageListener.attackList = new HashMap<>();
        ConfigurationSection percentageAConfig = config.getConfigurationSection("百分比伤害");
        PercentageDamageListener.damagePattern = Pattern.compile(toPatternString(percentageAConfig.getString("关键词")));
        PercentageDamageListener.playerMaxP = percentageAConfig.getDouble("玩家最大百分比");
        PercentageDamageListener.otherMaxP = percentageAConfig.getDouble("怪物最大百分比");
        PercentageDamageListener.PDCTip = percentageAConfig.getString("命令提示");
        PercentageDamageListener.PDTip = percentageAConfig.getString("攻击提示");
        registerPercentageDamage();
    }

    private static void setPercentageProtectionConfig() {
        ConfigurationSection percentageDConfig = config.getConfigurationSection("百分比减伤");
        PercentageProtectionListener.protectPattern = Pattern.compile(toPatternString(percentageDConfig.getString("关键词")));
        registerPercentageProtection();
    }

    private static void setProtectionBreakerListenerConfig() {
        ConfigurationSection percentageBConfig = config.getConfigurationSection("百分比破伤");
        ProtectionBreakerListener.keyPattern = Pattern.compile(toPatternString(percentageBConfig.getString("关键词")));
        ProtectionBreakerListener.effectMessage = percentageBConfig.getString("攻击提示");
        ProtectionBreakerListener.commandMessage = percentageBConfig.getString("命令提示");
        ProtectionBreakerListener.pbList = new HashMap<>();
        registerProtectionBreaker();
    }

    private static void setPIDConfig() {
        ConfigurationSection pIDamageConfig = config.getConfigurationSection("百分比增伤-普通伤害");
        PIDamageListener.iDChancePattern = Pattern.compile(toPatternString(pIDamageConfig.getString("普攻概率")));
        PIDamageListener.iDPattern = Pattern.compile(toPatternString(pIDamageConfig.getString("普攻倍率")));
        PIDamageListener.iDList = new HashMap<>();
        PIDamageListener.IDTip = pIDamageConfig.getString("触发提示");
        PIDamageListener.IDCTip = pIDamageConfig.getString("命令提示");
        registerPIDamage();
    }

    private static void setPIRDConfig() {
        ConfigurationSection pIRDamageConfig = config.getConfigurationSection("百分比增伤-真实伤害");
        PIRDamageListener.iRDChancePattern = Pattern.compile(toPatternString(pIRDamageConfig.getString("真攻概率")));
        PIRDamageListener.iRDPattern = Pattern.compile(toPatternString(pIRDamageConfig.getString("真攻倍率")));
        PIRDamageListener.iRDList = new HashMap<>();
        PIRDamageListener.IRDTip = pIRDamageConfig.getString("触发提示");
        PIRDamageListener.IRDCTip = pIRDamageConfig.getString("命令提示");
        registerPIRDamage();
    }

    private static void setMustHitConfig() {
        ConfigurationSection mustHitConfig = config.getConfigurationSection("必中");
        MustHitListener.mustHitPattern = Pattern.compile(toPatternString(mustHitConfig.getString("关键词")));
        MustHitListener.mustHitOnceTip = mustHitConfig.getString("单次提示");
        MustHitListener.mustHitTimeTip = mustHitConfig.getString("时间提示");
        MustHitListener.mustHitSuccessTip = mustHitConfig.getString("成功提示");
        MustHitListener.entityHitMap = new HashMap<>();
        MustHitListener.commandHitMap = new HashMap<>();
        MustHitListener.mustHitMap = new HashMap<>();
        MustHitListener.mustHitTimeMap = new HashMap<>();
        registerMustHit();
    }


    private static void registerPercentageDamage() {
        if (config.getBoolean("百分比伤害.开关")) {
            if (percentageDamageListener == null) {
                percentageDamageListener = new PercentageDamageListener();
                Bukkit.getPluginManager().registerEvents(percentageDamageListener, getInstance());
                Bukkit.getPluginCommand("percentageA").setExecutor(new PDamageCommand());
            }
            LogSender.sendLog(ChatColor.YELLOW + "属性：" + ChatColor.RED + "百分比伤害" + ChatColor.AQUA + "-目标血真伤" + ChatColor.GREEN + "已开启");
        } else {
            EntityDamageByEntityEvent.getHandlerList().unregister(percentageDamageListener);
            percentageDamageListener = null;
            Bukkit.getPluginCommand("percentageA").setExecutor(null);
            LogSender.sendLog(ChatColor.YELLOW + "属性：" + ChatColor.RED + "百分比伤害" + ChatColor.RED + "已关闭");
        }

    }

    private static void registerPercentageProtection() {
        if (config.getBoolean("百分比减伤.开关")) {
            if (percentageProtectionListener == null) {
                percentageProtectionListener = new PercentageProtectionListener();
                Bukkit.getPluginManager().registerEvents(percentageProtectionListener, getInstance());
            }
            LogSender.sendLog(ChatColor.YELLOW + "属性：" + ChatColor.DARK_BLUE + "百分比减伤" + ChatColor.GREEN + "已开启");
        } else {
            EntityDamageEvent.getHandlerList().unregister(percentageProtectionListener);
            percentageProtectionListener = null;
            LogSender.sendLog(ChatColor.YELLOW + "属性：" + ChatColor.DARK_BLUE + "百分比减伤" + ChatColor.RED + "已关闭");
        }
    }

    private static void registerProtectionBreaker() {
        if (config.getBoolean("百分比破伤.开关")) {
            if (protectionBreakerListener == null) {
                protectionBreakerListener = new ProtectionBreakerListener();
                Bukkit.getPluginManager().registerEvents(protectionBreakerListener, getInstance());
                Bukkit.getPluginCommand("protectionBreaker").setExecutor(new ProtectionBreakerCommand());
            }
            LogSender.sendLog(ChatColor.YELLOW + "属性：" + ChatColor.BLUE + "百分比破伤" + ChatColor.GREEN + "已开启");
        } else {
            EntityDamageEvent.getHandlerList().unregister(protectionBreakerListener);
            protectionBreakerListener = null;
            Bukkit.getPluginCommand("protectionBreaker").setExecutor(null);
            LogSender.sendLog(ChatColor.YELLOW + "属性：" + ChatColor.BLUE + "百分比破伤" + ChatColor.RED + "已关闭");
        }
    }

    private static void registerPIDamage() {
        if (config.getBoolean("百分比增伤-普通伤害.开关")) {
            if (piDamageListener == null) {
                piDamageListener = new PIDamageListener();
                Bukkit.getPluginManager().registerEvents(piDamageListener, getInstance());
                Bukkit.getPluginCommand("percentageID").setExecutor(new PIDamageCommand());
            }
            LogSender.sendLog(ChatColor.YELLOW + "属性：" + ChatColor.LIGHT_PURPLE + "百分比增伤" + ChatColor.BLUE + "-自身血普伤" + ChatColor.GREEN + "已开启");
        } else {
            EntityDamageEvent.getHandlerList().unregister(piDamageListener);
            piDamageListener = null;
            Bukkit.getPluginCommand("percentageID").setExecutor(null);
            LogSender.sendLog(ChatColor.YELLOW + "属性：" + ChatColor.LIGHT_PURPLE + "百分比增伤" + ChatColor.BLUE + "-自身血普伤" + ChatColor.RED + "已关闭");
        }
    }

    private static void registerPIRDamage() {
        if (config.getBoolean("百分比增伤-真实伤害.开关")) {
            if (pirDamageListener == null) {
                pirDamageListener = new PIRDamageListener();
                Bukkit.getPluginManager().registerEvents(pirDamageListener, getInstance());
                Bukkit.getPluginCommand("percentageIRD").setExecutor(new PIRDamageCommand());
            }
            LogSender.sendLog(ChatColor.YELLOW + "属性：" + ChatColor.DARK_PURPLE + "百分比增伤" + ChatColor.DARK_AQUA + "-自身血真伤" + ChatColor.GREEN + "已开启");
        } else {
            EntityDamageEvent.getHandlerList().unregister(pirDamageListener);
            pirDamageListener = null;
            Bukkit.getPluginCommand("percentageIRD").setExecutor(null);
            LogSender.sendLog(ChatColor.YELLOW + "属性：" + ChatColor.DARK_PURPLE + "百分比增伤" + ChatColor.DARK_AQUA + "-自身血真伤" + ChatColor.RED + "已关闭");
        }
    }

    private static void registerMustHit() {
        if (config.getBoolean("必中.开关")) {
            if (mustHitListener == null) {
                mustHitListener = new MustHitListener();
                Bukkit.getPluginManager().registerEvents(mustHitListener, getInstance());
                Bukkit.getPluginCommand("mustHit").setExecutor(new MustHitCommand());
            }
            LogSender.sendLog(ChatColor.YELLOW + "属性：" + ChatColor.DARK_PURPLE + "必中" + ChatColor.GREEN + "已开启");
        } else {
            EntityDamageEvent.getHandlerList().unregister(mustHitListener);
            mustHitListener = null;
            Bukkit.getPluginCommand("mustHit").setExecutor(null);
            LogSender.sendLog(ChatColor.YELLOW + "属性：" + ChatColor.DARK_PURPLE + "必中" + ChatColor.RED + "已关闭");
        }
    }

    public static String toPatternString(String string) {
        return string.replace("[data]", "([0-9]+.*-.*[0-9]+|[0-9]+)");
    }

    public static Set<String> getBlackList() {
        return blackList;
    }
}
