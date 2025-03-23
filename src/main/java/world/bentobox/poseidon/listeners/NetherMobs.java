package world.bentobox.poseidon.listeners;

import java.util.Random;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import world.bentobox.poseidon.Poseidon;

/**
 * Changes mobs in the nether
 */
public class NetherMobs implements Listener {

    private Poseidon addon;

    public NetherMobs(Poseidon addon) {
        super();
        this.addon = addon;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onMobDeath(EntityDeathEvent e) {
        World w = e.getEntity().getWorld();
        if (addon.inWorld(w) && w.getEnvironment() == Environment.NETHER && e.getDamageSource() instanceof Player p) {
            // Looting
            ItemStack item = p.getItemInUse();
            int looting = item == null ? 2 : item.getEnchantmentLevel(Enchantment.LOOTING) + 2;
            switch (e.getEntityType()) {
            case ELDER_GUARDIAN:
                e.getDrops().add(new ItemStack(Material.GHAST_TEAR, new Random().nextInt(looting)));
            default:
                break;

            }
        }
    }
}
