package com.zenteno125.gui;

import com.zenteno125.game.GameManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * Menú para configurar la dificultad de los enemigos (cantidad máxima de zombies adicionales)
 */
public class EnemyDifficultyConfigMenu extends AbstractMenu {

    private final GameManager gameManager;

    public EnemyDifficultyConfigMenu(Player player) {
        super(player, 4, ChatColor.RED + "Configuración de Dificultad");
        this.gameManager = GameManager.getInstance();
    }

    @Override
    protected void draw() {
        // Fondo
        ItemStack grayPane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta grayMeta = grayPane.getItemMeta();
        grayMeta.setDisplayName(" ");
        grayPane.setItemMeta(grayMeta);

        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, grayPane);
        }

        // Item informativo - Cabeza de zombie
        ItemStack zombieHead = new ItemStack(Material.ZOMBIE_HEAD);
        ItemMeta zombieMeta = zombieHead.getItemMeta();
        zombieMeta.setDisplayName(ChatColor.RED + "Dificultad de Enemigos");

        List<String> zombieLore = new ArrayList<>();
        zombieLore.add(ChatColor.GRAY + "Establece la cantidad máxima de");
        zombieLore.add(ChatColor.GRAY + "zombies adicionales que pueden");
        zombieLore.add(ChatColor.GRAY + "aparecer por punto de spawn.");
        zombieLore.add("");
        zombieLore.add(ChatColor.YELLOW + "Máximo actual: " +
                      ChatColor.WHITE + gameManager.getMaxAdditionalMobs() +
                      ChatColor.YELLOW + " zombies adicionales");
        zombieMeta.setLore(zombieLore);
        zombieHead.setItemMeta(zombieMeta);
        inventory.setItem(4, zombieHead);

        // Opciones para aumentar
        setupGlassOptions();

        // Botón Guardar y Salir
        ItemStack emerald = new ItemStack(Material.EMERALD);
        ItemMeta emeraldMeta = emerald.getItemMeta();
        emeraldMeta.setDisplayName(ChatColor.GREEN + "Guardar y Salir");
        emerald.setItemMeta(emeraldMeta);
        inventory.setItem(31, emerald);
    }

    private void setupGlassOptions() {
        // Mostrar valor actual
        ItemStack current = new ItemStack(Material.GLASS);
        ItemMeta currentMeta = current.getItemMeta();
        currentMeta.setDisplayName(ChatColor.YELLOW + "Cantidad: " + gameManager.getMaxAdditionalMobs());
        current.setItemMeta(currentMeta);
        inventory.setItem(13, current);

        // Opciones para aumentar (vidrios de colores)
        ItemStack plus1 = createColoredGlass(Material.LIME_STAINED_GLASS, ChatColor.GREEN + "+1 Zombie");
        ItemStack plus2 = createColoredGlass(Material.GREEN_STAINED_GLASS, ChatColor.DARK_GREEN + "+2 Zombies");
        ItemStack plus5 = createColoredGlass(Material.BLUE_STAINED_GLASS, ChatColor.BLUE + "+5 Zombies");

        // Opciones para disminuir
        ItemStack minus1 = createColoredGlass(Material.RED_STAINED_GLASS, ChatColor.RED + "-1 Zombie");
        ItemStack minus2 = createColoredGlass(Material.ORANGE_STAINED_GLASS, ChatColor.GOLD + "-2 Zombies");
        ItemStack minus5 = createColoredGlass(Material.PURPLE_STAINED_GLASS, ChatColor.DARK_PURPLE + "-5 Zombies");

        inventory.setItem(10, minus5);
        inventory.setItem(11, minus2);
        inventory.setItem(12, minus1);
        inventory.setItem(14, plus1);
        inventory.setItem(15, plus2);
        inventory.setItem(16, plus5);
    }

    private ItemStack createColoredGlass(Material material, String displayName) {
        ItemStack glass = new ItemStack(material);
        ItemMeta meta = glass.getItemMeta();
        meta.setDisplayName(displayName);
        glass.setItemMeta(meta);
        return glass;
    }

    @Override
    public void click(InventoryClickEvent e) {
        e.setCancelled(true);
        int slot = e.getRawSlot();

        switch (slot) {
            // === DIFICULTAD ENEMIGOS ===
            case 10: // -5 zombies
                if (gameManager.getMaxAdditionalMobs() > 5) {
                    gameManager.setMaxAdditionalMobs(gameManager.getMaxAdditionalMobs() - 5);
                    open();
                }
                break;
            case 11: // -2 zombies
                if (gameManager.getMaxAdditionalMobs() > 2) {
                    gameManager.setMaxAdditionalMobs(gameManager.getMaxAdditionalMobs() - 2);
                    open();
                }
                break;
            case 12: // -1 zombie
                if (gameManager.getMaxAdditionalMobs() > 1) {
                    gameManager.setMaxAdditionalMobs(gameManager.getMaxAdditionalMobs() - 1);
                    open();
                }
                break;
            case 14: // +1 zombie
                gameManager.increaseMaxAdditionalMobs(1);
                open();
                break;
            case 15: // +2 zombies
                gameManager.increaseMaxAdditionalMobs(2);
                open();
                break;
            case 16: // +5 zombies
                gameManager.increaseMaxAdditionalMobs(5);
                open();
                break;

            // === GUARDAR Y SALIR ===
            case 31: // Guardar y salir
                player.sendMessage(ChatColor.GREEN + "¡Configuración de dificultad guardada!");
                new MainMenu(player).open();
                break;
        }
    }
}
