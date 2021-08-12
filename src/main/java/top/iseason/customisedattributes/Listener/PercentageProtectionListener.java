package top.iseason.customisedattributes.Listener;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
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
        if (!(entity instanceof Player)) {
            return;
        }
        Player player = (Player) entity;
        ItemStack[] eq = player.getInventory().getArmorContents();
        List<ItemStack> eqItem = new ArrayList<>(Arrays.asList(eq));
        eqItem.add(player.getItemInHand());
        double percentage = 0.0;
        for (ItemStack item : eqItem) {
            if (item == null) {
                continue;
            }
            if (item.getType() == Material.AIR) {
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
        percentage = new PercentageEvent(damager, percentage).getPercentage();
        UUID uniqueId = player.getUniqueId();
        percentage += playerMap.getOrDefault(uniqueId, 0D);
        if (percentage <= 0.0) {
            return;
        }
        if (percentage >= 100.0) {
            e.setDamage(ABSORPTION, 0);
            e.setDamage(ARMOR, 0);
            e.setDamage(BASE, 0);
            e.setDamage(BLOCKING, 0);
            e.setDamage(MAGIC, 0);
        } else {
            double damage = e.getFinalDamage(); //获取受到的伤害(集合MAGIC、BLOCKING这些)
            double deDamage = damage * (percentage / 100.0);//计算应减伤害
            //重设伤害参数
            e.setDamage(MAGIC, 0);
            e.setDamage(BLOCKING, 0);
            e.setDamage(RESISTANCE, 0);
            e.setDamage(ARMOR, -deDamage);
            e.setDamage(BASE, damage);
            //最终伤害等于上面全部加起来
        }

    }
}
