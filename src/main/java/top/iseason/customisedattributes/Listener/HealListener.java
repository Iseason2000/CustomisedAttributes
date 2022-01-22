package top.iseason.customisedattributes.Listener;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import top.iseason.customisedattributes.ConfigManager;
import top.iseason.customisedattributes.Events.EntityLoreInHandEvent;
import top.iseason.customisedattributes.Main;
import top.iseason.customisedattributes.Util.Binder;
import top.iseason.customisedattributes.Util.ColorTranslator;
import top.iseason.customisedattributes.Util.PercentageGetter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HealListener implements Listener {
    public static String tip1;
    public static String tip2;
    public static HashMap<UUID, Double> map;
    public static HashSet<UUID> coolDown;
    public static Pattern pattern;

    @EventHandler
    public void onEntityLoreInHandEvent(EntityLoreInHandEvent event) {
        if (event.getDamage() == 0.0D) return;
        List<String> lore = event.getLore();
        double chance = 0.0;
        double health = 0.0;
        for (String s : lore) {
            Matcher matcher = pattern.matcher(s);
            if (matcher.find()) {
                chance = PercentageGetter.formatString(matcher.group(1));
                health = PercentageGetter.formatString(matcher.group(2));
                break;
            }
        }
        if (chance == 0.0 || health == 0.0) return;
        if (ConfigManager.getDoubleRandom() > chance / 100.0D) {
            return;
        }
        UUID uniqueId = event.getAttacker().getUniqueId();
        map.put(uniqueId, health);
        new BukkitRunnable() {
            @Override
            public void run() {
                map.remove(uniqueId);
            }
        }.runTaskAsynchronously(Main.getInstance());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
        if (event.isCancelled()) return;
        Entity attacker = event.getDamager();
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
        if (ConfigManager.getBlackList().contains(handItem.getType().toString()) && !isArrow) return;
        UUID uniqueId = attacker.getUniqueId();
        if (coolDown.contains(uniqueId)) return;
        Entity entity1 = event.getEntity();
        if (!(entity1 instanceof LivingEntity)) return;
        LivingEntity entity = (LivingEntity) entity1;
        if (!Binder.checkBind(damager, handItem)) return;
        if (!map.containsKey(uniqueId)) return;
        Double health = map.get(uniqueId);
        double maxHealth = entity.getMaxHealth();
        double h = entity.getHealth() + health;
        if (h > maxHealth) h = maxHealth;
        entity.setHealth(h);
        event.setCancelled(true);
        coolDown.add(uniqueId);
        new BukkitRunnable() {
            @Override
            public void run() {
                coolDown.remove(uniqueId);
            }
        }.runTaskLaterAsynchronously(Main.getInstance(), 7L);
        if (entity instanceof Player && damager instanceof Player) {
            if (tip2 != null && !tip2.isEmpty()) {
                ((Player) entity).sendMessage(ColorTranslator.toColor(tip2.replace("[player]", ((Player) damager).getName()).replace("[data]", String.format("%.0f", health))));

            }
        }
    }
}

