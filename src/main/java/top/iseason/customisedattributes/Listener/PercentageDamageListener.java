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
import top.iseason.customisedattributes.Util.Binder;
import top.iseason.customisedattributes.Util.ColorTranslator;
import top.iseason.customisedattributes.Util.PercentageGetter;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Math.abs;

/**
 * @author Iseason
 */
public class PercentageDamageListener implements Listener {
    public static Pattern damagePattern;
    public static String PDCTip;
    public static String PDCTip2;
    public static String PDTip;
    public static HashMap<UUID, Double> attackList;
    public static double playerMaxP;
    public static double otherMaxP;

    @EventHandler(priority = EventPriority.HIGHEST)
    public void entityDamageByEntityEvent(EntityDamageByEntityEvent e) {
        if (e.isCancelled()) return;
        Entity attacker = e.getDamager();
        boolean isArrow = false;
        if (attacker instanceof Projectile && (
                (Projectile) attacker).getShooter() instanceof LivingEntity) {
            attacker = (Entity) ((Projectile) attacker).getShooter();
            isArrow = true;
        }
        if (!(attacker instanceof LivingEntity)) {
            return;
        }
        LivingEntity damager = (LivingEntity) attacker;
        ItemStack handItem = damager.getEquipment().getItemInHand();
        if (handItem == null) return;
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
                    chane = PercentageGetter.formatString(matcher.group(1));
                    percentage = PercentageGetter.formatString(matcher.group(2));
                }
            }
        }
        if (!Binder.checkBind(damager, handItem)) return;
        UUID uniqueId = damager.getUniqueId();
        if (attackList.containsKey(uniqueId)) {
            percentage = attackList.get(uniqueId);
            if (percentage < 0) {
                attackList.remove(uniqueId);
                Binder.remove(damager);
            }
            skipRandom = true;
            percentage = abs(percentage);
        }
        if (ConfigManager.getBlackList().contains(handItem.getType().toString()) && !isArrow) {
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
        if (damager instanceof Player && PDTip != null && !PDTip.isEmpty()) {
            ((Player) damager).sendMessage(ColorTranslator.toColor(PDTip.replace("[data]", String.format("%.0f", percentage))));
        }
    }
}
