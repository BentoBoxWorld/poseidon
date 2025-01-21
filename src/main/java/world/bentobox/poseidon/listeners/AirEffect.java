package world.bentobox.poseidon.listeners;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.WeakHashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Levelled;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
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
            dryPlayers.keySet().removeIf(p -> isInWater(p) != WaterBlock.NONE);
            // Players get a grace period before they start hurting
            dryPlayers.entrySet().stream().filter(
                    en -> System.currentTimeMillis() > en.getValue() + (addon.getSettings().getAirEffectTime() * 1000))
                    .map(Entry::getKey).forEach(this::processAir);
        }, 20L, 20L);
    }

    private void processAir(Player player) {
        // Reduce player's air
        player.setRemainingAir(0);
        player.damage(addon.getSettings().getAirEffectDamage(), DamageSource.builder(DamageType.WIND_CHARGE).build());
    }

    private enum WaterBlock {
        TOP, BOTTOM, BOTH, NONE
    }

    /**
     * Check if a player is in the water (or in general not to be affected by air).
     * Be sure to check that the player is in the world
     * @param p player
     * @return true if player is safe
     */
    private WaterBlock isInWater(Player p) {
        if (!addon.inWorld(p.getLocation())) {
            throw new IllegalStateException(
                    "Player is not in the Poseidon world but the isInWater method was called. Make sure to check if the player is in the world before calling this method.");
        }
        // In this case, "water breathing" literally means breathing water!
        if (p.hasPotionEffect(PotionEffectType.WATER_BREATHING)) {
            return WaterBlock.TOP;
        }
        Location loc = p.getLocation();
        // Top half of player
        Material top = loc.getBlock().getRelative(BlockFace.UP).getType();
        // Bottom half
        Material bot = loc.getBlock().getType();
        boolean topWater = top == Material.WATER || top == Material.BUBBLE_COLUMN || top == Material.TALL_SEAGRASS
                || top == Material.SEAGRASS;
        boolean botWater = bot == Material.WATER || bot == Material.BUBBLE_COLUMN || bot == Material.TALL_SEAGRASS
                || bot == Material.SEAGRASS;
        if (topWater && botWater) {
            return WaterBlock.BOTH;
        }
        if (topWater && !botWater) {
            return WaterBlock.TOP;
        }
        if (botWater && !topWater) {
            return WaterBlock.BOTTOM;
        }
        // Check cauldron
        if (loc.getBlock().getType() == Material.CAULDRON && loc.getBlock().getBlockData() instanceof Levelled cauldron
                && cauldron.getLevel() > 0) {
            // Any level of water is fine
            return WaterBlock.BOTTOM;
        }
        // Check waterlogged blocks
        BlockData bd = loc.getBlock().getBlockData();
        if (bd instanceof Waterlogged wl) {
            return wl.isWaterlogged() ? WaterBlock.BOTTOM : WaterBlock.NONE;
        }
        return WaterBlock.NONE;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerDrinkWater(PlayerItemConsumeEvent e) {
        if (!addon.inWorld(e.getPlayer().getWorld()) || isInWater(e.getPlayer()) != WaterBlock.NONE) {
            // No effect in water
            return;
        }
        if (addon.inWorld(e.getPlayer().getLocation()) && e.getItem() != null
                && e.getItem().getType() == Material.POTION) {
            PotionMeta meta = (PotionMeta) e.getItem().getItemMeta();
            if (meta.getBasePotionType() == PotionType.WATER) {
                e.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING,
                        addon.getSettings().getWaterEffectTime() * 20, 1));
                dryPlayers.remove(e.getPlayer()); // Player is now wet
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerDeath(PlayerDeathEvent e) {
        // Run in main thread
        Bukkit.getScheduler().runTask(addon.getPlugin(), () -> dryPlayers.remove(e.getEntity()));
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent e) {
        if (!addon.inWorld(e.getFrom())) {
            return;
        }
        Player player = e.getPlayer();
        WaterBlock result = isInWater(player);
        if (result == WaterBlock.TOP || result == WaterBlock.BOTH) {
            // Players can always breath underwater
            player.setRemainingAir(player.getMaximumAir());
            // Players can also see underwater
            player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 5 * 20 * 60, 1)); // 5 minutes
            return;
        } else {
            player.removePotionEffect(PotionEffectType.NIGHT_VISION);
        }
        // Player is not in water - only log the initial time they were dry
        if (result == WaterBlock.NONE) {
            dryPlayers.putIfAbsent(player, System.currentTimeMillis());
        }
    }

    public void cancel() {
        task.cancel();
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onDrowned(EntityTargetLivingEntityEvent e) {
        if (!addon.inWorld(e.getEntity().getWorld())) {
            return;
        }
        Random r = new Random();
        if ((e.getEntityType() == EntityType.DROWNED || e.getEntityType() == EntityType.GUARDIAN
                || e.getEntityType() == EntityType.ELDER_GUARDIAN) && e.getTarget() instanceof Player
                && r.nextDouble() < (addon.getSettings().getIngoreChance() / 100D)) {
            e.setCancelled(true);
        }
    }

}