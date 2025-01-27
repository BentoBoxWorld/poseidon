package world.bentobox.poseidon.listeners;

import java.util.Map;
import java.util.NavigableMap;
import java.util.Random;
import java.util.TreeMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.TreeType;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.util.Vector;

import world.bentobox.poseidon.Poseidon;

/**
 * This generates trees on new chunks
 * Why not use a BlockPopulator? Well, the reason is that our islands are actually converted caves, so a BlockPopulator would run before
 * the caves have been created, so this is the only option.
 */
public class IslandTreeGrower implements Listener {

    private static final Random RANDOM = new Random();

    private Poseidon addon;

    public IslandTreeGrower(Poseidon addon) {
        super();
        this.addon = addon;
    }

    // Build the TreeMap with cumulative probabilities
    private static NavigableMap<Double, TreeType> buildProbabilityTree(Map<TreeType, Double> probabilities) {
        NavigableMap<Double, TreeType> treeMap = new TreeMap<>();
        double cumulativeProbability = 0.0;

        for (Map.Entry<TreeType, Double> entry : probabilities.entrySet()) {
            cumulativeProbability += entry.getValue();
            treeMap.put(cumulativeProbability, entry.getKey());
        }

        return treeMap;
    }

    // Select a random tree type based on probabilities
    public static TreeType getRandomTree(Map<TreeType, Double> probabilities) {
        NavigableMap<Double, TreeType> treeMap = buildProbabilityTree(probabilities);

        // Get a random number between 0 and the highest cumulative probability
        double randomValue = RANDOM.nextDouble() * treeMap.lastKey();

        // Find the treetype corresponding to the random value
        return treeMap.ceilingEntry(randomValue).getValue();
    }
    /*
    @EventHandler(priority = EventPriority.NORMAL)
    public void onChunkLoad(ChunkLoadEvent e) {
        if (!e.isNewChunk() || e.getWorld().getEnvironment() != Environment.NORMAL
                || !addon.inWorld(e.getChunk().getWorld())) {
            return;
        }
        // Check if this is at sea level
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                Location loc = new Location(e.getWorld(), (e.getChunk().getX() << 4) + x,
                        addon.getSettings().getSeaHeight(), (e.getChunk().getZ() << 4) + z);
                Block bd = loc.getBlock();
                if (bd.getType() == Material.GRASS_BLOCK || bd.getType() == Material.DIRT) {
                    loc = loc.add(new Vector(0, 1, 0));
                    // Pick a random tree
                    e.getWorld().generateTree(loc, RANDOM, getRandomTree(addon.getSettings().getTreeTypes()));
                }
            }
        }
    }
       */
}
