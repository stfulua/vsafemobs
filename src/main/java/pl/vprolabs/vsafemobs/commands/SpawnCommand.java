// Made by vProLabs [www.vprolabs.xyz]

package pl.vprolabs.vsafemobs.commands;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.vprolabs.vsafemobs.vSafemobsPlugin;

import java.util.*;

public class SpawnCommand implements CommandExecutor, TabCompleter {

    private final vSafemobsPlugin plugin;
    private final MiniMessage miniMessage;
    private final Set<String> availableAttributes;

    public SpawnCommand(vSafemobsPlugin plugin) {
        this.plugin = plugin;
        this.miniMessage = plugin.getMiniMessage();
        this.availableAttributes = new HashSet<>(Arrays.asList(
            "noattack", "silent", "invulnerable", "persistent", "baby",
            "glow", "nogravity", "leadable", "custommodel", "health", "speed", "scale", "name", "team"
        ));
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            sendUsage(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "spawn" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(miniMessage.deserialize("<red>Only players can use this command!</red>"));
                    return true;
                }
                if (!player.hasPermission("vsafemobs.spawn")) {
                    player.sendMessage(miniMessage.deserialize("<red>You don't have permission to use this command!</red>"));
                    return true;
                }
                handleSpawn(player, args);
            }
            case "kill" -> {
                if (!sender.hasPermission("vsafemobs.admin")) {
                    sender.sendMessage(miniMessage.deserialize("<red>You don't have permission to kill mobs!</red>"));
                    return true;
                }
                handleKill(sender, args);
            }
            case "killall" -> {
                if (!sender.hasPermission("vsafemobs.admin")) {
                    sender.sendMessage(miniMessage.deserialize("<red>You don't have permission to kill all mobs!</red>"));
                    return true;
                }
                handleKillAll(sender);
            }
            case "list" -> {
                if (!sender.hasPermission("vsafemobs.admin")) {
                    sender.sendMessage(miniMessage.deserialize("<red>You don't have permission to list mobs!</red>"));
                    return true;
                }
                handleList(sender);
            }
            case "reload" -> {
                if (!sender.hasPermission("vsafemobs.admin")) {
                    sender.sendMessage(miniMessage.deserialize("<red>You don't have permission to reload!</red>"));
                    return true;
                }
                sender.sendMessage(miniMessage.deserialize("<green>vSafemobs reloaded!</green>"));
            }
            default -> sendUsage(sender);
        }

        return true;
    }

    private void handleSpawn(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(miniMessage.deserialize("<red>Usage: /vsm spawn <entityType> [attribute:value]...</red>"));
            return;
        }

        String entityTypeStr = args[1].toUpperCase();
        EntityType entityType;

        try {
            entityType = EntityType.valueOf(entityTypeStr);
        } catch (IllegalArgumentException e) {
            player.sendMessage(miniMessage.deserialize("<red>Invalid entity type: " + args[1] + "</red>"));
            return;
        }

        if (!entityType.isAlive() || !entityType.isSpawnable()) {
            player.sendMessage(miniMessage.deserialize("<red>This entity type cannot be spawned!</red>"));
            return;
        }

        Map<String, String> attributes = new HashMap<>();

        for (int i = 2; i < args.length; i++) {
            String arg = args[i];
            int colonIndex = arg.indexOf(':');

            if (colonIndex == -1) {
                player.sendMessage(miniMessage.deserialize("<red>Invalid attribute format: " + arg + ". Use attribute:value</red>"));
                return;
            }

            String key = arg.substring(0, colonIndex).toLowerCase();
            String val = arg.substring(colonIndex + 1);

            if (!availableAttributes.contains(key)) {
                player.sendMessage(miniMessage.deserialize("<red>Unknown attribute: " + key + "</red>"));
                return;
            }

            attributes.put(key, val);
        }

        plugin.getMobManager().spawnMob(player, entityType, attributes);
    }

    private void handleKill(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(miniMessage.deserialize("<red>Usage: /vsm kill <id></red>"));
            return;
        }

        String mobId = args[1];
        boolean killed = plugin.getMobManager().killMob(mobId, sender);
        
        if (killed) {
            sender.sendMessage(miniMessage.deserialize("<green>Killed mob with ID <yellow>" + mobId + "</yellow>!</green>"));
        } else {
            sender.sendMessage(miniMessage.deserialize("<red>Could not find mob with ID <yellow>" + mobId + "</yellow>!</red>"));
        }
    }

    private void handleKillAll(CommandSender sender) {
        int count = plugin.getMobManager().killAllMobs();
        sender.sendMessage(miniMessage.deserialize("<green>Killed <yellow>" + count + "</yellow> safe mobs!</green>"));
    }

    private void handleList(CommandSender sender) {
        Map<String, java.util.UUID> mobs = plugin.getMobManager().getTrackedMobs();
        
        if (mobs.isEmpty()) {
            sender.sendMessage(miniMessage.deserialize("<yellow>No tracked safe mobs found.</yellow>"));
            return;
        }

        sender.sendMessage(miniMessage.deserialize("<yellow>=== Tracked Safe Mobs ===</yellow>"));
        for (Map.Entry<String, java.util.UUID> entry : mobs.entrySet()) {
            sender.sendMessage(miniMessage.deserialize("<gray>ID: <aqua>" + entry.getKey() + "</aqua> | UUID: " + entry.getValue().toString().substring(0, 8) + "...</gray>"));
        }
        sender.sendMessage(miniMessage.deserialize("<yellow>Total: <aqua>" + mobs.size() + "</aqua> mobs</yellow>"));
    }

    private void sendUsage(CommandSender sender) {
        sender.sendMessage(miniMessage.deserialize("<yellow>=== vSafemobs Commands ===</yellow>"));
        sender.sendMessage(miniMessage.deserialize("<gold>/vsm spawn <entityType> [attribute:value]...</gold> <gray>- Spawn a safe mob</gray>"));
        sender.sendMessage(miniMessage.deserialize("<gold>/vsm kill <id></gold> <gray>- Kill a specific safe mob</gray>"));
        sender.sendMessage(miniMessage.deserialize("<gold>/vsm killall</gold> <gray>- Kill all safe mobs</gray>"));
        sender.sendMessage(miniMessage.deserialize("<gold>/vsm list</gold> <gray>- List all tracked safe mobs</gray>"));
        sender.sendMessage(miniMessage.deserialize("<gold>/vsm reload</gold> <gray>- Reload plugin</gray>"));
        sender.sendMessage(miniMessage.deserialize(""));
        sender.sendMessage(miniMessage.deserialize("<yellow>Available Attributes:</yellow>"));
        sender.sendMessage(miniMessage.deserialize("<gray>noattack, silent, invulnerable, persistent, baby, glow, nogravity, leadable</gray>"));
        sender.sendMessage(miniMessage.deserialize("<gray>custommodel:<integer>, health:<double>, speed:<double>, scale:<double></gray>"));
        sender.sendMessage(miniMessage.deserialize("<gray>name:<string>, team:<string></gray>"));
        sender.sendMessage(miniMessage.deserialize(""));
        sender.sendMessage(miniMessage.deserialize("<yellow>Examples:</yellow>"));
        sender.sendMessage(miniMessage.deserialize("<gray>/vsm spawn warden noattack:true leadable:true name:\"&6Guardian\" scale:1.5</gray>"));
        sender.sendMessage(miniMessage.deserialize("<gray>/vsm spawn zombie noattack:true invulnerable:true baby:true glow:true</gray>"));
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.add("spawn");
            if (sender.hasPermission("vsafemobs.admin")) {
                completions.add("kill");
                completions.add("killall");
                completions.add("list");
                completions.add("reload");
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("spawn")) {
            if (!(sender instanceof Player)) {
                return completions;
            }
            for (EntityType type : EntityType.values()) {
                if (type.isAlive() && type.isSpawnable()) {
                    completions.add(type.name().toLowerCase());
                }
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("kill")) {
            // List tracked mob IDs
            completions.addAll(plugin.getMobManager().getTrackedMobs().keySet());
        } else if (args.length >= 3 && args[0].equalsIgnoreCase("spawn")) {
            String lastArg = args[args.length - 1].toLowerCase();

            if (lastArg.contains(":")) {
                int colonIndex = lastArg.indexOf(':');
                String prefix = lastArg.substring(0, colonIndex + 1);
                String partialValue = lastArg.substring(colonIndex + 1);

                switch (prefix) {
                    case "noattack:", "silent:", "invulnerable:", "persistent:", "baby:", "glow:", "nogravity:", "leadable:" -> {
                        if ("true".startsWith(partialValue)) completions.add(prefix + "true");
                        if ("false".startsWith(partialValue)) completions.add(prefix + "false");
                    }
                    case "custommodel:" -> {
                        completions.add(prefix + "1");
                        completions.add(prefix + "100");
                    }
                    case "health:" -> {
                        completions.add(prefix + "20");
                        completions.add(prefix + "100");
                    }
                    case "speed:" -> {
                        completions.add(prefix + "0.5");
                        completions.add(prefix + "1.0");
                        completions.add(prefix + "2.0");
                    }
                    case "scale:" -> {
                        completions.add(prefix + "0.5");
                        completions.add(prefix + "1.0");
                        completions.add(prefix + "1.5");
                        completions.add(prefix + "2.0");
                    }
                    case "name:" -> {
                        completions.add(prefix + "\"Guardian\"");
                        completions.add(prefix + "\"&6CustomName\"");
                    }
                    case "team:" -> {
                        completions.add(prefix + "safemobs");
                        for (org.bukkit.scoreboard.Team team : Bukkit.getScoreboardManager().getMainScoreboard().getTeams()) {
                            completions.add(prefix + team.getName());
                        }
                    }
                }
            } else {
                for (String attr : availableAttributes) {
                    if (attr.startsWith(lastArg)) {
                        completions.add(attr + ":");
                    }
                }
            }
        }

        return completions;
    }
}
