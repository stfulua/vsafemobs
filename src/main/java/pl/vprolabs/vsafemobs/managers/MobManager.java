// Made by vProLabs [www.vprolabs.xyz]

package pl.vprolabs.vsafemobs.managers;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.*;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.command.CommandSender;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import pl.vprolabs.vsafemobs.vSafemobsPlugin;

import java.util.*;

public class MobManager {

    private final vSafemobsPlugin plugin;
    private final MiniMessage miniMessage;
    private final NamespacedKey safeMobKey;
    private final NamespacedKey noAttackKey;
    private final NamespacedKey leadableKey;
    private final NamespacedKey customModelKey;
    private final NamespacedKey mobIdKey;
    
    // Track spawned safe mobs by their ID
    private final Map<String, UUID> trackedMobs = new HashMap<>();
    private int nextMobId = 1;

    public MobManager(vSafemobsPlugin plugin) {
        this.plugin = plugin;
        this.miniMessage = plugin.getMiniMessage();
        this.safeMobKey = new NamespacedKey(plugin, "safemob");
        this.noAttackKey = new NamespacedKey(plugin, "noattack");
        this.leadableKey = new NamespacedKey(plugin, "leadable");
        this.customModelKey = new NamespacedKey(plugin, "custommodel");
        this.mobIdKey = new NamespacedKey(plugin, "mobid");
    }

    public void spawnMob(Player player, EntityType entityType, Map<String, String> attributes) {
        Location location = player.getLocation();
        LivingEntity entity = (LivingEntity) player.getWorld().spawnEntity(location, entityType);

        if (entity == null) {
            player.sendMessage(miniMessage.deserialize("<red>Failed to spawn entity!</red>"));
            return;
        }

        // Generate unique ID for this mob
        String mobId = String.valueOf(nextMobId++);
        trackedMobs.put(mobId, entity.getUniqueId());

        entity.getPersistentDataContainer().set(safeMobKey, PersistentDataType.BYTE, (byte) 1);
        entity.getPersistentDataContainer().set(mobIdKey, PersistentDataType.STRING, mobId);

        // Apply attributes
        for (Map.Entry<String, String> entry : attributes.entrySet()) {
            applyAttribute(entity, entry.getKey(), entry.getValue());
        }

        // If noattack is set, apply additional safety measures
        if (attributes.containsKey("noattack") && Boolean.parseBoolean(attributes.get("noattack"))) {
            applyNoAttack(entity);
        }

        // If leadable is set, mark the mob
        if (attributes.containsKey("leadable") && Boolean.parseBoolean(attributes.get("leadable"))) {
            entity.getPersistentDataContainer().set(leadableKey, PersistentDataType.BYTE, (byte) 1);
        }

        player.sendMessage(miniMessage.deserialize("<green>Successfully spawned <yellow>" + entityType.name().toLowerCase() + "</yellow> with ID <aqua>" + mobId + "</aqua>!</green>"));
    }

    private void applyAttribute(LivingEntity entity, String key, String value) {
        switch (key.toLowerCase()) {
            case "noattack" -> {
                if (Boolean.parseBoolean(value)) {
                    entity.getPersistentDataContainer().set(noAttackKey, PersistentDataType.BYTE, (byte) 1);
                }
            }
            case "leadable" -> {
                // Handled in spawnMob after all attributes are processed
            }
            case "silent" -> entity.setSilent(Boolean.parseBoolean(value));
            case "invulnerable" -> entity.setInvulnerable(Boolean.parseBoolean(value));
            case "persistent" -> entity.setRemoveWhenFarAway(!Boolean.parseBoolean(value));
            case "baby" -> {
                if (entity instanceof Ageable ageable) {
                    if (Boolean.parseBoolean(value)) {
                        ageable.setBaby();
                    } else {
                        ageable.setAdult();
                    }
                }
            }
            case "glow" -> entity.setGlowing(Boolean.parseBoolean(value));
            case "nogravity" -> entity.setGravity(!Boolean.parseBoolean(value));
            case "custommodel" -> {
                try {
                    int modelData = Integer.parseInt(value);
                    EntityEquipment equipment = entity.getEquipment();
                    if (equipment != null) {
                        ItemStack item = new ItemStack(org.bukkit.Material.STICK);
                        item.editMeta(meta -> meta.setCustomModelData(modelData));
                        equipment.setItemInMainHand(item);
                        equipment.setItemInMainHandDropChance(0f);
                    }
                    entity.getPersistentDataContainer().set(customModelKey, PersistentDataType.INTEGER, modelData);
                } catch (NumberFormatException ignored) {}
            }
            case "health" -> {
                try {
                    double health = Double.parseDouble(value);
                    AttributeInstance maxHealth = entity.getAttribute(Attribute.MAX_HEALTH);
                    if (maxHealth != null) {
                        maxHealth.setBaseValue(health);
                        entity.setHealth(health);
                    }
                } catch (NumberFormatException ignored) {}
            }
            case "speed" -> {
                try {
                    double speed = Double.parseDouble(value);
                    AttributeInstance movementSpeed = entity.getAttribute(Attribute.MOVEMENT_SPEED);
                    if (movementSpeed != null) {
                        movementSpeed.setBaseValue(speed);
                    }
                } catch (NumberFormatException ignored) {}
            }
            case "scale" -> {
                try {
                    double scale = Double.parseDouble(value);
                    AttributeInstance scaleAttr = entity.getAttribute(Attribute.SCALE);
                    if (scaleAttr != null) {
                        scaleAttr.setBaseValue(scale);
                    }
                } catch (NumberFormatException | NullPointerException ignored) {}
            }
            case "name" -> {
                String name = value.replace("\"", "");
                name = name.replace('&', '§');
                Component nameComponent = LegacyComponentSerializer.legacySection().deserialize(name);
                entity.customName(nameComponent);
                entity.setCustomNameVisible(true);
            }
            case "team" -> {
                String teamName = value.replace("\"", "");
                Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
                Team team = scoreboard.getTeam(teamName);
                if (team == null) {
                    team = scoreboard.registerNewTeam(teamName);
                }
                team.addEntity(entity);
            }
        }
    }

    private void applyNoAttack(LivingEntity entity) {
        // Mark with metadata for easy checking
        entity.setMetadata("noattack", new FixedMetadataValue(plugin, true));
        
        // Store in persistent data container
        entity.getPersistentDataContainer().set(noAttackKey, PersistentDataType.BYTE, (byte) 1);

        // Immediately clear any target
        if (entity instanceof Mob mob) {
            mob.setTarget(null);
        }

        // For Wardens - they need special handling due to their anger system
        if (entity instanceof Warden warden) {
            // Clear anger for all online players and constantly monitor
            Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                if (warden.isDead()) return;
                
                // Clear anger for all players
                for (Player player : Bukkit.getOnlinePlayers()) {
                    warden.clearAnger(player);
                }
                warden.setTarget(null);
            }, 1L, 1L); // Run every tick
        }

        // For Zombies and other aggressive mobs - constantly clear target
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (entity.isDead()) return;
            
            if (entity instanceof Mob mob) {
                mob.setTarget(null);
            }
        }, 1L, 1L); // Run every tick for maximum safety
    }

    // Kill a specific mob by ID
    public boolean killMob(String mobId, CommandSender sender) {
        UUID uuid = trackedMobs.get(mobId);
        if (uuid == null) {
            // Try to find by checking all entities in all worlds
            for (org.bukkit.World world : Bukkit.getWorlds()) {
                for (Entity entity : world.getEntities()) {
                    if (entity instanceof LivingEntity livingEntity) {
                        String entityMobId = getMobId(livingEntity);
                        if (mobId.equals(entityMobId)) {
                            uuid = entity.getUniqueId();
                            break;
                        }
                    }
                }
                if (uuid != null) break;
            }
        }

        if (uuid == null) {
            return false;
        }

        for (org.bukkit.World world : Bukkit.getWorlds()) {
            Entity entity = world.getEntity(uuid);
            if (entity instanceof LivingEntity livingEntity && isSafeMob(livingEntity)) {
                livingEntity.remove();
                trackedMobs.remove(mobId);
                return true;
            }
        }

        return false;
    }

    // Kill all safe mobs
    public int killAllMobs() {
        int count = 0;
        
        for (org.bukkit.World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (entity instanceof LivingEntity livingEntity && isSafeMob(livingEntity)) {
                    livingEntity.remove();
                    count++;
                }
            }
        }
        
        trackedMobs.clear();
        return count;
    }

    // List all tracked mobs
    public Map<String, UUID> getTrackedMobs() {
        return new HashMap<>(trackedMobs);
    }

    public String getMobId(LivingEntity entity) {
        PersistentDataContainer container = entity.getPersistentDataContainer();
        if (container.has(mobIdKey, PersistentDataType.STRING)) {
            return container.get(mobIdKey, PersistentDataType.STRING);
        }
        return null;
    }

    public boolean isSafeMob(LivingEntity entity) {
        PersistentDataContainer container = entity.getPersistentDataContainer();
        return container.has(safeMobKey, PersistentDataType.BYTE);
    }

    public boolean isLeadable(LivingEntity entity) {
        PersistentDataContainer container = entity.getPersistentDataContainer();
        return container.has(leadableKey, PersistentDataType.BYTE);
    }
}
