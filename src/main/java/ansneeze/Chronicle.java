package ansneeze;

import ansneeze.commands.ChronicleCommands;
import ansneeze.utilidades.MinasConfig;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public class Chronicle extends JavaPlugin {
    public static String prefix = "&a✦ &c&eChronicle &8&l↠ ";
    public MinasConfig minasConfig;

    @Override
    public void onEnable() {
        minasConfig = new MinasConfig(this);
        ChronicleCommands comandos = new ChronicleCommands(this, minasConfig);
        getCommand("crn").setExecutor(comandos);
        comandos.iniciarTodosLosResets();
        Bukkit.getConsoleSender().sendMessage
                (ChatColor.translateAlternateColorCodes('&', prefix+ " &ase cargo correctamente."));
    }

    @Override
    public void onDisable() {
        Bukkit.getConsoleSender().sendMessage
                (ChatColor.translateAlternateColorCodes('&', prefix+ " &cse cerró correctamente."));
    }
}