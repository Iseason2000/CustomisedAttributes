package top.iseason.customisedattributes.Util;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import top.iseason.customisedattributes.Main;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    public static class HandItemTimer extends BukkitRunnable {
        public static Pattern lorePattern;
        public static HashMap<UUID, Double> playerMap = new HashMap<>();

        public static void remove(Player player) {
            UUID uniqueId = player.getUniqueId();
            Double aDouble = playerMap.get(player.getUniqueId());
            if (aDouble != null) {
                player.setMaxHealth(player.getMaxHealth() - aDouble);
                playerMap.remove(uniqueId);
            }
        }

        @Override
        public void run() {
            Collection<? extends Player> onlinePlayers = Main.getInstance().getServer().getOnlinePlayers();
            onlinePlayers.forEach(player -> {
                ItemStack itemInHand = player.getItemInHand();
                boolean hasLore = false;
                if (itemInHand != null) {
                    if (itemInHand.hasItemMeta()) {
                        ItemMeta itemMeta = itemInHand.getItemMeta();
                        hasLore = itemMeta.hasLore();
                    }
                }
                UUID uniqueId = player.getUniqueId();
                boolean hasFind = false;
                if (hasLore) {
                    List<String> loreList = itemInHand.getItemMeta().getLore();
                    for (String lore : loreList) {
                        Matcher matcher = lorePattern.matcher(lore);
                        if (matcher.find()) {
                            hasFind = true;
                            String group = matcher.group(1);
                            double num;
                            if (group.contains("%")) {
                                String replace = group.replace("%", "");
                                num = HealthModifier.toDouble(replace) / 100.0 * player.getMaxHealth();
                            } else {
                                num = HealthModifier.toDouble(group);
                            }
                            Double aDouble = playerMap.get(uniqueId);
                            if (aDouble != null) {
                                if (aDouble == num) continue;
                                else player.setMaxHealth(player.getMaxHealth() - aDouble);
                            }
                            player.setMaxHealth(player.getMaxHealth() + num);
                            playerMap.put(uniqueId, num);
                            return;
                        }
                    }
                }
                if (!hasFind && playerMap.containsKey(uniqueId)) {
                    player.setMaxHealth(player.getMaxHealth() - playerMap.get(uniqueId));
                    playerMap.remove(uniqueId);
                }
            });
        }
    }

    public static class Timer extends BukkitRunnable {
        public static HashMap<UUID, Timer> modifierMap;
        public final double num;
        private final int tick;
        private final LivingEntity entity;

        public Timer(LivingEntity entity, String num, int tick) {
            this.entity = entity;
            this.tick = tick;
            Timer timer = modifierMap.get(entity.getUniqueId());
            if (timer != null) {
                timer.run();
            }
            if (num.contains("%")) {
                String replace = num.replace("%", "");
                this.num = HealthModifier.toDouble(replace) / 100.0 * entity.getMaxHealth();
            } else {
                this.num = HealthModifier.toDouble(num);
            }
        }

        public static void reset() {
            if (modifierMap != null) {
                modifierMap.forEach((K, V) -> V.run());
                modifierMap.clear();
            }
            modifierMap = new HashMap<>();
        }

        public static void remove(Player player) {
            UUID uniqueId = player.getUniqueId();
            Timer timer = modifierMap.get(uniqueId);
            if (timer == null) return;
            timer.run();
        }

        public void start() {
            UUID uniqueId = entity.getUniqueId();

            double v = entity.getMaxHealth() + num;
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
            entity.setMaxHealth(entity.getMaxHealth() - num);
            modifierMap.remove(entity.getUniqueId());
            cancel();
        }
    }
}
