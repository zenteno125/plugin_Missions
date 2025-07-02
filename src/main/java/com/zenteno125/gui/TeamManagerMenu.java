package com.zenteno125.gui;

import com.zenteno125.game.TeamManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class TeamManagerMenu extends AbstractMenu {

    public TeamManagerMenu(Player player) {
        super(player, 6, "§8§lDuelo por Equipos");
    }

    @Override
    protected void draw() {
        // Lana de colores para unirse a equipos (primera fila)
        for (int i = 0; i < TeamManager.TeamColor.values().length; i++) {
            TeamManager.TeamColor color = TeamManager.TeamColor.values()[i];

            ItemStack wool = new ItemStack(color.getMaterial());
            ItemMeta meta = wool.getItemMeta();
            meta.setDisplayName(color.getChatColor() + "Equipo " + color.getSpanishName());

            List<String> lore = new ArrayList<>();
            lore.add("§7Click para unirte al equipo " + color.getSpanishName());
            lore.add("§7Miembros: §f" + TeamManager.getInstance().getTeamMembers(color).size());
            meta.setLore(lore);

            wool.setItemMeta(meta);
            inventory.setItem(i + 1, wool);
        }

        // Limpiar primero los slots donde van las cabezas de jugadores (filas 2-5)
        // IMPORTANTE: Limpiamos antes de colocar los otros botones para no eliminarlos
        for (int colorIndex = 0; colorIndex < TeamManager.TeamColor.values().length; colorIndex++) {
            int slotBase = 9 + colorIndex * 9; // Inicio de la fila para este equipo

            // Solo limpiamos los 4 slots de cada equipo donde pueden ir cabezas
            for (int memberSlot = 0; memberSlot < 4; memberSlot++) {
                inventory.setItem(slotBase + memberSlot, null);
            }
        }

        // Botón para ver los puntos de spawn (lana negra)
        ItemStack spawnPoints = new ItemStack(Material.BLACK_WOOL);
        ItemMeta spawnMeta = spawnPoints.getItemMeta();
        spawnMeta.setDisplayName("§8Puntos de Aparición");
        List<String> spawnLore = new ArrayList<>();
        spawnLore.add("§7Configura los puntos de aparición");
        spawnLore.add("§7para cada equipo");

        // Añadir conteo de puntos para cada equipo
        for (TeamManager.TeamColor color : TeamManager.TeamColor.values()) {
            int count = TeamManager.getInstance().getSpawnPointCount(color);
            spawnLore.add(color.getChatColor() + color.getSpanishName() + ": §f" + count);
        }

        spawnMeta.setLore(spawnLore);
        spawnPoints.setItemMeta(spawnMeta);
        inventory.setItem(22, spawnPoints);

        // Botón para configurar vidas
        ItemStack lives = new ItemStack(Material.HEART_OF_THE_SEA);
        ItemMeta livesMeta = lives.getItemMeta();
        livesMeta.setDisplayName("§cConfigurar Vidas");
        List<String> livesLore = new ArrayList<>();
        livesLore.add("§7Vidas actuales: §f" + TeamManager.getInstance().getDefaultLives());
        livesLore.add("§7Click izquierdo: §a+1");
        livesLore.add("§7Click derecho: §c-1");
        livesMeta.setLore(livesLore);
        lives.setItemMeta(livesMeta);
        inventory.setItem(31, lives);

        // Botón para iniciar/detener duelo
        ItemStack control;
        ItemMeta controlMeta;

        if (TeamManager.getInstance().isDuelActive()) {
            control = new ItemStack(Material.RED_CONCRETE);
            controlMeta = control.getItemMeta();
            controlMeta.setDisplayName("§cFinalizar Duelo");
            controlMeta.setLore(List.of("§7Click para terminar el duelo"));
        } else {
            control = new ItemStack(Material.GREEN_CONCRETE);
            controlMeta = control.getItemMeta();
            controlMeta.setDisplayName("§aIniciar Duelo");
            controlMeta.setLore(List.of("§7Click para comenzar el duelo"));
        }

        control.setItemMeta(controlMeta);
        inventory.setItem(40, control);

        // Mostrar miembros de cada equipo (filas restantes)
        for (int colorIndex = 0; colorIndex < TeamManager.TeamColor.values().length; colorIndex++) {
            TeamManager.TeamColor color = TeamManager.TeamColor.values()[colorIndex];
            Set<UUID> members = TeamManager.getInstance().getTeamMembers(color);

            int slotBase = 9 + colorIndex * 9; // Empezar en la segunda fila

            int memberIndex = 0;
            for (UUID uuid : members) {
                if (memberIndex >= 4) break; // Máximo 4 miembros visibles por equipo

                Player member = Bukkit.getPlayer(uuid);
                if (member != null) {
                    ItemStack playerHead = new ItemStack(Material.PLAYER_HEAD);
                    org.bukkit.inventory.meta.SkullMeta skullMeta = (org.bukkit.inventory.meta.SkullMeta) playerHead.getItemMeta();
                    skullMeta.setOwningPlayer(member);
                    skullMeta.setDisplayName(color.getChatColor() + member.getName());
                    List<String> playerLore = new ArrayList<>();
                    playerLore.add("§7Click para remover del equipo");
                    skullMeta.setLore(playerLore);
                    playerHead.setItemMeta(skullMeta);

                    inventory.setItem(slotBase + memberIndex, playerHead);
                    memberIndex++;
                }
            }
        }
    }

    @Override
    public void click(InventoryClickEvent e) {
        e.setCancelled(true);
        Player player = (Player) e.getWhoClicked();
        int slot = e.getSlot();

        // Unirse a equipo (slots 1-4)
        if (slot >= 1 && slot <= 4) {
            TeamManager.TeamColor color = TeamManager.TeamColor.values()[slot - 1];

            // Cambiar el equipo del jugador
            TeamManager.getInstance().addPlayerToTeam(player.getUniqueId(), color);
            player.sendMessage(color.getChatColor() + "Te has unido al equipo " + color.getSpanishName());

            // Solo limpiar las áreas específicas de cabezas de jugadores
            cleanPlayerHeads();

            // Actualizar la información de equipos y cabezas de jugadores
            updateTeamWool();
            updatePlayerHeads();

            return;
        }

        // Configurar puntos de aparición
        if (slot == 22) {
            new TeamSpawnMenu(player).open();
            return;
        }

        // Configurar vidas
        if (slot == 31) {
            int currentLives = TeamManager.getInstance().getDefaultLives();
            if (e.isLeftClick()) {
                TeamManager.getInstance().setDefaultLives(currentLives + 1);
                player.sendMessage(ChatColor.GREEN + "Vidas aumentadas a " + (currentLives + 1));
            } else if (e.isRightClick() && currentLives > 1) {
                TeamManager.getInstance().setDefaultLives(currentLives - 1);
                player.sendMessage(ChatColor.RED + "Vidas reducidas a " + (currentLives - 1));
            }
            updateLivesItem();
            return;
        }

        // Iniciar/Finalizar duelo
        if (slot == 40) {
            if (TeamManager.getInstance().isDuelActive()) {
                TeamManager.getInstance().endDuel();
                player.sendMessage(ChatColor.RED + "Has finalizado el duelo por equipos");
                updateDuelControlButton();
            } else {
                player.closeInventory();
                TeamManager.getInstance().startDuel();
            }
            return;
        }

        // Remover jugador de equipo (cabezas de jugadores)
        for (int colorIndex = 0; colorIndex < TeamManager.TeamColor.values().length; colorIndex++) {
            int slotBase = 9 + colorIndex * 9;
            if (slot >= slotBase && slot < slotBase + 4) {
                ItemStack item = e.getCurrentItem();
                if (item != null && item.getType() == Material.PLAYER_HEAD) {
                    String playerName = ChatColor.stripColor(item.getItemMeta().getDisplayName());
                    Player targetPlayer = Bukkit.getPlayer(playerName);
                    if (targetPlayer != null) {
                        TeamManager.getInstance().removePlayerFromTeam(targetPlayer.getUniqueId());
                        player.sendMessage(ChatColor.YELLOW + "Has removido a " + playerName + " de su equipo");

                        // Actualizar solo las áreas necesarias
                        cleanPlayerHeads();
                        updateTeamWool();
                        updatePlayerHeads();
                    }
                }
            }
        }
    }

    /**
     * Limpia todas las cabezas de jugadores del inventario
     * Solo limpia los espacios específicos donde van las cabezas de jugadores
     */
    private void cleanPlayerHeads() {
        // Solo limpiamos las áreas donde aparecen las cabezas de jugadores
        // Para cada equipo, sabemos exactamente qué slots contienen cabezas
        for (int colorIndex = 0; colorIndex < TeamManager.TeamColor.values().length; colorIndex++) {
            int slotBase = 9 + colorIndex * 9; // Inicio de la fila para este equipo

            // Solo limpiamos los 4 slots de cada equipo donde pueden ir cabezas
            for (int memberSlot = 0; memberSlot < 4; memberSlot++) {
                inventory.setItem(slotBase + memberSlot, null);
            }
        }
    }

    /**
     * Actualiza solo las cabezas de los jugadores en el inventario
     */
    private void updatePlayerHeads() {
        for (int colorIndex = 0; colorIndex < TeamManager.TeamColor.values().length; colorIndex++) {
            TeamManager.TeamColor color = TeamManager.TeamColor.values()[colorIndex];
            Set<UUID> members = TeamManager.getInstance().getTeamMembers(color);

            int slotBase = 9 + colorIndex * 9; // Slot base para este equipo

            int memberIndex = 0;
            for (UUID uuid : members) {
                if (memberIndex >= 4) break; // Máximo 4 miembros visibles por equipo

                Player member = Bukkit.getPlayer(uuid);
                if (member != null) {
                    ItemStack playerHead = new ItemStack(Material.PLAYER_HEAD);
                    org.bukkit.inventory.meta.SkullMeta skullMeta = (org.bukkit.inventory.meta.SkullMeta) playerHead.getItemMeta();
                    skullMeta.setOwningPlayer(member);
                    skullMeta.setDisplayName(color.getChatColor() + member.getName());
                    List<String> playerLore = new ArrayList<>();
                    playerLore.add("§7Click para remover del equipo");
                    skullMeta.setLore(playerLore);
                    playerHead.setItemMeta(skullMeta);

                    inventory.setItem(slotBase + memberIndex, playerHead);
                    memberIndex++;
                }
            }
        }
    }

    /**
     * Actualiza solo los items de lana que muestran los equipos disponibles
     */
    private void updateTeamWool() {
        for (int i = 0; i < TeamManager.TeamColor.values().length; i++) {
            TeamManager.TeamColor color = TeamManager.TeamColor.values()[i];

            ItemStack wool = new ItemStack(color.getMaterial());
            ItemMeta meta = wool.getItemMeta();
            meta.setDisplayName(color.getChatColor() + "Equipo " + color.getSpanishName());

            List<String> lore = new ArrayList<>();
            lore.add("§7Click para unirte al equipo " + color.getSpanishName());
            lore.add("§7Miembros: §f" + TeamManager.getInstance().getTeamMembers(color).size());
            meta.setLore(lore);

            wool.setItemMeta(meta);
            inventory.setItem(i + 1, wool);
        }
    }

    /**
     * Actualiza solo el botón de vidas
     */
    private void updateLivesItem() {
        ItemStack lives = new ItemStack(Material.HEART_OF_THE_SEA);
        ItemMeta livesMeta = lives.getItemMeta();
        livesMeta.setDisplayName("§cConfigurar Vidas");
        List<String> livesLore = new ArrayList<>();
        livesLore.add("§7Vidas actuales: §f" + TeamManager.getInstance().getDefaultLives());
        livesLore.add("§7Click izquierdo: §a+1");
        livesLore.add("§7Click derecho: §c-1");
        livesMeta.setLore(livesLore);
        lives.setItemMeta(livesMeta);
        inventory.setItem(31, lives);
    }

    /**
     * Actualiza solo el botón de control del duelo
     */
    private void updateDuelControlButton() {
        ItemStack control;
        ItemMeta controlMeta;

        if (TeamManager.getInstance().isDuelActive()) {
            control = new ItemStack(Material.RED_CONCRETE);
            controlMeta = control.getItemMeta();
            controlMeta.setDisplayName("§cFinalizar Duelo");
            controlMeta.setLore(List.of("§7Click para terminar el duelo"));
        } else {
            control = new ItemStack(Material.GREEN_CONCRETE);
            controlMeta = control.getItemMeta();
            controlMeta.setDisplayName("§aIniciar Duelo");
            controlMeta.setLore(List.of("§7Click para comenzar el duelo"));
        }

        control.setItemMeta(controlMeta);
        inventory.setItem(40, control);
    }
}
