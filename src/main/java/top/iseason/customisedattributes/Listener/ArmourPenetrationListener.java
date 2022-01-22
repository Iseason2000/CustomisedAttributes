package top.iseason.customisedattributes.Listener;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import top.iseason.customisedattributes.ConfigManager;
import top.iseason.customisedattributes.Events.EntityLoreInHandEvent;
import top.iseason.customisedattributes.Main;
import top.iseason.customisedattributes.Util.Binder;
import top.iseason.customisedattributes.Util.PercentageGetter;

import java.util.HashMap;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ArmourPenetrationListener implements Listener {
    public static Pattern pattern;
    public static String commandTip;
    public static HashMap<UUID, Double> map;

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityLoreInHandEvent(EntityLoreInHandEvent event) {
        if (event.getDamage() == 0.0D) return;
        UUID uniqueId = event.getAttacker().getUniqueId();
        if (map.containsKey(uniqueId)) return;
        double percent = 0.0;
        for (String s : event.getLore()) {
            Matcher matcher = pattern.matcher(s);
            if (matcher.find()) {
                percent = PercentageGetter.formatString(matcher.group(1));
                break;
            }
        }
        if (percent == 0.0) return;

        map.put(uniqueId, percent);
        new BukkitRunnable() {
            @Override
            public void run() {
                map.remove(uniqueId);
            }
        }.runTaskAsynchronously(Main.getInstance());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
        if (event.isCancelled()) {
            return;
        }
        if (event.getFinalDamage() == 0.0D) return;
        Entity attacker = event.getDamager();
        LivingEntity damager = null;
        boolean isArrow = false;
        if (attacker instanceof Projectile && (
                (Projectile) attacker).getShooter() instanceof LivingEntity) {
            damager = (LivingEntity) ((Projectile) attacker).getShooter();
            isArrow = true;
        } else if (attacker instanceof LivingEntity) {
            damager = (LivingEntity) attacker;
        }
        if (damager == null) return;
        ItemStack itemInHand = damager.getEquipment().getItemInHand();
        if (itemInHand == null) return;
        if (!Binder.checkBind(damager, itemInHand)) return;
        if (ConfigManager.getBlackList().contains(itemInHand.getType().toString()) && !isArrow) return;
        UUID uniqueId = damager.getUniqueId();
        if (!map.containsKey(uniqueId)) return;
        Double aDouble = map.get(uniqueId);
        if (ConfigManager.getDoubleRandom() > aDouble / 100.0D) {
            return;
        }
        event.setDamage(EntityDamageEvent.DamageModifier.ARMOR, 0);
    }
}
