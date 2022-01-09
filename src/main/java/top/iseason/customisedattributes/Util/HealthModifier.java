package top.iseason.customisedattributes.Util;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import top.iseason.customisedattributes.Main;

public class HealthModifier {
    public static void heal(Player player, Double num) {
        double maxHealth = player.getMaxHealth();
        double health = player.getHealth();
        double heal = health + num;
        if (heal >= maxHealth) {
            player.setHealth(maxHealth);
            return;
        }
        player.setHealth(heal);
    }

    public static void healPercent(Player player, Double percent) {
        if (percent < 0) return;
        heal(player, player.getMaxHealth() * percent);
    }

    public static double toDouble(String s) {
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException ignored) {
            return 0.0;
        }
    }

    public static int toInt(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    public static class Timer extends BukkitRunnable {
        LivingEntity entity;
        double num;

        public Timer(LivingEntity entity, double num, int tick) {
            this.entity = entity;
            this.num = num;
            double v = entity.getMaxHealth() + num;
            if (v <= 0) {
                entity.setHealth(0);
                return;
            }
            entity.setMaxHealth(v);
            runTaskLater(Main.getInstance(), tick);
        }

        @Override
        public void run() {
            entity.setMaxHealth(entity.getMaxHealth() - num);
            cancel();
        }
    }
}
