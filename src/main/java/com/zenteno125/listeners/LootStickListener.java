package com.zenteno125.listeners;

import com.zenteno125.items.ItemRegistry;
import com.zenteno125.gui.ChestConfigMenu;
import com.zenteno125.items.LootChestManager;
import com.zenteno125.items.LootTemplateManager;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class LootStickListener implements Listener {
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (!ItemRegistry.isLootStick(item)) return;

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Block block = event.getClickedBlock();
            if (block != null && (block.getType() == Material.CHEST || block.getType() == Material.TRAPPED_CHEST)) {
                event.setCancelled(true);
                new ChestConfigMenu(player, block).open();
            }
        }
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        // Solo procesar si es un cofre
        if (!(event.getInventory().getHolder() instanceof Chest)) return;

        Chest chest = (Chest) event.getInventory().getHolder();
        Block block = chest.getBlock();

        // Verificar si el cofre tiene una plantilla asignada
        LootChestManager chestManager = LootChestManager.getInstance();
        String templateName = chestManager.getAssignedTemplate(block);
        if (templateName == null) return;

        // Solo rellenar si el GameManager lo permite (inicio de partida o ronda)
        if (!chestManager.shouldRefillChest(block)) return;

        // Obtener la plantilla y llenar el cofre con su contenido
        Inventory chestInv = event.getInventory();
        chestInv.clear(); // Borrar contenido anterior

        // Cargar ítems de la plantilla y mezclarlos aleatoriamente
        java.util.List<ItemStack> templateItems = new java.util.ArrayList<>(LootTemplateManager.getInstance().getLoot(templateName));
        java.util.List<ItemStack> nonNullItems = new java.util.ArrayList<>();

        // Filtrar ítems no nulos
        for (ItemStack item : templateItems) {
            if (item != null) {
                nonNullItems.add(item.clone()); // Clonar para no modificar la plantilla original
            }
        }

        // Mezclar aleatoriamente
        java.util.Collections.shuffle(nonNullItems);

        // Determinar cuántos ítems podemos colocar (como máximo, el tamaño del cofre)
        int size = chestInv.getSize();
        int itemsToPlace = Math.min(nonNullItems.size(), size);

        // Crear una lista de slots disponibles
        java.util.List<Integer> availableSlots = new java.util.ArrayList<>();
        for (int i = 0; i < size; i++) {
            availableSlots.add(i);
        }

        // Mezclar los slots para colocación aleatoria
        java.util.Collections.shuffle(availableSlots);

        // Colocar los ítems en slots aleatorios
        for (int i = 0; i < itemsToPlace; i++) {
            chestInv.setItem(availableSlots.get(i), nonNullItems.get(i));
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        // Solo procesar si es un cofre
        if (block.getType() != Material.CHEST && block.getType() != Material.TRAPPED_CHEST) return;

        // Si el cofre tiene asignada una plantilla, no deja que dropee ítems vanilla
        if (LootChestManager.getInstance().hasTemplate(block)) {
            // No cancelamos el evento, solo aseguramos que no dropee los ítems normalmente
            ((Chest)block.getState()).getBlockInventory().clear();

            // Remover la asignación de la plantilla cuando se rompe el cofre
            LootChestManager.getInstance().removeTemplate(block);
        }
    }
}
