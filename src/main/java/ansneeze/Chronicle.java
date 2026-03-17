package ansneeze;

import ansneeze.commands.ChronicleCommands;
import ansneeze.utilidades.MinasConfig;
import ansneeze.listeners.MinaPrefixListener;
import net.luckperms.api.LuckPerms;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public class Chronicle extends JavaPlugin {
    public static String prefix = "&6✶ &e&lChronicle &8&l↠ ";
    public static String separator = "&8◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆"; // Separador estético
    public MinasConfig minasConfig;

    @Override
    public void onEnable() {
        minasConfig = new MinasConfig(this);
        minasConfig.backup();
        ChronicleCommands comandos = new ChronicleCommands(this, minasConfig);
        getCommand("chronicle").setExecutor(comandos);
        getCommand("crn").setExecutor(comandos);
        comandos.iniciarTodosLosResets();

        LuckPerms luckPerms = getServer().getServicesManager().load(LuckPerms.class);
        getServer().getPluginManager().registerEvents(new MinaPrefixListener(minasConfig, luckPerms), this);

        Bukkit.getConsoleSender().sendMessage(
                ChatColor.translateAlternateColorCodes('&',
                        prefix + " &aEl plugin se cargó correctamente."));
    }

    @Override
    public void onDisable() {
        Bukkit.getConsoleSender().sendMessage(
                ChatColor.translateAlternateColorCodes('&',
                        prefix + " &cEl plugin se cerró correctamente."));
    }
}