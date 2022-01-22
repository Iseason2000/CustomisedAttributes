package top.iseason.customisedattributes.Listener;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import top.iseason.customisedattributes.ConfigManager;
import top.iseason.customisedattributes.Events.EntityLoreInHandEvent;
import top.iseason.customisedattributes.Util.ColorTranslator;

import java.util.ArrayList;
import java.util.List;

public class EventListener implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
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
        List<String> lore = new ArrayList<>();
        for (String s : itemMeta.getLore()) {
            lore.add(ColorTranslator.noColor(s));
        }
        new EntityLoreInHandEvent(damager, event.getEntity(), itemInHand, lore, event.getDamage(), event);
    }
}
