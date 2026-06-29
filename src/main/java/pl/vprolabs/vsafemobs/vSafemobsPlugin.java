// Made by vProLabs [www.vprolabs.xyz]

package pl.vprolabs.vsafemobs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import pl.vprolabs.vsafemobs.commands.SpawnCommand;
import pl.vprolabs.vsafemobs.listeners.MobListener;
import pl.vprolabs.vsafemobs.managers.MobManager;

public class vSafemobsPlugin extends JavaPlugin {

    private static vSafemobsPlugin instance;
    private MobManager mobManager;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private boolean vapiInstalled = false;

    @Override
    public void onLoad() {
        File vapiFile = new File("plugins", "vAPI.jar");
        if (!vapiFile.exists()) {
            getLogger().info("╔══════════════════════════════════════════╗");
            getLogger().info("║  vAPI not found! Auto-downloading...    ║");
            getLogger().info("╚══════════════════════════════════════════╝");
            try {
                HttpURLConnection conn = (HttpURLConnection) URI.create("https://www.vprolabs.xyz/api/download").toURL().openConnection();
                conn.setInstanceFollowRedirects(true);
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(10000);
                try (InputStream in = conn.getInputStream();
                     FileOutputStream out = new FileOutputStream(vapiFile)) {
                    in.transferTo(out);
                }
                vapiInstalled = true;
                getLogger().info("╔══════════════════════════════════════════╗");
                getLogger().info("║  vAPI downloaded to plugins/vAPI.jar    ║");
                getLogger().info("║  Server restart required to activate!   ║");
                getLogger().info("╚══════════════════════════════════════════╝");
            } catch (Exception e) {
                getLogger().severe("Failed to download vAPI: " + e.getMessage());
                getLogger().severe("Download manually: https://www.vprolabs.xyz/api/download");
            }
        }
    }

    @Override
    public void onEnable() {
        // === vAPI dependency check ===
        if (Bukkit.getPluginManager().getPlugin("vAPI") == null) {
            getLogger().severe("╔══════════════════════════════════════════╗");
            getLogger().severe("║  vAPI IS REQUIRED but not loaded!      ║");
            getLogger().severe("╚══════════════════════════════════════════╝");
            if (vapiInstalled) {
                startRestartReminder();
            } else {
                getLogger().severe("Download: https://www.vprolabs.xyz/api/download");
                getLogger().severe("Place vAPI.jar in plugins/ folder and restart.");
            }
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

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

    private void startRestartReminder() {
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            String msg = "§8[§cvPlugins§8] §evAPI has been installed. §cServer requires a restart §eto initialize it.";
            Bukkit.getOnlinePlayers().stream()
                .filter(p -> p.isOp() || p.hasPermission("vplugind.admin"))
                .forEach(p -> p.sendMessage(msg));
            getLogger().warning("vAPI has been installed. Server requires a restart to initialize it.");
        }, 0L, 6000L);
    }
}
