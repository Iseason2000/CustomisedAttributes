package top.iseason.customisedattributes.Listener;

import Test.GetStatsBonusEvent;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import top.iseason.customisedattributes.ConfigManager;
import top.iseason.customisedattributes.Events.EntityLoreInHandEvent;
import top.iseason.customisedattributes.Util.Binder;
import top.iseason.customisedattributes.Util.ColorTranslator;
import top.iseason.customisedattributes.Util.PercentageGetter;

import java.util.HashMap;
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
    public void onEntityLoreInHandEvent(EntityLoreInHandEvent event) {
        double percentage = 0.0;
        Entity entity = event.getEntity();
        if (!(entity instanceof LivingEntity)) return;
        for (String lore : event.getLore()) {
            Matcher matcher = mustHitPattern.matcher(ColorTranslator.noColor(lore));
            if (!matcher.find()) {
                continue;
            }
            percentage += PercentageGetter.formatString(matcher.group(1));
        }
        if (percentage == 0.0) {
            return;
        }
        if (ConfigManager.getDoubleRandom() > percentage / 100) {
            return;
        }//成功触发
        LivingEntity attacker = event.getAttacker();
        entityHitMap.put((LivingEntity) entity, attacker);
        mustHitMap.put(attacker, event.getItemInHand());
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
    public void onEntityLoreInHandEvent2(EntityLoreInHandEvent event) {
        ItemStack item = event.getItemInHand();
        LivingEntity attacker = event.getAttacker();
        if (!Binder.checkBind(attacker, item)) return;
        Entity entity = event.getEntity();
        if (!(entity instanceof LivingEntity)) return;
        if (!(attacker instanceof Player)) return;
        if ((mustHitMap.containsKey(attacker) && mustHitMap.containsValue(item)) ||
                (mustHitTimeMap.containsKey(attacker) && mustHitTimeMap.containsValue(item))) {
            commandHitMap.put((LivingEntity) entity, (Player) attacker);
        }
    }

}
