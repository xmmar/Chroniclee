package ansneeze;

import ansneeze.commands.ChronicleCommands;
import ansneeze.utilidades.MinasConfig;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public class Chronicle extends JavaPlugin {
    public static String prefix = "&6✶ &e&lChronicle &8&l↠ ";
    public static String webLink = "&8&l[&eplay.invictus.lat&8&l]";
    public MinasConfig minasConfig;

    @Override
    public void onEnable() {
        minasConfig = new MinasConfig(this);
        ChronicleCommands comandos = new ChronicleCommands(this, minasConfig);
        getCommand("chronicle").setExecutor(comandos);
        getCommand("crn").setExecutor(comandos);
        comandos.iniciarTodosLosResets();
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