package top.iseason.customisedattributes.Events;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class EntityLoreInHandEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final LivingEntity attacker;
    private final ItemStack itemInHand;
    private final Entity entity;
    private final List<String> lore;
    private final double damage;
    private final EntityDamageByEntityEvent parent;
    private boolean isCancelled = false;

    public EntityLoreInHandEvent(LivingEntity attacker, Entity entity, ItemStack itemInHand, List<String> lore, double damage, EntityDamageByEntityEvent parent) {
        this.attacker = attacker;
        this.lore = lore;
        this.entity = entity;
        this.itemInHand = itemInHand;
        this.damage = damage;
        this.parent = parent;

    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public EntityDamageByEntityEvent getParent() {
        return parent;
    }

    public double getDamage() {
        return damage;
    }

    public ItemStack getItemInHand() {
        return itemInHand;
    }

    public LivingEntity getAttacker() {
        return attacker;
    }

    public List<String> getLore() {
        return lore;
    }

    public Entity getEntity() {
        return entity;
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        this.isCancelled = b;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
