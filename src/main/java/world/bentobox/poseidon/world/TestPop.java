package world.bentobox.poseidon.world;

import java.util.HashMap;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Random;
import java.util.TreeMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.TreeType;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;
import org.bukkit.util.Vector;

import world.bentobox.bentobox.BentoBox;
import world.bentobox.poseidon.Poseidon;

/**
 * Test
 */
public class TestPop extends BlockPopulator {

    private static final Random RANDOM = new Random();
    private static NavigableMap<Double, TreeType> treeMap = new TreeMap<>();

    private Poseidon addon;

    public TestPop(Poseidon addon) {
        super();
        this.addon = addon;
        treeMap = buildProbabilityTree(addon.getSettings().getTreeTypes());
    }

    @Override
    public void populate(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, LimitedRegion limitedRegion) {
        if (worldInfo.getEnvironment() != Environment.NORMAL) {
            return;
        }
        BentoBox.getInstance().logDebug("Block populator for " + chunkX + " " + chunkZ);
        World world = Bukkit.getWorld(worldInfo.getUID());
        // Check if this is at sea level
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                Location loc = new Location(world, (chunkX << 4) + x, addon.getSettings().getSeaHeight(),
                        (chunkZ << 4) + z);
                if (limitedRegion.isInRegion(loc)) {
                    Block bd = loc.getBlock();
                    if (bd.getType() == Material.GRASS_BLOCK || bd.getType() == Material.DIRT) {
                        loc = loc.add(new Vector(0, 1, 0));
                        // Pick a random tree
                        // limitedRegion.generateTree(loc, RANDOM, getRandomTree());
                        BentoBox.getInstance().logDebug("generrate tree at " + loc + " of type " + getRandomTree());
                    }
                }
            }
        }
    }

    // Build the TreeMap with cumulative probabilities
    private static NavigableMap<Double, TreeType> buildProbabilityTree(Map<TreeType, ?> probabilities) {
        BentoBox.getInstance().logDebug("Building tree map");
        // Ensure all values in the map are Doubles
        Map<TreeType, Double> correctedMap = ensureDoubleValues(probabilities);

        NavigableMap<Double, TreeType> treeMap = new TreeMap<>();
        double cumulativeProbability = 0.0;

        for (Map.Entry<TreeType, Double> entry : correctedMap.entrySet()) {
            cumulativeProbability += entry.getValue();
            treeMap.put(cumulativeProbability, entry.getKey());
        }
        BentoBox.getInstance().logDebug("done");
        return treeMap;
    }

    // Select a random tree type based on probabilities
    public static TreeType getRandomTree() {
        // Get a random number between 0 and the highest cumulative probability
        double randomValue = RANDOM.nextDouble() * treeMap.lastKey();

        // Find the treetype corresponding to the random value
        return treeMap.ceilingEntry(randomValue).getValue();
    }

    // Utility method to ensure all values in the map are Doubles
    private static Map<TreeType, Double> ensureDoubleValues(Map<TreeType, ?> probabilities) {
        Map<TreeType, Double> correctedMap = new HashMap<>();

        for (Map.Entry<TreeType, ?> entry : probabilities.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof Number) {
                correctedMap.put(entry.getKey(), ((Number) value).doubleValue());
            } else {
                throw new IllegalArgumentException("Value for key " + entry.getKey() + " is not a number: " + value);
            }
        }

        return correctedMap;
    }
}