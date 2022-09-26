package top.iseason.customisedattributes.Listener;

import Test.LoreAttributes;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import top.iseason.customisedattributes.Events.PercentageEvent;
import top.iseason.customisedattributes.Util.ColorTranslator;
import top.iseason.customisedattributes.Util.PercentageGetter;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.bukkit.event.entity.EntityDamageEvent.DamageModifier.*;

/**
 * @author Iseason
 */
public class PercentageProtectionListener implements Listener {
    public static Pattern protectPattern;
    public static HashMap<UUID, Double> playerMap;
    public static String commandMessage;

    @EventHandler(priority = EventPriority.MONITOR)
    public void entityDamageEvent(EntityDamageByEntityEvent e) {
        if (e.isCancelled()) {
            return;
        }
        Entity entity = e.getEntity();
        if (!(entity instanceof LivingEntity)) {
            return;
        }
//        System.out.println(e.getDamage(ABSORPTION));
//        System.out.println(e.getDamage(ARMOR));
//        System.out.println(e.getDamage(BASE));
//        System.out.println(e.getDamage(BLOCKING));
//        System.out.println(e.getDamage(MAGIC));
        LivingEntity livingEntity = (LivingEntity) entity;
        ItemStack[] eq = livingEntity.getEquipment().getArmorContents();
        List<ItemStack> eqItem = new ArrayList<>(Arrays.asList(eq));
        if (livingEntity instanceof Player) {
            Player player = (Player) livingEntity;
            List<Integer> artifactslots = LoreAttributes.config.getIntegerList("artifactslots");
            String artifactkeyword = LoreAttributes.config.getString("artifactkeyword");
            for (Integer artifactslot : artifactslots) {
                ItemStack item = player.getInventory().getItem(artifactslot);
                if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasLore()) continue;
                List<String> lore = item.getItemMeta().getLore();
                for (String s : lore) {
                    if (s.contains(artifactkeyword)) {
                        eqItem.add(item);
                        break;
                    }
                }
            }
        }
        eqItem.add(livingEntity.getEquipment().getItemInHand());
        double percentage = 0.0;
        for (ItemStack item : eqItem) {
            if (item == null) {
                continue;
            }
            if (!item.hasItemMeta()) {
                continue;
            }
            if (!item.getItemMeta().hasLore()) {
                continue;
            }
            List<String> loreList = item.getItemMeta().getLore();
            for (String lore : loreList) {
                Matcher matcher = protectPattern.matcher(ColorTranslator.noColor(lore));
                if (!matcher.find()) {
                    continue;
                }
                percentage += PercentageGetter.formatString(matcher.group(1));
            }
        }
        Entity damager = e.getDamager();
        UUID uniqueId = livingEntity.getUniqueId();
        percentage += playerMap.getOrDefault(uniqueId, 0D);
        percentage = new PercentageEvent(damager, percentage).getPercentage();
        if (percentage <= 0.0) {
            return;
        }
        if (percentage >= 100.0) {
            e.setDamage(ABSORPTION, 0);
            e.setDamage(ARMOR, 0);
            e.setDamage(BASE, 0);
            try {
                e.setDamage(BLOCKING, 0);
            } catch (Exception ignored) {
            }
            e.setDamage(MAGIC, 0);
        } else {
            double damage = e.getFinalDamage(); //获取受到的伤害(集合MAGIC、BLOCKING这些)
            double deDamage = damage * (percentage / 100.0);//计算应减伤害
            //重设伤害参数
            e.setDamage(MAGIC, 0);
            try {
                e.setDamage(BLOCKING, 0);
            } catch (Exception ignored) {
            }
            e.setDamage(RESISTANCE, 0);
            e.setDamage(ARMOR, -deDamage);
            e.setDamage(BASE, damage);
            //最终伤害等于上面全部加起来
        }


    }
}
