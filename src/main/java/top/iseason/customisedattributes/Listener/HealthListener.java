package top.iseason.customisedattributes.Listener;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import top.iseason.customisedattributes.ConfigManager;
import top.iseason.customisedattributes.Main;
import top.iseason.customisedattributes.Util.ColorTranslator;
import top.iseason.customisedattributes.Util.HealthModifier;
import top.iseason.customisedattributes.Util.PercentageGetter;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HealthListener implements Listener {
    public static HashMap<UUID, String[]> attackMap;
    public static String RTip;
    public static String RTip2;
    public static Pattern pattern;
    private static HealthModifier.HandItemTimer timer;

    public HealthListener() {
        timer = new HealthModifier.HandItemTimer();
        timer.runTaskTimerAsynchronously(Main.getInstance(), 0L, 10L);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamageByEntityEventLore(EntityDamageByEntityEvent event) {
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
        if (!itemInHand.hasItemMeta()) return;
        if (ConfigManager.getBlackList().contains(itemInHand.getType().toString()) && !isArrow) return;
        ItemMeta itemMeta = itemInHand.getItemMeta();
        if (!itemMeta.hasLore()) return;
        List<String> lore = itemMeta.getLore();
        double chance = 0.0;
        String health = "";
        String time = "";
        String time2 = "";
        for (String s : lore) {
            Matcher matcher = pattern.matcher(ColorTranslator.noColor(s));
            if (matcher.find()) {
                //不是百分比
                chance = PercentageGetter.formatString(matcher.group(1));
                String heal = matcher.group(2);
                health = String.valueOf(PercentageGetter.formatString(heal));
                if (heal.contains("%")) health = health.concat("%");
                time = String.valueOf((int) (HealthModifier.toDouble(matcher.group(3)) * 20));
                time2 = matcher.group(3);
                break;
            }
        }
        if (ConfigManager.getDoubleRandom() > chance / 100.0D) {
            return;
        }
        String[] strings = new String[2];
        strings[0] = health;
        strings[1] = time;
        UUID uniqueId = damager.getUniqueId();
        if (damager instanceof Player) {
            if (RTip2 != null && !RTip.isEmpty()) {
                ((Player) damager).sendMessage(ColorTranslator.toColor(RTip2.replace("[data]", health).replace("[time]", time2)));
            }
        }
        attackMap.put(uniqueId, strings);
    }

    //下次攻击生效的目标
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
        if (event.isCancelled()) {
            return;
        }
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
        UUID uniqueId = damager.getUniqueId();
        if (!attackMap.containsKey(uniqueId)) return;
        ItemStack itemInHand = damager.getEquipment().getItemInHand();
        if (itemInHand == null) {
            return;
        }
        if (ConfigManager.getBlackList().contains(itemInHand.getType().toString()) && !isArrow) return;
        Entity entity1 = event.getEntity();
        if (!(entity1 instanceof LivingEntity)) return;
        LivingEntity entity = (LivingEntity) entity1;
        String[] strings = attackMap.get(uniqueId);
        String health = strings[0];
        int tick = HealthModifier.toInt(strings[1]);
        new HealthModifier.Timer(entity, "-".concat(health), tick).start();
        if (entity instanceof Player) {
            if (RTip != null && !RTip.isEmpty()) {
                ((Player) entity).sendMessage(ColorTranslator.toColor(RTip.replace("[data]", health).replace("[time]", String.valueOf(tick / 20.0))));
            }
        }
        attackMap.remove(uniqueId);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        HealthModifier.Timer.remove(player);
        HealthModifier.HandItemTimer.remove(player);
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity instanceof Player) {
            Player player = (Player) entity;
            HealthModifier.Timer.remove(player);
            HealthModifier.HandItemTimer.remove(player);
        }
    }
}