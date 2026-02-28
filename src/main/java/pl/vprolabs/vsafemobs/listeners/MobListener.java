// Made by vProLabs [www.vprolabs.xyz]

package pl.vprolabs.vsafemobs.listeners;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import pl.vprolabs.vsafemobs.vSafemobsPlugin;

public class MobListener implements Listener {

    private final vSafemobsPlugin plugin;
    private final NamespacedKey safeMobKey;
    private final NamespacedKey leadableKey;

    public MobListener(vSafemobsPlugin plugin) {
        this.plugin = plugin;
        this.safeMobKey = new NamespacedKey(plugin, "safemob");
        this.leadableKey = new NamespacedKey(plugin, "leadable");
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityTarget(EntityTargetEvent event) {
        Entity entity = event.getEntity();
        
        if (!(entity instanceof LivingEntity livingEntity)) {
            return;
        }

        if (isSafeMob(livingEntity)) {
            // Prevent safe mob from targeting anything
            event.setCancelled(true);
            event.setTarget(null);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();
        
        // Handle direct attacks from mobs
        if (damager instanceof LivingEntity livingDamager && isSafeMob(livingDamager)) {
            event.setCancelled(true);
            return;
        }

        // Handle projectiles (arrows, tridents, etc.) shot by safe mobs
        if (damager instanceof Projectile projectile) {
            if (projectile.getShooter() instanceof LivingEntity shooter && isSafeMob(shooter)) {
                event.setCancelled(true);
            }
        }

        // Handle area effect clouds (lingering potions) from safe mobs
        if (damager instanceof AreaEffectCloud cloud) {
            if (cloud.getSource() instanceof LivingEntity source && isSafeMob(source)) {
                event.setCancelled(true);
            }
        }

        // Handle evoker fangs
        if (damager instanceof EvokerFangs fangs) {
            if (fangs.getOwner() instanceof LivingEntity owner && isSafeMob(owner)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof LivingEntity entity)) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        // Check if player is trying to use a lead on a leadable safe mob
        if (item.getType() == org.bukkit.Material.LEAD && isLeadable(entity)) {
            // Allow leashing even for normally unleasheable mobs (like Wardens)
            if (!entity.isLeashed()) {
                entity.setLeashHolder(player);
                
                // Consume one lead from player's hand
                if (item.getAmount() > 1) {
                    item.setAmount(item.getAmount() - 1);
                } else {
                    player.getInventory().setItemInMainHand(null);
                }
                
                event.setCancelled(true);
            }
        }
    }

    private boolean isSafeMob(LivingEntity entity) {
        PersistentDataContainer container = entity.getPersistentDataContainer();
        return container.has(safeMobKey, PersistentDataType.BYTE);
    }

    private boolean isLeadable(LivingEntity entity) {
        PersistentDataContainer container = entity.getPersistentDataContainer();
        return container.has(leadableKey, PersistentDataType.BYTE);
    }
}
