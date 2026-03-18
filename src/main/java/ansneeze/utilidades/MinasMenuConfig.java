package ansneeze.utilidades;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public class MinasMenuConfig {
    private final JavaPlugin plugin;
    private final File minasFile;
    private FileConfiguration config;

    public MinasMenuConfig(JavaPlugin plugin) {
        this.plugin = plugin;
        minasFile = new File(plugin.getDataFolder(), "minasmenus.yml");
        if (!minasFile.exists()) {
            try {
                minasFile.createNewFile();
                config = YamlConfiguration.loadConfiguration(minasFile);
                config.set("minas", null);
                save();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        config = YamlConfiguration.loadConfiguration(minasFile);
        if (!config.contains("minas")) config.set("minas", null);
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public void save() {
        try {
            config.save(minasFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void reload() {
        config = YamlConfiguration.loadConfiguration(minasFile);
    }
}