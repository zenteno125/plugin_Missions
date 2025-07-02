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
 * GUI para asignar una plantilla de botín a un cofre.
 */
public class AsignarPlantillaMenu extends AbstractMenu {
    private final Block chestBlock;
    private final List<String> templates;
    private final String currentTemplate;

    public AsignarPlantillaMenu(Player player, Block chestBlock) {
        super(player, 3, "§aAsignar plantilla a cofre");
        this.chestBlock = chestBlock;
        this.templates = new ArrayList<>(LootTemplateManager.getInstance().list());
        this.currentTemplate = LootChestManager.getInstance().getAssignedTemplate(chestBlock);
    }

    @Override
    protected void draw() {
        // Mostrar la plantilla actual si existe
        if (currentTemplate != null) {
            ItemStack current = new ItemStack(Material.CHEST);
            ItemMeta meta = current.getItemMeta();
            meta.setDisplayName("§6Plantilla actual");
            meta.setLore(Arrays.asList(
                "§fNombre: §e" + currentTemplate,
                "§7Click para quitar asignación"
            ));
            current.setItemMeta(meta);
            inventory.setItem(18, current);
        } else {
            ItemStack noCurrent = new ItemStack(Material.BARRIER);
            ItemMeta meta = noCurrent.getItemMeta();
            meta.setDisplayName("§cSin plantilla asignada");
            noCurrent.setItemMeta(meta);
            inventory.setItem(18, noCurrent);
        }

        // Mostrar todas las plantillas disponibles
        for (int i = 0; i < Math.min(templates.size(), 9); i++) {
            String name = templates.get(i);
            ItemStack paper = new ItemStack(Material.PAPER);
            ItemMeta meta = paper.getItemMeta();
            meta.setDisplayName("§f" + name);

            // Resaltar si es la plantilla actualmente asignada
            if (name.equals(currentTemplate)) {
                meta.setLore(Arrays.asList(
                    "§aActualmente asignada",
                    "§7Click para reasignar"
                ));
            } else {
                int itemCount = LootTemplateManager.getInstance().getLoot(name).size();
                meta.setLore(Arrays.asList(
                    "§7Contiene: §f" + itemCount + " §7items",
                    "§7Click para asignar"
                ));
            }

            paper.setItemMeta(meta);
            inventory.setItem(i, paper);
        }

        // Botón cancelar
        ItemStack cancel = new ItemStack(Material.BARRIER);
        ItemMeta cancelMeta = cancel.getItemMeta();
        cancelMeta.setDisplayName("§cCancelar");
        cancel.setItemMeta(cancelMeta);
        inventory.setItem(26, cancel);
    }

    @Override
    public void click(InventoryClickEvent e) {
        e.setCancelled(true);
        int slot = e.getRawSlot();

        // Click en plantilla actual (quitar asignación)
        if (slot == 18 && currentTemplate != null) {
            LootChestManager.getInstance().removeTemplate(chestBlock);
            player.sendMessage("§eSe ha quitado la plantilla asignada al cofre.");
            player.closeInventory();
            return;
        }

        // Click en botón cancelar
        if (slot == 26) {
            player.closeInventory();
            return;
        }

        // Click en una plantilla disponible
        if (slot >= 0 && slot < Math.min(templates.size(), 9)) {
            String selectedTemplate = templates.get(slot);
            LootChestManager.getInstance().assignTemplate(chestBlock, selectedTemplate);
            player.sendMessage("§aPlantilla §f" + selectedTemplate + " §aasignada al cofre.");
            player.closeInventory();
        }
    }
}
