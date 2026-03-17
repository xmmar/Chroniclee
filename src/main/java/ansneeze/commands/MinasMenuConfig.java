package ansneeze.utilidades;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class MinasMenuConfig {
    private final JavaPlugin plugin;
    private final File minasFile;
    private FileConfiguration config;

    public MinasMenuConfig(JavaPlugin plugin) {
        this.plugin = plugin;
        minasFile = new File(plugin.getDataFolder(), "minasmenus.yml");
        if (!minasFile.exists()) {
            plugin.saveResource("minasmenus.yml", false);
            config = YamlConfiguration.loadConfiguration(minasFile);
        }
        config = YamlConfiguration.loadConfiguration(minasFile);
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
}