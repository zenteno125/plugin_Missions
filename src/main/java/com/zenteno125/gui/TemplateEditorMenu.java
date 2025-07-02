package com.zenteno125.gui;

import com.zenteno125.items.LootTemplateManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.*;

/**
 * Editor de plantilla de botín (inventario doble, 54 slots).
 */
public class TemplateEditorMenu extends AbstractMenu {
    private final String templateName;

    public TemplateEditorMenu(Player player, String templateName) {
        super(player, 6, "§bEditar plantilla: " + templateName);
        this.templateName = templateName;
    }

    @Override
    protected void draw() {
        // Cargar loot actual
        List<ItemStack> loot = LootTemplateManager.getInstance().getLoot(templateName);
        for (int i = 0; i < Math.min(loot.size(), 54); i++) {
            inventory.setItem(i, loot.get(i));
        }
        // Botón borrar todo
        ItemStack clear = new ItemStack(Material.BARRIER);
        ItemMeta clearMeta = clear.getItemMeta();
        clearMeta.setDisplayName("§cBorrar todo");
        clear.setItemMeta(clearMeta);
        inventory.setItem(45, clear);
        // Botón eliminar plantilla
        ItemStack tnt = new ItemStack(Material.TNT);
        ItemMeta tntMeta = tnt.getItemMeta();
        tntMeta.setDisplayName("§cEliminar plantilla");
        tnt.setItemMeta(tntMeta);
        inventory.setItem(46, tnt);
        // Botón guardar
        ItemStack slime = new ItemStack(Material.SLIME_BALL);
        ItemMeta slimeMeta = slime.getItemMeta();
        slimeMeta.setDisplayName("§aGuardar cambios");
        slime.setItemMeta(slimeMeta);
        inventory.setItem(53, slime);
    }

    @Override
    public void click(InventoryClickEvent e) {
        // Solo cancelar clicks en botones especiales
        if (e.getRawSlot() >= 45 && e.getRawSlot() <= 53) {
            e.setCancelled(true);

            // Lógica de botones
            switch (e.getRawSlot()) {
                case 45: // Botón borrar todo
                    for (int i = 0; i < 45; i++) {
                        inventory.setItem(i, null);
                    }
                    player.sendMessage("§cSe han borrado todo");
                    break;

                case 46: // Botón eliminar plantilla
                    player.closeInventory();
                    LootTemplateManager.getInstance().delete(templateName);
                    player.sendMessage("§cSe ha eliminado la plantilla: " + templateName);
                    new LootTemplatesMenu(player, 0).open();
                    break;

                case 53: // Botón guardar cambios
                    ItemStack[] contents = Arrays.copyOfRange(inventory.getContents(), 0, 45);
                    LootTemplateManager.getInstance().setLoot(templateName, contents);
                    player.sendMessage("§aSe han guardado los cambios");
                    // Regresar al menú de plantillas después de guardar
                    new LootTemplatesMenu(player, 0).open();
                    break;
            }
        }
    }

    @Override
    public void close(InventoryCloseEvent e) {
        // Guardar automáticamente al cerrar
        // Solo tomar los slots 0-44, ignorando los botones del menú
        ItemStack[] contents = new ItemStack[45];
        for (int i = 0; i < 45; i++) {
            contents[i] = inventory.getItem(i);
        }
        LootTemplateManager.getInstance().setLoot(templateName, contents);
    }
}

