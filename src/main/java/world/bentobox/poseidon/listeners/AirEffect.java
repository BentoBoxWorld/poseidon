package world.bentobox.poseidon.listeners;

import java.util.Map;
import java.util.WeakHashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitTask;

import world.bentobox.bentobox.BentoBox;
import world.bentobox.poseidon.Poseidon;

/**
 * Applies the acid effect to players
 *
 * @author tastybento
 */
public class AirEffect implements Listener {

    private final Map<Player, Long> dryPlayers = new WeakHashMap<>();
    private final BukkitTask task;
    private Poseidon addon;

    public AirEffect(Poseidon addon) {
        this.addon = addon;
        task = Bukkit.getScheduler().runTaskTimer(addon.getPlugin(), () -> {
            dryPlayers.keySet().removeIf(this::isInWater);
            dryPlayers.forEach(this::processAir);
        }, 20L, 20L);
    }

    /**
     * Check if a player is in the water (or in general not to be affected by air)
     * @param p player
     * @return true if player is safe
     */
    private boolean isInWater(Player p) {
        Location l = p.getLocation();
        if (!addon.inWorld(l) || l.getBlock().getType() == Material.WATER
                || l.getBlock().getType() == Material.BUBBLE_COLUMN || l.getBlock().getType() == Material.TALL_SEAGRASS
                || l.getBlock().getType() == Material.SEAGRASS) {
            return true;
        }
        BlockData bd = l.getBlock().getBlockData();
        if (bd instanceof Waterlogged wl) {
            boolean wlc = wl.isWaterlogged();
            if (!wlc) {
                BentoBox.getInstance().logDebug("Not water logged " + wl);
            }
            return wl.isWaterlogged();
        }
        BentoBox.getInstance().logDebug(l.getBlock());
        return false;
    }

    private void processAir(Player player, Long l) {
        // Reduce player's air
        player.setRemainingAir(0);
        player.damage(2, DamageSource.builder(DamageType.WIND_CHARGE).build());
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerDeath(PlayerDeathEvent e) {
        dryPlayers.remove(e.getEntity());
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent e) {
        Player player = e.getPlayer();
        if (isInWater(player)) {
            // Players can always breath underwater
            player.setRemainingAir(player.getMaximumAir());
            return;
        }
        // Player is not in water
        dryPlayers.put(player, System.currentTimeMillis());
    }

    public void cancel() {
        task.cancel();

    }

    /*
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onDrowned(EntityTargetLivingEntityEvent e) {
    
        if ((e.getEntityType().equals(EntityType.DROWNED)
                || e.getEntityType().equals(EntityType.GUARDIAN)
                || e.getEntityType().equals(EntityType.ELDER_GUARDIAN)
                )
                && e.getTarget() instanceof Player) {
            e.setCancelled(true);
        }
    }*/

}