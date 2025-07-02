package com.zenteno125.gui;

import com.zenteno125.game.TeamManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class TeamSpawnMenu extends AbstractMenu {

    public TeamSpawnMenu(Player player) {
        super(player, 3, "§8§lPuntos de Aparición");
    }

    @Override
    protected void draw() {
        // Bloques de lana para cada equipo
        for (int i = 0; i < TeamManager.TeamColor.values().length; i++) {
            TeamManager.TeamColor color = TeamManager.TeamColor.values()[i];

            ItemStack wool = new ItemStack(color.getMaterial());
            ItemMeta meta = wool.getItemMeta();
            meta.setDisplayName(color.getChatColor() + "Puntos de " + color.getSpanishName());

            List<String> lore = new ArrayList<>();
            lore.add("§7Click para obtener bloques de spawn");
            lore.add("§7Puntos actuales: §f" + TeamManager.getInstance().getSpawnPointCount(color));
            lore.add("§7Al colocar el bloque en el mundo,");
            lore.add("§7se creará un punto de aparición");
            lore.add("§7para el equipo " + color.getSpanishName());
            meta.setLore(lore);

            wool.setItemMeta(meta);
            inventory.setItem(10 + i, wool);
        }

        // Botón para limpiar todos los puntos de spawn
        ItemStack clearAll = new ItemStack(Material.BARRIER);
        ItemMeta clearMeta = clearAll.getItemMeta();
        clearMeta.setDisplayName("§c§lBorrar Todos los Puntos");
        List<String> clearLore = new ArrayList<>();
        clearLore.add("§7Elimina todos los puntos de");
        clearLore.add("§7aparición de todos los equipos");
        clearMeta.setLore(clearLore);
        clearAll.setItemMeta(clearMeta);
        inventory.setItem(22, clearAll);

        // Botones para limpiar puntos específicos de cada equipo
        for (int i = 0; i < TeamManager.TeamColor.values().length; i++) {
            TeamManager.TeamColor color = TeamManager.TeamColor.values()[i];

            ItemStack clear = new ItemStack(Material.REDSTONE);
            ItemMeta meta = clear.getItemMeta();
            meta.setDisplayName("§c§lBorrar puntos " + color.getChatColor() + color.getSpanishName());

            List<String> lore = new ArrayList<>();
            lore.add("§7Elimina todos los puntos de aparición");
            lore.add("§7del equipo " + color.getSpanishName());
            meta.setLore(lore);

            clear.setItemMeta(meta);
            inventory.setItem(15 + i, clear);
        }
    }

    @Override
    public void click(InventoryClickEvent e) {
        e.setCancelled(true);
        Player player = (Player) e.getWhoClicked();
        int slot = e.getSlot();

        // Obtener bloques de lana para configurar puntos
        if (slot >= 10 && slot <= 13) {
            TeamManager.TeamColor color = TeamManager.TeamColor.values()[slot - 10];
            ItemStack spawnBlock = new ItemStack(color.getMaterial());
            ItemMeta meta = spawnBlock.getItemMeta();
            meta.setDisplayName(color.getChatColor() + "Punto de aparición " + color.getSpanishName());
            List<String> lore = new ArrayList<>();
            lore.add("§7Coloca este bloque para crear");
            lore.add("§7un punto de aparición para");
            lore.add("§7el equipo " + color.getSpanishName());
            meta.setLore(lore);
            spawnBlock.setItemMeta(meta);

            player.getInventory().addItem(spawnBlock);
            player.sendMessage(color.getChatColor() + "Has obtenido un bloque de punto de aparición para el equipo " + color.getSpanishName());
            return;
        }

        // Borrar todos los puntos de spawn
        if (slot == 22) {
            TeamManager.getInstance().clearAllSpawnPoints();
            player.sendMessage(ChatColor.RED + "Has borrado todos los puntos de aparición de todos los equipos");
            draw();
            return;
        }

        // Borrar puntos de spawn de un equipo específico
        if (slot >= 15 && slot <= 18) {
            TeamManager.TeamColor color = TeamManager.TeamColor.values()[slot - 15];
            TeamManager.getInstance().clearSpawnPoints(color);
            player.sendMessage(ChatColor.RED + "Has borrado todos los puntos de aparición del equipo " + color.getSpanishName());
            draw();
            return;
        }
    }
}
