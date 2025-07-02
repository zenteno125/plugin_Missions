package com.zenteno125.listeners;

import com.zenteno125.game.TeamManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class TeamBlockListener implements Listener {

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItemInHand();

        if (isSpawnBlock(item)) {
            Material material = item.getType();
            TeamManager.TeamColor color = TeamManager.TeamColor.fromMaterial(material);

            if (color != null) {
                Location location = event.getBlockPlaced().getLocation().add(0.5, 0, 0.5);
                TeamManager.getInstance().addSpawnPoint(color, location);

                player.sendMessage(color.getChatColor() + "Has creado un punto de aparición para el equipo " +
                                   color.getSpanishName() + " en " +
                                   formatLocation(location));
            }
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        if (TeamManager.getInstance().isDuelActive()) {
            // Manejar muerte de jugador en duelo
            TeamManager.getInstance().handlePlayerDeath(player);
        }
    }

    private boolean isSpawnBlock(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;

        ItemMeta meta = item.getItemMeta();
        if (!meta.hasDisplayName()) return false;

        String displayName = meta.getDisplayName();
        return displayName.contains("Punto de aparición");
    }

    private String formatLocation(Location loc) {
        return String.format("X: %.1f, Y: %.1f, Z: %.1f",
                            loc.getX(), loc.getY(), loc.getZ());
    }
}
