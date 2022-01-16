package top.iseason.customisedattributes.Listener;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import top.iseason.customisedattributes.ConfigManager;
import top.iseason.customisedattributes.Events.EntityLoreInHandEvent;
import top.iseason.customisedattributes.Main;
import top.iseason.customisedattributes.Util.PercentageGetter;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BleedListener implements Listener {
    public static Pattern pattern1;
    public static Pattern pattern2;
    public static Pattern pattern3;
    public static Pattern pattern4;

    @EventHandler
    public void onEntityLoreInHandEvent(EntityLoreInHandEvent event) {
        List<String> lore = event.getLore();
        double chance = 0.0;
        String damage = "";
        double time = 0.0;
        double period = 1.0;
        for (int i = 0; i < lore.size() - 2; i++) {
            String key1 = lore.get(i);
            String key2 = lore.get(i + 1);
            String key3 = lore.get(i + 2);
            Matcher m1 = pattern1.matcher(key1);
            Matcher m2 = pattern2.matcher(key2);
            Matcher m3 = pattern3.matcher(key3);
            if (!m1.find() || !m2.find() || !m3.find()) continue;
            chance = PercentageGetter.formatString(m1.group(1));
            damage = m2.group(1);
            time = PercentageGetter.formatString(m3.group(1));
            if (i + 3 < lore.size()) {
                String s = lore.get(i + 3);
                Matcher matcher = pattern4.matcher(s);
                if (matcher.find()) {
                    period = PercentageGetter.formatString(matcher.group(1));
                }
            }
            break;
        }
        if (chance == 0.0 || damage.isEmpty() || time == 0.0) return;
//        System.out.println(chance);
//        System.out.println(damage);
//        System.out.println(time);
//        System.out.println(period);
        if (ConfigManager.getDoubleRandom() > chance / 100.0D) {
            return;
        }
        Entity entity = event.getEntity();
        if (!(entity instanceof LivingEntity)) return;
        LivingEntity entity1 = (LivingEntity) entity;
        double finalTime = time;
        long p = new Double(period * 20).longValue();
        String finalDamage = damage;
        new BukkitRunnable() {
            final long endTime = System.currentTimeMillis() + new Double(finalTime * 1000).longValue();

            @Override
            public void run() {
                if (System.currentTimeMillis() >= endTime || entity1.isDead()) {
                    cancel();
                    return;
                }
                double v = PercentageGetter.formatString(finalDamage);
                if (entity1.getHealth() - v <= 1.0) {
                    entity1.damage(0);
                    entity1.setHealth(1.0);
                } else
                    entity1.damage(PercentageGetter.formatString(finalDamage));
            }
        }.runTaskTimerAsynchronously(Main.getInstance(), p, p);
    }
}
