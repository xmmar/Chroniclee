package ansneeze.utilidades;

import org.bukkit.ChatColor;

public class mensaje {
    public static String getColoredMessage(String msg){
        // Soporte para hex: reemplaza &#hex por §x§h§e§x...
        msg = msg.replaceAll("&#([A-Fa-f0-9]{6})", "§x$1");
        // El sistema de Spigot no reconoce la forma &l&o, entonces paso el resto normal.
        return ChatColor.translateAlternateColorCodes('&', msg);
    }
}