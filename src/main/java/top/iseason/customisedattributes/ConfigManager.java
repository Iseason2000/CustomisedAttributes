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
import java.util.Set;
import java.util.regex.Pattern;

import static top.iseason.customisedattributes.Main.getInstance;

public class ConfigManager {
    private static FileConfiguration config;
    private static SecureRandom random;
    private static PercentageDamageListener percentageDamageListener;
    private static PercentageProtectionListener percentageProtectionListener;
    private static PIDamageListener piDamageListener;
    private static PIRDamageListener pirDamageListener;
    private static MustHitListener mustHitListener;
    private static Set<String> blackList;

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
        setPIDConfig();
        setPIRDConfig();
        setMustHitConfig();
        registers();
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
        blackList = yamlConfiguration.getKeys(false);
        Bukkit.getPluginCommand("CustomisedAttributesBlackList").setExecutor(new BlackListCommand());

    }

    private static void setPercentageDamageConfig() {
        PercentageDamageListener.attackList = new HashMap<>();
        ConfigurationSection percentageAConfig = config.getConfigurationSection("???????????????");
        PercentageDamageListener.damagePattern = Pattern.compile(toPatternString(percentageAConfig.getString("?????????")));
        PercentageDamageListener.playerMaxP = percentageAConfig.getDouble("?????????????????????");
        PercentageDamageListener.otherMaxP = percentageAConfig.getDouble("?????????????????????");
        PercentageDamageListener.PDCTip = percentageAConfig.getString("????????????");
        PercentageDamageListener.PDTip = percentageAConfig.getString("????????????");
    }

    private static void setPercentageProtectionConfig() {
        ConfigurationSection percentageDConfig = config.getConfigurationSection("???????????????");
        PercentageProtectionListener.protectPattern = Pattern.compile(toPatternString(percentageDConfig.getString("?????????")));
    }

    private static void setPIDConfig() {
        ConfigurationSection pIDamageConfig = config.getConfigurationSection("???????????????-????????????");
        PIDamageListener.iDChancePattern = Pattern.compile(toPatternString(pIDamageConfig.getString("????????????")));
        PIDamageListener.iDPattern = Pattern.compile(toPatternString(pIDamageConfig.getString("????????????")));
        PIDamageListener.iDList = new HashMap<>();
        PIDamageListener.IDTip = pIDamageConfig.getString("????????????");
        PIDamageListener.IDCTip = pIDamageConfig.getString("????????????");
    }

    private static void setPIRDConfig() {
        ConfigurationSection pIRDamageConfig = config.getConfigurationSection("???????????????-????????????");
        PIRDamageListener.iRDChancePattern = Pattern.compile(toPatternString(pIRDamageConfig.getString("????????????")));
        PIRDamageListener.iRDPattern = Pattern.compile(toPatternString(pIRDamageConfig.getString("????????????")));
        PIRDamageListener.iRDList = new HashMap<>();
        PIRDamageListener.IRDTip = pIRDamageConfig.getString("????????????");
        PIRDamageListener.IRDCTip = pIRDamageConfig.getString("????????????");
    }

    private static void setMustHitConfig() {
        ConfigurationSection mustHitConfig = config.getConfigurationSection("??????");
        MustHitListener.mustHitPattern = Pattern.compile(toPatternString(mustHitConfig.getString("?????????")));
        MustHitListener.mustHitOnceTip = mustHitConfig.getString("????????????");
        MustHitListener.mustHitTimeTip = mustHitConfig.getString("????????????");
        MustHitListener.mustHitSuccessTip = mustHitConfig.getString("????????????");
        MustHitListener.entityHitMap = new HashMap<>();
        MustHitListener.commandHitMap = new HashMap<>();
        MustHitListener.mustHitMap = new HashMap<>();
        MustHitListener.mustHitTimeMap = new HashMap<>();
    }

    private static void registers() {
        registerPercentageProtection();
        registerPercentageDamage();
        registerPIDamage();
        registerPIRDamage();
        registerMustHit();
    }

    private static void registerPercentageDamage() {
        if (config.getBoolean("???????????????.??????")) {
            if (percentageDamageListener == null) {
                percentageDamageListener = new PercentageDamageListener();
                Bukkit.getPluginManager().registerEvents(percentageDamageListener, getInstance());
                Bukkit.getPluginCommand("percentageA").setExecutor(new PDamageCommand());
            }
            LogSender.sendLog(ChatColor.YELLOW + "?????????" + ChatColor.RED + "???????????????" + ChatColor.AQUA + "-???????????????" + ChatColor.GREEN + "?????????");
        } else {
            EntityDamageByEntityEvent.getHandlerList().unregister(percentageDamageListener);
            percentageDamageListener = null;
            Bukkit.getPluginCommand("percentageA").setExecutor(null);
            LogSender.sendLog(ChatColor.YELLOW + "?????????" + ChatColor.RED + "???????????????" + ChatColor.RED + "?????????");
        }

    }

    private static void registerPercentageProtection() {
        if (config.getBoolean("???????????????.??????")) {
            if (percentageProtectionListener == null) {
                percentageProtectionListener = new PercentageProtectionListener();
                Bukkit.getPluginManager().registerEvents(percentageProtectionListener, getInstance());
            }
            LogSender.sendLog(ChatColor.YELLOW + "?????????" + ChatColor.DARK_BLUE + "???????????????" + ChatColor.GREEN + "?????????");
        } else {
            EntityDamageEvent.getHandlerList().unregister(percentageProtectionListener);
            percentageProtectionListener = null;
            LogSender.sendLog(ChatColor.YELLOW + "?????????" + ChatColor.DARK_BLUE + "???????????????" + ChatColor.RED + "?????????");
        }
        Bukkit.getPluginCommand("CustomisedAttributes").setExecutor(new ReloadCommand());
    }

    private static void registerPIDamage() {
        if (config.getBoolean("???????????????-????????????.??????")) {
            if (piDamageListener == null) {
                piDamageListener = new PIDamageListener();
                Bukkit.getPluginManager().registerEvents(piDamageListener, getInstance());
                Bukkit.getPluginCommand("percentageID").setExecutor(new PIDamageCommand());
            }
            LogSender.sendLog(ChatColor.YELLOW + "?????????" + ChatColor.LIGHT_PURPLE + "???????????????" + ChatColor.BLUE + "-???????????????" + ChatColor.GREEN + "?????????");
        } else {
            EntityDamageEvent.getHandlerList().unregister(piDamageListener);
            piDamageListener = null;
            Bukkit.getPluginCommand("percentageID").setExecutor(null);
            LogSender.sendLog(ChatColor.YELLOW + "?????????" + ChatColor.LIGHT_PURPLE + "???????????????" + ChatColor.BLUE + "-???????????????" + ChatColor.RED + "?????????");
        }
    }

    private static void registerPIRDamage() {
        if (config.getBoolean("???????????????-????????????.??????")) {
            if (pirDamageListener == null) {
                pirDamageListener = new PIRDamageListener();
                Bukkit.getPluginManager().registerEvents(pirDamageListener, getInstance());
                Bukkit.getPluginCommand("percentageIRD").setExecutor(new PIRDamageCommand());
            }
            LogSender.sendLog(ChatColor.YELLOW + "?????????" + ChatColor.DARK_PURPLE + "???????????????" + ChatColor.DARK_AQUA + "-???????????????" + ChatColor.GREEN + "?????????");
        } else {
            EntityDamageEvent.getHandlerList().unregister(pirDamageListener);
            pirDamageListener = null;
            Bukkit.getPluginCommand("percentageIRD").setExecutor(null);
            LogSender.sendLog(ChatColor.YELLOW + "?????????" + ChatColor.DARK_PURPLE + "???????????????" + ChatColor.DARK_AQUA + "-???????????????" + ChatColor.RED + "?????????");
        }
    }

    private static void registerMustHit() {
        if (config.getBoolean("??????.??????")) {
            if (mustHitListener == null) {
                mustHitListener = new MustHitListener();
                Bukkit.getPluginManager().registerEvents(mustHitListener, getInstance());
                Bukkit.getPluginCommand("mustHit").setExecutor(new MustHitCommand());
            }
            LogSender.sendLog(ChatColor.YELLOW + "?????????" + ChatColor.DARK_PURPLE + "??????" + ChatColor.GREEN + "?????????");
        } else {
            EntityDamageEvent.getHandlerList().unregister(mustHitListener);
            mustHitListener = null;
            Bukkit.getPluginCommand("mustHit").setExecutor(null);
            LogSender.sendLog(ChatColor.YELLOW + "?????????" + ChatColor.DARK_PURPLE + "??????" + ChatColor.RED + "?????????");
        }
    }

    public static String toPatternString(String string) {
        return string.replace("[data]", "([0-9.]+)");
    }

    public static Set<String> getBlackList() {
        return blackList;
    }
}
