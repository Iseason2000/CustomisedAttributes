package top.iseason.customisedattributes.Listener;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import top.iseason.customisedattributes.ConfigManager;
import top.iseason.customisedattributes.Events.PercentageEvent;
import top.iseason.customisedattributes.Util.ColorTranslator;
import top.iseason.customisedattributes.Util.PercentageGetter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProtectionBreakerListener implements Listener {
    public static Pattern keyPattern;
    public static String effectMessage;
    public static String commandMessage;
    public static HashMap<Player, Double> pbList;
    public static HashSet<ItemStack> itemSet;

    @EventHandler
    public void onPercentageEvent(PercentageEvent event) {
        if (event.isCancelled()) return;
        Entity entity = event.getEntity();
        boolean isArrow = false;
        if (entity instanceof Projectile && (
                (Projectile) entity).getShooter() instanceof Player) {
            entity = (LivingEntity) ((Projectile) entity).getShooter();
            isArrow = true;
        }
        if (!(entity instanceof Player)) {
            return;
        }
        Player damager = (Player) entity;
        ItemStack itemInHand = damager.getEquipment().getItemInHand();
        if (itemInHand == null || !itemInHand.hasItemMeta()) return;
        ItemMeta itemMeta = itemInHand.getItemMeta();
        if (!itemMeta.hasLore()) return;
        List<String> loreList = itemMeta.getLore();
        double percentage = 0.0D, chance = 0.0D;
        boolean skipRandom = false;
        for (String lore : loreList) {
            Matcher matcher = keyPattern.matcher(ColorTranslator.noColor(lore));
            if (!matcher.find()) {
                continue;
            }
            chance = PercentageGetter.formatString(matcher.group(1));
            percentage = PercentageGetter.formatString(matcher.group(2));
            break;
        }
        if (pbList.containsKey(damager) && itemSet.contains(itemInHand)) {
            percentage = pbList.get(damager);
            skipRandom = true;
            pbList.remove(damager);
            itemSet.remove(itemInHand);
        }

        if (ConfigManager.getBlackList().contains(itemInHand.getType().toString()) && !isArrow && !skipRandom) {
            return;
        }
        if (!skipRandom) {
            if (ConfigManager.getDoubleRandom() > chance / 100.0D) {
                return;
            }
        }
        event.setPercentage((event.getPercentage() - percentage) <= 0 ? 0 : (event.getPercentage() - percentage));
        if (effectMessage != null && !effectMessage.isEmpty()) {
            damager.sendMessage(ColorTranslator.toColor(effectMessage.replace("[data]", String.valueOf(percentage > 100 ? 100 : percentage))));
        }
    }
}

