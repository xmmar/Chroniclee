package ansneeze.utilidades;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class MinasConfig {
    private final JavaPlugin plugin;
    private final File archivo;
    private FileConfiguration config;

    public MinasConfig(JavaPlugin plugin) {
        this.plugin = plugin;
        this.archivo = new File(plugin.getDataFolder(), "minas.yml");
        if (!archivo.exists()) {
            archivo.getParentFile().mkdirs();
            try {
                archivo.createNewFile();
                config = YamlConfiguration.loadConfiguration(archivo);
                config.set("minas", null);
                config.set("minas_eliminadas", null);
                save();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.config = YamlConfiguration.loadConfiguration(archivo);
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public void save() {
        try {
            config.save(archivo);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void reload() {
        config = YamlConfiguration.loadConfiguration(archivo);
    }

    public void backup() {
        try {
            File backupsFolder = new File(plugin.getDataFolder(), "backups");
            if (!backupsFolder.exists()) {
                backupsFolder.mkdirs();
            }
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
            File backupFile = new File(backupsFolder, "minas_" + sdf.format(new Date()) + ".yml");
            java.nio.file.Files.copy(archivo.toPath(), backupFile.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}