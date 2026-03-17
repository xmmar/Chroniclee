package ansneeze.utilidades;

import org.bukkit.ChatColor;

public class mensaje {
    public static String getColoredMessage(String msg){
        return ChatColor.translateAlternateColorCodes('&', msg);
    }
}