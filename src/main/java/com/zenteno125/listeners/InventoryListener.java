package com.zenteno125.listeners;

import com.zenteno125.gui.AbstractMenu;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class InventoryListener implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (e.getInventory().getHolder() instanceof AbstractMenu menu) {
            menu.click(e);
            // slots de la GUI (parte superior)
            if (e.getRawSlot() < e.getInventory().getSize()) e.setCancelled(true);
        }
    }


    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        if (e.getInventory().getHolder() instanceof AbstractMenu menu) {
            menu.close(e);
        }
    }
}
