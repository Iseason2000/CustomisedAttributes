package top.iseason.customisedattributes.Util;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
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
        public static HashMap<UUID, String> lastString = new HashMap<>();

        public static void remove(Player player) {
            UUID uniqueId = player.getUniqueId();
            Double aDouble = playerMap.get(player.getUniqueId());
            if (aDouble != null) {
                double v = player.getMaxHealth() - aDouble;
                if (player.getHealth() > v) player.setHealth(v);
                player.setMaxHealth(v);
                playerMap.remove(uniqueId);
                lastString.remove(uniqueId);
            }
        }

        @Override
        public void run() {
            Collection<? extends Player> onlinePlayers = Main.getInstance().getServer().getOnlinePlayers();
            onlinePlayers.forEach(player -> {
                ItemStack itemInHand = player.getItemInHand();
                UUID uniqueId = player.getUniqueId();
                //无有效物品在手
                if (itemInHand == null || !itemInHand.hasItemMeta() || !itemInHand.getItemMeta().hasLore()) {
                    if (playerMap.containsKey(uniqueId)) {
                        remove(player);
                    }
                    return;
                }
                List<String> loreList = itemInHand.getItemMeta().getLore();
                for (String lore : loreList) {
                    Matcher matcher = lorePattern.matcher(ColorTranslator.noColor(lore));
                    if (!matcher.find()) {
                        continue;
                    }
                    String group = matcher.group(1);
                    String str = lastString.get(uniqueId);
                    //之前存在且效果一样
                    if (group.equals(str)) {
                        return;
                    } else {
                        remove(player);
                    }
                    //解析
                    double num;
                    if (group.contains("%")) {
                        String replace = group.replace("%", "");
                        num = HealthModifier.toDouble(replace) / 100.0 * player.getMaxHealth();
                    } else {
                        num = HealthModifier.toDouble(group);
                    }
                    double v = player.getMaxHealth() + num;
                    if (player.getHealth() > v) player.setHealth(v);
                    player.setMaxHealth(v);
                    playerMap.put(uniqueId, num);
                    lastString.put(uniqueId, group);
                    break;
                }
            });
        }
    }


}
