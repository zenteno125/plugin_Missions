package com.zenteno125.particles;

import com.zenteno125.gui.SpawnPointManager;
import com.zenteno125.items.ItemRegistry;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class SpawnParticlesTask extends BukkitRunnable {

    private final SpawnPointManager mgr = SpawnPointManager.getInstance();

    @Override
    public void run() {

        for (Player p : Bukkit.getOnlinePlayers()) {

            /* ¿Tiene el palo en mano principal u off-hand? */
            ItemStack main = p.getInventory().getItemInMainHand();
            ItemStack off  = p.getInventory().getItemInOffHand();
            if (!ItemRegistry.isSpawnStick(main) && !ItemRegistry.isSpawnStick(off)) continue;

            for (Location loc : mgr.getAllPoints()) {
                if (!loc.getWorld().equals(p.getWorld())) continue;        // mismo mundo

                /* Centro del bloque +1 y de nuevo -1 para no modificar map */
                Location view = loc.clone().add(0.5, 1.0, 0.5);
                p.spawnParticle(
                        Particle.REDSTONE,
                        view,
                        1,
                        new Particle.DustOptions(Color.RED, 1.0F)
                );
            }
        }
    }
}
