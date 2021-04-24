package top.iseason.customisedattributes.Listener;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import top.iseason.customisedattributes.ConfigManager;
import top.iseason.customisedattributes.Util.ColorTranslator;

import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Iseason
 */
public class PercentageDamageListener implements Listener {
    public static Pattern damagePattern;
    public static String PDCTip;
    public static String PDTip;
    public static HashMap<Player, Double> attackList;
    public static double playerMaxP;
    public static double otherMaxP;

    @EventHandler(priority = EventPriority.HIGHEST)
    public void entityDamageByEntityEvent(EntityDamageByEntityEvent e) {
        Entity attacker = e.getDamager();
        boolean isArrow = false;
        if (attacker instanceof Projectile && (
                (Projectile) attacker).getShooter() instanceof Player) {
            attacker = (Entity) ((Projectile) attacker).getShooter();
            isArrow = true;
        }
        if (!(attacker instanceof Player)) {
            return;
        }
        Player damager = (Player) attacker;
        ItemStack handItem = damager.getItemInHand();
        double percentage = 0.0D, chane = 0.0D;
        boolean skipRandom = false;
        if (handItem.hasItemMeta()) {
            ItemMeta handItemMeta = handItem.getItemMeta();
            if (handItemMeta.hasLore()) {
                List<String> loreList = handItemMeta.getLore();
                for (String lore : loreList) {
                    Matcher matcher = damagePattern.matcher(ColorTranslator.noColor(lore));
                    if (!matcher.find()) {
                        continue;
                    }
                    chane = Double.parseDouble(matcher.group(1));
                    percentage = Double.parseDouble(matcher.group(2));
                }
            }
        }
        if (attackList.containsKey(attacker)) {
            percentage = attackList.get(attacker);
            skipRandom = true;
            attackList.remove(attacker);
        }
        if (ConfigManager.getBlackList().contains(handItem.getType().toString()) && !isArrow && !skipRandom) {
            return;
        }
        if (!skipRandom) {
            if (ConfigManager.getDoubleRandom() > chane / 100.0D) {
                return;
            }
        }
        Entity target = e.getEntity();
        if (!(target instanceof Player) && percentage >= otherMaxP) {
            percentage = otherMaxP;
        }
        if (target instanceof Player && percentage >= playerMaxP) {
            percentage = playerMaxP;
        }
        double maxHealth = ((LivingEntity) target).getMaxHealth();
        double realDamage = maxHealth * percentage / 100.0D;
        e.setDamage(EntityDamageEvent.DamageModifier.MAGIC, realDamage + e.getDamage(EntityDamageEvent.DamageModifier.MAGIC));
        damager.sendMessage(ColorTranslator.toColor(PDTip.replace("[data]", String.valueOf(percentage))));
    }
}
