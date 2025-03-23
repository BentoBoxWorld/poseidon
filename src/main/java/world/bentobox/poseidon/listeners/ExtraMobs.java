package world.bentobox.poseidon.listeners;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.bukkit.Difficulty;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

import world.bentobox.bentobox.util.ExpiringMap;
import world.bentobox.poseidon.Poseidon;

/**
 * Generates extra mobs in water
 */
public class ExtraMobs implements Listener {
    
    private Poseidon addon;
    private static final Map<Difficulty, Integer> SIZING = Map.of(Difficulty.EASY, 1, Difficulty.NORMAL, 4,
            Difficulty.PEACEFUL, 0, Difficulty.HARD, 6);
    private ExpiringMap<Player, Boolean> attacked = new ExpiringMap<>(5, TimeUnit.MINUTES); // 5 minutes means once or twice a night

    public ExtraMobs(Poseidon addon) {
        super();
        this.addon = addon;
    }
    
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location playerLocation = player.getLocation();
        World world = player.getWorld();
        // Check if peaceful or if the player already was attacked recently
        if (player.getGameMode() != GameMode.SURVIVAL || world.getDifficulty() == Difficulty.PEACEFUL
                || attacked.containsKey(player)) {
            return;
        }

        // Check if the player's current block is water
        if (!playerLocation.getBlock().getType().equals(Material.WATER)) {
            return;
        }

        // Check if the player is below the sea level
        if (playerLocation.getBlockY() >= addon.getSettings().getSeaHeight()
                || playerLocation.getBlockY() <= addon.getSettings().getSeaFloor()) {
            return;
        }

        // Check if it is night time (Minecraft night is approximately between 13000 and 23000 ticks)
        long time = world.getTime();
        if (time < 13000 || time > 23000) {
            return;
        }

        // Check for random chance to spawn
        if (Math.random() > addon.getSettings().getExtraMobChance() / 100D) {
            return;
        }
        // Get the number of mobs around the player
        Collection<Entity> ents = world.getNearbyEntities(playerLocation, 24, 24, 24);
        if (ents.stream().filter(e -> e instanceof Monster).count() > SIZING.get(world.getDifficulty())) {
            return;
        }

        // Calculate the spawn location: 24 blocks behind the player
        Vector direction = playerLocation.getDirection().normalize();
        Location spawnLocation = playerLocation.clone().subtract(direction.multiply(24));

        // Ensure the spawn location is in water
        if (!spawnLocation.getBlock().getType().equals(Material.WATER)
                || !spawnLocation.getBlock().getRelative(BlockFace.UP).getType().equals(Material.WATER)
                || spawnLocation.getBlock().getLightLevel() > 0) {
            return;
        }

        // Spawn the Drowned entity at the calculated location
        world.spawnEntity(spawnLocation, EntityType.DROWNED);
        attacked.put(player, true);
    }
}
