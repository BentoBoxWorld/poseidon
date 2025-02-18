package world.bentobox.poseidon.generator;

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
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.entity.Villager.Type;
import org.bukkit.entity.boat.MangroveChestBoat;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;
import org.bukkit.loot.LootTables;

import world.bentobox.poseidon.Poseidon;

/**
 * Change mobs to water mobs in the Nether.
 */
public class PoseidonBlockPop extends BlockPopulator {

    private static final List<EntityType> WATER_MOBS = List.of(EntityType.DROWNED, EntityType.GUARDIAN,
            EntityType.ELDER_GUARDIAN, EntityType.GLOW_SQUID, EntityType.SQUID, EntityType.DROWNED, EntityType.DROWNED,
            EntityType.DROWNED, EntityType.GLOW_SQUID);
    private static final Random RANDOM = new Random();
    private static NavigableMap<Double, TreeType> treeMap = new TreeMap<>();

    private final int maxMobsPerChunk;
    private int seaHeight;
    private int treeDensity;
    private int turtleEggs;
    private double fisherman;

    public PoseidonBlockPop(Poseidon addon) {
        maxMobsPerChunk = addon.getSettings().getMobsPerChunk();
        treeMap = buildProbabilityTree(addon.getSettings().getTreeTypes());
        seaHeight = addon.getSettings().getSeaHeight();
        treeDensity = addon.getSettings().getIslandTrees();
        turtleEggs = addon.getSettings().getTurtleEggs();
        fisherman = addon.getSettings().getFisherman() / 25600D; // 16 x 16 blocks in chunk
    }

    @Override
    public void populate(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, LimitedRegion limitedRegion) {
        if (worldInfo.getEnvironment() == Environment.NORMAL) {
            normalPop(worldInfo, random, chunkX, chunkZ, limitedRegion);
            return;
        }
        if (worldInfo.getEnvironment() == Environment.NETHER) {
            netherPop(worldInfo, random, chunkX, chunkZ, limitedRegion);
            return;
        }
    }

    private void netherPop(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, LimitedRegion limitedRegion) {

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

    private boolean nearWater(int x, int y, int z, LimitedRegion lr) {
        // Check around location for water
        for (int i = -1; i < 2; i++) {
            for (int j = -1; j < 2; j++) {
                if (lr.isInRegion(x + i, y, z + j) && lr.getType(x + i, y, z + j) == Material.WATER) {
                    return true;
                }
            }
        }
        return false;
    }

    private void normalPop(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, LimitedRegion limitedRegion) {
        World world = Bukkit.getWorld(worldInfo.getUID());
        boolean fishermanInChunk = false;
        int maxTurtleEggsPerChunk = 8;
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int xx = (chunkX << 4) + x;
                int zz = (chunkZ << 4) + z;
                int y = seaHeight;
                if (limitedRegion.isInRegion(xx, y, zz)) {
                    Material type = limitedRegion.getType(xx, y, zz);
                    if (type.isSolid() && !nearWater(xx, y, zz, limitedRegion)) {
                        // Make island centers one block higher
                        limitedRegion.setType(xx, y + 1, zz, Material.GRASS_BLOCK);
                        placeTree(limitedRegion, new Location(world, xx, y + 2, zz));
                        continue;
                    }

                    // Turtle eggs on the beach only
                    if (type == Material.SAND && nearWater(xx, y, zz, limitedRegion)
                            && maxTurtleEggsPerChunk > 0) {
                        if (random.nextInt(100) < turtleEggs) {
                            limitedRegion.setType(xx, y + 1, zz, Material.TURTLE_EGG);
                            maxTurtleEggsPerChunk--;
                        }
                    }

                    // Fisherman - first surface water block found in chunk
                    if (type == Material.WATER && !fishermanInChunk
                            && random.nextDouble() < this.fisherman) {
                        Location loc = new Location(world, xx, y + 1, zz);
                        MangroveChestBoat b = limitedRegion.createEntity(loc, MangroveChestBoat.class);
                        Villager passenger = limitedRegion.createEntity(loc, Villager.class);
                        passenger.setProfession(Profession.FISHERMAN);
                        passenger.setVillagerType(Type.SWAMP);
                        b.addPassenger(passenger);
                        limitedRegion.addEntity(b);
                        limitedRegion.addEntity(passenger);
                        // Fill chest - for now, just one item
                        int chance = RANDOM.nextInt(100);
                        if (chance < 5) {
                            b.setLootTable(LootTables.FISHING_TREASURE.getLootTable());
                        } else if (chance < 20) {
                            b.setLootTable(LootTables.FISHING_JUNK.getLootTable());
                        } else {
                            b.setLootTable(LootTables.FISHING_FISH.getLootTable());
                        }
                        // Only have one per chunk
                        fishermanInChunk = true;
                    }

                    // Tree placement
                    if (type == Material.GRASS_BLOCK || type == Material.DIRT) {
                        placeTree(limitedRegion, new Location(world, xx, y + 1, zz));
                    }
                }

            }
        }
    }

    private void placeTree(LimitedRegion limitedRegion, Location loc) {
        // Pick a random tree
        TreeType tree = getRandomTree();
        // Do not grow everywhere
        if (tree != null) {
            limitedRegion.generateTree(loc, RANDOM, tree);
        }
    }

    // Build the TreeMap with cumulative probabilities
    private static NavigableMap<Double, TreeType> buildProbabilityTree(Map<TreeType, ?> probabilities) {
        // Ensure all values in the map are Doubles
        Map<TreeType, Double> correctedMap = ensureDoubleValues(probabilities);

        NavigableMap<Double, TreeType> treeMap = new TreeMap<>();
        double cumulativeProbability = 0.0;

        for (Map.Entry<TreeType, Double> entry : correctedMap.entrySet()) {
            cumulativeProbability += entry.getValue();
            treeMap.put(cumulativeProbability, entry.getKey());
        }
        return treeMap;
    }

    // Select a random tree type based on probabilities
    public TreeType getRandomTree() {
        if (RANDOM.nextInt(100) > this.treeDensity || treeMap.isEmpty()) {
            return null;
        }
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
                // Only positive values
                double v = ((Number) value).doubleValue();
                if (v > 0) {
                    correctedMap.put(entry.getKey(), v);
                }
            } else {
                throw new IllegalArgumentException("Value for key " + entry.getKey() + " is not a number: " + value);
            }
        }

        return correctedMap;
    }
}