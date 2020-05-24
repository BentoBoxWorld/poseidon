package world.bentobox.poseidon.listeners;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitTask;

import world.bentobox.poseidon.Poseidon;

/**
 * Applies the acid effect to players
 *
 * @author tastybento
 */
public class AirEffect implements Listener {

    private final Poseidon addon;
    private final Map<Player, Long> dryPlayers = new WeakHashMap<>();
    private final BukkitTask task;

    public AirEffect(Poseidon addon) {
        this.addon = addon;
        task = Bukkit.getScheduler().runTaskTimer(addon.getPlugin(), () -> {
            dryPlayers.keySet().removeIf(p -> p.getLocation().getBlock().getType().equals(Material.WATER));
            dryPlayers.forEach(this::processAir);
        }, 20L, 20L);
    }

    private void processAir(Player player, Long l) {
        // Reduce player's air
        player.setFireTicks(20);
        player.setPlayerTime(14000, false);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerDeath(PlayerDeathEvent e) {
        dryPlayers.remove(e.getEntity());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent e) {
        Player player = e.getPlayer();
        if (player.getLocation().getBlock().getType().equals(Material.WATER)) {
            // Players can always breath underwater
            player.setRemainingAir(player.getMaximumAir());
            return;
        }
        // Player is not in water
        dryPlayers.put(player, System.currentTimeMillis());
    }

    public void cancel() {
        task.cancel();

    }


}