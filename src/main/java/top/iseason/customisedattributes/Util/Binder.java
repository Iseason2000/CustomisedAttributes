package top.iseason.customisedattributes.Util;


import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import top.iseason.customisedattributes.Main;

import java.util.HashMap;

public class Binder {
    public static HashMap<LivingEntity, ItemStack> itemBinder = new HashMap<>();

    public static void bind(Player player, ItemStack item) {
        if (item == null) remove(player);
        itemBinder.put(player, item);
        new BukkitRunnable() {
            @Override
            public void run() {
                remove(player);
            }
        }.runTaskLaterAsynchronously(Main.getInstance(), 200L);
    }

    public static boolean checkBind(LivingEntity player, ItemStack item) {
        if (item == null) return false;
        ItemStack itemStack = itemBinder.get(player);
        if (itemStack == null) return true;
        return itemStack.equals(item);
    }

    public static void remove(LivingEntity player) {
        itemBinder.remove(player);
    }
}
