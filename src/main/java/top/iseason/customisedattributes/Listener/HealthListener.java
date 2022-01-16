package top.iseason.customisedattributes.Listener;

import Test.GetStatsBonusEvent;
import Test.StatsEnum;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import top.iseason.customisedattributes.ConfigManager;
import top.iseason.customisedattributes.Events.EntityLoreInHandEvent;
import top.iseason.customisedattributes.Main;
import top.iseason.customisedattributes.Util.ColorTranslator;
import top.iseason.customisedattributes.Util.HealthModifier;
import top.iseason.customisedattributes.Util.PercentageGetter;

import java.util.HashMap;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HealthListener implements Listener {
    public static HashMap<UUID, String[]> attackMap;
    public static String RTip;
    public static String RTip2;
    public static Pattern pattern;
    private static HealthModifier.HandItemTimer timer;

    public HealthListener() {
        timer = new HealthModifier.HandItemTimer();
        timer.runTaskTimerAsynchronously(Main.getInstance(), 0L, 10L);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityLoreInHandEvent1(EntityLoreInHandEvent event) {
        double chance = 0.0;
        String health = "";
        String time = "";
        String time2 = "";
        for (String s : event.getLore()) {
            Matcher matcher = pattern.matcher(ColorTranslator.noColor(s));
            if (matcher.find()) {
                //不是百分比
                chance = PercentageGetter.formatString(matcher.group(1));
                String heal = matcher.group(2);
                health = String.valueOf(PercentageGetter.formatString(heal));
                if (heal.contains("%")) health = health.concat("%");
                time = String.valueOf((int) (HealthModifier.toDouble(matcher.group(3)) * 20));
                time2 = matcher.group(3);
                break;
            }
        }
        if (ConfigManager.getDoubleRandom() > chance / 100.0D) {
            return;
        }
        String[] strings = new String[2];
        strings[0] = health;
        strings[1] = time;
        LivingEntity attacker = event.getAttacker();
        UUID uniqueId = attacker.getUniqueId();
        if (attacker instanceof Player) {
            if (RTip2 != null && !RTip.isEmpty()) {
                ((Player) attacker).sendMessage(ColorTranslator.toColor(RTip2.replace("[data]", health).replace("[time]", time2)));
            }
        }
        attackMap.put(uniqueId, strings);
    }

    //下次攻击生效的目标
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityLoreInHandEvent2(EntityLoreInHandEvent event) {
        LivingEntity attacker = event.getAttacker();
        UUID uniqueId = attacker.getUniqueId();
        if (!attackMap.containsKey(uniqueId)) return;
        Entity entity1 = event.getEntity();
        if (!(entity1 instanceof LivingEntity)) return;
        LivingEntity entity = (LivingEntity) entity1;
        String[] strings = attackMap.get(uniqueId);
        String health = strings[0];
        int tick = HealthModifier.toInt(strings[1]);
        new HealthModifier.Timer(entity, "-".concat(health), tick).start();
        if (entity instanceof Player) {
            if (RTip != null && !RTip.isEmpty()) {
                ((Player) entity).sendMessage(ColorTranslator.toColor(RTip.replace("[data]", health).replace("[time]", String.valueOf(tick / 20.0))));
            }
        }
        attackMap.remove(uniqueId);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        HealthModifier.Timer.remove(player);
        HealthModifier.HandItemTimer.remove(player);
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity instanceof Player) {
            Player player = (Player) entity;
            HealthModifier.Timer.remove(player);
            HealthModifier.HandItemTimer.remove(player);
        }
    }

    //防止血量被重置
    @EventHandler
    public void onGetStatsBonusEvent(GetStatsBonusEvent event) {
        if (event.getStatsEnum() != StatsEnum.HEALTH) return;
        LivingEntity entity = event.getEntity();
        HealthModifier.Timer timer = HealthModifier.Timer.modifierMap.get(entity.getUniqueId());
        if (timer != null) {
            event.setValue(event.getValue() + timer.num);
        }
        Double aDouble = HealthModifier.HandItemTimer.playerMap.get(entity.getUniqueId());
        if (aDouble != null) {
            event.setValue(event.getValue() + aDouble);
        }
    }
}
