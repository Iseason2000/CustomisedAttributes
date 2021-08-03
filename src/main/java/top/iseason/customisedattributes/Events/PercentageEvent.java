package top.iseason.customisedattributes.Events;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PercentageEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private Entity entity;
    private double percentage;
    private boolean isCancelled = false;

    public PercentageEvent(Entity entity, double percentage) {
        this.entity = entity;
        this.percentage = percentage;
        Bukkit.getPluginManager().callEvent(this);
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public Entity getEntity() {
        return entity;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        if (isCancelled) return;
        this.percentage = percentage;
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        isCancelled = b;
    }
}
