package world.bentobox.poseidon.world;

import java.util.List;

import org.bukkit.block.Biome;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.WorldInfo;

import world.bentobox.poseidon.Poseidon;

/**
 * Biome provider for AcidIsland
 * @author tastybento
 *
 */
public class PoseidonBiomeProvider extends BiomeProvider {

    private final Poseidon addon;

    /**
     * @param addon Addon
     */
    public PoseidonBiomeProvider(Poseidon addon) {
        this.addon = addon;
    }

    @Override
    public Biome getBiome(WorldInfo worldInfo, int x, int y, int z) {
        return switch(worldInfo.getEnvironment()) {
        case NETHER -> addon.getSettings().getDefaultNetherBiome();
        case THE_END -> addon.getSettings().getDefaultEndBiome();
        default -> y <= addon.getSettings().getSeaHeight() ? addon.getSettings().getDefaultSeaBiome()
                : addon.getSettings().getDefaultAirBiome();
        };
    }

    @Override
    public List<Biome> getBiomes(WorldInfo worldInfo) {
        return List.of(this.getBiome(worldInfo, 0, 0, 0));
    }

}
