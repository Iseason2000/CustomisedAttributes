package top.iseason.customisedattributes.Listener;

import org.bukkit.entity.Entity;
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
public class PIRDamageListener implements Listener {
    public static Pattern iRDChancePattern; //真实伤害概率模板
    public static Pattern iRDPattern;       //真实伤害倍率模板
    public static String IRDTip;            //真 触发提示
    public static String IRDCTip;            //真 命令提示
    public static HashMap<Player, Double> iRDList; //下次必定增加真实伤害列表

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
        double percentage = 0.0D, chance = 0.0D;
        boolean skipRandom = false;
        if (handItem.hasItemMeta()) {
            ItemMeta handItemMeta = handItem.getItemMeta();
            if (handItemMeta.hasLore()) {
                List<String> loreList = handItemMeta.getLore();
                int count = 0;
                for (String lore : loreList) {
                    Matcher matcher = iRDChancePattern.matcher(ColorTranslator.noColor(lore));
                    if (matcher.find()) {
                        String nextLore = loreList.get(count + 1);
                        chance = Double.parseDouble(matcher.group(1));
                        Matcher matcher2 = iRDPattern.matcher(ColorTranslator.noColor(nextLore));
                        if (!matcher2.find()) {
                            continue;
                        }
                        percentage = Double.parseDouble(matcher2.group(1));
                        break;
                    }
                    count++;
                }
            }
        }
        if (chance != 0.0D && percentage == 0.0D) {
            return;
        }
        if (iRDList.containsKey(attacker)) {
            percentage = iRDList.get(attacker);
            skipRandom = true;
            iRDList.remove(attacker);
        }
        if (ConfigManager.getBlackList().contains(handItem.getType().toString()) && !isArrow && !skipRandom) {
            return;
        }
        if (!skipRandom) {
            if (ConfigManager.getDoubleRandom() > chance / 100.0D) {
                return;
            }
        }
        damager.sendMessage(ColorTranslator.toColor(IRDTip.replace("[data]", String.valueOf(percentage))));
        e.setDamage(EntityDamageEvent.DamageModifier.ARMOR, e.getDamage(EntityDamageEvent.DamageModifier.ARMOR) + damager.getHealth() * percentage / 100.0D);
    }
}
