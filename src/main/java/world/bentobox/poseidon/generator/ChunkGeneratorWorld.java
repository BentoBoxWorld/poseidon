package world.bentobox.poseidon.generator;

import java.util.Collections;
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

import world.bentobox.poseidon.Poseidon;

public class ChunkGeneratorWorld extends ChunkGenerator {

    private record FloorMats(Material base, Material top) {
    }

    /**
     * @param seaHeight sea height
     * @param seaFloor sea floor
     * @param waterBlock water material
     */
    private record WorldConfig(int seaHeight, int seaFloor, Material waterBlock) {
    }

    private final Poseidon addon;
    // We keep a single Random instance, but we will seed it properly in generateNoise()
    private final Random rand = new Random();

    private final Map<Environment, WorldConfig> seaConfig = new EnumMap<>(Environment.class);
    private static final Map<Environment, FloorMats> floorMats = Map.of(Environment.NETHER,
            new FloorMats(Material.NETHERRACK, Material.SOUL_SAND), Environment.NORMAL,
            new FloorMats(Material.SANDSTONE, Material.SAND), Environment.THE_END,
            new FloorMats(Material.END_STONE, Material.PURPUR_BLOCK));

    private static final int NOISE_MAX = 25;

    // Probability map for cave-filling blocks
    private static final Map<Material, Double> BASE_BLOCKS = Map.of(Material.STONE, 1D, Material.DIRT, 80D,
            Material.DIORITE, 0.05, Material.SANDSTONE, 1D, Material.SAND, 10D, Material.GRANITE, 0.05,
            Material.ANDESITE, 0.04, Material.COBBLESTONE, 0.25D, Material.AIR, 0.05);

    private final NavigableMap<Double, Material> baseBlockMap = new TreeMap<>();
    private PerlinOctaveGenerator gen;
    private PoseidonBlockPop blockPop;

    public ChunkGeneratorWorld(Poseidon addon) {
        super();
        this.addon = addon;

        // Store water & floor settings for each environment
        seaConfig.put(Environment.NORMAL, new WorldConfig(addon.getSettings().getSeaHeight(),
                addon.getSettings().getSeaFloor(), addon.getSettings().getWaterBlock()));
        seaConfig.put(Environment.NETHER, new WorldConfig(0, 0, addon.getSettings().getNetherWaterBlock()));
        seaConfig.put(Environment.THE_END, new WorldConfig(addon.getSettings().getEndSeaHeight(),
                addon.getSettings().getEndSeaFloor(), addon.getSettings().getEndWaterBlock()));
        buildProbabilityTree(BASE_BLOCKS);
    }

    /**
     * Build a probability TreeMap for selecting random block materials.
     */
    private NavigableMap<Double, Material> buildProbabilityTree(Map<Material, Double> probabilities) {
        double cumulativeProbability = 0.0;
        for (Map.Entry<Material, Double> entry : probabilities.entrySet()) {
            cumulativeProbability += entry.getValue();
            baseBlockMap.put(cumulativeProbability, entry.getKey());
        }
        return baseBlockMap;
    }

    @Override
    public List<BlockPopulator> getDefaultPopulators(World world) {
        if (blockPop == null) {
            blockPop = new PoseidonBlockPop(addon);
        }
        return Collections.singletonList(blockPop);
    }

    /**
     * The main noise-generation method. We seed both the PerlinOctaveGenerator and
     * the local Random with the world's seed to ensure consistent terrain across restarts.
     */
    @Override
    public void generateNoise(@NonNull WorldInfo worldInfo, @NonNull Random random, int chunkX, int chunkZ,
            @NonNull ChunkData chunkData) {

        // If our Perlin generator hasn't been set up yet, initialize it with the world's seed.
        if (gen == null) {
            gen = new PerlinOctaveGenerator(worldInfo.getSeed(), 8);
            gen.setScale(1.0 / 30.0);
        }

        // Re-seed our local Random for consistency (avoids abrupt chunk border shifts across sessions).
        rand.setSeed(worldInfo.getSeed());

        // Now proceed with normal generation
        if (worldInfo.getEnvironment() == Environment.NORMAL) {
            WorldConfig wc = seaConfig.get(worldInfo.getEnvironment());
            int seaHeight = wc.seaHeight();
            if (seaHeight > worldInfo.getMinHeight()) {
                // Clear out above sea level
                chunkData.setRegion(0, wc.seaHeight() + 1, 0, 16, worldInfo.getMaxHeight(), 16, Material.AIR);
                // Fill water from sea floor up to sea level
                chunkData.setRegion(0, wc.seaFloor() + 1, 0, 16, wc.seaHeight() + 1, 16, wc.waterBlock());
                // Add noise-based floor variation
                addNoise(worldInfo, chunkX, chunkZ, chunkData);
            }
        }

        // NETHER environment
        if (worldInfo.getEnvironment() == Environment.NETHER || worldInfo.getEnvironment() == Environment.THE_END) {
            WorldConfig wc = seaConfig.get(worldInfo.getEnvironment());
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    for (int y = worldInfo.getMinHeight(); y < worldInfo.getMaxHeight() - 1; y++) {
                        BlockData bd = chunkData.getBlockData(x, y, z);
                        if (bd.getMaterial() == Material.AIR) {
                            chunkData.setBlock(x, y, z, wc.waterBlock());
                        }
                        // Change Lava to Air boundaries to Magma block to prevent obsidian 
                        if (bd.getMaterial() == Material.LAVA
                                && chunkData.getBlockData(x, y + 1, z).getMaterial() == Material.AIR) {
                            chunkData.setBlock(x, y, z, Material.MAGMA_BLOCK);
                        }
                        // Make Fire still illuminate, but as a lamp
                        if (bd.getMaterial() == Material.FIRE) {
                            chunkData.setBlock(x, y, z, Material.REDSTONE_LAMP);
                        }
                        if (bd.getMaterial() == Material.SOUL_FIRE) {
                            chunkData.setBlock(x, y, z, Material.SOUL_LANTERN);
                        }
                    }
                }
            }
        }
    }

    /**
     * Applies Perlin noise to shape the sea floor. The noise generator is already
     * seeded with the world seed, so it remains consistent across sessions.
     */
    private void addNoise(@NonNull WorldInfo worldInfo, int chunkX, int chunkZ, @NonNull ChunkData chunkData) {
        int seaFloor = seaConfig.get(worldInfo.getEnvironment()).seaFloor();
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                // Use PerlinOctaveGenerator for height variation
                double noiseVal = gen.noise((chunkX << 4) + x, (chunkZ << 4) + z, 0.5, 0.5, true);
                int n = (int) (NOISE_MAX * noiseVal);

                // Fill blocks from the base seaFloor up to seaFloor + NOISE_MAX + n
                for (int y = seaFloor; y < seaFloor + NOISE_MAX + n; y++) {
                    // Use our seeded 'rand' to choose top or base block
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

    @Override
    public void generateCaves(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, ChunkData chunkData) {
        if (worldInfo.getEnvironment() == Environment.NORMAL) {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    for (int y = addon.getSettings().getSeaFloor() + NOISE_MAX; y < Math
                            .min(addon.getSettings().getSeaHeight() + 1, worldInfo.getMaxHeight() - 1); y++) {
                        if (chunkData.getType(x, y, z) == Material.AIR) {
                            double randomValue = rand.nextDouble() * baseBlockMap.lastKey();
                            Material mat = baseBlockMap.ceilingEntry(randomValue).getValue();
                            // Convert top blocks to something else
                            if (y == addon.getSettings().getSeaHeight()) {
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

    @Override
    public boolean canSpawn(World world, int x, int z) {
        return true;
    }
}
