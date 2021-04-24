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
import org.bukkit.projectiles.ProjectileSource;
import top.iseason.customisedattributes.ConfigManager;
import top.iseason.customisedattributes.Util.ColorTranslator;

import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * @author Iseason
 */

public class MustHitListener implements Listener {
    public static Pattern mustHitPattern;
    public static String mustHitOnceTip;
    public static String mustHitTimeTip;
    public static String mustHitSuccessTip;
    public static HashSet<Player> mustHitSet;
    public static HashSet<Player> mustHitTimeSet;

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityDamageByEntityEvent2(EntityDamageByEntityEvent event) {
        Entity entity = event.getDamager();
        if (!(entity instanceof Player)) {
            return;
        }
        Player player = (Player) entity;
        ItemStack item = player.getEquipment().getItemInHand();
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
        mustHitSet.add(player);
    }

    @EventHandler(priority = EventPriority.HIGHEST)//设置必中
    public void onEntityDamageByEntityEvent1(EntityDamageByEntityEvent event) {
        Entity entity = event.getDamager();
        Player player;
        //获取攻击者玩家 不是玩家返回
        if (entity instanceof Player) {
            player = (Player) entity;
        } else if (entity instanceof Projectile) {
            ProjectileSource projectileSource = ((Projectile) entity).getShooter();
            if (!(projectileSource instanceof Player)) {
                return;
            }
            player = (Player) projectileSource;
        } else {
            return;
        }
        if (!event.isCancelled()) { //判断是否取消
            mustHitSet.remove(player);
            return;
        }
        Entity entity1 = event.getEntity();//受害者
        if (!(entity1 instanceof Player)) {
            return;
        }
        Player livingEntity = (Player) entity1;
        if (mustHitSet.contains(player) || mustHitTimeSet.contains(player)) {
            mustHitSet.remove(player);
            event.setCancelled(false);
            player.sendMessage(ColorTranslator
                    .toColor(mustHitSuccessTip.replace("[data]", livingEntity.getName())));
        }
    }
}
