package com.zenteno125.gui;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Menú de confirmación para borrar TODOS los puntos de spawn.
 */
public class ConfirmClearMenu extends AbstractMenu {

    public ConfirmClearMenu(Player player) {
        super(player, 1, "§7¿Seguro?");
    }

    @Override
    protected void draw() {
        /* Botón Confirmar (verde) */
        ItemStack yes = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
        ItemMeta y = yes.getItemMeta();
        y.setDisplayName("§aConfirmar");
        yes.setItemMeta(y);

        /* Botón Cancelar (rojo) */
        ItemStack no = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta n = no.getItemMeta();
        n.setDisplayName("§cCancelar");
        no.setItemMeta(n);

        inventory.setItem(3, yes);
        inventory.setItem(5, no);
    }

    @Override
    public void click(InventoryClickEvent e) {
        e.setCancelled(true);

        if (e.getRawSlot() == 3) {                      // Confirmar
            int cleared = SpawnPointManager.getInstance().resetAll();
            player.closeInventory();
            player.sendMessage("§aEliminados " + cleared + " puntos de spawn.");
        }

        if (e.getRawSlot() == 5) {                      // Cancelar
            player.closeInventory();
        }
    }
}
