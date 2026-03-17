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
            sender.sendMessage(mensaje.getColoredMessage(Chronicle.prefix + " &cSolo para jugadores."));
            return true;
        }
        Player player = (Player) sender;
        UUID uuid = player.getUniqueId();

        // Selección de puntos tipo WorldEdit
        if (args.length >= 3 && args[0].equalsIgnoreCase("mine") && args[1].equalsIgnoreCase("place")) {
            int pos;
            try { pos = Integer.parseInt(args[2]); }
            catch (NumberFormatException e) {
                player.sendMessage(mensaje.getColoredMessage(Chronicle.prefix + "&cPosición inválida. Usa 1 o 2."));
                return true;
            }
            if (pos < 1 || pos > 2) {
                player.sendMessage(mensaje.getColoredMessage(Chronicle.prefix + "&cPosición inválida. Usa 1 o 2."));
                return true;
            }
            Location[] sel = seleccionJugador.getOrDefault(uuid, new Location[2]);
            sel[pos - 1] = player.getLocation();
            seleccionJugador.put(uuid, sel);
            player.sendMessage(mensaje.getColoredMessage(Chronicle.prefix + "&aPunto " + pos + " seleccionado &7en tus coordenadas actuales."));
            return true;
        }

        // Crear mina
        if (args.length >= 4 && args[0].equalsIgnoreCase("mine") && args[1].equalsIgnoreCase("set")) {
            String materials = args[2];
            String nombre = args[3].toLowerCase();

            Location[] sel = seleccionJugador.get(uuid);
            if (sel == null || sel[0] == null || sel[1] == null) {
                player.sendMessage(mensaje.getColoredMessage(Chronicle.prefix + "&cSelecciona dos puntos primero (&e/crn mine place 1&c y &e/crn mine place 2&c)."));
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
                        "\n&6✶ &e&lChronicle &8&l↠ &8[&eInvictus&8]&f"
                                + "\n&f━━━━━━━━━━━━━━━━━━━━━━━"
                                + "\n&7¡La mina &e" + nombre + " &7ha sido &bcreada correctamente&7!"
                                + "\n&f━━━━━━━━━━━━━━━━━━━━━━━\n"
                ));
                seleccionJugador.remove(uuid);
            }
            return true;
        }

        // Eliminar mina (Permisos: chronicle.delete)
        if (args.length >= 3 && args[0].equalsIgnoreCase("mine") && args[1].equalsIgnoreCase("delete")) {
            if (!player.hasPermission("chronicle.delete")) {
                player.sendMessage(mensaje.getColoredMessage(Chronicle.prefix + "&cNo tienes permiso para eliminar minas."));
                return true;
            }
            String nombre = args[2].toLowerCase();
            FileConfiguration cfg = minasConfig.getConfig();
            if (!cfg.contains("minas." + nombre)) {
                player.sendMessage(mensaje.getColoredMessage(Chronicle.prefix + "&cNo existe esa mina."));
                return true;
            }
            cfg.set("minas." + nombre, null);
            minasConfig.save();
            player.sendMessage(mensaje.getColoredMessage(
                    Chronicle.prefix + "&aMina '" + nombre + "' eliminada correctamente."
                            + "\n&f━━━━━━━━━━━━━━━━━━━━━━━"
            ));
            return true;
        }

        // Reseteo manual de mina
        if (args.length >= 3 && args[0].equalsIgnoreCase("mine") && args[1].equalsIgnoreCase("reset")) {
            String nombre = args[2].toLowerCase();

            FileConfiguration cfg = minasConfig.getConfig();
            if (!cfg.contains("minas." + nombre)) {
                player.sendMessage(mensaje.getColoredMessage(Chronicle.prefix + "&cNo existe esa mina."));
                return true;
            }

            boolean resultado = generarMina(cfg, nombre, player);
            if (resultado) {
                player.sendMessage(mensaje.getColoredMessage(
                        Chronicle.prefix + "&b¡La mina '" + nombre + "' ha sido regenerada manualmente!"
                                + "\n&f━━━━━━━━━━━━━━━━━━━━━━━"
                ));
            }
            return true;
        }

        // TP por encima de la mina (Permisos: chronicle.tp)
        if (args.length >= 3 && args[0].equalsIgnoreCase("mine") && args[1].equalsIgnoreCase("tp")) {
            if (!player.hasPermission("chronicle.tp")) {
                player.sendMessage(mensaje.getColoredMessage(Chronicle.prefix + "&cNo tienes permiso para teletransportarte a minas."));
                return true;
            }
            String nombre = args[2].toLowerCase();
            FileConfiguration cfg = minasConfig.getConfig();
            if (!cfg.contains("minas." + nombre)) {
                player.sendMessage(mensaje.getColoredMessage(Chronicle.prefix + "&cNo existe esa mina."));
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
                    Chronicle.prefix + "&aTeletransportado por encima de la mina '" + nombre + "'."
                            + "\n&f━━━━━━━━━━━━━━━━━━━━━━━"
            ));
            return true;
        }

        // Listar minas
        if (args.length >= 2 && args[0].equalsIgnoreCase("mine") && args[1].equalsIgnoreCase("list")) {
            FileConfiguration cfg = minasConfig.getConfig();
            if (!cfg.contains("minas")) {
                player.sendMessage(mensaje.getColoredMessage(Chronicle.prefix + "&cNo hay minas registradas."));
                return true;
            }
            Set<String> minas = cfg.getConfigurationSection("minas").getKeys(false);
            if (minas.isEmpty()) {
                player.sendMessage(mensaje.getColoredMessage(Chronicle.prefix + "&cNo hay minas registradas."));
                return true;
            }
            StringBuilder sb = new StringBuilder(Chronicle.prefix + "&7Minas existentes:\n&f━━━━━━━━━━━━━━━━━━━━━━━\n");
            for (String mina : minas) {
                sb.append("&e- ").append(mina).append("\n");
            }
            sb.append("&f━━━━━━━━━━━━━━━━━━━━━━━");
            player.sendMessage(mensaje.getColoredMessage(sb.toString()));
            return true;
        }

        // /crn reload (Permisos: chronicle.reload)
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            if (!player.hasPermission("chronicle.reload")) {
                player.sendMessage(mensaje.getColoredMessage(Chronicle.prefix + "&cNo tienes permiso para recargar el plugin."));
                return true;
            }
            minasConfig.reload();
            iniciarTodosLosResets();
            player.sendMessage(mensaje.getColoredMessage(
                    Chronicle.prefix + "&aPlugin recargado. Archivos YML actualizados."
                            + "\n&f━━━━━━━━━━━━━━━━━━━━━━━"
            ));
            return true;
        }

        // Ayuda
        if (args.length >= 2 && args[0].equalsIgnoreCase("mine") && args[1].equalsIgnoreCase("help")) {
            player.sendMessage(mensaje.getColoredMessage(
                    Chronicle.prefix + "&6Comandos de mina:\n"
                            + "&f━━━━━━━━━━━━━━━━━━━━━━━\n"
                            + "&e/crn mine place 1 &7- Seleccionar primer punto\n"
                            + "&e/crn mine place 2 &7- Seleccionar segundo punto\n"
                            + "&e/crn mine set MATERIAL1,MATERIAL2 NOMBRE &7- Crea la mina (ej: &b/crn mine set GOLD_ORE,STONE minaprueba)\n"
                            + "&e/crn mine reset <nombre> &7- Resetear mina\n"
                            + "&e/crn mine list &7- Listar minas\n"
                            + "&e/crn mine delete <nombre> &7- Eliminar mina\n"
                            + "&e/crn mine tp <nombre> &7- Teleport por encima\n"
                            + "&e/crn reload &7- Recargar plugin\n"
                            + "&f━━━━━━━━━━━━━━━━━━━━━━━"
            ));
            return true;
        }

        if (args.length == 0 || (args.length == 1 && args[0].equalsIgnoreCase("mine"))) {
            player.sendMessage(mensaje.getColoredMessage(
                    Chronicle.prefix + "&ePor favor, usa &a/chronicle mine help &epara más información."));
            return true;
        }

        player.sendMessage(mensaje.getColoredMessage(
                Chronicle.prefix + "&ePor favor, usa &a/chronicle mine help &epara más información."));
        return true;
    }

    // Generar/regenerar mina
    private boolean generarMina(FileConfiguration cfg, String nombre, Player jugador) {
        String path = "minas." + nombre;
        World world = Bukkit.getWorld(cfg.getString(path + ".world"));
        if (world == null) {
            if (jugador != null)
                jugador.sendMessage(mensaje.getColoredMessage(Chronicle.prefix + "&cEl mundo de la mina no existe."));
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
                    jugador.sendMessage(mensaje.getColoredMessage(Chronicle.prefix + "&cNo se encontraron materiales válidos. Materiales inválidos: " + String.join(", ", materialesInvalidos)));
                return false;
            }
            if (!materialesInvalidos.isEmpty() && jugador != null) {
                jugador.sendMessage(mensaje.getColoredMessage(Chronicle.prefix + "&cLos siguientes materiales no existen: " + String.join(", ", materialesInvalidos)));
            }
            int minX = Math.min(x1, x2), maxX = Math.max(x1, x2);
            int minY = Math.min(y1, y2), maxY = Math.max(y1, y2);
            int minZ = Math.min(z1, z2), maxZ = Math.max(z1, z2);

            if (maxX - minX < 0 || maxY - minY < 0 || maxZ - minZ < 0) {
                if (jugador != null)
                    jugador.sendMessage(mensaje.getColoredMessage(Chronicle.prefix + "&cLa región seleccionada es inválida."));
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
                jugador.sendMessage(mensaje.getColoredMessage(Chronicle.prefix + "&7Minando &f" + bloquesModificados + " &7bloques."));
            return true;
        } catch (Exception ex) {
            if (jugador != null) jugador.sendMessage(mensaje.getColoredMessage(Chronicle.prefix + "&c¡Error al generar la mina!"));
            ex.printStackTrace();
            return false;
        }
    }

    // Reset auto
    public void iniciarResetAutomatico(String minanombre) {
        if (tareasReset.containsKey(minanombre)) {
            tareasReset.get(minanombre).cancel();
        }
        int interval = minasConfig.getConfig().getInt("minas." + minanombre + ".reset", 180);
        BukkitRunnable tarea = new BukkitRunnable() {
            @Override
            public void run() {
                generarMina(minasConfig.getConfig(), minanombre, null);
                Bukkit.broadcastMessage(mensaje.getColoredMessage(
                        "\n&6✶ &e&lChronicle &8&l↠ &8[&eInvictus&8]"
                                + "\n&f━━━━━━━━━━━━━━━━━━━━━━━"
                                + "\n&b¡La mina &e" + minanombre + " &bha sido regenerada automáticamente!"
                                + "\n&f━━━━━━━━━━━━━━━━━━━━━━━\n"
                ));
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