package world.bentobox.poseidon.world;

import java.util.HashMap;
import java.util.List;
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
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.EntityType;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;
import org.bukkit.util.Vector;

import world.bentobox.bentobox.BentoBox;
import world.bentobox.poseidon.Poseidon;

/**
 * Change mobs to water mobs in the Nether.
 */
public class NetherPop extends BlockPopulator {

    private static final List<EntityType> WATER_MOBS = List.of(EntityType.DROWNED, EntityType.GUARDIAN,
            EntityType.ELDER_GUARDIAN, EntityType.GLOW_SQUID, EntityType.SQUID, EntityType.DROWNED, EntityType.DROWNED,
            EntityType.DROWNED, EntityType.GLOW_SQUID);
    private static final Random RANDOM = new Random();
    private static NavigableMap<Double, TreeType> treeMap = new TreeMap<>();

    private final int maxMobsPerChunk;
    private double seaHeight;

    public NetherPop(Poseidon addon) {
        maxMobsPerChunk = addon.getSettings().getMobsPerChunk();
        treeMap = buildProbabilityTree(addon.getSettings().getTreeTypes());
        seaHeight = addon.getSettings().getSeaHeight();
    }

    @Override
    public void populate(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, LimitedRegion limitedRegion) {
        if (worldInfo.getEnvironment() == Environment.NORMAL) {
            normalPop(worldInfo, random, chunkX, chunkZ, limitedRegion);
            return;
        }
        if (worldInfo.getEnvironment() != Environment.NETHER) {
            return;
        }

        // Calculate the chunk's world coordinates
        int startX = chunkX << 4;
        int startZ = chunkZ << 4;

        // Spawn a limited number of water mobs
        for (int i = 0; i < maxMobsPerChunk; i++) {
            // Randomly select a block within the chunk
            int x = startX + random.nextInt(16);
            int z = startZ + random.nextInt(16);
            int y = random.nextInt(worldInfo.getMaxHeight());

            BlockData block = limitedRegion.getBlockData(x, y, z);

            // Check if the block is water
            if (block.getMaterial() == Material.WATER) {
                // Randomly choose a water mob type
                EntityType mobType = WATER_MOBS.stream().skip(random.nextInt(WATER_MOBS.size())).findFirst()
                        .orElse(EntityType.DROWNED);

                // Spawn the mob at the water block's location
                Location spawnLocation = new Location(Bukkit.getWorld(worldInfo.getUID()), x, y, z);
                limitedRegion.spawnEntity(spawnLocation, mobType);
            }
        }
    }

    private void normalPop(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, LimitedRegion limitedRegion) {
        BentoBox.getInstance().logDebug("Block populator for " + chunkX + " " + chunkZ);
        World world = Bukkit.getWorld(worldInfo.getUID());
        // Check if this is at sea level
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                Location loc = new Location(world, (chunkX << 4) + x, seaHeight,
                        (chunkZ << 4) + z);
                if (limitedRegion.isInRegion(loc)) {
                    BlockData bd = limitedRegion.getBlockData(loc);
                    if (bd.getMaterial() == Material.GRASS_BLOCK || bd.getMaterial() == Material.DIRT) {
                        loc = loc.add(new Vector(0, 1, 0));
                        // Pick a random tree
                        limitedRegion.generateTree(loc, RANDOM, getRandomTree());
                        BentoBox.getInstance().logDebug("generrate tree at " + loc + " of type " + getRandomTree());
                    }
                } else {
                    BentoBox.getInstance().logDebug("Not in limited region");
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