package top.iseason.customisedattributes.Util;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import top.iseason.customisedattributes.Listener.HealthListener;
import top.iseason.customisedattributes.Main;

import java.util.HashMap;
import java.util.UUID;

public class HealthTimer extends BukkitRunnable {
    public static HashMap<UUID, HealthTimer> modifierMap;
    private final int tick;
    private final LivingEntity entity;
    public int num;

    public HealthTimer(LivingEntity entity, String num, int tick) {
        this.entity = entity;
        this.tick = tick;
        if (num.contains("%")) {
            String replace = num.replace("%", "");
            this.num = (int) (PercentageGetter.formatString(replace) / 100.0 * entity.getMaxHealth());
        } else {
            this.num = (int) PercentageGetter.formatString(num);
        }
        if (num.startsWith("-")) this.num = -this.num;
        remove(entity.getUniqueId());
    }

    public static void reset() {
        if (modifierMap != null) {
            modifierMap.forEach((K, V) -> V.run());
            modifierMap.clear();
        }
        modifierMap = new HashMap<>();
    }

    public static void remove(UUID uuid) {
        HealthTimer timer = modifierMap.get(uuid);
        if (timer == null) return;
        timer.run();
    }

    public void start() {
        UUID uniqueId = entity.getUniqueId();

        double v = entity.getMaxHealth() + num;
        if (entity instanceof Player) {
            if (this.num > 0 && HealthListener.Tip2 != null && !HealthListener.Tip2.isEmpty()) {
                ((Player) entity).sendMessage(ColorTranslator.toColor(HealthListener.Tip2.replace("[data]", String.valueOf(num)).replace("[time]", String.valueOf(tick / 20.0))));
            }
            if (this.num < 0 && HealthListener.Tip1 != null && !HealthListener.Tip1.isEmpty()) {
                ((Player) entity).sendMessage(ColorTranslator.toColor(HealthListener.Tip2.replace("[data]", String.valueOf(num)).replace("[time]", String.valueOf(tick / 20.0))));
            }
        }
        modifierMap.put(uniqueId, this);
        if (v <= 0) {
            entity.setHealth(0);
            return;
        }
        entity.setMaxHealth(v);
        runTaskLater(Main.getInstance(), tick);
    }

    @Override
    public void run() {
        double v = (entity.getMaxHealth() - num);
        entity.setMaxHealth(v);
        modifierMap.remove(entity.getUniqueId());
        cancel();
    }
}
