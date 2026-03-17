package ansneeze;

import ansneeze.commands.ChronicleMineCommands;
import ansneeze.utilidades.MinasConfig;
import ansneeze.commands.MinaPrefixListener;
import ansneeze.utilidades.mensaje;
import net.luckperms.api.LuckPerms;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class ChronicleMines extends JavaPlugin {
    public static String prefix = "&#D8F454&l&oC&#D8E759&l&oh&#D9DB5E&l&or&#D9CE63&l&oo&#DAC267&l&on&#DAB56C&l&oi&#DAA871&l&oc&#DB9C76&l&ol&#DB8F7B&l&oe &#DC7685&l&oM&#DC698A&l&oi&#DD5D8E&l&on&#DD5093&l&oe&#DE4498&l&os ";
    public static String separator = "&8━━━━━━━━━━━━━━━━━━━━━━━━";
    public MinasConfig minasConfig;

    @Override
    public void onEnable() {
        minasConfig = new MinasConfig(this);
        minasConfig.backup();
        ChronicleMineCommands comandos = new ChronicleMineCommands(this, minasConfig);
        getCommand("chroniclemines").setExecutor(comandos);
        getCommand("crnmines").setExecutor(comandos);
        comandos.iniciarTodosLosResets();

        LuckPerms luckPerms = getServer().getServicesManager().load(LuckPerms.class);
        getServer().getPluginManager().registerEvents(new MinaPrefixListener(minasConfig, luckPerms), this);

        Bukkit.getConsoleSender().sendMessage(
                mensaje.getColoredMessage(prefix + " &aEl plugin se cargó correctamente."));
    }

    @Override
    public void onDisable() {
        Bukkit.getConsoleSender().sendMessage(
                mensaje.getColoredMessage(prefix + " &cEl plugin se cerró correctamente."));
    }
}