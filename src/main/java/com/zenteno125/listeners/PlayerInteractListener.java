package com.zenteno125.listeners;

import com.zenteno125.gui.CapturedEggsMenu;
import com.zenteno125.gui.EditSpawnPointMenu;
import com.zenteno125.gui.GlobalActionsMenu;
import com.zenteno125.gui.SelectMobMenu;
import com.zenteno125.gui.SpawnPointManager;
import com.zenteno125.items.ItemRegistry;
import com.zenteno125.items.SpawnStickData;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class PlayerInteractListener implements Listener {

    /* ───────────────────────────────
     * 1. Click derecho a una entidad
     *    → capturar mob en el palo
     * ─────────────────────────────── */
    @EventHandler
    public void onInteractEntity(PlayerInteractEntityEvent e) {
        if (e.getHand() != EquipmentSlot.HAND) return;

        ItemStack stick = e.getPlayer().getInventory().getItemInMainHand();
        if (!ItemRegistry.isSpawnStick(stick)) return;

        e.setCancelled(true);
        EntityType type = e.getRightClicked().getType();
        SpawnStickData.addEntity(stick, type);
        e.getPlayer().sendMessage("§aCapturado §f" + type.getKey());
    }

    /* ───────────────────────────────
     * 2. Click aire / bloque con Palo
     * ─────────────────────────────── */
    @EventHandler
    public void onInteract(PlayerInteractEvent e) {

        ItemStack stick = e.getPlayer().getInventory().getItemInMainHand();
        if (!ItemRegistry.isSpawnStick(stick)) return;

        /* 2.1 Sneak + click aire ⇒ menú global (borrar todos) */
        if (e.getAction() == Action.RIGHT_CLICK_AIR && e.getPlayer().isSneaking()) {
            e.setCancelled(true);
            new GlobalActionsMenu(e.getPlayer()).open();
            return;
        }

        /* 2.2 Click aire normal ⇒ GUI Capturar Huevos */
        if (e.getAction() == Action.RIGHT_CLICK_AIR) {
            e.setCancelled(true);
            new CapturedEggsMenu(e.getPlayer(), stick).open();
            return;
        }

        /* 2.3 Click en bloque
         * - Bloque: crear punto de spawn o editar existente */
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            e.setCancelled(true);
            Block block = e.getClickedBlock();

            // Verificar si ya existe un punto de spawn en esta ubicación
            SpawnPointManager manager = SpawnPointManager.getInstance();
            if (manager.get(block.getLocation()).isPresent()) {
                // Si el punto ya existe, abrir menú de edición
                new EditSpawnPointMenu(e.getPlayer(), block.getLocation()).open();
                return;
            }

            // Si es un punto nuevo, abrir el menú de selección de mobs
            List<EntityType> entities = SpawnStickData.asEntityTypes(stick);
            if (entities.isEmpty()) {
                e.getPlayer().sendMessage("§cNo hay entidades capturadas en el palo.");
                return;
            }

            // Callback para cuando se seleccionen los mobs
            new SelectMobMenu(e.getPlayer(), stick,
                    (selection) -> {
                        manager.addPoint(block.getLocation(), selection);
                        e.getPlayer().sendMessage("§aGuardado punto de spawn con §f" + selection.size() + "§a mobs.");

                        // Lanzar efecto visual en el punto de spawn
                        block.getWorld().spawnParticle(
                                org.bukkit.Particle.SPELL_WITCH,
                                block.getLocation().clone().add(0.5, 1.0, 0.5),
                                30, 0.5, 0.5, 0.5, 0.01);
                    }).open();
        }
    }
}
