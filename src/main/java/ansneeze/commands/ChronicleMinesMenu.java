package ansneeze.commands;

import ansneeze.ChronicleMines;
import ansneeze.utilidades.mensaje;
import ansneeze.utilidades.MinasMenuConfig;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;

public class ChronicleMinesMenu {
    private static final String GUI_NAME = mensaje.getColoredMessage("&#D8F454&l&oChronicle &#DC7685&l&oMines");

    public static void open(Player player, MinasMenuConfig minasMenuConfig) {
        FileConfiguration cfg = minasMenuConfig.getConfig();

        // CORRECCIÓN para evitar NullPointerException
        if (!cfg.contains("minas") || cfg.getConfigurationSection("minas") == null) {
            player.sendMessage(mensaje.getColoredMessage("&cNo hay minas menú configuradas aún."));
            return;
        }

        Set<String> minas = cfg.getConfigurationSection("minas").getKeys(false);
        int invSize = 27;
        Inventory inv = Bukkit.createInventory(null, invSize, GUI_NAME);

        int[] slots = {10, 12, 14, 16};
        int idx = 0;

        for (String mina : minas) {
            String path = "minas." + mina;
            Material mat = Material.matchMaterial(cfg.getString(path + ".display", "STONE"));
            ItemStack item = new ItemStack(mat != null ? mat : Material.STONE);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(mensaje.getColoredMessage(cfg.getString(path + ".prefix", mina)));
            List<String> lore = new ArrayList<>();
            lore.add(mensaje.getColoredMessage(cfg.getString(path + ".desc",
                    "&aEjemplo &c(Todavía no están definidas, pero podés probarlas.)")));
            meta.setLore(lore);
            item.setItemMeta(meta);
            if (idx < slots.length) inv.setItem(slots[idx], item);
            idx++;
        }

        ItemStack paper = new ItemStack(Material.PAPER);
        ItemMeta infoMeta = paper.getItemMeta();
        infoMeta.setDisplayName(mensaje.getColoredMessage("&dTus datos:"));
        List<String> infoLore = Arrays.asList(
                "&7Nombre: &f" + player.getName(),
                "&7Exp: &f" + player.getLevel(),
                "&7Mina actual: &b-"
        );
        infoMeta.setLore(infoLore);
        paper.setItemMeta(infoMeta);
        inv.setItem(22, paper);

        ItemStack book = new ItemStack(Material.BOOK);
        ItemMeta bookMeta = book.getItemMeta();
        bookMeta.setDisplayName(mensaje.getColoredMessage("&#F49554&l&oI&#EF994D&l&on&#E99C46&l&ov&#E4A03E&l&oi&#DEA337&l&oc&#E5B82A&l&ot&#EBCE1D&l&ou&#F2E310&l&os &bInformacion"));
        List<String> bookLore = Arrays.asList(
                "&7Github: &f@invictuslat",
                "&7Tiktok: &f@invictuslat",
                "&7Youtube: &f@invictuslat",
                "&7Discord: &9&nhttps://invite.gg/invictusmc",
                "&f¿No nos seguís? &a¡Entra a nuestro discord y enterate de todas las novedades!"
        );
        bookMeta.setLore(bookLore);
        book.setItemMeta(bookMeta);
        inv.setItem(18, book);

        player.openInventory(inv);
        player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 1, 1);
    }

    public static void handleMenu(InventoryClickEvent event, MinasMenuConfig minasMenuConfig) {
        if (!event.getView().getTitle().equals(GUI_NAME)) return;
        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        ItemStack item = event.getCurrentItem();
        if (item == null || !item.hasItemMeta()) return;

        String display = item.getItemMeta().getDisplayName();
        FileConfiguration cfg = minasMenuConfig.getConfig();

        if (!cfg.contains("minas") || cfg.getConfigurationSection("minas") == null) return;

        for (String mina : cfg.getConfigurationSection("minas").getKeys(false)) {
            String prefix = mensaje.getColoredMessage(cfg.getString("minas." + mina + ".prefix"));
            if (display.equals(prefix)) {
                String path = "minas." + mina;
                World world = Bukkit.getWorld(cfg.getString(path + ".world"));
                double x = cfg.getDouble(path + ".x");
                double y = cfg.getDouble(path + ".y");
                double z = cfg.getDouble(path + ".z");
                float yaw = (float) cfg.getDouble(path + ".yaw");
                float pitch = (float) cfg.getDouble(path + ".pitch");
                if (world != null) player.teleport(new Location(world, x, y, z, yaw, pitch));
                player.sendMessage(mensaje.getColoredMessage(ChronicleMines.prefix + "&aTeleportado a la mina &f" + mina + "&a!"));
                break;
            }
        }
    }
}