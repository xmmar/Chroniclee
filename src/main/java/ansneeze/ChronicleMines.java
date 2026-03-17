package ansneeze;

import ansneeze.commands.ChronicleMineCommands;
import ansneeze.commands.ChronicleMinesMenu;
import ansneeze.commands.MinaPrefixListener;
import ansneeze.utilidades.MinasConfig;
import ansneeze.utilidades.MinasMenuConfig;
import ansneeze.utilidades.mensaje;
import net.luckperms.api.LuckPerms;
import org.bukkit.Bukkit;
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
        // Inicializar configs
        minasConfig = new MinasConfig(this);
        minasMenuConfig = new MinasMenuConfig(this);
        minasConfig.backup();

        // Comandos principales
        ChronicleMineCommands comandos = new ChronicleMineCommands(this, minasConfig, minasMenuConfig);
        getCommand("chroniclemines").setExecutor(comandos);
        getCommand("crnmines").setExecutor(comandos);
        comandos.iniciarTodosLosResets();

        // LuckPerms y listeners
        LuckPerms luckPerms = getServer().getServicesManager().load(LuckPerms.class);
        getServer().getPluginManager().registerEvents(new MinaPrefixListener(minasConfig, luckPerms), this);

        // Listener para GUI menú
        getServer().getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onInventoryClick(InventoryClickEvent event) {
                ChronicleMinesMenu.handleMenu(event, minasMenuConfig);
            }
        }, this);

        // Mensaje de consola
        Bukkit.getConsoleSender().sendMessage(
                mensaje.getColoredMessage(prefix + " &aEl plugin se cargó correctamente."));
    }

    @Override
    public void onDisable() {
        Bukkit.getConsoleSender().sendMessage(
                mensaje.getColoredMessage(prefix + " &cEl plugin se cerró correctamente."));
    }
}