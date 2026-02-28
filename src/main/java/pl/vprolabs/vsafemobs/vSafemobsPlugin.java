// Made by vProLabs [www.vprolabs.xyz]

package pl.vprolabs.vsafemobs;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.plugin.java.JavaPlugin;
import pl.vprolabs.vsafemobs.commands.SpawnCommand;
import pl.vprolabs.vsafemobs.listeners.MobListener;
import pl.vprolabs.vsafemobs.managers.MobManager;

public class vSafemobsPlugin extends JavaPlugin {

    private static vSafemobsPlugin instance;
    private MobManager mobManager;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    @Override
    public void onEnable() {
        instance = this;
        this.mobManager = new MobManager(this);

        SpawnCommand spawnCommand = new SpawnCommand(this);
        getCommand("vsafemobs").setExecutor(spawnCommand);
        getCommand("vsafemobs").setTabCompleter(spawnCommand);

        // Register event listener for noattack functionality
        getServer().getPluginManager().registerEvents(new MobListener(this), this);

        getLogger().info("vSafemobs v1.0.0 enabled successfully!");
        getLogger().info("Made by vProLabs [www.vprolabs.xyz]");
    }

    @Override
    public void onDisable() {
        getLogger().info("vSafemobs disabled.");
    }

    public static vSafemobsPlugin getInstance() {
        return instance;
    }

    public MobManager getMobManager() {
        return mobManager;
    }

    public MiniMessage getMiniMessage() {
        return miniMessage;
    }
}
