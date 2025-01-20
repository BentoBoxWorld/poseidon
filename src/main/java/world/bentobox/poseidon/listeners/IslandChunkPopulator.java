package world.bentobox.poseidon.listeners;

import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.TreeType;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.util.Vector;

import world.bentobox.bentobox.BentoBox;
import world.bentobox.poseidon.Poseidon;

public class IslandChunkPopulator implements Listener {
    private Poseidon addon;
    private Random random = new Random();

    public IslandChunkPopulator(Poseidon addon) {
        super();
        this.addon = addon;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onChunkLoad(ChunkLoadEvent e) {
        if (e.getWorld().getEnvironment() != Environment.NORMAL || !addon.inWorld(e.getChunk().getWorld())) {
            return;
        }
        // Check if this is at sea level
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                Location loc = new Location(e.getWorld(), (e.getChunk().getX() << 4) + x,
                        addon.getSettings().getSeaHeight(), (e.getChunk().getZ() << 4) + z);
                Block bd = loc.getBlock();
                if (bd.getType() == Material.GRASS_BLOCK || bd.getType() == Material.DIRT) {
                    loc = loc.add(new Vector(0, 1, 0));
                    switch (random.nextInt(6 + addon.getSettings().getIslandTrees())) {
                    case 0 -> e.getWorld().generateTree(loc, random, TreeType.MANGROVE);
                    case 1 -> e.getWorld().generateTree(loc, random, TreeType.TALL_MANGROVE);
                    case 2 -> e.getWorld().generateTree(loc, random, TreeType.JUNGLE);
                    case 3 -> e.getWorld().generateTree(loc, random, TreeType.TREE);
                    case 4 -> e.getWorld().generateTree(loc, random, TreeType.ACACIA);
                    case 5 -> e.getWorld().generateTree(loc, random, TreeType.BIRCH);
                    default -> {
                        // No tree
                    }
                    }
                }
            }
        }
    }
    
}
