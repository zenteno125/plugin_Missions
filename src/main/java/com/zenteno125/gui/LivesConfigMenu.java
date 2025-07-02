package com.zenteno125.gui;

import com.zenteno125.game.GameManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;


public class LivesConfigMenu extends AbstractMenu {

    public LivesConfigMenu(Player player) {
        super(player, 3, "§bConfigurar Vidas");
    }

    @Override
    protected void draw() {
        // Placeholder gris
        ItemStack placeholder = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta phMeta = placeholder.getItemMeta();
        phMeta.setDisplayName(" ");
        placeholder.setItemMeta(phMeta);
        for (int i = 0; i < 27; i++) inventory.setItem(i, placeholder);

        // Mostrar vidas actuales
        int currentLives = GameManager.getInstance().getDefaultLives();

        ItemStack lives = new ItemStack(Material.PAPER);
        ItemMeta livesMeta = lives.getItemMeta();
        livesMeta.setDisplayName("§eVidas: §f" + currentLives);
        lives.setItemMeta(livesMeta);
        inventory.setItem(13, lives);

        // Botón -1
        ItemStack minus1 = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta m1Meta = minus1.getItemMeta();
        m1Meta.setDisplayName("§c-1 Vida");
        minus1.setItemMeta(m1Meta);
        inventory.setItem(11, minus1);

        // Botón -5
        ItemStack minus5 = new ItemStack(Material.RED_STAINED_GLASS);
        ItemMeta m5Meta = minus5.getItemMeta();
        m5Meta.setDisplayName("§c-5 Vidas");
        minus5.setItemMeta(m5Meta);
        inventory.setItem(10, minus5);

        // Botón +1
        ItemStack plus1 = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
        ItemMeta p1Meta = plus1.getItemMeta();
        p1Meta.setDisplayName("§a+1 Vida");
        plus1.setItemMeta(p1Meta);
        inventory.setItem(15, plus1);

        // Botón +5
        ItemStack plus5 = new ItemStack(Material.LIME_STAINED_GLASS);
        ItemMeta p5Meta = plus5.getItemMeta();
        p5Meta.setDisplayName("§a+5 Vidas");
        plus5.setItemMeta(p5Meta);
        inventory.setItem(16, plus5);

        // Botón guardar
        ItemStack save = new ItemStack(Material.EMERALD);
        ItemMeta saveMeta = save.getItemMeta();
        saveMeta.setDisplayName("§aGuardar cambios");
        save.setItemMeta(saveMeta);
        inventory.setItem(22, save);
    }

    @Override
    public void click(InventoryClickEvent e) {
        e.setCancelled(true);
        GameManager gameManager = GameManager.getInstance();
        int currentLives = gameManager.getDefaultLives();

        switch (e.getRawSlot()) {
            case 10: // -5 vidas
                if (currentLives > 5) {
                    gameManager.setDefaultLives(currentLives - 5);
                    refresh();
                } else {
                    player.sendMessage("§cNo se pueden bajar más las vidas (mínimo: 1)");
                }
                break;

            case 11: // -1 vida
                if (currentLives > 1) {
                    gameManager.setDefaultLives(currentLives - 1);
                    refresh();
                } else {
                    player.sendMessage("§cNo se pueden bajar más las vidas (mínimo: 1)");
                }
                break;

            case 15: // +1 vida
                gameManager.setDefaultLives(currentLives + 1);
                refresh();
                break;

            case 16: // +5 vidas
                gameManager.setDefaultLives(currentLives + 5);
                refresh();
                break;

            case 22: // Guardar cambios
                player.closeInventory();
                player.sendMessage("§aVidas por partida configuradas: " + gameManager.getDefaultLives());
                break;
        }
    }

    /**
     * Actualiza la GUI con los valores actuales
     */
    private void refresh() {
        int currentLives = GameManager.getInstance().getDefaultLives();

        ItemStack lives = new ItemStack(Material.PAPER);
        ItemMeta livesMeta = lives.getItemMeta();
        livesMeta.setDisplayName("§eVidas: §f" + currentLives);
        lives.setItemMeta(livesMeta);
        inventory.setItem(13, lives);
    }
}
