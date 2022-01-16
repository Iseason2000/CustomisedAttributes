package top.iseason.customisedattributes;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.HandlerList;
import top.iseason.customisedattributes.Command.*;
import top.iseason.customisedattributes.Listener.*;
import top.iseason.customisedattributes.Util.HealthModifier;
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

    public static void reload() {
        HandlerList.unregisterAll(getInstance());
        Bukkit.getPluginManager().registerEvents(new EventListener(), getInstance());
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
        setHealthConfig();
        setDamageRangeConfig();
        setBleedConfig();
        setArmourPenetrationConfig();
        setHealConfig();
        readBlackList();
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
        PercentageProtectionListener.playerMap = new HashMap<>();
        PercentageProtectionListener.commandMessage = percentageDConfig.getString("命令提示");
        registerPercentageProtection();
    }

    private static void setProtectionBreakerListenerConfig() {
        ConfigurationSection percentageBConfig = config.getConfigurationSection("百分比破伤");
        ProtectionBreakerListener.keyPattern = Pattern.compile(toPatternString(percentageBConfig.getString("关键词")));
        ProtectionBreakerListener.effectMessage = percentageBConfig.getString("攻击提示");
        ProtectionBreakerListener.commandMessage = percentageBConfig.getString("命令提示");
        ProtectionBreakerListener.pbList = new HashMap<>();
        ProtectionBreakerListener.itemSet = new HashSet<>();
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

    private static void setHealthConfig() {
        ConfigurationSection healthConfig = config.getConfigurationSection("血量修改");
        HealthModifier.HandItemTimer.lorePattern = Pattern.compile(healthConfig.getString("关键词1").replaceAll("\\[data]", "([0-9]+.*-.*[0-9]%?+|[0-9]+%?)"));
        HealthListener.pattern = Pattern.compile(healthConfig.getString("关键词2")
                .replaceFirst("\\[data]", "([0-9]+.*-.*[0-9]+|[0-9]+)")
                .replaceFirst("\\[data]", "([0-9]+.*-.*[0-9]%?+|[0-9]+%?)")
                .replace("[time]", "(\\d+\\.?/?\\d*%?)"));
        HealthListener.RTip = healthConfig.getString("减血提示");
        HealthListener.RTip2 = healthConfig.getString("减血提示2");
        HealthCommand.Tip = healthConfig.getString("下一击提示");
        HealthListener.attackMap = new HashMap<>();
        HealthModifier.Timer.reset();
        registerCustomHealth();
    }

    private static void registerPercentageDamage() {
        if (config.getBoolean("百分比伤害.开关")) {
            Bukkit.getPluginManager().registerEvents(new PercentageDamageListener(), getInstance());
            Bukkit.getPluginCommand("percentageA").setExecutor(new PDamageCommand());
            LogSender.sendLog(ChatColor.YELLOW + "属性：" + ChatColor.RED + "百分比伤害" + ChatColor.AQUA + "-目标血真伤" + ChatColor.GREEN + "已开启");
        } else {
            Bukkit.getPluginCommand("percentageA").setExecutor(null);
            LogSender.sendLog(ChatColor.YELLOW + "属性：" + ChatColor.RED + "百分比伤害" + ChatColor.RED + "已关闭");
        }

    }

    private static void registerPercentageProtection() {
        if (config.getBoolean("百分比减伤.开关")) {
            Bukkit.getPluginCommand("percentageProtection").setExecutor(new ProtectionCommand());
            Bukkit.getPluginManager().registerEvents(new PercentageProtectionListener(), getInstance());
            LogSender.sendLog(ChatColor.YELLOW + "属性：" + ChatColor.DARK_BLUE + "百分比减伤" + ChatColor.GREEN + "已开启");
        } else {
            Bukkit.getPluginCommand("percentageProtection").setExecutor(null);
            LogSender.sendLog(ChatColor.YELLOW + "属性：" + ChatColor.DARK_BLUE + "百分比减伤" + ChatColor.RED + "已关闭");
        }
    }

    private static void registerProtectionBreaker() {
        if (config.getBoolean("百分比破伤.开关")) {
            Bukkit.getPluginManager().registerEvents(new ProtectionBreakerListener(), getInstance());
            Bukkit.getPluginCommand("protectionBreaker").setExecutor(new ProtectionBreakerCommand());
            LogSender.sendLog(ChatColor.YELLOW + "属性：" + ChatColor.BLUE + "百分比破伤" + ChatColor.GREEN + "已开启");
        } else {
            Bukkit.getPluginCommand("protectionBreaker").setExecutor(null);
            LogSender.sendLog(ChatColor.YELLOW + "属性：" + ChatColor.BLUE + "百分比破伤" + ChatColor.RED + "已关闭");
        }
    }

    private static void registerPIDamage() {
        if (config.getBoolean("百分比增伤-普通伤害.开关")) {
            Bukkit.getPluginManager().registerEvents(new PIDamageListener(), getInstance());
            Bukkit.getPluginCommand("percentageID").setExecutor(new PIDamageCommand());
            LogSender.sendLog(ChatColor.YELLOW + "属性：" + ChatColor.LIGHT_PURPLE + "百分比增伤" + ChatColor.BLUE + "-自身血普伤" + ChatColor.GREEN + "已开启");
        } else {
            Bukkit.getPluginCommand("percentageID").setExecutor(null);
            LogSender.sendLog(ChatColor.YELLOW + "属性：" + ChatColor.LIGHT_PURPLE + "百分比增伤" + ChatColor.BLUE + "-自身血普伤" + ChatColor.RED + "已关闭");
        }
    }

    private static void registerPIRDamage() {
        if (config.getBoolean("百分比增伤-真实伤害.开关")) {
            Bukkit.getPluginManager().registerEvents(new PIRDamageListener(), getInstance());
            Bukkit.getPluginCommand("percentageIRD").setExecutor(new PIRDamageCommand());
            LogSender.sendLog(ChatColor.YELLOW + "属性：" + ChatColor.DARK_PURPLE + "百分比增伤" + ChatColor.DARK_AQUA + "-自身血真伤" + ChatColor.GREEN + "已开启");
        } else {
            Bukkit.getPluginCommand("percentageIRD").setExecutor(null);
            LogSender.sendLog(ChatColor.YELLOW + "属性：" + ChatColor.DARK_PURPLE + "百分比增伤" + ChatColor.DARK_AQUA + "-自身血真伤" + ChatColor.RED + "已关闭");
        }
    }

    private static void registerMustHit() {
        if (config.getBoolean("必中.开关")) {
            Bukkit.getPluginManager().registerEvents(new MustHitListener(), getInstance());
            Bukkit.getPluginCommand("mustHit").setExecutor(new MustHitCommand());
            LogSender.sendLog(ChatColor.YELLOW + "属性：" + ChatColor.DARK_PURPLE + "必中" + ChatColor.GREEN + "已开启");
        } else {
            Bukkit.getPluginCommand("mustHit").setExecutor(null);
            LogSender.sendLog(ChatColor.YELLOW + "属性：" + ChatColor.DARK_PURPLE + "必中" + ChatColor.RED + "已关闭");
        }
    }

    private static void registerCustomHealth() {
        if (config.getBoolean("血量修改.开关")) {
            Bukkit.getPluginManager().registerEvents(new HealthListener(), getInstance());
            Bukkit.getPluginCommand("healthModifier").setExecutor(new HealthCommand());
            LogSender.sendLog(ChatColor.YELLOW + "属性：" + ChatColor.DARK_RED + "血量修改" + ChatColor.GREEN + "已开启");
        } else {
            Bukkit.getPluginCommand("healthModifier").setExecutor(null);
            LogSender.sendLog(ChatColor.YELLOW + "属性：" + ChatColor.DARK_RED + "血量修改" + ChatColor.RED + "已关闭");
        }
    }

    private static void setDamageRangeConfig() {
        ConfigurationSection rangeConfig = config.getConfigurationSection("范围攻击");
        DamageRangeListener.pattern = Pattern.compile(toPatternString(rangeConfig.getString("关键词").replace("+", "\\+")));
        registerDamageRange();
    }

    private static void registerDamageRange() {
        if (config.getBoolean("范围攻击.开关")) {
            Bukkit.getPluginManager().registerEvents(new DamageRangeListener(), getInstance());
            LogSender.sendLog(ChatColor.YELLOW + "属性：" + ChatColor.GRAY + "范围伤害" + ChatColor.GREEN + "已开启");
        } else {
            LogSender.sendLog(ChatColor.YELLOW + "属性：" + ChatColor.GRAY + "范围伤害" + ChatColor.RED + "已关闭");
        }
    }

    private static void setBleedConfig() {
        ConfigurationSection rangeConfig = config.getConfigurationSection("流血");
        BleedListener.pattern1 = Pattern.compile(toPatternString(rangeConfig.getString("关键词1").replace("+", "\\+")));
        BleedListener.pattern2 = Pattern.compile(toPatternString(rangeConfig.getString("关键词2")));
        BleedListener.pattern3 = Pattern.compile(toPatternString(rangeConfig.getString("关键词3")));
        BleedListener.pattern4 = Pattern.compile(toPatternString(rangeConfig.getString("关键词4")));
        registerBleed();
    }

    private static void registerBleed() {
        if (config.getBoolean("流血.开关")) {
            Bukkit.getPluginManager().registerEvents(new BleedListener(), getInstance());
            LogSender.sendLog(ChatColor.YELLOW + "属性：" + ChatColor.GOLD + "流血" + ChatColor.GREEN + "已开启");
        } else {
            LogSender.sendLog(ChatColor.YELLOW + "属性：" + ChatColor.GOLD + "流血" + ChatColor.RED + "已关闭");
        }
    }

    private static void setArmourPenetrationConfig() {
        ConfigurationSection rangeConfig = config.getConfigurationSection("破甲");
        ArmourPenetrationListener.pattern = Pattern.compile(toPatternString(rangeConfig.getString("关键词")));
        ArmourPenetrationListener.commandTip = rangeConfig.getString("下一击提示");
        ArmourPenetrationListener.map = new HashMap<>();
        registerArmourPenetration();
    }

    private static void registerArmourPenetration() {
        if (config.getBoolean("破甲.开关")) {
            Bukkit.getPluginManager().registerEvents(new ArmourPenetrationListener(), getInstance());
            LogSender.sendLog(ChatColor.YELLOW + "属性：" + ChatColor.WHITE + "破甲" + ChatColor.GREEN + "已开启");
            Bukkit.getPluginCommand("ArmourPenetration").setExecutor(new ArmourPenetrationCommand());
        } else {
            LogSender.sendLog(ChatColor.YELLOW + "属性：" + ChatColor.WHITE + "破甲" + ChatColor.RED + "已关闭");
            Bukkit.getPluginCommand("ArmourPenetration").setExecutor(null);
        }
    }

    private static void setHealConfig() {
        ConfigurationSection healConfig = config.getConfigurationSection("回血");
        HealListener.tip1 = healConfig.getString("提示");
        HealListener.tip2 = healConfig.getString("治疗提示");
        HealListener.pattern = Pattern.compile(toPatternString(healConfig.getString("关键词")));
        HealListener.map = new HashMap<>();
        HealListener.coolDown = new HashSet<>();
        registerHeal();
    }

    private static void registerHeal() {
        if (config.getBoolean("回血.开关")) {
            Bukkit.getPluginManager().registerEvents(new HealListener(), getInstance());
            LogSender.sendLog(ChatColor.YELLOW + "属性：" + ChatColor.DARK_AQUA + "治疗" + ChatColor.GREEN + "已开启");
            Bukkit.getPluginCommand("healths").setExecutor(new HealCommand());
        } else {
            LogSender.sendLog(ChatColor.YELLOW + "属性：" + ChatColor.DARK_AQUA + "治疗" + ChatColor.RED + "已关闭");
            Bukkit.getPluginCommand("healths").setExecutor(null);
        }
    }

    public static String toPatternString(String string) {
        return string.replace("[data]", "([0-9]+[.[0-9]+]?.*-.*[0-9]+[.[0-9]+]?|[0-9]+[.[0-9]+]?)");
    }

    public static Set<String> getBlackList() {
        return blackList;
    }
}
