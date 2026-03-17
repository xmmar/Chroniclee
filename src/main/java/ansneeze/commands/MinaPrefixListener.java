package ansneeze.commands;

import ansneeze.utilidades.MinasConfig;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.types.PrefixNode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.Set;
import java.util.ArrayList;
import java.util.List;

public class MinaPrefixListener implements Listener {
    private final MinasConfig minasConfig;
    private final LuckPerms luckPerms;

    public MinaPrefixListener(MinasConfig minasConfig, LuckPerms luckPerms) {
        this.minasConfig = minasConfig;
        this.luckPerms = luckPerms;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        String minaActual = getMinaEn(player);
        User user = luckPerms.getUserManager().getUser(player.getUniqueId());
        if (user == null) return;

        // Elimina todos los prefijos personalizados
        List<Node> nodosAEliminar = new ArrayList<>();
        for (Node node : user.getNodes()) {
            if (node instanceof PrefixNode && node.getMeta().contains("&7[")) {
                nodosAEliminar.add(node);
            }
        }
        nodosAEliminar.forEach(node -> user.data().remove(node));

        // Asigna prefix si está en la mina
        if (minaActual != null) {
            String letra = minaActual.substring(0, 1).toUpperCase();
            PrefixNode prefixNode = PrefixNode.builder("&7[" + letra + "] ", 101).build();
            user.data().add(prefixNode);
        }

        luckPerms.getUserManager().saveUser(user);
    }

    private String getMinaEn(Player player) {
        org.bukkit.configuration.file.FileConfiguration cfg = minasConfig.getConfig();
        if (!cfg.contains("minas")) return null;
        Set<String> minas = cfg.getConfigurationSection("minas").getKeys(false);
        for (String nombre : minas) {
            String path = "minas." + nombre;
            String worldName = cfg.getString(path + ".world");
            int x1 = cfg.getInt(path + ".x1"), y1 = cfg.getInt(path + ".y1"), z1 = cfg.getInt(path + ".z1");
            int x2 = cfg.getInt(path + ".x2"), y2 = cfg.getInt(path + ".y2"), z2 = cfg.getInt(path + ".z2");
            if (!player.getWorld().getName().equals(worldName)) continue;
            int minX = Math.min(x1, x2), maxX = Math.max(x1, x2);
            int minY = Math.min(y1, y2), maxY = Math.max(y1, y2);
            int minZ = Math.min(z1, z2), maxZ = Math.max(z1, z2);
            int px = player.getLocation().getBlockX();
            int py = player.getLocation().getBlockY();
            int pz = player.getLocation().getBlockZ();
            if (px >= minX && px <= maxX && py >= minY && py <= maxY && pz >= minZ && pz <= maxZ) {
                return nombre;
            }
        }
        return null;
    }
}