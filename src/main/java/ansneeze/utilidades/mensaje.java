package ansneeze.utilidades;

import org.bukkit.ChatColor;

public class mensaje {
    public static String getColoredMessage(String message){
        return ChatColor.translateAlternateColorCodes('&',message);
    }
}
