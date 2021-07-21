package top.iseason.customisedattributes.Listener;

import Test.GetStatsBonusEvent;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.projectiles.ProjectileSource;
import top.iseason.customisedattributes.ConfigManager;
import top.iseason.customisedattributes.Util.ColorTranslator;

import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static Test.StatsEnum.DODGE;


/**
 * @author Iseason
 */

public class MustHitListener implements Listener {
    public static Pattern mustHitPattern;
    public static String mustHitOnceTip;
    public static String mustHitTimeTip;
    public static String mustHitSuccessTip;
    public static HashMap<LivingEntity, LivingEntity> entityHitMap;
    public static HashMap<LivingEntity, Player> commandHitMap;
    public static HashMap<LivingEntity, ItemStack> mustHitMap;
    public static HashMap<LivingEntity, ItemStack> mustHitTimeMap;

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityDamageByEntityEvent1(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();
        Entity entity = event.getEntity();
        if (!(entity instanceof LivingEntity)) {
            return;
        }
        LivingEntity attacker;
        if (damager instanceof Projectile) {
            ProjectileSource projectileSource = ((Projectile) damager).getShooter();
            if (projectileSource instanceof LivingEntity) {
                attacker = (LivingEntity) projectileSource;
            } else {
                return;
            }
        } else if (damager instanceof LivingEntity) {
            attacker = (LivingEntity) damager;
        } else {
            return;
        }

        ItemStack item = attacker.getEquipment().getItemInHand();
        if (item == null) {
            return;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return;
        }
        if (!meta.hasLore()) {
            return;
        }
        List<String> loreList = meta.getLore();
        double percentage = 0.0;
        for (String lore : loreList) {
            Matcher matcher = mustHitPattern.matcher(ColorTranslator.noColor(lore));
            if (!matcher.find()) {
                continue;
            }
            percentage += Double.parseDouble(matcher.group(1));
        }
        if (percentage == 0.0) {
            return;
        }
        if (ConfigManager.getDoubleRandom() > percentage / 100) {
            return;
        }//成功触发
        entityHitMap.put((LivingEntity) entity, attacker);
        mustHitMap.put(attacker, item);
    }

    @EventHandler//设置必中
    public void onGetStatsBonusEvent(GetStatsBonusEvent event) {
        //被攻击者
        LivingEntity entity = event.getEntity();
        LivingEntity attacker = entityHitMap.get(entity);
        entityHitMap.remove(entity);
        if (event.getStatsEnum() != DODGE) {
            return;
        }
        //攻击者
        if (attacker == null) {
            attacker = commandHitMap.get(entity);
        }
        if (attacker == null) {
            return;
        }
        ItemStack item = attacker.getEquipment().getItemInHand();
        if ((mustHitMap.containsKey(attacker) && mustHitMap.containsValue(item)) ||
                (mustHitTimeMap.containsKey(attacker) && mustHitTimeMap.containsValue(item))) {
            mustHitMap.remove(attacker, item);
            event.setValue(0.0);
            if (attacker instanceof Player && entity instanceof Player) {
                if (mustHitSuccessTip != null && !mustHitSuccessTip.isEmpty()) {
                    ((Player) attacker).sendMessage(ColorTranslator
                            .toColor(mustHitSuccessTip.replace("[data]", ((Player) entity).getName())));
                }
            }
        }

    }

    @EventHandler(priority = EventPriority.LOWEST)//设置必中
    public void onEntityDamageByEntityEvent2(EntityDamageByEntityEvent event) {
        if (event.isCancelled()) {
            return;
        }
        Entity entity = event.getEntity();
        if (!(entity instanceof LivingEntity)) {
            return;
        }
        Entity damager = event.getDamager();
        Player player;
        //获取攻击者玩家 不是玩家返回
        if (damager instanceof Player) {
            player = (Player) damager;
        } else if (damager instanceof Projectile) {
            ProjectileSource projectileSource = ((Projectile) damager).getShooter();
            if (!(projectileSource instanceof Player)) {
                return;
            }
            player = (Player) projectileSource;
        } else {
            return;
        }
        ItemStack item = player.getEquipment().getItemInHand();
        if ((mustHitMap.containsKey(player) && mustHitMap.containsValue(item)) ||
                (mustHitTimeMap.containsKey(player) && mustHitTimeMap.containsValue(item))) {
            commandHitMap.put((LivingEntity) entity, player);
        }

    }

}
