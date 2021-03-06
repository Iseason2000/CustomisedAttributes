package top.iseason.customisedattributes.Listener;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import top.iseason.customisedattributes.Util.ColorTranslator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.bukkit.event.entity.EntityDamageEvent.DamageModifier.*;

/**
 * @author Iseason
 */
public class PercentageProtectionListener implements Listener {
    public static Pattern protectPattern;

    @EventHandler(priority = EventPriority.MONITOR)
    public void entityDamageEvent(EntityDamageEvent e) {
        if (e.isCancelled()) {
            return;
        }
        Entity player = e.getEntity();
        if (!(player instanceof Player)) {
            return;
        }
        ItemStack[] eq = ((Player) player).getInventory().getArmorContents();
        List<ItemStack> eqItem = new ArrayList<>(Arrays.asList(eq));
        eqItem.add(((Player) player).getItemInHand());
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
                percentage += Double.parseDouble(matcher.group(1));
            }
        }
        if (percentage == 0.0) {
            return;
        }
        if (percentage >= 100.0) {
            e.setDamage(ABSORPTION, 0);
            e.setDamage(ARMOR, 0);
            e.setDamage(BASE, 0);
            e.setDamage(BLOCKING, 0);
            e.setDamage(MAGIC, 0);
        } else {
            double damage = e.getFinalDamage(); //?????????????????????(??????MAGIC???BLOCKING??????)
            double deDamage = damage * (percentage / 100.0);//??????????????????
            //??????????????????
            e.setDamage(MAGIC, 0);
            e.setDamage(BLOCKING, 0);
            e.setDamage(RESISTANCE, 0);
            e.setDamage(ARMOR, -deDamage);
            e.setDamage(BASE, damage);
            //???????????????????????????????????????
        }

    }
}
