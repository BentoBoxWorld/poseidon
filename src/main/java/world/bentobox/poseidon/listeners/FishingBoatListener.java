package world.bentobox.poseidon.listeners;

import java.util.Arrays;
import java.util.Random;

import org.bukkit.World.Environment;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.entity.boat.MangroveChestBoat;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.util.Vector;

import world.bentobox.bentobox.BentoBox;
import world.bentobox.poseidon.Poseidon;

public class FishingBoatListener implements Listener {

    private Poseidon addon;
    private Random r = new Random();

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onChunkLoad(ChunkLoadEvent e) {
        if (e.getWorld().getEnvironment() != Environment.NORMAL || !addon.inWorld(e.getWorld())) {
            return;
        }
        for (Entity en : e.getChunk().getEntities()) {
            if (en instanceof MangroveChestBoat mcb) {
                for (Entity p : mcb.getPassengers()) {
                    if (p instanceof Villager) {
                        mcb.setVelocity(new Vector(r.nextInt(3) - r.nextInt(3), 0, r.nextInt(3) - r.nextInt(3)));
                        return;
                    }
                }
            }
        }
        /*
        Arrays.stream(e.getChunk().getEntities()).filter(en -> en instanceof MangroveChestBoat).map(mb -> (Boat) mb)
        .filter(b -> b.getPassengers().stream().filter(p -> p instanceof Villager)
                .anyMatch(v -> ((Villager) v).getProfession() == Profession.FISHERMAN))
        .forEach(this::moveBoat);
        // Nudge the boat a bit
        Arrays.stream(e.getChunk().getEntities()).filter(en -> en instanceof MangroveChestBoat).map(mb -> (Boat) mb)
                .filter(b -> b.getPassengers().stream().filter(p -> p instanceof Villager)
                        .anyMatch(v -> ((Villager) v).getProfession() == Profession.FISHERMAN))
                .forEach(this::moveBoat);
                */
    }

    private void moveBoat(Boat boat) {
        BentoBox.getInstance().logDebug("Moving boat");
        boat.setVelocity(new Vector(r.nextDouble() - 0.5, 0, r.nextDouble() - 0.5));
    }

    public FishingBoatListener(Poseidon addon) {
        super();
        this.addon = addon;
    }
}
