package ansneeze.commands;

import ansneeze.utilidades.mensaje;
import ansneeze.utilidades.MinasMenuConfig;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;

public class ChronicleMinesMenu {
    private static final String GUI_NAME =
            "&#F49554&l&oI&#E99C46&l&oN&#DEA337&l&oV&#E5AF2A&l&oI&#EBBB1D&l&oC&#F2C710&l&oT&#D4AF11&l&oU&#B59612&l&oS"; // Puedes leerlo de config

    public static void open(Player player, Plugin plugin, MinasMenuConfig minasMenuConfig) {
        FileConfiguration config = plugin.getConfig();
        FileConfiguration minasCfg = minasMenuConfig.getConfig();

        // 1. Tamaño y título dinámicos desde config
        String title = mensaje.getColoredMessage(config.getString("gui_menu.title", "Mines"));
        int size = config.getInt("gui_menu.size", 36);
        Inventory inv = Bukkit.createInventory(null, size, title);

        // 2. Paneles/marcos desde config
        for (String color : Arrays.asList("pink", "blue", "cyan")) {
            List<Integer> slots = config.getIntegerList("gui_menu.border." + color);
            Material mat = Material.matchMaterial(color.toUpperCase() + "_STAINED_GLASS_PANE");
            ItemStack pane = new ItemStack(mat);
            ItemMeta meta = pane.getItemMeta(); meta.setDisplayName(" "); pane.setItemMeta(meta);
            for (int slot : slots) inv.setItem(slot, pane);
        }

        // 3. Libro desde config
        ConfigurationSection bookSec = config.getConfigurationSection("gui_menu.items.book");
        if (bookSec != null) {
            ItemStack book = new ItemStack(Material.matchMaterial(bookSec.getString("material", "BOOK")));
            ItemMeta bm = book.getItemMeta();
            bm.setDisplayName(mensaje.getColoredMessage(bookSec.getString("name")));
            bm.setLore(bookSec.getStringList("lore").stream()
                    .map(mensaje::getColoredMessage).toList());
            book.setItemMeta(bm);
            inv.setItem(bookSec.getInt("slot"), book);
        }

        // 4. Info desde config
        ConfigurationSection infoSec = config.getConfigurationSection("gui_menu.items.info");
        if (infoSec != null) {
            ItemStack paper = new ItemStack(Material.matchMaterial(infoSec.getString("material", "PAPER")));
            ItemMeta pm = paper.getItemMeta();
            pm.setDisplayName(mensaje.getColoredMessage(infoSec.getString("name")));
            List<String> lore = new ArrayList<>();
            for (String line : infoSec.getStringList("lore")) {
                lore.add(mensaje.getColoredMessage(
                        line.replace("%player%", player.getName())
                                .replace("%exp%", String.valueOf(player.getLevel()))
                                .replace("%mina%", "-") // Puedes poner la mina actual si la tienes
                ));
            }
            pm.setLore(lore);
            paper.setItemMeta(pm);
            inv.setItem(infoSec.getInt("slot"), paper);
        }

        // 5. Minas desde config (recorriendo lista)
        List<Map<?, ?>> minasList = config.getMapList("gui_menu.items.minas");
        for (Map<?, ?> minaEntry : minasList) {
            int slot = (int) minaEntry.get("slot");
            String minaKey = (String) minaEntry.get("path");
            // Datos de la mina del 'minasmenus.yml':
            String prefix = minasCfg.getString("minas." + minaKey + ".prefix", "&bMina");
            String desc = minasCfg.getString("minas." + minaKey + ".desc", "&eMina de ejemplo");
            String block = minasCfg.getString("minas." + minaKey + ".display", "STONE");
            ItemStack item = new ItemStack(Material.matchMaterial(block));
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(mensaje.getColoredMessage(prefix));
            meta.setLore(Collections.singletonList(mensaje.getColoredMessage(desc)));
            // Shine
            meta.addEnchant(org.bukkit.enchantments.Enchantment.ARROW_INFINITE, 1, true);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(meta);
            inv.setItem(slot, item);
        }
        player.openInventory(inv);
    }
    // ----- AUX LORE -----
    private static String obtenerDesbloqueo(String key) {
        switch (key) {
            case "mina_a": return "Hollow (rango 0)";
            case "mina_b": return "Bound (rango 3)";
            case "mina_c": return "Omen (rango 7)";
            case "mina_d": return "Warlord (rango 10)";
            default: return "-";
        }
    }
    private static String obtenerRecursos(String key) {
        switch (key) {
            case "mina_a": return "Base — referencia";
            case "mina_b": return "+25% recursos";
            case "mina_c": return "+55% recursos";
            case "mina_d": return "+90% recursos";
            default: return "-";
        }
    }
    private static String obtenerVip(String key) {
        switch (key) {
            case "mina_a": return "Todos los VIPs";
            case "mina_b": return "Arcane, Abyssal, Primordial, Oblivion, Invictus";
            case "mina_c": return "Abyssal, Primordial, Oblivion, Invictus";
            case "mina_d": return "Primordial, Oblivion, Invictus";
            default: return "-";
        }
    }

    //----- BLOQUEO DE MOVIMIENTO Y TP -----
    public static void handleMenu(InventoryClickEvent event, MinasMenuConfig minasMenuConfig) {
        if (!event.getView().getTitle().equals(GUI_NAME)) return;
        event.setCancelled(true); // BLOQUEA mover/cambiar items

        Player player = (Player) event.getWhoClicked();
        ItemStack item = event.getCurrentItem();
        if (item == null || !item.hasItemMeta()) return;

        String display = item.getItemMeta().getDisplayName();

        // Teleport por minas
        if (display != null && display.startsWith("§bMina")) {
            String key = "";
            if(display.contains("A")) key="mina_a";
            else if(display.contains("B")) key="mina_b";
            else if(display.contains("C")) key="mina_c";
            else if(display.contains("D")) key="mina_d";
            // tp a la posición de la mina guardada en minasMenuConfig
            FileConfiguration cfg = minasMenuConfig.getConfig();
            String path = "minas."+key;
            World world = Bukkit.getWorld(cfg.getString(path+".world","Minas"));
            double x = cfg.getDouble(path+".x", player.getLocation().getX());
            double y = cfg.getDouble(path+".y", player.getLocation().getY());
            double z = cfg.getDouble(path+".z", player.getLocation().getZ());
            float yaw = (float)cfg.getDouble(path+".yaw", player.getLocation().getYaw());
            float pitch = (float)cfg.getDouble(path+".pitch", player.getLocation().getPitch());
            player.teleport(new Location(world, x, y, z, yaw, pitch));
            player.closeInventory(); // <--- Cierra el menú al tepear
            player.sendMessage(mensaje.getColoredMessage("&#91ff7f¡Bienvenido a la mina!"));
        }
    }
}