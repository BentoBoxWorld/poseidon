package world.bentobox.poseidon.world;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;

import world.bentobox.bentobox.util.Util;
import world.bentobox.poseidon.Poseidon;

/**
 * Gradually pregenerates chunks in advance of players using them.
 */
public class Pregenerator {
    private final Poseidon addon;
    private boolean isRunning;
    private boolean generateContinuously;
    private long delayTicks;

    public Pregenerator(Poseidon addon) {
        this.addon = addon;
        this.isRunning = false;
        this.generateContinuously = true;
        this.delayTicks = 20L; // Default delay of 1 second
    }

    /**
     * Starts the pregeneration process to ensure an additional buffer of islands is pregenerated.
     * 
     * @param world               The world to pregenerate chunks in.
     * @param additionalIslandsToGenerate The number of additional islands to pregenerate over the current count.
     */
    public void start(World world, int additionalIslandsToGenerate) {
        if (isRunning) {
            addon.log("Pregeneration is already running.");
            return;
        }

        isRunning = true;
        addon.log("Starting pregeneration...");

        long currentIslandCount = addon.getIslands().getIslandCount(world);
        long totalIslandsToGenerate = currentIslandCount + additionalIslandsToGenerate;

        Location lastIslandLocation = new Location(world, addon.getSettings().getIslandStartX(), 0,
                addon.getSettings().getIslandStartZ());

        int viewDistance = Bukkit.getServer().getViewDistance();

        Set<Vector> chunksToGenerate = new HashSet<>();

        // Generate chunk coordinates for the buffer islands
        for (int i = 0; i < totalIslandsToGenerate; i++) {
            int islandX = lastIslandLocation.getBlockX();
            int islandZ = lastIslandLocation.getBlockZ();

            int startChunkX = islandX >> 4;
            int startChunkZ = islandZ >> 4;

            for (int dx = -viewDistance; dx <= viewDistance; dx++) {
                for (int dz = -viewDistance; dz <= viewDistance; dz++) {
                    chunksToGenerate.add(new Vector(startChunkX + dx, 0, startChunkZ + dz));
                }
            }
            lastIslandLocation = nextGridLocation(lastIslandLocation);
        }

        // Convert to a final set of chunks to process sequentially
        preGen(world, new HashSet<>(chunksToGenerate));
    }

    void preGen(World world, Set<Vector> remainingChunks) {
        if (remainingChunks.isEmpty() || !isRunning) {
            addon.log("Pregeneration complete.");
            return;
        }
        Vector chunkCoords = remainingChunks.iterator().next();
        remainingChunks.remove(chunkCoords);

        int chunkX = chunkCoords.getBlockX();
        int chunkZ = chunkCoords.getBlockZ();

        if (Util.isChunkGenerated(world, chunkX, chunkZ)) {
            // Skip pregenerated
            preGen(world, remainingChunks);
            return;
        }
        addon.log("Pregenerating chunk at (" + chunkX + ", " + chunkZ + ")...");
        Util.getChunkAtAsync(world, chunkX, chunkZ).thenAccept(chunk -> {
            addon.log("Chunk at (" + chunkX + ", " + chunkZ + ") generated successfully.");
            if (generateContinuously) {
                preGen(world, remainingChunks);
            } else {
                Bukkit.getScheduler().runTaskLater(addon.getPlugin(), () -> preGen(world, remainingChunks), delayTicks);
            }
        }).exceptionally(throwable -> {
            addon.log("Failed to generate chunk at (" + chunkX + ", " + chunkZ + "): " + throwable.getMessage());
            return null;
        });
    }

    /**
     * Finds the next free island spot based on the last known island.
     * 
     * @param lastIsland The last island location.
     * @return The next free island location.
     */
    private Location nextGridLocation(final Location lastIsland) {
        int x = lastIsland.getBlockX();
        int z = lastIsland.getBlockZ();
        int d = addon.getSettings().getIslandDistance() * 2;

        if (x < z) {
            if (-1 * x < z) {
                lastIsland.setX(lastIsland.getX() + d);
                return lastIsland;
            }
            lastIsland.setZ(lastIsland.getZ() + d);
            return lastIsland;
        }
        if (x > z) {
            if (-1 * x >= z) {
                lastIsland.setX(lastIsland.getX() - d);
                return lastIsland;
            }
            lastIsland.setZ(lastIsland.getZ() - d);
            return lastIsland;
        }
        if (x <= 0) {
            lastIsland.setZ(lastIsland.getZ() + d);
            return lastIsland;
        }
        lastIsland.setZ(lastIsland.getZ() - d);
        return lastIsland;
    }

    /**
     * Stops the pregeneration process.
     */
    public void stop() {
        if (!isRunning) {
            addon.log("Pregeneration is not running.");
            return;
        }

        isRunning = false;
        addon.log("Pregeneration stopped.");
    }

    /**
     * Sets whether to generate continuously.
     *
     * @param generateContinuously True to generate continuously, false to add delay.
     */
    public void setGenerateContinuously(boolean generateContinuously) {
        this.generateContinuously = generateContinuously;
    }

    /**
     * Sets the delay between chunk generations when not generating continuously.
     *
     * @param delayTicks The delay in ticks.
     */
    public void setDelayTicks(long delayTicks) {
        this.delayTicks = delayTicks;
    }
}