package world.bentobox.poseidon;

import java.util.Objects;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;
import org.bukkit.entity.SpawnCategory;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.ChunkGenerator;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import world.bentobox.bentobox.api.addons.GameModeAddon;
import world.bentobox.bentobox.api.commands.admin.DefaultAdminCommand;
import world.bentobox.bentobox.api.commands.island.DefaultPlayerCommand;
import world.bentobox.bentobox.api.configuration.Config;
import world.bentobox.bentobox.api.configuration.WorldSettings;
import world.bentobox.bentobox.lists.Flags;
import world.bentobox.poseidon.commands.IslandAboutCommand;
import world.bentobox.poseidon.listeners.AirEffect;
import world.bentobox.poseidon.listeners.IslandChunkPopulator;
import world.bentobox.poseidon.world.ChunkGeneratorWorld;
import world.bentobox.poseidon.world.PoseidonBiomeProvider;
import world.bentobox.poseidon.world.Pregenerator;

/**
 * Add-on to BentoBox that enables under sea exploration
 * @author tastybento
 *
 */
public class Poseidon extends GameModeAddon {

    private @Nullable Settings settings;
    private @Nullable ChunkGenerator chunkGenerator;
    private final Config<Settings> config = new Config<>(this, Settings.class);
    private BiomeProvider biomeProvider;

    private static final String NETHER = "_nether";
    private static final String THE_END = "_the_end";

    /**
     * This addon uses the new chunk generation API for the sea bottom
     */
    @Override
    public boolean isUsesNewChunkGeneration() {
        return true;
    }

    @Override
    public void onLoad() {
        // Save the default config from config.yml
        saveDefaultConfig();
        // Load settings from config.yml. This will check if there are any issues with it too.
        loadSettings();
        // Make the biome provider
        this.biomeProvider = new PoseidonBiomeProvider(this);
        // Chunk generator
        chunkGenerator = settings.isUseOwnGenerator() ? null : new ChunkGeneratorWorld(this);
        // Register commands
        playerCommand = new DefaultPlayerCommand(this)

        {
            @Override
            public void setup()
            {
                super.setup();
                new IslandAboutCommand(this);
            }
        };
        adminCommand = new DefaultAdminCommand(this) {};
    }

    private boolean loadSettings() {
        settings = config.loadConfigObject();
        if (settings == null) {
            // Woops
            this.logError("Poseidon settings could not load! Addon disabled.");
            this.setState(State.DISABLED);
            return false;
        }

        return true;
    }

    @Override
    public void onEnable() {
        if (settings == null) {
            return;
        }
        // Set default access to boats
        Flags.BOAT.setDefaultSetting(islandWorld, true);
        if (netherWorld != null) Flags.BOAT.setDefaultSetting(netherWorld, true);
        if (endWorld != null) Flags.BOAT.setDefaultSetting(endWorld, true);

        // Register listeners
        // Acid Effects
        registerListener(new AirEffect(this));
    }

    @Override
    public void onDisable() {
        // Do nothing
    }

    @NonNull
    public Settings getSettings() {
        return Objects.requireNonNull(settings);
    }

    /* (non-Javadoc)
     * @see world.bentobox.bentobox.api.addons.GameModeAddon#createWorlds()
     */
    @Override
    public void createWorlds() {
        String worldName = settings.getWorldName().toLowerCase();
        if (Bukkit.getWorld(worldName) == null) {
            log("Creating the sea kingdom...");
            logWarning("The error 'No key layers in MapLike[{}]' is normal and not an error. You can ignore it.");
        }
        // Create the world if it does not exist
        chunkGenerator = new ChunkGeneratorWorld(this);
        islandWorld = getWorld(worldName, World.Environment.NORMAL, chunkGenerator);
        // Make the nether if it does not exist
        if (settings.isNetherGenerate()) {
            if (Bukkit.getWorld(worldName + NETHER) == null) {
                log("Creating the sea kingdom's Nether...");
                logWarning("The error 'No key layers in MapLike[{}]' is normal and not an error. You can ignore it.");
            }
            netherWorld = settings.isNetherIslands() ? getWorld(worldName, World.Environment.NETHER, chunkGenerator) : getWorld(worldName, World.Environment.NETHER, null);
        }
        // Make the end if it does not exist
        if (settings.isEndGenerate()) {
            if (Bukkit.getWorld(worldName + THE_END) == null) {
                log("Creating the sea kingdom's End World...");
                logWarning("The error 'No key layers in MapLike[{}]' is normal and not an error. You can ignore it.");
            }
            endWorld = settings.isEndIslands() ? getWorld(worldName, World.Environment.THE_END, chunkGenerator) : getWorld(worldName, World.Environment.THE_END, null);
        }
        // Grow trees
        this.registerListener(new IslandChunkPopulator(this));
        // Pregen
        Pregenerator pregen = new Pregenerator(this);
        pregen.start(getOverWorld(), 10);
    }

    /**
     * Gets a world or generates a new world if it does not exist
     * @param worldName2 - the overworld name
     * @param env - the environment
     * @param chunkGenerator2 - the chunk generator. If <tt>null</tt> then the generator will not be specified
     * @return world loaded or generated
     */
    private World getWorld(String worldName2, Environment env, @Nullable ChunkGenerator chunkGenerator2) {
        // Set world name
        worldName2 = env.equals(World.Environment.NETHER) ? worldName2 + NETHER : worldName2;
        worldName2 = env.equals(World.Environment.THE_END) ? worldName2 + THE_END : worldName2;
        WorldCreator wc = WorldCreator.name(worldName2).environment(env);
        World w = settings.isUseOwnGenerator() ? wc.createWorld() : wc.generator(chunkGenerator2).createWorld();
        // Set spawn rates
        if (w != null && getSettings() != null) {
            if (getSettings().getSpawnLimitMonsters() > 0) {
                w.setSpawnLimit(SpawnCategory.MONSTER, getSettings().getSpawnLimitMonsters());
            }
            if (getSettings().getSpawnLimitAmbient() > 0) {
                w.setSpawnLimit(SpawnCategory.AMBIENT, getSettings().getSpawnLimitAmbient());
            }
            if (getSettings().getSpawnLimitAnimals() > 0) {
                w.setSpawnLimit(SpawnCategory.ANIMAL, getSettings().getSpawnLimitAnimals());
            }
            if (getSettings().getSpawnLimitWaterAnimals() > 0) {
                w.setSpawnLimit(SpawnCategory.WATER_ANIMAL, getSettings().getSpawnLimitWaterAnimals());
            }
            if (getSettings().getTicksPerAnimalSpawns() > 0) {
                w.setTicksPerSpawns(SpawnCategory.ANIMAL, getSettings().getTicksPerAnimalSpawns());
            }
            if (getSettings().getTicksPerMonsterSpawns() > 0) {
                w.setTicksPerSpawns(SpawnCategory.MONSTER, getSettings().getTicksPerMonsterSpawns());
            }
        }
        return w;

    }

    @Override
    public WorldSettings getWorldSettings() {
        return getSettings();
    }

    @Override
    public void onReload() {
        if (loadSettings()) {
            log("Reloaded AcidIsland settings");
        }
    }

    @Override
    public @NonNull ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
        return chunkGenerator;
    }

    @Override
    public void saveWorldSettings() {
        if (settings != null) {
            config.saveConfigObject(settings);
        }
    }

    /* (non-Javadoc)
     * @see world.bentobox.bentobox.api.addons.Addon#allLoaded()
     */
    @Override
    public void allLoaded() {
        // Save settings. This will occur after all addons have loaded
        this.saveWorldSettings();
    }

    public BiomeProvider getBiomeProvider() {
        return this.biomeProvider;
    }
}