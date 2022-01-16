package top.iseason.customisedattributes.Listener;

import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import top.iseason.customisedattributes.Events.EntityLoreInHandEvent;
import top.iseason.customisedattributes.Main;
import top.iseason.customisedattributes.Util.ColorTranslator;
import top.iseason.customisedattributes.Util.PercentageGetter;

import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DamageRangeListener implements Listener {
    private static final HashSet<LivingEntity> swiping = new HashSet<>();
    public static Pattern pattern;

    @EventHandler
    public void onEntityDamageByEntityEvent(EntityLoreInHandEvent event) {
        LivingEntity attacker = event.getAttacker();
        if (swiping.contains(attacker)) {
            return;
        }
        double range = 0.0;
        for (String s : event.getLore()) {
            Matcher matcher = pattern.matcher(ColorTranslator.noColor(s));
            if (matcher.find()) {
                //不是百分比
                range = PercentageGetter.formatString(matcher.group(1));
                break;
            }
        }
        if (range == 0.0) return;
        swiping.add(attacker);
        Entity entity = event.getEntity();
        for (Entity nearbyEntity : entity.getNearbyEntities(range, range, range)) {
            if (nearbyEntity == attacker) continue;
            if (nearbyEntity == entity) continue;
            if (!(nearbyEntity instanceof Damageable)) continue;
            new BukkitRunnable() {
                @Override
                public void run() {
                    ((Damageable) nearbyEntity).damage(event.getDamage(), attacker);
                }
            }.runTask(Main.getInstance());
        }
        new BukkitRunnable() {
            public void run() {
                swiping.remove(attacker);
            }
        }.runTask(Main.getInstance());

    }
}
