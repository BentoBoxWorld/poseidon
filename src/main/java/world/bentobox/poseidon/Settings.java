package world.bentobox.poseidon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Difficulty;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.TreeType;
import org.bukkit.block.Biome;
import org.bukkit.entity.EntityType;

import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.api.configuration.ConfigComment;
import world.bentobox.bentobox.api.configuration.ConfigEntry;
import world.bentobox.bentobox.api.configuration.StoreAt;
import world.bentobox.bentobox.api.configuration.WorldSettings;
import world.bentobox.bentobox.api.flags.Flag;
import world.bentobox.bentobox.database.objects.adapters.Adapter;
import world.bentobox.bentobox.database.objects.adapters.FlagBooleanSerializer;


/**
 * Settings for Poseidon
 * @author tastybento
 *
 */
@ConfigComment("Poseidon Configuration [version]")
@StoreAt(filename = "config.yml", path = "addons/Poseidon") // Explicitly call out what name this should have.
public class Settings implements WorldSettings {

    // ---------------------------------------------

    // Pregen
    @ConfigComment("Pregeneration is very important otherwise there can be lag for players. The pregenerator")
    @ConfigComment("will generate chunks around potential starting points out to the view distance of the server.")
    @ConfigComment("Pregeneration is done for the overworld, the nether, and the end.")
    @ConfigComment("")
    @ConfigComment("Pregeneration: Continuous or not. Continuous will not wait between pre-generations.")
    @ConfigEntry(path = "poseidon.pregeneration.continuous")
    private boolean preGenContinuous = false;

    @ConfigComment("Pregeneration: Delay in ticks between chunk generations if not continuous")
    @ConfigEntry(path = "poseidon.pregeneration.delay")
    private int pregenDelay = 20;

    @ConfigComment("Pregeneration: How many realms to pregenerate")
    @ConfigEntry(path = "poseidon.pregeneration.number")
    private int pregenNumber = 10;

    // Air
    @ConfigComment("The time a player can be out of the water without suffering in seconds.")
    @ConfigEntry(path = "poseidon.air-effect.grace-period")
    private int airGracePeriod = 3;

    @ConfigComment("Damage per second from being in the air.")
    @ConfigEntry(path = "poseidon.air-effect.damage")
    private int airEffectDamage = 2;

    @ConfigComment("The time drinking water will prevent damage from air in seconds. Drinking water is the same as drinking a water breathing potion.")
    @ConfigEntry(path = "poseidon.air-effect.water-effect-time")
    private int waterEffectTime = 30;

    @ConfigComment("Chance that water mobs will ignore posiedon's children. In percent.")
    @ConfigComment("Makes game easier..")
    @ConfigEntry(path = "poseidon.water-mob-ignore")
    private int ingoreChance = 0;

    // Command
    @ConfigComment("Realm Command. What command users will run to access their realm.")
    @ConfigEntry(path = "poseidon.command.realm")
    private String playerCommandAliases = "po";

    @ConfigComment("The realm admin command.")
    @ConfigEntry(path = "poseidon.command.admin")
    private String adminCommandAliases = "padmin";

    @ConfigComment("The default action for new player command call.")
    @ConfigComment("Sub-command of main player command that will be run on first player command call.")
    @ConfigComment("By default it is sub-command 'create'.")
    @ConfigEntry(path = "poseidon.command.new-player-action")
    private String defaultNewPlayerAction = "create";

    @ConfigComment("The default action for player command.")
    @ConfigComment("Sub-command of main player command that will be run on each player command call.")
    @ConfigComment("By default it is sub-command 'go'.")
    @ConfigEntry(path = "poseidon.command.default-action")
    private String defaultPlayerAction = "go";

    /*      WORLD       */
    @ConfigComment("Friendly name for this world. Used in admin commands. Must be a single word")
    @ConfigEntry(path = "world.friendly-name", needsReset = true)
    private String friendlyName = "Poseidon";

    @ConfigComment("Name of the world - if it does not exist then it will be generated.")
    @ConfigComment("It acts like a prefix for nether and end (e.g. poseidon_world, poseidon_world_nether, poseidon_world_end)")
    @ConfigEntry(path = "world.world-name", needsReset = true)
    private String worldName = "poseidon_world";

    @ConfigComment("Realm tree generation density in %")
    @ConfigEntry(path = "world.realm-trees")
    private int islandTrees = 25;

    @ConfigComment("Realm turtle egg chance to spawn on sand %")
    @ConfigEntry(path = "world.turtle-eggs")
    private int turtleEggs = 25;

    @ConfigComment("Chance of fisherman spawning on a water block in a chunk %")
    @ConfigEntry(path = "world.fisherman")
    private double fisherman = 0.01;

    @ConfigComment("Realm tree types. List type and probability of growing relative to others in the list")
    @ConfigEntry(path = "world.realm-tree-types")
    private Map<TreeType, Double> treeTypes;

    @ConfigComment("World difficulty setting - PEACEFUL, EASY, NORMAL, HARD")
    @ConfigComment("Other plugins may override this setting")
    @ConfigEntry(path = "world.difficulty")
    private Difficulty difficulty = Difficulty.NORMAL;

    @ConfigComment("Spawn limits. These override the limits set in bukkit.yml")
    @ConfigComment("If set to a negative number, the server defaults will be used")
    @ConfigEntry(path = "world.spawn-limits.monsters")
    private int spawnLimitMonsters = -1;
    @ConfigEntry(path = "world.spawn-limits.animals")
    private int spawnLimitAnimals = -1;
    @ConfigEntry(path = "world.spawn-limits.water-animals")
    private int spawnLimitWaterAnimals = -1;
    @ConfigEntry(path = "world.spawn-limits.ambient")
    private int spawnLimitAmbient = -1;
    @ConfigComment("Setting to 0 will disable animal spawns, but this is not recommended. Minecraft default is 400.")
    @ConfigComment("A negative value uses the server default")
    @ConfigEntry(path = "world.spawn-limits.ticks-per-animal-spawns")
    private int ticksPerAnimalSpawns = -1;
    @ConfigComment("Setting to 0 will disable monster spawns, but this is not recommended. Minecraft default is 400.")
    @ConfigComment("A negative value uses the server default")
    @ConfigEntry(path = "world.spawn-limits.ticks-per-monster-spawns")
    private int ticksPerMonsterSpawns = -1;

    @ConfigComment("Radius of realm in blocks. (So distance between realms is twice this)")
    @ConfigComment("It is the same for every dimension : Overworld, Nether and End.")
    @ConfigComment("This value cannot be changed mid-game and the plugin will not start if it is different.")
    @ConfigEntry(path = "world.distance-between-realms", needsReset = true)
    private int islandDistance = 400;

    @ConfigComment("Default protection range radius in blocks. Cannot be larger than distance.")
    @ConfigComment("Admins can change protection sizes for players individually using /padmin range set <player> <new range>")
    @ConfigComment("or set this permission: poseidon.realm.range.<number>")
    @ConfigEntry(path = "world.protection-range", overrideOnChange = true)
    private int islandProtectionRange = 200;

    @ConfigComment("Start realms at these coordinates. This is where new realms will start in the")
    @ConfigComment("world. These must be a factor of your realm distance, but the plugin will auto")
    @ConfigComment("calculate the closest location on the grid. Islands develop around this location")
    @ConfigComment("both positively and negatively in a square grid.")
    @ConfigComment("If none of this makes sense, leave it at 0,0.")
    @ConfigEntry(path = "world.start-x", needsReset = true)
    private int islandStartX = 0;

    @ConfigEntry(path = "world.start-z", needsReset = true)
    private int islandStartZ = 0;

    @ConfigEntry(path = "world.offset-x")
    private int islandXOffset;
    @ConfigEntry(path = "world.offset-z")
    private int islandZOffset;

    @ConfigComment("Blueprint y coordinate of bedrock block.")
    @ConfigComment("If the blueprint is set to sink, this value is ignored and the bedrock will rest on the ocean floor.")
    @ConfigEntry(path = "world.blueprint-height")
    private int islandHeight = 50;
    
    @ConfigComment("The number of concurrent realms a player can have in the world")
    @ConfigComment("A value of 0 will use the BentoBox config.yml default")
    @ConfigEntry(path = "world.concurrent-realms")
    private int concurrentIslands = 0;

    @ConfigComment("Disallow players to have other realms if they are in a team.")
    @ConfigEntry(path = "world.disallow-team-member-realms")
    boolean disallowTeamMemberIslands = true;

    @ConfigComment("Use your own world generator for this world.")
    @ConfigComment("In this case, the plugin will not generate anything.")
    @ConfigEntry(path = "world.use-own-generator", experimental = true)
    private boolean useOwnGenerator;

    @ConfigComment("Sea height (don't changes this mid-game unless you delete the world)")
    @ConfigEntry(path = "world.sea-height")
    private int seaHeight = 70;
    
    @ConfigComment("Sea floor (don't changes this mid-game unless you delete the world)")
    @ConfigEntry(path = "world.sea-floor")
    private int seaFloor = 25;

    @ConfigComment("Water block. This should usually stay as WATER, but may be LAVA for fun")
    @ConfigEntry(path = "world.water-block", needsReset = true)
    private Material waterBlock = Material.WATER;
    
    @ConfigComment("Ocean Floor")
    @ConfigComment("This creates an ocean floor environment, with vanilla elements.")
    @ConfigEntry(path = "world.ocean-floor", needsReset = true)
    private boolean oceanFloor = true;

    @ConfigComment("Structures")
    @ConfigComment("This creates an vanilla structures in the worlds.")
    @ConfigEntry(path = "world.make-structures", needsReset = true)
    private boolean makeStructures = true;

    @ConfigComment("Caves")
    @ConfigComment("This creates an vanilla caves in the worlds.")
    @ConfigEntry(path = "world.make-caves", needsReset = true)
    private boolean makeCaves = true;

    @ConfigComment("Decorations")
    @ConfigComment("This creates an vanilla decorations in the worlds.")
    @ConfigEntry(path = "world.make-decorations", needsReset = true)
    private boolean makeDecorations = true;

    @ConfigComment("Maximum number of realms in the world. Set to -1 or 0 for unlimited. ")
    @ConfigComment("If the number of realms is greater than this number, no new realm will be created.")
    @ConfigEntry(path = "world.max-realms")
    private int maxIslands = -1;

    @ConfigComment("The default game mode for this world. Players will be set to this mode when they create")
    @ConfigComment("a new realm for example. Options are SURVIVAL, CREATIVE, ADVENTURE, SPECTATOR")
    @ConfigEntry(path = "world.default-game-mode")
    private GameMode defaultGameMode = GameMode.SURVIVAL;

    @ConfigComment("The default biome for the overworld sea")
    @ConfigEntry(path = "world.default-sea-biome")
    private Biome defaultSeaBiome = Biome.WARM_OCEAN;
    @ConfigComment("The default biome for the overworld air")
    @ConfigEntry(path = "world.default-air-biome")
    private Biome defaultAirBiome = Biome.PALE_GARDEN;
    @ConfigComment("The default biome for the nether world (this may affect what mobs can spawn)")
    @ConfigEntry(path = "world.default-nether-biome")
    private Biome defaultNetherBiome = Biome.NETHER_WASTES;
    @ConfigComment("The default biome for the end world (this may affect what mobs can spawn)")
    @ConfigEntry(path = "world.default-end-biome")
    private Biome defaultEndBiome = Biome.THE_END;

    @ConfigComment("The maximum number of players a player can ban at any one time in this game mode.")
    @ConfigComment("The permission poseidon.ban.maxlimit.X where X is a number can also be used per player")
    @ConfigComment("-1 = unlimited")
    @ConfigEntry(path = "world.ban-limit")
    private int banLimit = -1;

    // Nether
    @ConfigComment("Generate Nether - if this is false, the nether world will not be made and access to")
    @ConfigComment("the nether will not occur. Other plugins may still enable portal usage.")
    @ConfigComment("Note: Some default challenges will not be possible if there is no nether.")
    @ConfigComment("Note that with a standard nether all players arrive at the same portal and entering a")
    @ConfigComment("portal will return them back to their realms.")
    @ConfigEntry(path = "world.nether.generate")
    private boolean netherGenerate = true;

    @ConfigComment("Realms in Nether. Change to false for standard vanilla nether.")
    @ConfigEntry(path = "world.nether.realms", needsReset = true)
    private boolean netherIslands = true;

    @ConfigComment("Number of mobs per chunk in Poseidon nether.")
    @ConfigEntry(path = "world.nether.mobs-per-chunk")
    private int mobsPerChunk = 5;
    
    @ConfigComment("Poseidon air will be water.")
    @ConfigEntry(path = "world.nether.water-block", needsReset = true)
    private Material netherWaterBlock = Material.WATER;

    @ConfigComment("Nether spawn protection radius - this is the distance around the nether spawn")
    @ConfigComment("that will be protected from player interaction (breaking blocks, pouring lava etc.)")
    @ConfigComment("Minimum is 0 (not recommended), maximum is 100. Default is 25.")
    @ConfigComment("Only applies to vanilla nether")
    @ConfigEntry(path = "world.nether.spawn-radius")
    private int netherSpawnRadius = 32;

    @ConfigComment("This option indicates if nether portals should be linked via dimensions.")
    @ConfigComment("Option will simulate vanilla portal mechanics that links portals together")
    @ConfigComment("or creates a new portal, if there is not a portal in that dimension.")
    @ConfigEntry(path = "world.nether.create-and-link-portals")
    private boolean makeNetherPortals = true;

    // End
    @ConfigComment("The End - if this is false, the end world will not be made and access to")
    @ConfigComment("the end will not occur. Other plugins may still enable portal usage.")
    @ConfigEntry(path = "world.end.generate")
    private boolean endGenerate = true;

    @ConfigComment("Realms in The End. Change to false for standard vanilla end.")
    @ConfigEntry(path = "world.end.realms", needsReset = true)
    private boolean endIslands = true;

    @ConfigComment("Sea height in The End. Only operates if end realms is true.")
    @ConfigComment("Changing mid-game will cause problems!")
    @ConfigEntry(path = "world.end.sea-height", needsReset = true)
    private int endSeaHeight = 80;
    
    @ConfigComment("Sea floor in The End. Only operates if end realms is true.")
    @ConfigComment("Changing mid-game will cause problems!")
    @ConfigEntry(path = "world.end.sea-floor", needsReset = true)
    private int endSeaFloor = 0;

    @ConfigComment("Water block. This should usually stay as WATER, but may be LAVA for fun")
    @ConfigEntry(path = "world.end.water-block", needsReset = true)
    private Material endWaterBlock = Material.WATER;

    @ConfigComment("This option indicates if obsidian platform in the end should be generated")
    @ConfigComment("when player enters the end world.")
    @ConfigEntry(path = "world.end.create-obsidian-platform")
    private boolean makeEndPortals = false;

    @ConfigEntry(path = "world.end.dragon-spawn", experimental = true)
    private boolean dragonSpawn = false;

    @ConfigComment("Removing mobs - this kills all monsters in the vicinity. Benefit is that it helps")
    @ConfigComment("players return to their realm if the realm has been overrun by monsters.")
    @ConfigComment("This setting is toggled in world flags and set by the settings GUI.")
    @ConfigComment("Mob white list - these mobs will NOT be removed when logging in or doing /po")
    @ConfigEntry(path = "world.remove-mobs-whitelist")
    private Set<EntityType> removeMobsWhitelist = new HashSet<>();

    @ConfigComment("World flags. These are boolean settings for various flags for this world")
    @ConfigEntry(path = "world.flags")
    private Map<String, Boolean> worldFlags = new HashMap<>();

    @ConfigComment("These are the default protection settings for new realms.")
    @ConfigComment("The value is the minimum realm rank required allowed to do the action")
    @ConfigComment("Ranks are: Visitor = 0, Member = 900, Owner = 1000")
    @ConfigEntry(path = "world.default-island-flags")
    private Map<String, Integer> defaultIslandFlagNames = new HashMap<>();

    @ConfigComment("These are the default settings for new realms")
    @ConfigEntry(path = "world.default-island-settings")
    @Adapter(FlagBooleanSerializer.class)
    private Map<String, Integer> defaultIslandSettingNames = new HashMap<>();

    @ConfigComment("These settings/flags are hidden from users")
    @ConfigComment("Ops can toggle hiding in-game using SHIFT-LEFT-CLICK on flags in settings")
    @ConfigEntry(path = "world.hidden-flags")
    private List<String> hiddenFlags = new ArrayList<>();

    @ConfigComment("Visitor banned commands - Visitors to realms cannot use these commands in this world")
    @ConfigEntry(path = "world.visitor-banned-commands")
    private List<String> visitorBannedCommands = new ArrayList<>();

    @ConfigComment("Falling banned commands - players cannot use these commands when falling")
    @ConfigComment("if the PREVENT_TELEPORT_WHEN_FALLING world setting flag is active")
    @ConfigEntry(path = "world.falling-banned-commands")
    private List<String> fallingBannedCommands = new ArrayList<>();

    // ---------------------------------------------

    /*      ISLAND      */
    @ConfigComment("Default max team size")
    @ConfigComment("Use this permission to set for specific user groups: poseidon.team.maxsize.<number>")
    @ConfigComment("Permission size cannot be less than the default below.")
    @ConfigEntry(path = "realm.max-team-size")
    private int maxTeamSize = 4;

    @ConfigComment("Default maximum number of coop rank members per realm")
    @ConfigComment("Players can have the poseidon.coop.maxsize.<number> permission to be bigger but")
    @ConfigComment("permission size cannot be less than the default below. ")
    @ConfigEntry(path = "realm.max-coop-size")
    private int maxCoopSize = 4;

    @ConfigComment("Default maximum number of trusted rank members per realm")
    @ConfigComment("Players can have the poseidon.trust.maxsize.<number> permission to be bigger but")
    @ConfigComment("permission size cannot be less than the default below. ")
    @ConfigEntry(path = "realm.max-trusted-size")
    private int maxTrustSize = 4;

    @ConfigComment("Default maximum number of homes a player can have. Min = 1")
    @ConfigComment("Accessed via /ai sethome <number> or /ai go <number>")
    @ConfigComment("Use this permission to set for specific user groups: poseidon.island.maxhomes.<number>")
    @ConfigEntry(path = "realm.max-homes")
    private int maxHomes = 5;

    // Reset
    @ConfigComment("How many resets a player is allowed (manage with /padmin reset add/remove/reset/set command)")
    @ConfigComment("Value of -1 means unlimited, 0 means hardcore - no resets.")
    @ConfigComment("Example, 2 resets means they get 2 resets or 3 realms lifetime")
    @ConfigEntry(path = "realm.reset.reset-limit")
    private int resetLimit = -1;

    @ConfigComment("Kicked or leaving players lose resets")
    @ConfigComment("Players who leave a team will lose an realm reset chance")
    @ConfigComment("If a player has zero resets left and leaves a team, they cannot make a new")
    @ConfigComment("realm by themselves and can only join a team.")
    @ConfigComment("Leave this true to avoid players exploiting free realms")
    @ConfigEntry(path = "realm.reset.leavers-lose-reset")
    private boolean leaversLoseReset = false;

    @ConfigComment("Allow kicked players to keep their inventory.")
    @ConfigComment("Overrides the on-leave inventory reset for kicked players.")
    @ConfigEntry(path = "realm.reset.kicked-keep-inventory")
    private boolean kickedKeepInventory = false;

    @ConfigComment("What the plugin should reset when the player joins or creates an realm")
    @ConfigComment("Reset Money - if this is true, will reset the player's money to the starting money")
    @ConfigComment("Recommendation is that this is set to true, but if you run multi-worlds")
    @ConfigComment("make sure your economy handles multi-worlds too.")
    @ConfigEntry(path = "realm.reset.on-join.money")
    private boolean onJoinResetMoney = false;

    @ConfigComment("Reset inventory - if true, the player's inventory will be cleared.")
    @ConfigComment("Note: if you have MultiInv running or a similar inventory control plugin, that")
    @ConfigComment("plugin may still reset the inventory when the world changes.")
    @ConfigEntry(path = "realm.reset.on-join.inventory")
    private boolean onJoinResetInventory = false;

    @ConfigComment("Reset health - if true, the player's health will be reset.")
    @ConfigEntry(path = "realm.reset.on-join.health")
    private boolean onJoinResetHealth = true;

    @ConfigComment("Reset hunger - if true, the player's hunger will be reset.")
    @ConfigEntry(path = "realm.reset.on-join.hunger")
    private boolean onJoinResetHunger = true;

    @ConfigComment("Reset experience points - if true, the player's experience will be reset.")
    @ConfigEntry(path = "realm.reset.on-join.exp")
    private boolean onJoinResetXP = false;

    @ConfigComment("Reset Ender Chest - if true, the player's Ender Chest will be cleared.")
    @ConfigEntry(path = "realm.reset.on-join.ender-chest")
    private boolean onJoinResetEnderChest = false;

    @ConfigComment("What the plugin should reset when the player leaves or is kicked from an realm")
    @ConfigComment("Reset Money - if this is true, will reset the player's money to the starting money")
    @ConfigComment("Recommendation is that this is set to true, but if you run multi-worlds")
    @ConfigComment("make sure your economy handles multi-worlds too.")
    @ConfigEntry(path = "realm.reset.on-leave.money")
    private boolean onLeaveResetMoney = false;

    @ConfigComment("Reset inventory - if true, the player's inventory will be cleared.")
    @ConfigComment("Note: if you have MultiInv running or a similar inventory control plugin, that")
    @ConfigComment("plugin may still reset the inventory when the world changes.")
    @ConfigEntry(path = "realm.reset.on-leave.inventory")
    private boolean onLeaveResetInventory = false;

    @ConfigComment("Reset health - if true, the player's health will be reset.")
    @ConfigEntry(path = "realm.reset.on-leave.health")
    private boolean onLeaveResetHealth = false;

    @ConfigComment("Reset hunger - if true, the player's hunger will be reset.")
    @ConfigEntry(path = "realm.reset.on-leave.hunger")
    private boolean onLeaveResetHunger = false;

    @ConfigComment("Reset experience - if true, the player's experience will be reset.")
    @ConfigEntry(path = "realm.reset.on-leave.exp")
    private boolean onLeaveResetXP = false;

    @ConfigComment("Reset Ender Chest - if true, the player's Ender Chest will be cleared.")
    @ConfigEntry(path = "realm.reset.on-leave.ender-chest")
    private boolean onLeaveResetEnderChest = false;

    @ConfigComment("Toggles the automatic realm creation upon the player's first login on your server.")
    @ConfigComment("If set to true,")
    @ConfigComment("   * Upon connecting to your server for the first time, the player will be told that")
    @ConfigComment("    an realm will be created for him.")
    @ConfigComment("  * Make sure you have a Blueprint Bundle called \"default\": this is the one that will")
    @ConfigComment("    be used to create the realm.")
    @ConfigComment("  * An realm will be created for the player without needing him to run the create command.")
    @ConfigComment("If set to false, this will disable this feature entirely.")
    @ConfigComment("Warning:")
    @ConfigComment("  * If you are running multiple gamemodes on your server, and all of them have")
    @ConfigComment("    this feature enabled, an realm in all the gamemodes will be created simultaneously.")
    @ConfigComment("    However, it is impossible to know on which realm the player will be teleported to afterwards.")
    @ConfigComment("  * Realm creation can be resource-intensive, please consider the options below to help mitigate")
    @ConfigComment("    the potential issues, especially if you expect a lot of players to connect to your server")
    @ConfigComment("    in a limited period of time.")
    @ConfigEntry(path = "realm.create-realm-on-first-login.enable")
    private boolean createIslandOnFirstLoginEnabled;

    @ConfigComment("Time in seconds after the player logged in, before his realm gets created.")
    @ConfigComment("If set to 0 or less, the realm will be created directly upon the player's login.")
    @ConfigComment("It is recommended to keep this value under a minute's time.")
    @ConfigEntry(path = "realm.create-realm-on-first-login.delay")
    private int createIslandOnFirstLoginDelay = 5;

    @ConfigComment("Toggles whether the realm creation should be aborted if the player logged off while the")
    @ConfigComment("delay (see the option above) has not worn off yet.")
    @ConfigComment("If set to true,")
    @ConfigComment("  * If the player has logged off the server while the delay (see the option above) has not")
    @ConfigComment("    worn off yet, this will cancel the realm creation.")
    @ConfigComment("  * If the player relogs afterward, since he will not be recognized as a new player, no realm")
    @ConfigComment("    would be created for him.")
    @ConfigComment("  * If the realm creation started before the player logged off, it will continue.")
    @ConfigComment("If set to false, the player's realm will be created even if he went offline in the meantime.")
    @ConfigComment("Note this option has no effect if the delay (see the option above) is set to 0 or less.")
    @ConfigEntry(path = "realm.create-realm-on-first-login.abort-on-logout")
    private boolean createIslandOnFirstLoginAbortOnLogout = true;

    @ConfigComment("Toggles whether the player should be teleported automatically to his realm when it is created.")
    @ConfigComment("If set to false, the player will be told his realm is ready but will have to teleport to his realm using the command.")
    @ConfigEntry(path = "realm.teleport-player-to-realm-when-created")
    private boolean teleportPlayerToIslandUponIslandCreation = true;

    @ConfigComment("Create Nether or End realms if they are missing when a player goes through a portal.")
    @ConfigComment("Nether and End realms are usually pasted when a player makes their realm, but if they are")
    @ConfigComment("missing for some reason, you can switch this on.")
    @ConfigComment("Note that bedrock removal glitches can exploit this option.")
    @ConfigEntry(path = "realm.create-missing-nether-end-realms")
    private boolean pasteMissingIslands = false;

    // Commands
    @ConfigComment("List of commands to run when a player joins an realm or creates one.")
    @ConfigComment("These commands are run by the console, unless otherwise stated using the [SUDO] prefix,")
    @ConfigComment("in which case they are executed by the player.")
    @ConfigComment("")
    @ConfigComment("Available placeholders for the commands are the following:")
    @ConfigComment("   * [name]: name of the player")
    @ConfigComment("")
    @ConfigComment("Here are some examples of valid commands to execute:")
    @ConfigComment("   * \"[SUDO] bbox version\"")
    @ConfigComment("   * \"padmin deaths set [player] 0\"")
    @ConfigEntry(path = "realm.commands.on-join")
    private List<String> onJoinCommands = new ArrayList<>();

    @ConfigComment("List of commands to run when a player leaves an realm, resets his realm or gets kicked from it.")
    @ConfigComment("These commands are run by the console, unless otherwise stated using the [SUDO] prefix,")
    @ConfigComment("in which case they are executed by the player.")
    @ConfigComment("")
    @ConfigComment("Available placeholders for the commands are the following:")
    @ConfigComment("   * [name]: name of the player")
    @ConfigComment("")
    @ConfigComment("Here are some examples of valid commands to execute:")
    @ConfigComment("   * '[SUDO] bbox version'")
    @ConfigComment("   * 'padmin deaths set [player] 0'")
    @ConfigComment("")
    @ConfigComment("Note that player-executed commands might not work, as these commands can be run with said player being offline.")
    @ConfigEntry(path = "realm.commands.on-leave")
    private List<String> onLeaveCommands = new ArrayList<>();

    @ConfigComment("List of commands that should be executed when the player respawns after death if Flags.ISLAND_RESPAWN is true.")
    @ConfigComment("These commands are run by the console, unless otherwise stated using the [SUDO] prefix,")
    @ConfigComment("in which case they are executed by the player.")
    @ConfigComment("")
    @ConfigComment("Available placeholders for the commands are the following:")
    @ConfigComment("   * [name]: name of the player")
    @ConfigComment("")
    @ConfigComment("Here are some examples of valid commands to execute:")
    @ConfigComment("   * '[SUDO] bbox version'")
    @ConfigComment("   * 'bsbadmin deaths set [player] 0'")
    @ConfigComment("")
    @ConfigComment("Note that player-executed commands might not work, as these commands can be run with said player being offline.")
    @ConfigEntry(path = "realm.commands.on-respawn")
    private List<String> onRespawnCommands = new ArrayList<>();

    // Sethome
    @ConfigComment("Allow setting home in the nether. Only available on nether realms, not vanilla nether.")
    @ConfigEntry(path = "realm.sethome.nether.allow")
    private boolean allowSetHomeInNether = true;

    @ConfigEntry(path = "realm.sethome.nether.require-confirmation")
    private boolean requireConfirmationToSetHomeInNether = true;

    @ConfigComment("Allow setting home in the end. Only available on end realms, not vanilla end.")
    @ConfigEntry(path = "realm.sethome.the-end.allow")
    private boolean allowSetHomeInTheEnd = true;

    @ConfigEntry(path = "realm.sethome.the-end.require-confirmation")
    private boolean requireConfirmationToSetHomeInTheEnd = true;

    // Deaths
    @ConfigComment("Whether deaths are counted or not.")
    @ConfigEntry(path = "realm.deaths.counted")
    private boolean deathsCounted = true;

    @ConfigComment("Maximum number of deaths to count. The death count can be used by add-ons.")
    @ConfigEntry(path = "realm.deaths.max")
    private int deathsMax = 10;

    @ConfigComment("When a player joins a team, reset their death count")
    @ConfigEntry(path = "realm.deaths.team-join-reset")
    private boolean teamJoinDeathReset = true;

    @ConfigComment("Reset player death count when they start a new realm or reset and realm")
    @ConfigEntry(path = "realm.deaths.reset-on-new-realm")
    private boolean deathsResetOnNewIsland = true;

    // Ranks
    @ConfigEntry(path = "realm.customranks")
    private Map<String, Integer> customRanks = new HashMap<>();

    // ---------------------------------------------

    /*      PROTECTION      */
    @ConfigComment("Geo restrict mobs.")
    @ConfigComment("Mobs that exit the realm space where they were spawned will be removed.")
    @ConfigEntry(path = "protection.geo-limit-settings")
    private List<String> geoLimitSettings = new ArrayList<>();

    @ConfigComment("Poseidon blocked mobs.")
    @ConfigComment("List of mobs that should not spawn in Poseidon.")
    @ConfigEntry(path = "protection.block-mobs")
    private List<String> mobLimitSettings = new ArrayList<>();

    // Invincible visitor settings
    @ConfigComment("Invincible visitors. List of damages that will not affect visitors.")
    @ConfigComment("Make list blank if visitors should receive all damages")
    @ConfigEntry(path = "protection.invincible-visitors")
    private List<String> ivSettings = new ArrayList<>();

    //---------------------------------------------------------------------------------------/
    @ConfigComment("These settings should not be edited")
    @ConfigEntry(path = "do-not-edit-these-settings.reset-epoch")
    private long resetEpoch = 0;

    @Override
    public int getBanLimit() {
        return banLimit;
    }
    //---------------------------------------------------------------------------------------/
    /**
     * @return the customRanks
     */
    public Map<String, Integer> getCustomRanks() {
        return customRanks;
    }
    /**
     * @return the deathsMax
     */
    @Override
    public int getDeathsMax() {
        return deathsMax;
    }
    /**
     * @return the defaultGameMode
     */
    @Override
    public GameMode getDefaultGameMode() {
        return defaultGameMode;
    }


    /**
     * @return the defaultIslandFlags
     * @since 1.21.0
     */
    @Override
    public Map<String, Integer> getDefaultIslandFlagNames()
    {
        return defaultIslandFlagNames;
    }


    /**
     * @return the defaultIslandSettings
     * @since 1.21.0
     */
    @Override
    public Map<String, Integer> getDefaultIslandSettingNames()
    {
        return defaultIslandSettingNames;
    }

    /**
     * @return the defaultIslandProtection
     * @deprecated since 1.21
     */
    @Override
    public Map<Flag, Integer> getDefaultIslandFlags() {
        return Collections.emptyMap();
    }

    /**
     * @return the defaultIslandSettings
     * @deprecated since 1.21
     */
    @Override
    public Map<Flag, Integer> getDefaultIslandSettings() {
        return Collections.emptyMap();
    }

    /**
     * @return the difficulty
     */
    @Override
    public Difficulty getDifficulty() {
        return difficulty;
    }
    /**
     * @return the endSeaHeight
     */
    public int getEndSeaHeight() {
        return endSeaHeight;
    }
    /* (non-Javadoc)
     * @see world.bentobox.bbox.api.configuration.WorldSettings#getFriendlyName()
     */
    @Override
    public String getFriendlyName() {
        return friendlyName;
    }
    /**
     * @return the geoLimitSettings
     */
    @Override
    public List<String> getGeoLimitSettings() {
        return geoLimitSettings;
    }
    /**
     * @return the islandDistance
     */
    @Override
    public int getIslandDistance() {
        return islandDistance;
    }
    /**
     * @return the islandHeight
     */
    @Override
    public int getIslandHeight() {
        return islandHeight;
    }
    /**
     * @return the islandProtectionRange
     */
    @Override
    public int getIslandProtectionRange() {
        return islandProtectionRange;
    }
    /**
     * @return the islandStartX
     */
    @Override
    public int getIslandStartX() {
        return islandStartX;
    }
    /**
     * @return the islandStartZ
     */
    @Override
    public int getIslandStartZ() {
        return islandStartZ;
    }
    /**
     * @return the islandXOffset
     */
    @Override
    public int getIslandXOffset() {
        return islandXOffset;
    }
    /**
     * @return the islandZOffset
     */
    @Override
    public int getIslandZOffset() {
        return islandZOffset;
    }
    /**
     * Invincible visitor settings
     * @return the ivSettings
     */
    @Override
    public List<String> getIvSettings() {
        return ivSettings;
    }
    /**
     * @return the maxHomes
     */
    @Override
    public int getMaxHomes() {
        return maxHomes;
    }
    /**
     * @return the maxIslands
     */
    @Override
    public int getMaxIslands() {
        return maxIslands;
    }
    /**
     * @return the maxTeamSize
     */
    @Override
    public int getMaxTeamSize() {
        return maxTeamSize;
    }
    /**
     * @return the netherSpawnRadius
     */
    @Override
    public int getNetherSpawnRadius() {
        return netherSpawnRadius;
    }
    @Override
    public String getPermissionPrefix() {
        return "poseidon";
    }
    /**
     * @return the removeMobsWhitelist
     */
    @Override
    public Set<EntityType> getRemoveMobsWhitelist() {
        return removeMobsWhitelist;
    }
    @Override
    public long getResetEpoch() {
        return resetEpoch;
    }
    /**
     * @return the resetLimit
     */
    @Override
    public int getResetLimit() {
        return resetLimit;
    }
    /**
     * @return the seaHeight
     */
    @Override
    public int getSeaHeight() {
        return seaHeight;
    }
    /**
     * @return the hidden flags
     */
    @Override
    public List<String> getHiddenFlags() {
        return hiddenFlags;
    }
    /**
     * @return the visitorbannedcommands
     */
    @Override
    public List<String> getVisitorBannedCommands() {
        return visitorBannedCommands;
    }

    /**
     * @return the fallingBannedCommands
     */
    @Override
    public List<String> getFallingBannedCommands() {
        return fallingBannedCommands;
    }

    /**
     * @return the worldFlags
     */
    @Override
    public Map<String, Boolean> getWorldFlags() {
        return worldFlags;
    }
    /**
     * @return the worldName
     */
    @Override
    public String getWorldName() {
        return worldName;
    }
    /**
     * @return the allowSetHomeInNether
     */
    @Override
    public boolean isAllowSetHomeInNether() {
        return allowSetHomeInNether;
    }
    /**
     * @return the allowSetHomeInTheEnd
     */
    @Override
    public boolean isAllowSetHomeInTheEnd() {
        return allowSetHomeInTheEnd;
    }
    /**
     * @return the isDeathsCounted
     */
    @Override
    public boolean isDeathsCounted() {
        return deathsCounted;
    }
    /**
     * @return the dragonSpawn
     */
    @Override
    public boolean isDragonSpawn() {
        return dragonSpawn;
    }
    /**
     * @return the endGenerate
     */
    @Override
    public boolean isEndGenerate() {
        return endGenerate;
    }
    /**
     */
    @Override
    public boolean isEndIslands() {
        return endIslands;
    }
    /**
     * @return the kickedKeepInventory
     */
    @Override
    public boolean isKickedKeepInventory() {
        return kickedKeepInventory;
    }


    /**
     * This method returns the createIslandOnFirstLoginEnabled boolean value.
     * @return the createIslandOnFirstLoginEnabled value
     * @since 1.9.0
     */
    @Override
    public boolean isCreateIslandOnFirstLoginEnabled()
    {
        return createIslandOnFirstLoginEnabled;
    }


    /**
     * This method returns the createIslandOnFirstLoginDelay int value.
     * @return the createIslandOnFirstLoginDelay value
     * @since 1.9.0
     */
    @Override
    public int getCreateIslandOnFirstLoginDelay()
    {
        return createIslandOnFirstLoginDelay;
    }


    /**
     * This method returns the createIslandOnFirstLoginAbortOnLogout boolean value.
     * @return the createIslandOnFirstLoginAbortOnLogout value
     * @since 1.9.0
     */
    @Override
    public boolean isCreateIslandOnFirstLoginAbortOnLogout()
    {
        return createIslandOnFirstLoginAbortOnLogout;
    }


    /**
     * @return the leaversLoseReset
     */
    @Override
    public boolean isLeaversLoseReset() {
        return leaversLoseReset;
    }
    /**
     * @return the netherGenerate
     */
    @Override
    public boolean isNetherGenerate() {
        return netherGenerate;
    }
    /**
     * @return the netherIslands
     */
    @Override
    public boolean isNetherIslands() {
        return netherIslands;
    }
    /**
     * @return the onJoinResetEnderChest
     */
    @Override
    public boolean isOnJoinResetEnderChest() {
        return onJoinResetEnderChest;
    }
    /**
     * @return the onJoinResetInventory
     */
    @Override
    public boolean isOnJoinResetInventory() {
        return onJoinResetInventory;
    }
    /**
     * @return the onJoinResetMoney
     */
    @Override
    public boolean isOnJoinResetMoney() {
        return onJoinResetMoney;
    }
    /**
     * @return the onLeaveResetEnderChest
     */
    @Override
    public boolean isOnLeaveResetEnderChest() {
        return onLeaveResetEnderChest;
    }
    /**
     * @return the onLeaveResetInventory
     */
    @Override
    public boolean isOnLeaveResetInventory() {
        return onLeaveResetInventory;
    }
    /**
     * @return the onLeaveResetMoney
     */
    @Override
    public boolean isOnLeaveResetMoney() {
        return onLeaveResetMoney;
    }
    /**
     * @return the requireConfirmationToSetHomeInNether
     */
    @Override
    public boolean isRequireConfirmationToSetHomeInNether() {
        return requireConfirmationToSetHomeInNether;
    }
    /**
     * @return the requireConfirmationToSetHomeInTheEnd
     */
    @Override
    public boolean isRequireConfirmationToSetHomeInTheEnd() {
        return requireConfirmationToSetHomeInTheEnd;
    }
    @Override
    public boolean isTeamJoinDeathReset() {
        return teamJoinDeathReset;
    }
    /**
     * @return the useOwnGenerator
     */
    @Override
    public boolean isUseOwnGenerator() {
        return useOwnGenerator;
    }
    @Override
    public boolean isWaterUnsafe() {
        // Water is safe
        return false;
    }
    /**
     * @param adminCommand what you want your admin command to be
     */
    public void setAdminCommand(String adminCommand) {
        this.adminCommandAliases = adminCommand;
    }
    /**
     * @param allowSetHomeInNether the allowSetHomeInNether to set
     */
    public void setAllowSetHomeInNether(boolean allowSetHomeInNether) {
        this.allowSetHomeInNether = allowSetHomeInNether;
    }
    /**
     * @param allowSetHomeInTheEnd the allowSetHomeInTheEnd to set
     */
    public void setAllowSetHomeInTheEnd(boolean allowSetHomeInTheEnd) {
        this.allowSetHomeInTheEnd = allowSetHomeInTheEnd;
    }
    /**
     * @param banLimit the banLimit to set
     */
    public void setBanLimit(int banLimit) {
        this.banLimit = banLimit;
    }
    /**
     * @param customRanks the customRanks to set
     */
    public void setCustomRanks(Map<String, Integer> customRanks) {
        this.customRanks = customRanks;
    }
    /**
     * @param deathsCounted the deathsCounted to set
     */
    public void setDeathsCounted(boolean deathsCounted) {
        this.deathsCounted = deathsCounted;
    }
    /**
     * @param deathsMax the deathsMax to set
     */
    public void setDeathsMax(int deathsMax) {
        this.deathsMax = deathsMax;
    }
    /**
     * @param defaultGameMode the defaultGameMode to set
     */
    public void setDefaultGameMode(GameMode defaultGameMode) {
        this.defaultGameMode = defaultGameMode;
    }
    /**
     */
    public void setDefaultIslandFlagNames(Map<String, Integer> defaultIslandFlags) {
        this.defaultIslandFlagNames = defaultIslandFlags;
    }
    /**
     * @param defaultIslandSettings the defaultIslandSettings to set
     */
    public void setDefaultIslandSettingNames(Map<String, Integer> defaultIslandSettings) {
        this.defaultIslandSettingNames = defaultIslandSettings;
    }
    /**
     * @param difficulty the difficulty to set
     */
    @Override
    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }
    /**
     * @param dragonSpawn the dragonSpawn to set
     */
    public void setDragonSpawn(boolean dragonSpawn) {
        this.dragonSpawn = dragonSpawn;
    }
    /**
     * @param endGenerate the endGenerate to set
     */
    public void setEndGenerate(boolean endGenerate) {
        this.endGenerate = endGenerate;
    }
    /**
     * @param endIslands the endIslands to set
     */
    public void setEndIslands(boolean endIslands) {
        this.endIslands = endIslands;
    }

    /**
     * @param friendlyName the friendlyName to set
     */
    public void setFriendlyName(String friendlyName) {
        this.friendlyName = friendlyName;
    }

    /**
     * @param geoLimitSettings the geoLimitSettings to set
     */
    public void setGeoLimitSettings(List<String> geoLimitSettings) {
        this.geoLimitSettings = geoLimitSettings;
    }
    
    /**
     * @param islandCommand what you want your realm command to be
     */
    public void setIslandCommand(String islandCommand) {
        this.playerCommandAliases = islandCommand;
    }

    /**
     * @param islandDistance the islandDistance to set
     */
    public void setIslandDistance(int islandDistance) {
        this.islandDistance = islandDistance;
    }
    /**
     * @param islandHeight the islandHeight to set
     */
    public void setIslandHeight(int islandHeight) {
        this.islandHeight = islandHeight;
    }

    /**
     * @param islandProtectionRange the islandProtectionRange to set
     */
    public void setIslandProtectionRange(int islandProtectionRange) {
        this.islandProtectionRange = islandProtectionRange;
    }

    /**
     * @param islandStartX the islandStartX to set
     */
    public void setIslandStartX(int islandStartX) {
        this.islandStartX = islandStartX;
    }
    /**
     * @param islandStartZ the islandStartZ to set
     */
    public void setIslandStartZ(int islandStartZ) {
        this.islandStartZ = islandStartZ;
    }
    /**
     * @param islandXOffset the islandXOffset to set
     */
    public void setIslandXOffset(int islandXOffset) {
        this.islandXOffset = islandXOffset;
    }
    /**
     * @param islandZOffset the islandZOffset to set
     */
    public void setIslandZOffset(int islandZOffset) {
        this.islandZOffset = islandZOffset;
    }
    /**
     * @param ivSettings the ivSettings to set
     */
    public void setIvSettings(List<String> ivSettings) {
        this.ivSettings = ivSettings;
    }
    /**
     * @param kickedKeepInventory the kickedKeepInventory to set
     */
    public void setKickedKeepInventory(boolean kickedKeepInventory) {
        this.kickedKeepInventory = kickedKeepInventory;
    }
    /**
     * @param leaversLoseReset the leaversLoseReset to set
     */
    public void setLeaversLoseReset(boolean leaversLoseReset) {
        this.leaversLoseReset = leaversLoseReset;
    }
    /**
     * @param maxHomes the maxHomes to set
     */
    public void setMaxHomes(int maxHomes) {
        this.maxHomes = maxHomes;
    }
    /**
     * @param maxIslands the maxIslands to set
     */
    public void setMaxIslands(int maxIslands) {
        this.maxIslands = maxIslands;
    }
    /**
     * @param maxTeamSize the maxTeamSize to set
     */
    public void setMaxTeamSize(int maxTeamSize) {
        this.maxTeamSize = maxTeamSize;
    }
    /**
     * @param netherGenerate the netherGenerate to set
     */
    public void setNetherGenerate(boolean netherGenerate) {
        this.netherGenerate = netherGenerate;
    }
    /**
     * @param netherIslands the netherIslands to set
     */
    public void setNetherIslands(boolean netherIslands) {
        this.netherIslands = netherIslands;
    }
    /**
     * @param netherSpawnRadius the netherSpawnRadius to set
     */
    public void setNetherSpawnRadius(int netherSpawnRadius) {
        this.netherSpawnRadius = netherSpawnRadius;
    }
    /**
     * @param onJoinResetEnderChest the onJoinResetEnderChest to set
     */
    public void setOnJoinResetEnderChest(boolean onJoinResetEnderChest) {
        this.onJoinResetEnderChest = onJoinResetEnderChest;
    }

    /**
     * @param onJoinResetInventory the onJoinResetInventory to set
     */
    public void setOnJoinResetInventory(boolean onJoinResetInventory) {
        this.onJoinResetInventory = onJoinResetInventory;
    }

    /**
     * @param onJoinResetMoney the onJoinResetMoney to set
     */
    public void setOnJoinResetMoney(boolean onJoinResetMoney) {
        this.onJoinResetMoney = onJoinResetMoney;
    }
    /**
     * @param onLeaveResetEnderChest the onLeaveResetEnderChest to set
     */
    public void setOnLeaveResetEnderChest(boolean onLeaveResetEnderChest) {
        this.onLeaveResetEnderChest = onLeaveResetEnderChest;
    }

    /**
     * @param onLeaveResetInventory the onLeaveResetInventory to set
     */
    public void setOnLeaveResetInventory(boolean onLeaveResetInventory) {
        this.onLeaveResetInventory = onLeaveResetInventory;
    }
    /**
     * @param onLeaveResetMoney the onLeaveResetMoney to set
     */
    public void setOnLeaveResetMoney(boolean onLeaveResetMoney) {
        this.onLeaveResetMoney = onLeaveResetMoney;
    }

    /**
     * @param removeMobsWhitelist the removeMobsWhitelist to set
     */
    public void setRemoveMobsWhitelist(Set<EntityType> removeMobsWhitelist) {
        this.removeMobsWhitelist = removeMobsWhitelist;
    }
    /**
     * @param requireConfirmationToSetHomeInNether the requireConfirmationToSetHomeInNether to set
     */
    public void setRequireConfirmationToSetHomeInNether(boolean requireConfirmationToSetHomeInNether) {
        this.requireConfirmationToSetHomeInNether = requireConfirmationToSetHomeInNether;
    }
    /**
     * @param requireConfirmationToSetHomeInTheEnd the requireConfirmationToSetHomeInTheEnd to set
     */
    public void setRequireConfirmationToSetHomeInTheEnd(boolean requireConfirmationToSetHomeInTheEnd) {
        this.requireConfirmationToSetHomeInTheEnd = requireConfirmationToSetHomeInTheEnd;
    }
    @Override
    public void setResetEpoch(long timestamp) {
        this.resetEpoch = timestamp;
    }
    /**
     * @param resetLimit the resetLimit to set
     */
    public void setResetLimit(int resetLimit) {
        this.resetLimit = resetLimit;
    }
    /**
     * @param seaHeight the seaHeight to set
     */
    public void setSeaHeight(int seaHeight) {
        this.seaHeight = seaHeight;
    }
    /**
     * @param endSeaHeight the endSeaHeight to set
     */
    public void setEndSeaHeight(int endSeaHeight) {
        this.endSeaHeight = endSeaHeight;
    }
    /**
     * @param teamJoinDeathReset the teamJoinDeathReset to set
     */
    public void setTeamJoinDeathReset(boolean teamJoinDeathReset) {
        this.teamJoinDeathReset = teamJoinDeathReset;
    }

    /**
     * @param useOwnGenerator the useOwnGenerator to set
     */
    public void setUseOwnGenerator(boolean useOwnGenerator) {
        this.useOwnGenerator = useOwnGenerator;
    }

    /**
     * @param hiddenFlags the hidden flags to set
     */
    public void setHiddenFlags(List<String> hiddenFlags) {
        this.hiddenFlags = hiddenFlags;
    }

    /**
     * @param visitorBannedCommands the visitorbannedcommands to set
     */
    public void setVisitorBannedCommands(List<String> visitorBannedCommands) {
        this.visitorBannedCommands = visitorBannedCommands;
    }

    /**
     * @param fallingBannedCommands the fallingBannedCommands to set
     */
    public void setFallingBannedCommands(List<String> fallingBannedCommands) {
        this.fallingBannedCommands = fallingBannedCommands;
    }

    /**
     * @param worldFlags the worldFlags to set
     */
    public void setWorldFlags(Map<String, Boolean> worldFlags) {
        this.worldFlags = worldFlags;
    }

    /**
     * @param worldName the worldName to set
     */
    public void setWorldName(String worldName) {
        this.worldName = worldName;
    }
    /**
     * @return the deathsResetOnNewIsland
     */
    @Override
    public boolean isDeathsResetOnNewIsland() {
        return deathsResetOnNewIsland;
    }
    /**
     * @param deathsResetOnNewIsland the deathsResetOnNewIsland to set
     */
    public void setDeathsResetOnNewIsland(boolean deathsResetOnNewIsland) {
        this.deathsResetOnNewIsland = deathsResetOnNewIsland;
    }
    /**
     * @return the onJoinCommands
     */
    @Override
    public List<String> getOnJoinCommands() {
        return onJoinCommands;
    }
    /**
     * @param onJoinCommands the onJoinCommands to set
     */
    public void setOnJoinCommands(List<String> onJoinCommands) {
        this.onJoinCommands = onJoinCommands;
    }
    /**
     * @return the onLeaveCommands
     */
    @Override
    public List<String> getOnLeaveCommands() {
        return onLeaveCommands;
    }
    /**
     * @param onLeaveCommands the onLeaveCommands to set
     */
    public void setOnLeaveCommands(List<String> onLeaveCommands) {
        this.onLeaveCommands = onLeaveCommands;
    }

    /**
     * @return the onRespawnCommands
     */
    @Override
    public List<String> getOnRespawnCommands() {
        return onRespawnCommands;
    }

    /**
     * Sets on respawn commands.
     *
     * @param onRespawnCommands the on respawn commands
     */
    public void setOnRespawnCommands(List<String> onRespawnCommands) {
        this.onRespawnCommands = onRespawnCommands;
    }

    /**
     * @return the onJoinResetHealth
     */
    @Override
    public boolean isOnJoinResetHealth() {
        return onJoinResetHealth;
    }
    /**
     * @param onJoinResetHealth the onJoinResetHealth to set
     */
    public void setOnJoinResetHealth(boolean onJoinResetHealth) {
        this.onJoinResetHealth = onJoinResetHealth;
    }
    /**
     * @return the onJoinResetHunger
     */
    @Override
    public boolean isOnJoinResetHunger() {
        return onJoinResetHunger;
    }
    /**
     * @param onJoinResetHunger the onJoinResetHunger to set
     */
    public void setOnJoinResetHunger(boolean onJoinResetHunger) {
        this.onJoinResetHunger = onJoinResetHunger;
    }
    /**
     * @return the onJoinResetXP
     */
    @Override
    public boolean isOnJoinResetXP() {
        return onJoinResetXP;
    }
    /**
     * @param onJoinResetXP the onJoinResetXP to set
     */
    public void setOnJoinResetXP(boolean onJoinResetXP) {
        this.onJoinResetXP = onJoinResetXP;
    }
    /**
     * @return the onLeaveResetHealth
     */
    @Override
    public boolean isOnLeaveResetHealth() {
        return onLeaveResetHealth;
    }
    /**
     * @param onLeaveResetHealth the onLeaveResetHealth to set
     */
    public void setOnLeaveResetHealth(boolean onLeaveResetHealth) {
        this.onLeaveResetHealth = onLeaveResetHealth;
    }
    /**
     * @return the onLeaveResetHunger
     */
    @Override
    public boolean isOnLeaveResetHunger() {
        return onLeaveResetHunger;
    }
    /**
     * @param onLeaveResetHunger the onLeaveResetHunger to set
     */
    public void setOnLeaveResetHunger(boolean onLeaveResetHunger) {
        this.onLeaveResetHunger = onLeaveResetHunger;
    }
    /**
     * @return the onLeaveResetXP
     */
    @Override
    public boolean isOnLeaveResetXP() {
        return onLeaveResetXP;
    }
    /**
     * @param onLeaveResetXP the onLeaveResetXP to set
     */
    public void setOnLeaveResetXP(boolean onLeaveResetXP) {
        this.onLeaveResetXP = onLeaveResetXP;
    }

    /**
     * @param createIslandOnFirstLoginEnabled the createIslandOnFirstLoginEnabled to set
     */
    public void setCreateIslandOnFirstLoginEnabled(boolean createIslandOnFirstLoginEnabled)
    {
        this.createIslandOnFirstLoginEnabled = createIslandOnFirstLoginEnabled;
    }

    /**
     * @param createIslandOnFirstLoginDelay the createIslandOnFirstLoginDelay to set
     */
    public void setCreateIslandOnFirstLoginDelay(int createIslandOnFirstLoginDelay)
    {
        this.createIslandOnFirstLoginDelay = createIslandOnFirstLoginDelay;
    }

    /**
     * @param createIslandOnFirstLoginAbortOnLogout the createIslandOnFirstLoginAbortOnLogout to set
     */
    public void setCreateIslandOnFirstLoginAbortOnLogout(boolean createIslandOnFirstLoginAbortOnLogout)
    {
        this.createIslandOnFirstLoginAbortOnLogout = createIslandOnFirstLoginAbortOnLogout;
    }

    /**
     * @return the pasteMissingIslands
     * @since 1.10.0
     */
    @Override
    public boolean isPasteMissingIslands() {
        return pasteMissingIslands;
    }

    /**
     * @param pasteMissingIslands the pasteMissingIslands to set
     * @since 1.10.0
     */
    public void setPasteMissingIslands(boolean pasteMissingIslands) {
        this.pasteMissingIslands = pasteMissingIslands;
    }
    /**
     * @return the spawnLimitMonsters
     */
    public int getSpawnLimitMonsters() {
        return spawnLimitMonsters;
    }
    /**
     * @param spawnLimitMonsters the spawnLimitMonsters to set
     */
    public void setSpawnLimitMonsters(int spawnLimitMonsters) {
        this.spawnLimitMonsters = spawnLimitMonsters;
    }
    /**
     * @return the spawnLimitAnimals
     */
    public int getSpawnLimitAnimals() {
        return spawnLimitAnimals;
    }
    /**
     * @param spawnLimitAnimals the spawnLimitAnimals to set
     */
    public void setSpawnLimitAnimals(int spawnLimitAnimals) {
        this.spawnLimitAnimals = spawnLimitAnimals;
    }
    /**
     * @return the spawnLimitWaterAnimals
     */
    public int getSpawnLimitWaterAnimals() {
        return spawnLimitWaterAnimals;
    }
    /**
     * @param spawnLimitWaterAnimals the spawnLimitWaterAnimals to set
     */
    public void setSpawnLimitWaterAnimals(int spawnLimitWaterAnimals) {
        this.spawnLimitWaterAnimals = spawnLimitWaterAnimals;
    }
    /**
     * @return the spawnLimitAmbient
     */
    public int getSpawnLimitAmbient() {
        return spawnLimitAmbient;
    }
    /**
     * @param spawnLimitAmbient the spawnLimitAmbient to set
     */
    public void setSpawnLimitAmbient(int spawnLimitAmbient) {
        this.spawnLimitAmbient = spawnLimitAmbient;
    }
    /**
     * @return the ticksPerAnimalSpawns
     */
    public int getTicksPerAnimalSpawns() {
        return ticksPerAnimalSpawns;
    }
    /**
     * @param ticksPerAnimalSpawns the ticksPerAnimalSpawns to set
     */
    public void setTicksPerAnimalSpawns(int ticksPerAnimalSpawns) {
        this.ticksPerAnimalSpawns = ticksPerAnimalSpawns;
    }
    /**
     * @return the ticksPerMonsterSpawns
     */
    public int getTicksPerMonsterSpawns() {
        return ticksPerMonsterSpawns;
    }
    /**
     * @param ticksPerMonsterSpawns the ticksPerMonsterSpawns to set
     */
    public void setTicksPerMonsterSpawns(int ticksPerMonsterSpawns) {
        this.ticksPerMonsterSpawns = ticksPerMonsterSpawns;
    }
    /**
     * @return the maxCoopSize
     */
    @Override
    public int getMaxCoopSize() {
        return maxCoopSize;
    }

    /**
     * @param maxCoopSize the maxCoopSize to set
     */
    public void setMaxCoopSize(int maxCoopSize) {
        this.maxCoopSize = maxCoopSize;
    }

    /**
     * @return the maxTrustSize
     */
    @Override
    public int getMaxTrustSize() {
        return maxTrustSize;
    }

    /**
     * @param maxTrustSize the maxTrustSize to set
     */
    public void setMaxTrustSize(int maxTrustSize) {
        this.maxTrustSize = maxTrustSize;
    }
    /**
     * @return the playerCommandAliases
     */
    @Override
    public String getPlayerCommandAliases() {
        return playerCommandAliases;
    }
    /**
     * @param playerCommandAliases the playerCommandAliases to set
     */
    public void setPlayerCommandAliases(String playerCommandAliases) {
        this.playerCommandAliases = playerCommandAliases;
    }
    /**
     * @return the adminCommandAliases
     */
    @Override
    public String getAdminCommandAliases() {
        return adminCommandAliases;
    }
    /**
     * @param adminCommandAliases the adminCommandAliases to set
     */
    public void setAdminCommandAliases(String adminCommandAliases) {
        this.adminCommandAliases = adminCommandAliases;
    }
    /**
     * @return the defaultNewPlayerAction
     */
    @Override
    public String getDefaultNewPlayerAction() {
        return defaultNewPlayerAction;
    }
    /**
     * @param defaultNewPlayerAction the defaultNewPlayerAction to set
     */
    public void setDefaultNewPlayerAction(String defaultNewPlayerAction) {
        this.defaultNewPlayerAction = defaultNewPlayerAction;
    }
    /**
     * @return the defaultPlayerAction
     */
    @Override
    public String getDefaultPlayerAction() {
        return defaultPlayerAction;
    }
    /**
     * @param defaultPlayerAction the defaultPlayerAction to set
     */
    public void setDefaultPlayerAction(String defaultPlayerAction) {
        this.defaultPlayerAction = defaultPlayerAction;
    }
    /**
     * @return the defaultNetherBiome
     */
    public Biome getDefaultNetherBiome() {
        return defaultNetherBiome;
    }
    /**
     * @param defaultNetherBiome the defaultNetherBiome to set
     */
    public void setDefaultNetherBiome(Biome defaultNetherBiome) {
        this.defaultNetherBiome = defaultNetherBiome;
    }
    /**
     * @return the defaultEndBiome
     */
    public Biome getDefaultEndBiome() {
        return defaultEndBiome;
    }
    /**
     * @param defaultEndBiome the defaultEndBiome to set
     */
    public void setDefaultEndBiome(Biome defaultEndBiome) {
        this.defaultEndBiome = defaultEndBiome;
    }
    /**
     * @return the teleportPlayerToIslandUponIslandCreation
     */
    @Override
    public boolean isTeleportPlayerToIslandUponIslandCreation() {
        return teleportPlayerToIslandUponIslandCreation;
    }
    /**
     * @param teleportPlayerToIslandUponIslandCreation the teleportPlayerToIslandUponIslandCreation to set
     */
    public void setTeleportPlayerToIslandUponIslandCreation(boolean teleportPlayerToIslandUponIslandCreation) {
        this.teleportPlayerToIslandUponIslandCreation = teleportPlayerToIslandUponIslandCreation;
    }
    /**
     * @return the mobLimitSettings
     */
    @Override
    public List<String> getMobLimitSettings() {
        return mobLimitSettings;
    }
    /**
     * @param mobLimitSettings the mobLimitSettings to set
     */
    public void setMobLimitSettings(List<String> mobLimitSettings) {
        this.mobLimitSettings = mobLimitSettings;
    }

    /**
     * @return the makeNetherPortals
     */
    @Override
    public boolean isMakeNetherPortals() {
        return makeNetherPortals;
    }

    /**
     * @return the makeEndPortals
     */
    @Override
    public boolean isMakeEndPortals() {
        return makeEndPortals;
    }

    /**
     * Sets make nether portals.
     * @param makeNetherPortals the make nether portals
     */
    public void setMakeNetherPortals(boolean makeNetherPortals) {
        this.makeNetherPortals = makeNetherPortals;
    }

    /**
     * Sets make end portals.
     * @param makeEndPortals the make end portals
     */
    public void setMakeEndPortals(boolean makeEndPortals) {
        this.makeEndPortals = makeEndPortals;
    }
    public Material getWaterBlock() {
        return waterBlock;
    }
    public void setWaterBlock(Material waterBlock) {
        this.waterBlock = waterBlock;
    }
    public Material getNetherWaterBlock() {
        return netherWaterBlock;
    }
    public void setNetherWaterBlock(Material netherWaterBlock) {
        this.netherWaterBlock = netherWaterBlock;
    }
    public Material getEndWaterBlock() {
        return endWaterBlock;
    }
    public void setEndWaterBlock(Material endWaterBlock) {
        this.endWaterBlock = endWaterBlock;
    }
    public boolean isOceanFloor() {
        return oceanFloor;
    }
    public void setOceanFloor(boolean oceanFloor) {
        this.oceanFloor = oceanFloor;
    }

    /**
     * @return the makeStructures
     */
    public boolean isMakeStructures() {
        return makeStructures;
    }

    /**
     * @param makeStructures the makeStructures to set
     */
    public void setMakeStructures(boolean makeStructures) {
        this.makeStructures = makeStructures;
    }

    /**
     * @return the makeCaves
     */
    public boolean isMakeCaves() {
        return makeCaves;
    }

    /**
     * @param makeCaves the makeCaves to set
     */
    public void setMakeCaves(boolean makeCaves) {
        this.makeCaves = makeCaves;
    }

    /**
     * @return the makeDecorations
     */
    public boolean isMakeDecorations() {
        return makeDecorations;
    }

    /**
     * @param makeDecorations the makeDecorations to set
     */
    public void setMakeDecorations(boolean makeDecorations) {
        this.makeDecorations = makeDecorations;
    }

    /**
     * @return the disallowTeamMemberIslands
     */
    @Override
    public boolean isDisallowTeamMemberIslands() {
        return disallowTeamMemberIslands;
    }

    /**
     * @param disallowTeamMemberIslands the disallowTeamMemberIslands to set
     */
    public void setDisallowTeamMemberIslands(boolean disallowTeamMemberIslands) {
        this.disallowTeamMemberIslands = disallowTeamMemberIslands;
    }

    /**
     * @return the concurrentIslands
     */
    @Override
    public int getConcurrentIslands() {
        if (concurrentIslands <= 0) {
            return BentoBox.getInstance().getSettings().getIslandNumber();
        }
        return concurrentIslands;
    }

    /**
     * @param concurrentIslands the concurrentIslands to set
     */
    public void setConcurrentIslands(int concurrentIslands) {
        this.concurrentIslands = concurrentIslands;
    }

    /**
     * @return the seaFloor
     */
    public int getSeaFloor() {
        return seaFloor;
    }

    /**
     * @param seaFloor the seaFloor to set
     */
    public void setSeaFloor(int seaFloor) {
        this.seaFloor = seaFloor;
    }

    /**
     * @return the airEffectTime
     */
    public int getAirGracePeriod() {
        return airGracePeriod;
    }

    /**
     * @param airGracePeriod the airEffectTime to set
     */
    public void setAirGracePeriod(int airGracePeriod) {
        this.airGracePeriod = airGracePeriod;
    }

    /**
     * @return the airEffectDamage
     */
    public int getAirEffectDamage() {
        return airEffectDamage;
    }

    /**
     * @param airEffectDamage the airEffectDamage to set
     */
    public void setAirEffectDamage(int airEffectDamage) {
        this.airEffectDamage = airEffectDamage;
    }

    /**
     * @return the waterEffectTime
     */
    public int getWaterEffectTime() {
        return waterEffectTime;
    }

    /**
     * @param waterEffectTime the waterEffectTime to set
     */
    public void setWaterEffectTime(int waterEffectTime) {
        this.waterEffectTime = waterEffectTime;
    }

    /**
     * @return the ingoreChance
     */
    public int getIngoreChance() {
        return ingoreChance;
    }

    /**
     * @param ingoreChance the ingoreChance to set
     */
    public void setIngoreChance(int ingoreChance) {
        this.ingoreChance = ingoreChance;
    }

    /**
     * @return the islandTrees
     */
    public int getIslandTrees() {
        if (islandTrees < 0) {
            islandTrees = 0;
        }
        return islandTrees;
    }

    /**
     * @param islandTrees the islandTrees to set
     */
    public void setIslandTrees(int islandTrees) {
        this.islandTrees = islandTrees;
    }

    /**
     * @return the mobsPerChunk
     */
    public int getMobsPerChunk() {
        return mobsPerChunk;
    }

    /**
     * @param mobsPerChunk the mobsPerChunk to set
     */
    public void setMobsPerChunk(int mobsPerChunk) {
        this.mobsPerChunk = mobsPerChunk;
    }

    /**
     * @return the treeTypes
     */
    public Map<TreeType, Double> getTreeTypes() {
        if (treeTypes == null) {
            treeTypes = new HashMap<>();
        }
        return treeTypes;
    }

    /**
     * @param treeTypes the treeTypes to set
     */
    public void setTreeTypes(Map<TreeType, Double> treeTypes) {
        this.treeTypes = treeTypes;
    }

    /**
     * @return the defaultSeaBiome
     */
    public Biome getDefaultSeaBiome() {
        return defaultSeaBiome;
    }

    /**
     * @param defaultSeaBiome the defaultSeaBiome to set
     */
    public void setDefaultSeaBiome(Biome defaultSeaBiome) {
        this.defaultSeaBiome = defaultSeaBiome;
    }

    /**
     * @return the defaultAirBiome
     */
    public Biome getDefaultAirBiome() {
        return defaultAirBiome;
    }

    /**
     * @param defaultAirBiome the defaultAirBiome to set
     */
    public void setDefaultAirBiome(Biome defaultAirBiome) {
        this.defaultAirBiome = defaultAirBiome;
    }

    /**
     * @return the turtleEggs
     */
    public int getTurtleEggs() {

        return turtleEggs;
    }

    /**
     * @param turtleEggs the turtleEggs to set
     */
    public void setTurtleEggs(int turtleEggs) {
        this.turtleEggs = turtleEggs;
    }
    /**
     * @return the fisherman
     */
    public double getFisherman() {
        return fisherman;
    }
    /**
     * @param fisherman the fisherman to set
     */
    public void setFisherman(double fisherman) {
        this.fisherman = fisherman;
    }

    public int getExtraMobChance() {
        return 100;
    }

    /**
     * @return the endSeaFloor
     */
    public int getEndSeaFloor() {
        return endSeaFloor;
    }

    /**
     * @param endSeaFloor the endSeaFloor to set
     */
    public void setEndSeaFloor(int endSeaFloor) {
        this.endSeaFloor = endSeaFloor;
    }

    /**
     * @return the preGenContinuous
     */
    public boolean isPreGenContinuous() {
        return preGenContinuous;
    }

    /**
     * @param preGenContinuous the preGenContinuous to set
     */
    public void setPreGenContinuous(boolean preGenContinuous) {
        this.preGenContinuous = preGenContinuous;
    }
    
    /**
     * @return the pregenDelay
     */
    public int getPregenDelay() {
        if (pregenDelay < 1) {
            pregenDelay = 1;
        }
        return pregenDelay;
    }
    
    /**
     * @param pregenDelay the pregenDelay to set
     */
    public void setPregenDelay(int pregenDelay) {
        if (pregenDelay < 1) {
            pregenDelay = 1;
        }
        this.pregenDelay = pregenDelay;
    }

    /**
     * @return the pregenNumber
     */
    public int getPregenNumber() {
        if (pregenNumber < 0) {
            pregenNumber = 0;
        }
        return pregenNumber;
    }
    
    /**
     * @param pregenNumber the pregenNumber to set
     */
    public void setPregenNumber(int pregenNumber) {
        if (pregenNumber < 0) {
            pregenNumber = 0;
        }
        this.pregenNumber = pregenNumber;
    }

}
