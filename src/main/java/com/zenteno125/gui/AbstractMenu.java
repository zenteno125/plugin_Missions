package com.zenteno125.gui;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public abstract class AbstractMenu implements InventoryHolder {

    protected final Player player;
    protected final Inventory inventory;

    protected AbstractMenu(Player player, int rows, String title) {
        this.player = player;
        // «this» como holder para que el listener detecte la GUI
        this.inventory = Bukkit.createInventory(this, rows * 9, title);
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public void open() {
        draw();
        player.openInventory(inventory);
    }

    protected abstract void draw();
    public abstract void click(InventoryClickEvent e);
    public void close(InventoryCloseEvent e) {}
}
