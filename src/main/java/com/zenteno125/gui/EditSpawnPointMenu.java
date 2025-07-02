package com.zenteno125.gui;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Menú para editar un punto de spawn existente
 */
public class EditSpawnPointMenu extends AbstractMenu {
    private final Location spawnLocation;
    private List<EntityType> assignedMobs;

    public EditSpawnPointMenu(Player player, Location spawnLocation) {
        super(player, 6, "§8Editar Punto de Spawn");
        this.spawnLocation = spawnLocation;

        // Obtener los mobs asignados actualmente al punto
        Optional<List<EntityType>> mobsOpt = SpawnPointManager.getInstance().get(spawnLocation);
        this.assignedMobs = mobsOpt.orElse(new ArrayList<>());
    }

    @Override
    protected void draw() {
        // Panel informativo del punto de spawn
        ItemStack infoItem = new ItemStack(Material.END_CRYSTAL);
        ItemMeta infoMeta = infoItem.getItemMeta();
        infoMeta.setDisplayName("§6Punto de Spawn");

        String locationText = String.format("%s (%d, %d, %d)",
                spawnLocation.getWorld().getName(),
                spawnLocation.getBlockX(),
                spawnLocation.getBlockY(),
                spawnLocation.getBlockZ());

        infoMeta.setLore(Arrays.asList(
            "§7Ubicación: §f" + locationText,
            "§7Mobs asignados: §f" + assignedMobs.size()
        ));
        infoItem.setItemMeta(infoMeta);
        inventory.setItem(4, infoItem);

        // Mobs actualmente asignados
        drawAssignedMobs();

        // Botón para eliminar este punto
        ItemStack deleteButton = new ItemStack(Material.BARRIER);
        ItemMeta deleteMeta = deleteButton.getItemMeta();
        deleteMeta.setDisplayName("§c§lELIMINAR PUNTO");
        deleteMeta.setLore(Arrays.asList("§7Click para eliminar", "§7este punto de spawn"));
        deleteButton.setItemMeta(deleteMeta);
        inventory.setItem(53, deleteButton);

        // Mensaje informativo de que hay que recrear el punto para añadir mobs
        ItemStack infoButton = new ItemStack(Material.PAPER);
        ItemMeta infoButtonMeta = infoButton.getItemMeta();
        infoButtonMeta.setDisplayName("§eInformación");
        infoButtonMeta.setLore(Arrays.asList(
            "§7Para añadir nuevos mobs:",
            "§7- Elimina este punto",
            "§7- Crea un nuevo punto con todos los mobs",
            "§7  que desees incluir"));
        infoButton.setItemMeta(infoButtonMeta);
        inventory.setItem(49, infoButton);
    }

    private void drawAssignedMobs() {
        // Mostrar los mobs actualmente asignados a este punto
        int slot = 18;
        for (EntityType type : assignedMobs) {
            if (slot >= 45) break; // Limitar a 27 slots

            Material iconMaterial;
            try {
                iconMaterial = Material.valueOf(type.name() + "_SPAWN_EGG");
            } catch (IllegalArgumentException e) {
                iconMaterial = Material.EGG;
            }

            ItemStack icon = new ItemStack(iconMaterial);
            ItemMeta meta = icon.getItemMeta();
            meta.setDisplayName("§f" + type.getKey().getKey());
            meta.setLore(Arrays.asList("§cShift+Click para eliminar", "§7de este punto de spawn"));
            icon.setItemMeta(meta);

            inventory.setItem(slot++, icon);
        }
    }

    @Override
    public void click(InventoryClickEvent e) {
        e.setCancelled(true);
        int slot = e.getRawSlot();

        // Botón para eliminar el punto
        if (slot == 53) {
            player.closeInventory();
            new ConfirmDeleteSpawnPoint(player, spawnLocation).open();
            return;
        }

        // Click en un mob asignado (para eliminarlo)
        if (slot >= 18 && slot < 45) {
            ItemStack clicked = e.getCurrentItem();
            if (clicked == null || clicked.getType() == Material.AIR) return;

            if (e.isShiftClick()) {
                int index = slot - 18;
                if (index < assignedMobs.size()) {
                    EntityType removed = assignedMobs.remove(index);
                    SpawnPointManager.getInstance().updatePoint(spawnLocation, assignedMobs);
                    player.sendMessage(ChatColor.YELLOW + "Mob eliminado del punto: " + removed.getKey().getKey());

                    // Actualizar el menú
                    draw();
                }
            }
        }
    }

    /**
     * Menú de confirmación para eliminar un punto de spawn
     */
    private class ConfirmDeleteSpawnPoint extends AbstractMenu {
        private final Location pointLocation;

        public ConfirmDeleteSpawnPoint(Player player, Location pointLocation) {
            super(player, 3, "§c¿Eliminar Punto de Spawn?");
            this.pointLocation = pointLocation;
        }

        @Override
        protected void draw() {
            // Información del punto a eliminar
            ItemStack info = new ItemStack(Material.PAPER);
            ItemMeta infoMeta = info.getItemMeta();
            infoMeta.setDisplayName("§eEliminar punto en:");
            infoMeta.setLore(Arrays.asList(
                "§7" + pointLocation.getWorld().getName() +
                " (" + pointLocation.getBlockX() + ", " +
                pointLocation.getBlockY() + ", " +
                pointLocation.getBlockZ() + ")",
                "§7Con " + assignedMobs.size() + " mobs asignados",
                "",
                "§c§lEsta acción no se puede deshacer."
            ));
            info.setItemMeta(infoMeta);
            inventory.setItem(4, info);

            // Botón confirmar
            ItemStack confirm = new ItemStack(Material.LIME_WOOL);
            ItemMeta confirmMeta = confirm.getItemMeta();
            confirmMeta.setDisplayName("§a§lCONFIRMAR");
            confirmMeta.setLore(Arrays.asList("§7Click para eliminar", "§7permanentemente"));
            confirm.setItemMeta(confirmMeta);
            inventory.setItem(11, confirm);

            // Botón cancelar
            ItemStack cancel = new ItemStack(Material.RED_WOOL);
            ItemMeta cancelMeta = cancel.getItemMeta();
            cancelMeta.setDisplayName("§c§lCANCELAR");
            cancelMeta.setLore(Arrays.asList("§7Click para volver", "§7sin eliminar"));
            cancel.setItemMeta(cancelMeta);
            inventory.setItem(15, cancel);
        }

        @Override
        public void click(InventoryClickEvent e) {
            e.setCancelled(true);
            int slot = e.getRawSlot();

            if (slot == 11) {
                // Confirmar eliminación
                if (SpawnPointManager.getInstance().removePoint(pointLocation)) {
                    player.sendMessage(ChatColor.GREEN + "Punto de spawn eliminado correctamente.");
                } else {
                    player.sendMessage(ChatColor.RED + "No se pudo eliminar el punto de spawn.");
                }
                player.closeInventory();
            } else if (slot == 15) {
                // Cancelar y volver al menú de edición
                player.closeInventory();
                new EditSpawnPointMenu(player, pointLocation).open();
            }
        }
    }
}
