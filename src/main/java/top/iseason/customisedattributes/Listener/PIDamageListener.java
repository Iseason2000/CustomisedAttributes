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
public class PIDamageListener implements Listener {
    public static Pattern iDChancePattern;  //普通伤害概率模板
    public static Pattern iDPattern;        //普通伤害倍率模板
    public static String IDTip;             //普 触发提示
    public static String IDCTip;             //普 命令提示
    public static String IDCTip2;             //普 命令提示2
    public static HashMap<UUID, Double> iDList; //下次必定增加普通伤害列表

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
        double percentage = 0.0D, chance = 0.0D;
        boolean skipRandom = false;
        if (handItem.hasItemMeta()) {
            ItemMeta handItemMeta = handItem.getItemMeta();
            if (handItemMeta.hasLore()) {
                List<String> loreList = handItemMeta.getLore();
                int count = 0;
                for (String lore : loreList) {
                    Matcher matcher = iDChancePattern.matcher(ColorTranslator.noColor(lore));
                    if (matcher.find()) {
                        String nextLore = loreList.get(count + 1);
                        chance = PercentageGetter.formatString(matcher.group(1));
                        Matcher matcher2 = iDPattern.matcher(ColorTranslator.noColor(nextLore));
                        if (!matcher2.find()) {
                            count++;
                            continue;
                        }
                        percentage = PercentageGetter.formatString(matcher2.group(1));
                        break;
                    }
                    count++;
                }
            }
        }
        if (chance != 0.0D && percentage == 0.0D) {
            return;
        }

        if (!Binder.checkBind(damager, handItem)) return;
        UUID uniqueId = damager.getUniqueId();
        if (iDList.containsKey(uniqueId)) {
            percentage = iDList.get(uniqueId);
            if (percentage < 0) {
                iDList.remove(uniqueId);
                Binder.remove(damager);
            }
            percentage = abs(percentage);
            skipRandom = true;
        }
        if (ConfigManager.getBlackList().contains(handItem.getType().toString()) && !isArrow) {
            return;
        }
        if (!skipRandom) {
            if (ConfigManager.getDoubleRandom() > chance / 100.0D) {
                return;
            }
        }
        e.setDamage(e.getDamage() + damager.getMaxHealth() * percentage / 100.0D);
        if (damager instanceof Player && IDTip != null && !IDTip.isEmpty()) {
            ((Player) damager).sendMessage(ColorTranslator.toColor(IDTip.replace("[data]", String.format("%.0f", percentage))));
        }
    }
}
