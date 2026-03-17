package ansneeze.commands;

import ansneeze.Chronicle;
import ansneeze.utilidades.mensaje;
import ansneeze.utilidades.MinasConfig;

import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class ChronicleCommands implements CommandExecutor {

    private final Map<UUID, Location[]> seleccionJugador = new HashMap<>();
    private final Plugin plugin;
    private final MinasConfig minasConfig;
    private final Map<String, BukkitRunnable> tareasReset = new HashMap<>();

    public ChronicleCommands(Plugin plugin, MinasConfig minasConfig) {
        this.plugin = plugin;
        this.minasConfig = minasConfig;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Comando solo para jugadores.");
            return true;
        }
        Player player = (Player) sender;
        UUID uuid = player.getUniqueId();

        if (args.length >= 3 && args[0].equalsIgnoreCase("mine") && args[1].equalsIgnoreCase("place")) {
            int pos;
            try {
                pos = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                player.sendMessage(mensaje.getColoredMessage("&cPosición inválida. Usa 1 o 2."));
                return true;
            }
            if (pos < 1 || pos > 2) {
                player.sendMessage(mensaje.getColoredMessage("&cPosición inválida. Usa 1 o 2."));
                return true;
            }
            Location[] sel = seleccionJugador.getOrDefault(uuid, new Location[2]);
            sel[pos - 1] = player.getLocation();
            seleccionJugador.put(uuid, sel);
            player.sendMessage(mensaje.getColoredMessage("&aPunto " + pos + " seleccionado en tus coordenadas actuales."));
            return true;
        }

        if (args.length >= 4 && args[0].equalsIgnoreCase("mine") && args[1].equalsIgnoreCase("set")) {
            String ids = args[2];
            String nombre = args[3].toLowerCase();

            Location[] sel = seleccionJugador.get(uuid);
            if (sel == null || sel[0] == null || sel[1] == null) {
                player.sendMessage(mensaje.getColoredMessage("&cDebes seleccionar dos puntos primero con &e/crn mine place 1 &cy &e/crn mine place 2"));
                return true;
            }

            FileConfiguration cfg = minasConfig.getConfig();
            String path = "minas." + nombre;
            cfg.set(path + ".world", player.getWorld().getName());
            cfg.set(path + ".x1", sel[0].getBlockX());
            cfg.set(path + ".y1", sel[0].getBlockY());
            cfg.set(path + ".z1", sel[0].getBlockZ());
            cfg.set(path + ".x2", sel[1].getBlockX());
            cfg.set(path + ".y2", sel[1].getBlockY());
            cfg.set(path + ".z2", sel[1].getBlockZ());
            cfg.set(path + ".ids", ids);
            cfg.set(path + ".reset", 180);
            minasConfig.save();

            generarMina(cfg, nombre);
            iniciarResetAutomatico(nombre);

            player.sendMessage(mensaje.getColoredMessage("&aMina '" + nombre + "' creada correctamente."));
            seleccionJugador.remove(uuid);
            return true;
        }

        if (args.length >= 3 && args[0].equalsIgnoreCase("mine") && args[1].equalsIgnoreCase("reset")) {
            String nombre = args[2].toLowerCase();

            FileConfiguration cfg = minasConfig.getConfig();
            if (!cfg.contains("minas." + nombre)) {
                player.sendMessage(mensaje.getColoredMessage("&cNo existe ninguna mina llamada '" + nombre + "'."));
                return true;
            }

            generarMina(cfg, nombre);
            player.sendMessage(mensaje.getColoredMessage("&aMina '" + nombre + "' regenerada correctamente."));
            return true;
        }

        if (args.length >= 2 && args[0].equalsIgnoreCase("mine") && args[1].equalsIgnoreCase("help")) {
            player.sendMessage(mensaje.getColoredMessage("&6Comandos de mina:"));
            player.sendMessage(mensaje.getColoredMessage("&e/crn mine place 1 &7- Seleccionar primer punto"));
            player.sendMessage(mensaje.getColoredMessage("&e/crn mine place 2 &7- Seleccionar segundo punto"));
            player.sendMessage(mensaje.getColoredMessage("&e/crn mine set <ids> <nombre> &7- Crea la mina"));
            player.sendMessage(mensaje.getColoredMessage("&e/crn mine reset <nombre> &7- Resetea manualmente la mina"));
            return true;
        }

        if (args.length == 0 || (args.length == 1 && args[0].equalsIgnoreCase("mine"))) {
            player.sendMessage(mensaje.getColoredMessage(
                    Chronicle.prefix + "&ePor favor, usa &a/crn mine help &epara más información."));
            return true;
        }

        return false;
    }

    private void generarMina(FileConfiguration cfg, String nombre) {
        String path = "minas." + nombre;
        World world = Bukkit.getWorld(cfg.getString(path + ".world"));
        if (world == null) return;
        try {
            int x1 = cfg.getInt(path + ".x1"), y1 = cfg.getInt(path + ".y1"), z1 = cfg.getInt(path + ".z1");
            int x2 = cfg.getInt(path + ".x2"), y2 = cfg.getInt(path + ".y2"), z2 = cfg.getInt(path + ".z2");
            String[] materialIds = cfg.getString(path + ".ids").split(",");
            List<Material> materiales = new ArrayList<>();
            for (String id : materialIds) {
                Material mat = Material.getMaterial(String.valueOf(Integer.parseInt(id.trim())));
                if (mat != null) materiales.add(mat);
            }
            if (materiales.isEmpty()) return;

            Random rnd = new Random();
            int minX = Math.min(x1, x2), maxX = Math.max(x1, x2);
            int minY = Math.min(y1, y2), maxY = Math.max(y1, y2);
            int minZ = Math.min(z1, z2), maxZ = Math.max(z1, z2);

            for (int x = minX; x <= maxX; x++)
                for (int y = minY; y <= maxY; y++)
                    for (int z = minZ; z <= maxZ; z++) {
                        Material mat = materiales.get(rnd.nextInt(materiales.size()));
                        world.getBlockAt(x, y, z).setType(mat);
                    }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void iniciarResetAutomatico(String minanombre) {
        if (tareasReset.containsKey(minanombre)) {
            tareasReset.get(minanombre).cancel();
        }
        int interval = minasConfig.getConfig().getInt("minas." + minanombre + ".reset", 180);
        BukkitRunnable tarea = new BukkitRunnable() {
            @Override
            public void run() {
                generarMina(minasConfig.getConfig(), minanombre);
                Bukkit.broadcastMessage(mensaje.getColoredMessage("&bLa mina '" + minanombre + "' ha sido regenerada automáticamente."));
            }
        };
        tarea.runTaskTimer(plugin, interval * 20L, interval * 20L);
        tareasReset.put(minanombre, tarea);
    }

    public void iniciarTodosLosResets() {
        FileConfiguration cfg = minasConfig.getConfig();
        if (!cfg.contains("minas")) return;
        Set<String> minas = cfg.getConfigurationSection("minas").getKeys(false);
        for (String mina : minas) {
            iniciarResetAutomatico(mina);
        }
    }
}