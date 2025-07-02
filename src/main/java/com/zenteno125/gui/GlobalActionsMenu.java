package com.zenteno125.gui;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.Arrays;

public class GlobalActionsMenu extends AbstractMenu {

    public GlobalActionsMenu(Player player) {
        super(player, 3, "§cAcciones Globales");
    }

    @Override
    protected void draw() {
        // Botón para limpiar puntos
        ItemStack clear = new ItemStack(Material.RED_CONCRETE);
        ItemMeta clearMeta = clear.getItemMeta();
        clearMeta.setDisplayName("§cLimpiar TODOS los puntos");
        clearMeta.setLore(Arrays.asList("§7Elimina todos los puntos de spawn configurados."));
        clear.setItemMeta(clearMeta);
        inventory.setItem(13, clear);

        // Botón para configurar mini aldeanos
        ItemStack villagerEgg = new ItemStack(Material.VILLAGER_SPAWN_EGG);
        ItemMeta villagerMeta = villagerEgg.getItemMeta();
        villagerMeta.setDisplayName("§6Configurar Mini Aldeanos");
        villagerMeta.setLore(Arrays.asList(
            "§7Ajusta las propiedades de los mini aldeanos:",
            "§7- Vida (corazones)",
            "§7- Velocidad (niveles)",
            "§7- Porcentaje de spawn"
        ));
        villagerEgg.setItemMeta(villagerMeta);
        inventory.setItem(11, villagerEgg);

        // Botón para configurar vidas
        ItemStack heart = new ItemStack(Material.GOLDEN_APPLE);
        ItemMeta heartMeta = heart.getItemMeta();
        heartMeta.setDisplayName("§cConfigurar Vidas");
        heartMeta.setLore(Arrays.asList(
            "§7Establece el número de vidas por jugador"
        ));
        heart.setItemMeta(heartMeta);
        inventory.setItem(15, heart);
    }

    @Override
    public void click(InventoryClickEvent e) {
        e.setCancelled(true);
        int slot = e.getRawSlot();

        switch (slot) {
            case 11: // Configurar mini aldeanos
                new MiniVillagerConfigMenu(player).open();
                break;

            case 13: // Limpiar todos los puntos
                new ConfirmClearMenu(player).open();
                break;

            case 15: // Configurar vidas
                new LivesConfigMenu(player).open();
                break;
        }
    }
}
