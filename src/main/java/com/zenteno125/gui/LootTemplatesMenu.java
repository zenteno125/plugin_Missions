package com.zenteno125.gui;

import com.zenteno125.items.LootRarity;
import com.zenteno125.items.LootTemplateManager;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.*;

/**
 * GUI paginable para mostrar y gestionar plantillas de botín.
 */
public class LootTemplatesMenu extends AbstractMenu {
    private int page = 0;
    private static final int PAGE_SIZE = 8;
    private final List<String> templates;
    private boolean cloneMode = false; // Modo clonación activado

    public LootTemplatesMenu(Player player, int page) {
        super(player, 6, "§2Plantillas de Botín");
        this.page = page;
        this.templates = new ArrayList<>(LootTemplateManager.getInstance().list());
    }

    @Override
    protected void draw() {
        // Placeholder gris
        ItemStack placeholder = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta phMeta = placeholder.getItemMeta();
        phMeta.setDisplayName(" ");
        placeholder.setItemMeta(phMeta);
        for (int i = 0; i < 54; i++) inventory.setItem(i, placeholder);

        // Plantillas
        int start = page * PAGE_SIZE;
        for (int i = 0; i < PAGE_SIZE && start + i < templates.size(); i++) {
            String name = templates.get(start + i);
            LootRarity rarity = LootTemplateManager.getInstance().getRarity(name);

            Material material = Material.PAPER;
            // Cambiar material según rareza
            switch (rarity) {
                case RARE:
                    material = Material.BLUE_DYE;
                    break;
                case EPIC:
                    material = Material.PURPLE_DYE;
                    break;
                default:
                    material = Material.PAPER;
            }

            ItemStack item = new ItemStack(material);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName("§f" + name);

            List<String> lore = new ArrayList<>();
            lore.add(rarity.getDisplayName() + " - " + rarity.getDescription());

            if (cloneMode) {
                lore.add("§eClick para clonar esta plantilla");
            } else {
                lore.add("§7Click: editar contenido");
                lore.add("§aShift+Click: asignar a cofre");
                lore.add("§bClick derecho: cambiar rareza");
            }

            meta.setLore(lore);
            item.setItemMeta(meta);
            inventory.setItem(i, item);
        }

        // Botón crear
        ItemStack slime = new ItemStack(Material.SLIME_BALL);
        ItemMeta slimeMeta = slime.getItemMeta();
        slimeMeta.setDisplayName("§aCrear plantilla");
        slime.setItemMeta(slimeMeta);
        inventory.setItem(45, slime);

        // Botón clonar (se resalta si está activo)
        ItemStack emerald = new ItemStack(Material.EMERALD);
        ItemMeta emMeta = emerald.getItemMeta();
        emMeta.setDisplayName("§aClonar plantilla");

        if (cloneMode) {
            emMeta.addEnchant(Enchantment.DURABILITY, 1, true);
            emMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            emMeta.setLore(Arrays.asList("§eSelecciona una plantilla para clonar"));
        } else {
            emMeta.setLore(Arrays.asList("§7Click para activar modo clonación"));
        }

        emerald.setItemMeta(emMeta);
        inventory.setItem(46, emerald);

        // Botón eliminar
        ItemStack barrier = new ItemStack(Material.BARRIER);
        ItemMeta bMeta = barrier.getItemMeta();
        bMeta.setDisplayName("§cEliminar plantilla");
        barrier.setItemMeta(bMeta);
        inventory.setItem(47, barrier);

        // Navegación
        if (page > 0) {
            ItemStack prev = new ItemStack(Material.ARROW);
            ItemMeta prevMeta = prev.getItemMeta();
            prevMeta.setDisplayName("§ePágina anterior");
            prev.setItemMeta(prevMeta);
            inventory.setItem(52, prev);
        }
        if ((page + 1) * PAGE_SIZE < templates.size()) {
            ItemStack next = new ItemStack(Material.ARROW);
            ItemMeta nextMeta = next.getItemMeta();
            nextMeta.setDisplayName("§ePágina siguiente");
            next.setItemMeta(nextMeta);
            inventory.setItem(53, next);
        }
    }

    @Override
    public void click(InventoryClickEvent e) {
        e.setCancelled(true);
        int slot = e.getRawSlot();
        int start = page * PAGE_SIZE;

        // Seleccionar plantilla
        if (slot >= 0 && slot < PAGE_SIZE && start + slot < templates.size()) {
            String name = templates.get(start + slot);

            if (cloneMode) {
                // Clonar esta plantilla
                String copyName = generarNombreCopiaNumerada(name);
                if (LootTemplateManager.getInstance().cloneTemplate(name, copyName)) {
                    player.sendMessage("§aPlantilla clonada como: " + copyName);
                    cloneMode = false; // Desactivar modo clonación
                    // Refrescar menú
                    new LootTemplatesMenu(player, page).open();
                } else {
                    player.sendMessage("§cNo se pudo clonar la plantilla");
                }
                return;
            }

            if (e.isShiftClick()) {
                // Si es shift+click, abrir AsignarPlantillaMenu (implementado posteriormente)
                player.sendMessage("§ePor implementar: Asignar plantilla " + name + " a cofre");
            } else if (e.isRightClick()) {
                // Click derecho: cambiar rareza
                LootRarity currentRarity = LootTemplateManager.getInstance().getRarity(name);
                LootRarity nextRarity = currentRarity.next();
                LootTemplateManager.getInstance().setRarity(name, nextRarity);
                player.sendMessage("§aRareza de la plantilla " + name + " cambiada a: " + nextRarity.getDisplayName());
                // Refrescar menú
                new LootTemplatesMenu(player, page).open();
            } else {
                // Click normal: editar
                new TemplateEditorMenu(player, name).open();
            }
            return;
        }

        // Botones de acción
        switch (slot) {
            case 45: // Crear plantilla
                player.closeInventory();
                // Crear con nombre simple y secuencial
                String name = "Plantilla_" + obtenerSiguienteNumero();
                LootTemplateManager.getInstance().create(name);
                new TemplateEditorMenu(player, name).open();
                break;

            case 46: // Botón modo clonación
                cloneMode = !cloneMode;
                player.sendMessage(cloneMode ?
                                   "§eModo clonación activado. Selecciona una plantilla para clonar." :
                                   "§eModo clonación desactivado.");
                // Refrescar menú para actualizar botón
                new LootTemplatesMenu(player, page).open();
                break;

            case 47: // Eliminar plantilla
                if (slot >= 0 && slot < PAGE_SIZE && start + slot < templates.size()) {
                    String templateToDelete = templates.get(start + slot);
                    if (LootTemplateManager.getInstance().delete(templateToDelete)) {
                        player.sendMessage("§cPlantilla eliminada: " + templateToDelete);
                        // Refrescar menú
                        new LootTemplatesMenu(player, page).open();
                    } else {
                        player.sendMessage("§cNo se pudo eliminar la plantilla");
                    }
                } else {
                    player.sendMessage("§cPrimero selecciona una plantilla");
                }
                break;

            case 52: // Página anterior
                if (page > 0) {
                    new LootTemplatesMenu(player, page - 1).open();
                }
                break;

            case 53: // Página siguiente
                if ((page + 1) * PAGE_SIZE < templates.size()) {
                    new LootTemplatesMenu(player, page + 1).open();
                }
                break;
        }
    }

    /**
     * Obtiene el siguiente número disponible para una plantilla
     * Ejemplo: 1, 2, 3, etc.
     */
    private int obtenerSiguienteNumero() {
        int maxNum = 0;

        for (String template : templates) {
            // Si la plantilla tiene formato "Plantilla_X" donde X es un número
            if (template.matches("Plantilla_\\d+")) {
                try {
                    int num = Integer.parseInt(template.substring(template.lastIndexOf("_") + 1));
                    if (num > maxNum) maxNum = num;
                } catch (NumberFormatException e) {
                    // Ignorar errores de parseo
                }
            }
        }

        return maxNum + 1;
    }

    /**
     * Genera un nombre para una copia con número incremental simple
     * Ejemplo: "Plantilla_1" → "Plantilla_2", etc.
     */
    private String generarNombreCopiaNumerada(String nombreOriginal) {
        return "Plantilla_" + obtenerSiguienteNumero();
    }
}
