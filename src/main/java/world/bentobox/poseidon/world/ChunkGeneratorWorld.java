package world.bentobox.poseidon.world;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Random;
import java.util.TreeMap;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.data.BlockData;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;
import org.bukkit.util.noise.PerlinOctaveGenerator;
import org.eclipse.jdt.annotation.NonNull;

import world.bentobox.bentobox.BentoBox;
import world.bentobox.poseidon.Poseidon;

/**
 * Generates the world
 * @author tastybento
 *
 */
public class ChunkGeneratorWorld extends ChunkGenerator {

    private record FloorMats(Material base, Material top) {
    }

    private final Poseidon addon;
    private final Random rand = new Random();
    private final Map<Environment, WorldConfig> seaConfig = new EnumMap<>(Environment.class);
    private static final Map<Environment, FloorMats> floorMats = Map.of(Environment.NETHER,
            new FloorMats(Material.NETHERRACK, Material.SOUL_SAND), Environment.NORMAL,
            new FloorMats(Material.SANDSTONE, Material.SAND), Environment.THE_END,
            new FloorMats(Material.END_STONE, Material.END_STONE));
    private static final int NOISE_MAX = 25;
    private static final Map<Material, Double> BASE_BLOCKS = Map.of(Material.STONE, 1D, Material.DIRT, 80D,
            Material.DIORITE, 0.05, Material.SANDSTONE, 1D, Material.SAND, 10D, Material.GRANITE, 0.05,
            Material.ANDESITE, 0.04, Material.COBBLESTONE, 1D, Material.AIR, 0.05);
    NavigableMap<Double, Material> treeMap = new TreeMap<>();
    private PerlinOctaveGenerator gen;
    private PoseidonBlockPop blockPop;

    private record WorldConfig(int seaHeight, int seaFloor, Material waterBlock) {
    }

    /**
     * @param addon - addon
     */
    public ChunkGeneratorWorld(Poseidon addon) {
        super();
        this.addon = addon;
        seaConfig.put(Environment.NORMAL,
                new WorldConfig(addon.getSettings().getSeaHeight(), addon.getSettings().getSeaFloor(),
                        addon.getSettings().getWaterBlock()));
        seaConfig.put(Environment.THE_END,
                new WorldConfig(addon.getSettings().getEndSeaHeight(), addon.getSettings().getEndSeaHeight(),
                        addon.getSettings().getEndWaterBlock()));
        rand.setSeed(System.currentTimeMillis());
        gen = new PerlinOctaveGenerator((long) (rand.nextLong() * rand.nextGaussian()), 8);
        gen.setScale(1.0 / 30.0);
        buildProbabilityTree(BASE_BLOCKS);

    }

    @Override
    public List<BlockPopulator> getDefaultPopulators(World world) {
        if (blockPop == null) {
            blockPop = new PoseidonBlockPop(addon);
        }
        List<BlockPopulator> result = new ArrayList<>();
        result.add(blockPop);
        return result;
    }

    // Build the TreeMap with cumulative probabilities
    private NavigableMap<Double, Material> buildProbabilityTree(Map<Material, Double> probabilities) {
        double cumulativeProbability = 0.0;

        for (Map.Entry<Material, Double> entry : probabilities.entrySet()) {
            cumulativeProbability += entry.getValue();
            treeMap.put(cumulativeProbability, entry.getKey());
        }

        return treeMap;
    }

    @Override
    public void generateNoise(@NonNull WorldInfo worldInfo, @NonNull Random random, int chunkX, int chunkZ,
            @NonNull ChunkData chunkData) {
        if (worldInfo.getEnvironment() == Environment.NORMAL) {
            WorldConfig wc = seaConfig.get(worldInfo.getEnvironment());
            int sh = wc.seaHeight();
            if (sh > worldInfo.getMinHeight()) {
                // Air
                chunkData.setRegion(0, wc.seaHeight() + 1, 0, 16, worldInfo.getMaxHeight(), 16, Material.AIR);
                // Sea level down to sea floor
                chunkData.setRegion(0, wc.seaFloor() + 1, 0, 16, wc.seaHeight() + 1, 16, wc.waterBlock());
                // Add some noise to sea floor
                addNoise(worldInfo, chunkX, chunkZ, chunkData);
            }
        }
        if (worldInfo.getEnvironment() == Environment.NETHER) {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    for (int y = worldInfo.getMinHeight(); y < worldInfo.getMaxHeight() - 1; y++) {
                        BlockData bd = chunkData.getBlockData(x, y, z);
                        if (bd.getMaterial() == Material.AIR) {
                            chunkData.setBlock(x, y, z, Material.WATER);
                        }
                        if (bd.getMaterial() == Material.LAVA
                                && chunkData.getBlockData(x, y + 1, z).getMaterial() == Material.AIR) {
                            chunkData.setBlock(x, y, z, Material.MAGMA_BLOCK); // Convert Lava to magma
                        }
                        if (bd.getMaterial() == Material.FIRE) {
                            chunkData.setBlock(x, y, z, Material.REDSTONE_LAMP); // Convert fire to lamp
                        }
                    }
                }
            }
        }
    }

    private void addNoise(@NonNull WorldInfo worldInfo, int chunkX, int chunkZ, @NonNull ChunkData chunkData) {
        int seaFloor = seaConfig.get(worldInfo.getEnvironment()).seaFloor();
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int n = (int) (NOISE_MAX
                        * gen.noise((chunkX << 4) + (double) x, (chunkZ << 4) + (double) z, 0.5, 0.5, true));
                for (int y = seaFloor; y < seaFloor + NOISE_MAX + n; y++) {
                    chunkData.setBlock(x, y, z, rand.nextBoolean() ? floorMats.get(worldInfo.getEnvironment()).top()
                            : floorMats.get(worldInfo.getEnvironment()).base());
                }
            }
        }
    }

    @Override
    public boolean shouldGenerateNoise() {
        return true;
    }

    @Override
    public boolean shouldGenerateSurface() {
        return true;
    }

    /**
     * This takes the vanilla caves and if they are in the water turns them into island blocks.
     * This takes advantage of a quirk where the cave generator makes caves even in water and leaves air blocks
     */
    @Override
    public void generateCaves(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, ChunkData chunkData) {
        if (worldInfo.getEnvironment() == Environment.NORMAL) {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    for (int y = addon.getSettings().getSeaFloor() + NOISE_MAX; y < Math
                            .min(addon.getSettings().getSeaHeight() + 1, worldInfo.getMaxHeight() - 1); y++) {
                        if (chunkData.getType(x, y, z) == Material.AIR) {
                            double randomValue = rand.nextDouble() * treeMap.lastKey();
                            Material mat = treeMap.ceilingEntry(randomValue).getValue();
                            // Adjust blocks above sea level
                            if (y == addon.getSettings().getSeaHeight()) {
                                // Top block - do some conversions
                                mat = switch (mat) {
                                case DIRT -> Material.GRASS_BLOCK;
                                case COBBLESTONE -> Material.DIRT;
                                case STONE -> Material.SAND;
                                default -> mat;
                                };
                            }
                            chunkData.setBlock(x, y, z, mat);
                            if (mat == Material.SAND) {
                                chunkData.setBlock(x, y - 1, z, Material.SANDSTONE);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean shouldGenerateCaves() {
        return addon.getSettings().isMakeCaves();
    }

    @Override
    public boolean shouldGenerateDecorations() {
        return addon.getSettings().isMakeDecorations();
    }

    @Override
    public boolean shouldGenerateMobs() {
        return true;
    }

    @Override
    public boolean shouldGenerateStructures() {
        return addon.getSettings().isMakeStructures();
    }

    @Override
    public BiomeProvider getDefaultBiomeProvider(WorldInfo worldInfo) {
        return addon.getBiomeProvider();
    }

    // This needs to be set to return true to override minecraft's default
    // behavior
    @Override
    public boolean canSpawn(World world, int x, int z) {
        return true;
    }

}
