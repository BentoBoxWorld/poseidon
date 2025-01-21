package world.bentobox.poseidon.world;

import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World.Environment;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.EntityType;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;

/**
 * Change mobs to water mobs in the Nether.
 */
public class NetherPop extends BlockPopulator {

    private static final List<EntityType> WATER_MOBS = List.of(EntityType.DROWNED, EntityType.GUARDIAN,
            EntityType.ELDER_GUARDIAN, EntityType.GLOW_SQUID, EntityType.SQUID, EntityType.DROWNED, EntityType.DROWNED,
            EntityType.DROWNED, EntityType.GLOW_SQUID);

    private final int maxMobsPerChunk;

    public NetherPop(int maxMobsPerChunk) {
        this.maxMobsPerChunk = maxMobsPerChunk;
    }

    @Override
    public void populate(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, LimitedRegion limitedRegion) {
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
}