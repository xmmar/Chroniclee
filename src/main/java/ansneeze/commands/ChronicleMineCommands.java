package ansneeze.commands;

import ansneeze.ChronicleMines;
import ansneeze.utilidades.mensaje;
import ansneeze.utilidades.MinasConfig;

import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class ChronicleMineCommands implements CommandExecutor {

    private final Map<UUID, Location[]> seleccionJugador = new HashMap<>();
    private final Plugin plugin;
    private final MinasConfig minasConfig;
    private final Map<String, BukkitRunnable> tareasReset = new HashMap<>();

    public ChronicleMineCommands(Plugin plugin, MinasConfig minasConfig) {
        this.plugin = plugin;
        this.minasConfig = minasConfig;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage(mensaje.getColoredMessage(ChronicleMines.prefix + " &cSolo para jugadores."));
            return true;
        }
        Player player = (Player) sender;
        UUID uuid = player.getUniqueId();

        // HELP
        if (args.length == 1 && args[0].equalsIgnoreCase("help")) {
            player.sendMessage(mensaje.getColoredMessage(
                    ChronicleMines.separator + "\n" +
                            ChronicleMines.prefix + " &7Comandos:\n" +
                            "&e/crnmines help &c- Muestra esta ayuda\n" +
                            "&e/crnmines list &c- Lista todas las minas\n" +
                            "&e/crnmines set &8(&bMateriales&8) &8(&bNombre&8) &c- Crear mina\n" +
                            "&e/crnmines place 1 &c- Punto inicial\n" +
                            "&e/crnmines place 2 &c- Punto final\n" +
                            "&e/crnmines delete &8(&bNombre&8) &c- Eliminar mina\n" +
                            "&e/crnmines tp &8(&bNombre&8) &c- TP por encima\n" +
                            "&e/crnmines reset &8(&bNombre&8) &c- Reset mina\n" +
                            "&e/crnmines menu &c- Menu de las minas actuales.\n" +
                            "&e/crnmines reload &c- Recargar plugin\n" +
                            ChronicleMines.separator
            ));
            return true;
        }

        // Selecciona puntos
        if (args.length == 2 && args[0].equalsIgnoreCase("place")) {
            int pos;
            try { pos = Integer.parseInt(args[1]); }
            catch (NumberFormatException e) {
                player.sendMessage(mensaje.getColoredMessage(ChronicleMines.prefix + "&cPosición inválida. Usa 1 o 2."));
                return true;
            }
            if (pos < 1 || pos > 2) {
                player.sendMessage(mensaje.getColoredMessage(ChronicleMines.prefix + "&cPosición inválida. Usa 1 o 2."));
                return true;
            }
            Location[] sel = seleccionJugador.getOrDefault(uuid, new Location[2]);
            sel[pos - 1] = player.getLocation();
            seleccionJugador.put(uuid, sel);
            player.sendMessage(mensaje.getColoredMessage(ChronicleMines.prefix + "&aPunto " + pos + " seleccionado &7en tus coordenadas actuales."));
            return true;
        }

        // Crear mina
        if (args.length == 3 && args[0].equalsIgnoreCase("set")) {
            String materials = args[1];
            String nombre = args[2].toLowerCase();

            Location[] sel = seleccionJugador.get(uuid);
            if (sel == null || sel[0] == null || sel[1] == null) {
                player.sendMessage(mensaje.getColoredMessage(ChronicleMines.prefix + "&cSelecciona dos puntos primero (&e/crnmines place 1&c y &e/crnmines place 2&c)."));
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
            cfg.set(path + ".materials", materials);
            cfg.set(path + ".reset", 180);
            minasConfig.save();

            boolean resultado = generarMina(cfg, nombre, player);
            if (resultado) {
                iniciarResetAutomatico(nombre);
                player.sendMessage(mensaje.getColoredMessage(
                        ChronicleMines.separator + "\n&6✶ &e&lChronicle &8&l↠ &8Creada: &a" + nombre + "\n" + ChronicleMines.separator));
                seleccionJugador.remove(uuid);
            }
            return true;
        }

        // Eliminar mina (ahora elimina los bloques en el mundo)
        if (args.length == 2 && args[0].equalsIgnoreCase("delete")) {
            if (!player.hasPermission("chronicle.delete")) {
                player.sendMessage(mensaje.getColoredMessage(ChronicleMines.prefix + "&cNo tienes permiso para eliminar minas."));
                return true;
            }
            String nombre = args[1].toLowerCase();
            FileConfiguration cfg = minasConfig.getConfig();
            if (!cfg.contains("minas." + nombre)) {
                player.sendMessage(mensaje.getColoredMessage(ChronicleMines.prefix + "&cNo existe esa mina."));
                return true;
            }
            String path = "minas." + nombre;
            String worldName = cfg.getString(path + ".world");
            int x1 = cfg.getInt(path + ".x1");
            int y1 = cfg.getInt(path + ".y1");
            int z1 = cfg.getInt(path + ".z1");
            int x2 = cfg.getInt(path + ".x2");
            int y2 = cfg.getInt(path + ".y2");
            int z2 = cfg.getInt(path + ".z2");
            World world = Bukkit.getWorld(worldName);
            if (world != null) {
                int minX = Math.min(x1, x2), maxX = Math.max(x1, x2);
                int minY = Math.min(y1, y2), maxY = Math.max(y1, y2);
                int minZ = Math.min(z1, z2), maxZ = Math.max(z1, z2);
                for (int x = minX; x <= maxX; x++)
                    for (int y = minY; y <= maxY; y++)
                        for (int z = minZ; z <= maxZ; z++)
                            world.getBlockAt(x, y, z).setType(Material.AIR);
            }
            cfg.set("minas_eliminadas." + nombre, cfg.getConfigurationSection("minas." + nombre));
            cfg.set("minas." + nombre, null);
            minasConfig.save();
            player.sendMessage(mensaje.getColoredMessage(
                    ChronicleMines.separator + "\n&6✶ &e&lChronicle &8&l↠ &cEliminada: &a" + nombre + "\n" + ChronicleMines.separator));
            return true;
        }

        // Reset manual
        if (args.length == 2 && args[0].equalsIgnoreCase("reset")) {
            String nombre = args[1].toLowerCase();
            FileConfiguration cfg = minasConfig.getConfig();
            if (!cfg.contains("minas." + nombre)) {
                player.sendMessage(mensaje.getColoredMessage(ChronicleMines.prefix + "&cNo existe esa mina."));
                return true;
            }
            boolean resultado = generarMina(cfg, nombre, player);
            if (resultado) {
                player.sendMessage(mensaje.getColoredMessage(
                        ChronicleMines.separator + "\n&6✶ &e&lChronicle &8&l↠ &b¡La mina '" + nombre + "' ha sido regenerada manualmente!\n" + ChronicleMines.separator));
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
            }
            return true;
        }

        // TP por encima de la mina
        if (args.length == 2 && args[0].equalsIgnoreCase("tp")) {
            if (!player.hasPermission("chronicle.tp")) {
                player.sendMessage(mensaje.getColoredMessage(ChronicleMines.prefix + "&cNo tienes permiso para teletransportarte a minas."));
                return true;
            }
            String nombre = args[1].toLowerCase();
            FileConfiguration cfg = minasConfig.getConfig();
            if (!cfg.contains("minas." + nombre)) {
                player.sendMessage(mensaje.getColoredMessage(ChronicleMines.prefix + "&cNo existe esa mina."));
                return true;
            }
            int x1 = cfg.getInt("minas." + nombre + ".x1");
            int y1 = cfg.getInt("minas." + nombre + ".y1");
            int z1 = cfg.getInt("minas." + nombre + ".z1");
            int x2 = cfg.getInt("minas." + nombre + ".x2");
            int y2 = cfg.getInt("minas." + nombre + ".y2");
            int z2 = cfg.getInt("minas." + nombre + ".z2");
            World world = Bukkit.getWorld(cfg.getString("minas." + nombre + ".world"));
            int topY = Math.max(y1, y2) + 5;
            int midX = (x1 + x2) / 2;
            int midZ = (z1 + z2) / 2;
            Location tp = new Location(world, midX + 0.5, topY, midZ + 0.5);
            player.teleport(tp);
            player.sendMessage(mensaje.getColoredMessage(
                    ChronicleMines.separator + "\n&6✶ &e&lChronicle &8&l↠ &aTeletransportado por encima de la mina '&e" + nombre + "&a'.\n" + ChronicleMines.separator));
            return true;
        }

        // Listar minas
        if (args.length == 1 && args[0].equalsIgnoreCase("list")) {
            FileConfiguration cfg = minasConfig.getConfig();
            if (!cfg.contains("minas")) {
                player.sendMessage(mensaje.getColoredMessage(ChronicleMines.prefix + "&cNo hay minas registradas.\n" + ChronicleMines.separator));
                return true;
            }
            Set<String> minas = cfg.getConfigurationSection("minas").getKeys(false);
            if (minas.isEmpty()) {
                player.sendMessage(mensaje.getColoredMessage(ChronicleMines.prefix + "&cNo hay minas registradas.\n" + ChronicleMines.separator));
                return true;
            }
            StringBuilder sb = new StringBuilder(ChronicleMines.separator + "\n&6✶ &e&lChronicle &8&l↠ &7Minas existentes:\n");
            for (String mina : minas) {
                sb.append("&e- ").append(mina).append("\n");
            }
            sb.append(ChronicleMines.separator);
            player.sendMessage(mensaje.getColoredMessage(sb.toString()));
            return true;
        }

        // Listar minas eliminadas
        if (args.length == 2 && args[0].equalsIgnoreCase("list") && args[1].equalsIgnoreCase("delete")) {
            FileConfiguration cfg = minasConfig.getConfig();
            if (!cfg.contains("minas_eliminadas")) {
                player.sendMessage(mensaje.getColoredMessage(ChronicleMines.prefix + "&cNo hay minas eliminadas registradas.\n" + ChronicleMines.separator));
                return true;
            }
            Set<String> minas = cfg.getConfigurationSection("minas_eliminadas").getKeys(false);
            if (minas.isEmpty()) {
                player.sendMessage(mensaje.getColoredMessage(ChronicleMines.prefix + "&cNo hay minas eliminadas registradas.\n" + ChronicleMines.separator));
                return true;
            }
            StringBuilder sb = new StringBuilder(ChronicleMines.separator + "\n&6✶ &e&lChronicle &8&l↠ &7Minas eliminadas:\n");
            for (String mina : minas) {
                sb.append("&c- ").append(mina).append("\n");
            }
            sb.append(ChronicleMines.separator);
            player.sendMessage(mensaje.getColoredMessage(sb.toString()));
            return true;
        }

        // Reload con auto-backup
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            if (!player.hasPermission("chronicle.reload")) {
                player.sendMessage(mensaje.getColoredMessage(ChronicleMines.prefix + "&cNo tienes permiso para recargar el plugin.\n" + ChronicleMines.separator));
                return true;
            }
            minasConfig.backup();
            minasConfig.reload();
            iniciarTodosLosResets();
            player.sendMessage(mensaje.getColoredMessage(
                    ChronicleMines.separator + "\n&6✶ &e&lChronicle &8&l↠ &aPlugin recargado. Archivos YML actualizados.\n" + ChronicleMines.separator));
            return true;
        }
        // Menú GUI
        if (args.length == 1 && args[0].equalsIgnoreCase("menu")) {
            ChronicleMinesMenu.open(player, minasConfig);
            return true;
        }

        // Mensaje por defecto si no coincide ningún comando
        player.sendMessage(mensaje.getColoredMessage(
                ChronicleMines.prefix + "&ePor favor, usa &a/crnmines help &epara más información."));
        return true;
    }

    // Genera o regenera una mina (igual que antes, pero con mensajes personalizados)
    private boolean generarMina(FileConfiguration cfg, String nombre, Player jugador) {
        String path = "minas." + nombre;
        World world = Bukkit.getWorld(cfg.getString(path + ".world"));
        if (world == null) {
            if (jugador != null)
                jugador.sendMessage(mensaje.getColoredMessage(ChronicleMines.prefix + "&cEl mundo de la mina no existe."));
            return false;
        }
        try {
            int x1 = cfg.getInt(path + ".x1"), y1 = cfg.getInt(path + ".y1"), z1 = cfg.getInt(path + ".z1");
            int x2 = cfg.getInt(path + ".x2"), y2 = cfg.getInt(path + ".y2"), z2 = cfg.getInt(path + ".z2");
            String[] materialNames = cfg.getString(path + ".materials").split(",");
            List<Material> materiales = new ArrayList<>();
            List<String> materialesInvalidos = new ArrayList<>();
            for (String matName : materialNames) {
                Material mat = Material.matchMaterial(matName.trim());
                if (mat != null) materiales.add(mat);
                else materialesInvalidos.add(matName.trim());
            }
            if (materiales.isEmpty()) {
                if (jugador != null)
                    jugador.sendMessage(mensaje.getColoredMessage(ChronicleMines.prefix + "&cMateriales inválidos: " + String.join(", ", materialesInvalidos)));
                return false;
            }
            if (!materialesInvalidos.isEmpty() && jugador != null) {
                jugador.sendMessage(mensaje.getColoredMessage(ChronicleMines.prefix + "&cMateriales desconocidos: " + String.join(", ", materialesInvalidos)));
            }
            int minX = Math.min(x1, x2), maxX = Math.max(x1, x2);
            int minY = Math.min(y1, y2), maxY = Math.max(y1, y2);
            int minZ = Math.min(z1, z2), maxZ = Math.max(z1, z2);

            if (maxX - minX < 0 || maxY - minY < 0 || maxZ - minZ < 0) {
                if (jugador != null)
                    jugador.sendMessage(mensaje.getColoredMessage(ChronicleMines.prefix + "&cLa región seleccionada es inválida."));
                return false;
            }
            Random rnd = new Random();
            int bloquesModificados = 0;
            for (int x = minX; x <= maxX; x++)
                for (int y = minY; y <= maxY; y++)
                    for (int z = minZ; z <= maxZ; z++) {
                        Material mat = materiales.get(rnd.nextInt(materiales.size()));
                        world.getBlockAt(x, y, z).setType(mat);
                        bloquesModificados++;
                    }
            if (jugador != null)
                jugador.sendMessage(mensaje.getColoredMessage(ChronicleMines.prefix + "&7Minando &f" + bloquesModificados + " &7bloques."));
            return true;
        } catch (Exception ex) {
            if (jugador != null) jugador.sendMessage(mensaje.getColoredMessage(ChronicleMines.prefix + "&c¡Error al generar la mina!"));
            ex.printStackTrace();
            return false;
        }
    }

    // Reset automático de mina, solo para quienes se encuentren dentro de la mina
    public void iniciarResetAutomatico(String minanombre) {
        if (tareasReset.containsKey(minanombre)) {
            tareasReset.get(minanombre).cancel();
        }
        int interval = minasConfig.getConfig().getInt("minas." + minanombre + ".reset", 180);
        BukkitRunnable tarea = new BukkitRunnable() {
            @Override
            public void run() {
                generarMina(minasConfig.getConfig(), minanombre, null);
                for (Player p : Bukkit.getOnlinePlayers()) {
                    // Si el jugador está dentro de la mina, le muestra y suena
                    String minaEn = getMinaEn(p, minanombre);
                    if (minaEn != null && minaEn.equalsIgnoreCase(minanombre)) {
                        p.sendMessage(mensaje.getColoredMessage(
                                ChronicleMines.separator + "\n&6✶ &e&lChronicle &8&l↠ &b¡La mina &e" + minanombre + " &bha sido regenerada!\n" + ChronicleMines.separator));
                        p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
                    }
                }
            }
        };
        tarea.runTaskTimer(plugin, interval * 20L, interval * 20L);
        tareasReset.put(minanombre, tarea);
    }

    // Auxiliar, para verificar si el jugador está en esa mina
    private String getMinaEn(Player player, String minaNombre) {
        FileConfiguration cfg = minasConfig.getConfig();
        if (!cfg.contains("minas." + minaNombre)) return null;
        String path = "minas." + minaNombre;
        String worldName = cfg.getString(path + ".world");
        int x1 = cfg.getInt(path + ".x1"), y1 = cfg.getInt(path + ".y1"), z1 = cfg.getInt(path + ".z1");
        int x2 = cfg.getInt(path + ".x2"), y2 = cfg.getInt(path + ".y2"), z2 = cfg.getInt(path + ".z2");
        if (!player.getWorld().getName().equals(worldName)) return null;
        int minX = Math.min(x1, x2), maxX = Math.max(x1, x2);
        int minY = Math.min(y1, y2), maxY = Math.max(y1, y2);
        int minZ = Math.min(z1, z2), maxZ = Math.max(z1, z2);
        int px = player.getLocation().getBlockX();
        int py = player.getLocation().getBlockY();
        int pz = player.getLocation().getBlockZ();
        if (px >= minX && px <= maxX && py >= minY && py <= maxY && pz >= minZ && pz <= maxZ) {
            return minaNombre;
        }
        return null;
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