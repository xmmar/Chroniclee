package ansneeze;

import ansneeze.commands.ChronicleMineCommands;
import ansneeze.commands.ChronicleMinesMenu;
import ansneeze.commands.MinaPrefixListener;
import ansneeze.utilidades.MinasConfig;
import ansneeze.utilidades.MinasMenuConfig;
import ansneeze.utilidades.mensaje;
import net.luckperms.api.LuckPerms;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class ChronicleMines extends JavaPlugin {
    public static String prefix = "&#D8F454&l&oChronicle &#DC7685&l&oMines ";
    public static String separator = "&8━━━━━━━━━━━━━━━━━━━━━━━━";
    public MinasConfig minasConfig;
    public MinasMenuConfig minasMenuConfig;

    @Override
    public void onEnable() {
        minasConfig = new MinasConfig(this);
        minasMenuConfig = new MinasMenuConfig(this);

        // --- Generar minas predeterminadas al iniciar ---
        String[] keys = {"mina_a", "mina_b", "mina_c", "mina_d"};
        String[] nombres = {"Mina A — Básica", "Mina B — Media", "Mina C — Avanzada", "Mina D — Élite"};
        String[] blocks = {"OAK_LOG", "COAL_BLOCK", "GOLD_BLOCK", "DIAMOND_BLOCK"};
        World world = Bukkit.getWorld("Minas"); // Usa el mundo 'Minas'
        Location baseLoc = world != null ? world.getSpawnLocation() : Bukkit.getWorlds().get(0).getSpawnLocation();

        int separation = 200; // DISTANCIA ENTRE MINAS

        for (int i = 0; i < keys.length; i++) {
            String key = keys[i];
            String path = "minas." + key;
            Location l = baseLoc.clone().add(i * separation, 0, 0); // minas separadas 200 en X

            // --- MinasMenuConfig (GUI) ---
            minasMenuConfig.getConfig().set(path + ".display", blocks[i]);
            minasMenuConfig.getConfig().set(path + ".prefix", "&b" + nombres[i]);
            minasMenuConfig.getConfig().set(path + ".desc", "&eMina predeterminada generada automáticamente");
            minasMenuConfig.getConfig().set(path + ".world", l.getWorld().getName());
            minasMenuConfig.getConfig().set(path + ".x", l.getX());
            minasMenuConfig.getConfig().set(path + ".y", l.getY() + 45); // tp encima por default
            minasMenuConfig.getConfig().set(path + ".z", l.getZ());
            minasMenuConfig.getConfig().set(path + ".yaw", 0D);
            minasMenuConfig.getConfig().set(path + ".pitch", 0D);

            // --- MinasConfig (bloques aleatorios clásicos) ---
            minasConfig.getConfig().set(path + ".world", l.getWorld().getName());
            minasConfig.getConfig().set(path + ".x1", l.getBlockX());
            minasConfig.getConfig().set(path + ".y1", l.getBlockY());
            minasConfig.getConfig().set(path + ".z1", l.getBlockZ());
            minasConfig.getConfig().set(path + ".x2", l.getBlockX() + 50);
            minasConfig.getConfig().set(path + ".y2", l.getBlockY() + 40);
            minasConfig.getConfig().set(path + ".z2", l.getBlockZ() + 50);
            // Bloques aleatorios por mina
            String blocksRandom = blocks[i] + ",COAL_ORE,GOLD_ORE,IRON_ORE,STONE";
            minasConfig.getConfig().set(path + ".materials", blocksRandom);
            minasConfig.getConfig().set(path + ".reset", 180);
        }

        minasMenuConfig.save();
        minasConfig.save();
        minasMenuConfig.reload();
        minasConfig.reload();

        // Setup comandos
        ChronicleMineCommands comandos = new ChronicleMineCommands(this, minasConfig, minasMenuConfig);
        getCommand("chroniclemines").setExecutor(comandos);
        getCommand("crnmines").setExecutor(comandos);
        comandos.iniciarTodosLosResets();

        // LuckPerms
        LuckPerms luckPerms = getServer().getServicesManager().load(LuckPerms.class);
        getServer().getPluginManager().registerEvents(new MinaPrefixListener(minasConfig, luckPerms), this);

        // Listener para menú GUI
        getServer().getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onInventoryClick(InventoryClickEvent event) {
                ChronicleMinesMenu.handleMenu(event, minasMenuConfig);
            }
        }, this);

        Bukkit.getConsoleSender().sendMessage(
                mensaje.getColoredMessage(prefix + " &aEl plugin se cargó correctamente."));
    }

    @Override
    public void onDisable() {
        Bukkit.getConsoleSender().sendMessage(
                mensaje.getColoredMessage(prefix + " &cEl plugin se cerró correctamente."));
    }
}