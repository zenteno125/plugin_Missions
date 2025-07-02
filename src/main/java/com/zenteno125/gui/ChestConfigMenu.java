package com.zenteno125.gui;

import com.zenteno125.items.LootChestManager;
import com.zenteno125.items.LootTemplateManager;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.*;

/**
 * GUI para gestionar las plantillas de un cofre y su rareza.
 */
public class ChestConfigMenu extends AbstractMenu {
    private final Block chestBlock;
    private final List<String> availableTemplates;
    private final List<String> assignedTemplates;
    private final String activeTemplate;
    private final int rarityValue;

    public ChestConfigMenu(Player player, Block chestBlock) {
        super(player, 5, "§aConfigurar Cofre");
        this.chestBlock = chestBlock;

        LootChestManager chestManager = LootChestManager.getInstance();
        LootTemplateManager templateManager = LootTemplateManager.getInstance();

        this.availableTemplates = new ArrayList<>(templateManager.list());
        this.assignedTemplates = chestManager.getAssignedTemplates(chestBlock);
        this.activeTemplate = chestManager.getActiveTemplate(chestBlock);
        this.rarityValue = chestManager.getRarityValue(chestBlock);
    }

    @Override
    protected void draw() {
        // Panel informativo del cofre
        drawChestInfoSection();

        // Sistema de rareza con lanas de colores
        drawRaritySection();

        // Lista de plantillas disponibles para agregar
        drawAvailableTemplatesSection();

        // Lista de plantillas asignadas
        drawAssignedTemplatesSection();

        // Botones de acción
        drawActionButtons();
    }

    private void drawChestInfoSection() {
        // Mostrar información del cofre
        ItemStack chestInfo = new ItemStack(Material.CHEST);
        ItemMeta meta = chestInfo.getItemMeta();
        meta.setDisplayName("§6Cofre en " + formatLocation(chestBlock.getLocation()));

        List<String> lore = new ArrayList<>();
        lore.add("§7Plantillas asignadas: §f" + assignedTemplates.size());
        lore.add("§7Plantilla activa: " + (activeTemplate != null ? "§e" + activeTemplate : "§cNinguna"));
        lore.add("§7Rareza: §f" + rarityValue + " §7(Aparece cada §f" + rarityValue + " §7rondas)");
        meta.setLore(lore);

        chestInfo.setItemMeta(meta);
        inventory.setItem(4, chestInfo);
    }

    private void drawRaritySection() {
        // Título de la sección
        ItemStack rarityTitle = new ItemStack(Material.GOLD_INGOT);
        ItemMeta titleMeta = rarityTitle.getItemMeta();
        titleMeta.setDisplayName("§eCONFIGURAR RAREZA");
        titleMeta.setLore(Collections.singletonList("§7Selecciona el valor de rareza entre 2 y 9"));
        rarityTitle.setItemMeta(titleMeta);
        inventory.setItem(9, rarityTitle);

        // Paleta de colores para la rareza
        Material[] woolColors = {
            Material.RED_WOOL,       // Rareza 2 (más frecuente)
            Material.ORANGE_WOOL,    // Rareza 3
            Material.YELLOW_WOOL,    // Rareza 4
            Material.LIME_WOOL,      // Rareza 5
            Material.GREEN_WOOL,     // Rareza 6
            Material.LIGHT_BLUE_WOOL,// Rareza 7
            Material.BLUE_WOOL,      // Rareza 8
            Material.PURPLE_WOOL     // Rareza 9 (menos frecuente)
        };

        // Mostrar las opciones de rareza (valores de 2 a 9)
        for (int i = 0; i < 8; i++) {
            int value = i + 2; // Valores del 2 al 9

            ItemStack wool = new ItemStack(woolColors[i]);
            ItemMeta meta = wool.getItemMeta();
            meta.setDisplayName("§fRareza: §b" + value);

            List<String> lore = new ArrayList<>();
            lore.add("§7Aparece cada §f" + value + " §7rondas");

            // Marcar la rareza actual
            if (value == rarityValue) {
                lore.add("§a§l✓ SELECCIONADA");
                meta.addEnchant(org.bukkit.enchantments.Enchantment.DURABILITY, 1, true);
                meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
            }

            meta.setLore(lore);
            wool.setItemMeta(meta);

            inventory.setItem(10 + i, wool);
        }
    }

    private void drawAvailableTemplatesSection() {
        // Título de la sección
        ItemStack templatesTitle = new ItemStack(Material.BOOK);
        ItemMeta titleMeta = templatesTitle.getItemMeta();
        titleMeta.setDisplayName("§bPLANTILLAS DISPONIBLES");
        titleMeta.setLore(Collections.singletonList("§7Click para añadir al cofre"));
        templatesTitle.setItemMeta(titleMeta);
        inventory.setItem(18, templatesTitle);

        // Mostrar plantillas disponibles que no están asignadas
        int slot = 19;
        for (String templateName : availableTemplates) {
            if (!assignedTemplates.contains(templateName) && slot < 27) {
                ItemStack paper = new ItemStack(Material.PAPER);
                ItemMeta meta = paper.getItemMeta();
                meta.setDisplayName("§f" + templateName);
                meta.setLore(Collections.singletonList("§aClick para añadir al cofre"));
                paper.setItemMeta(meta);
                inventory.setItem(slot++, paper);
            }
        }
    }

    private void drawAssignedTemplatesSection() {
        // Título de la sección
        ItemStack assignedTitle = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta titleMeta = assignedTitle.getItemMeta();
        titleMeta.setDisplayName("§dPLANTILLAS ASIGNADAS");
        titleMeta.setLore(Arrays.asList(
            "§7Click para establecer como activa",
            "§cShift+Click para remover"
        ));
        assignedTitle.setItemMeta(titleMeta);
        inventory.setItem(27, assignedTitle);

        // Mostrar plantillas asignadas
        int slot = 28;
        for (String templateName : assignedTemplates) {
            if (slot < 36) {
                ItemStack paper = new ItemStack(Material.PAPER);
                ItemMeta meta = paper.getItemMeta();
                meta.setDisplayName("§f" + templateName);

                List<String> lore = new ArrayList<>();

                // Marcar la plantilla activa
                if (templateName.equals(activeTemplate)) {
                    lore.add("§a§l✓ ACTIVA ACTUALMENTE");
                    meta.addEnchant(org.bukkit.enchantments.Enchantment.DURABILITY, 1, true);
                    meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
                } else {
                    lore.add("§eClick para activar");
                }

                lore.add("§cShift+Click para eliminar");
                meta.setLore(lore);
                paper.setItemMeta(meta);

                inventory.setItem(slot++, paper);
            }
        }
    }

    private void drawActionButtons() {
        // Botón para guardar y salir
        ItemStack saveButton = new ItemStack(Material.EMERALD_BLOCK);
        ItemMeta saveMeta = saveButton.getItemMeta();
        saveMeta.setDisplayName("§a§lGUARDAR Y SALIR");
        saveButton.setItemMeta(saveMeta);
        inventory.setItem(44, saveButton);
    }

    @Override
    public void click(InventoryClickEvent e) {
        e.setCancelled(true);
        int slot = e.getRawSlot();
        LootChestManager chestManager = LootChestManager.getInstance();

        // Click en botón de guardar
        if (slot == 44) {
            player.closeInventory();
            player.sendMessage("§aConfiguración de cofre guardada");
            return;
        }

        // Click en opciones de rareza
        if (slot >= 10 && slot <= 17) {
            int newRarity = slot - 8; // Valores del 2 al 9
            chestManager.setRarityValue(chestBlock, newRarity);
            player.sendMessage("§aRareza del cofre establecida en: §e" + newRarity);
            // Reabrir el menú actualizado en lugar de usar redraw()
            new ChestConfigMenu(player, chestBlock).open();
            return;
        }

        // Click en plantilla disponible para añadir
        if (slot >= 19 && slot < 27) {
            ItemStack clicked = e.getCurrentItem();
            if (clicked != null && clicked.getType() == Material.PAPER) {
                String templateName = clicked.getItemMeta().getDisplayName().substring(2); // Quitar el §f
                chestManager.assignTemplate(chestBlock, templateName);
                player.sendMessage("§aPlantilla §e" + templateName + "§a añadida al cofre");
                new ChestConfigMenu(player, chestBlock).open(); // Reabrir menú actualizado
                return;
            }
        }

        // Click en plantilla asignada
        if (slot >= 28 && slot < 36) {
            ItemStack clicked = e.getCurrentItem();
            if (clicked != null && clicked.getType() == Material.PAPER) {
                String templateName = clicked.getItemMeta().getDisplayName().substring(2); // Quitar el §f

                // Shift+Click = Eliminar plantilla
                if (e.isShiftClick()) {
                    chestManager.removeTemplate(chestBlock, templateName);
                    player.sendMessage("§cPlantilla §e" + templateName + "§c eliminada del cofre");
                }
                // Click normal = Activar plantilla
                else {
                    List<String> templates = chestManager.getAssignedTemplates(chestBlock);
                    if (templates.contains(templateName)) {
                        // Usar el método público de LootChestManager para establecer una plantilla como activa
                        // en lugar de acceder directamente a la propiedad activeTemplates
                        chestManager.setActiveTemplate(chestBlock, templateName);
                        player.sendMessage("§aPlantilla §e" + templateName + "§a establecida como activa");
                    }
                }

                new ChestConfigMenu(player, chestBlock).open(); // Reabrir menú actualizado
                return;
            }
        }
    }

    private String formatLocation(org.bukkit.Location location) {
        return location.getWorld().getName() + " (" +
               location.getBlockX() + ", " +
               location.getBlockY() + ", " +
               location.getBlockZ() + ")";
    }
}
