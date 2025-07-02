package com.zenteno125.gui;

import com.zenteno125.game.GameManager;
import com.zenteno125.items.ItemRegistry;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class MainMenu extends AbstractMenu {

    public MainMenu(Player player) {
        super(player, 3, "§8§lMissions");
    }

    @Override
    protected void draw() {
        // ── Botón Plantillas de Botín (cofre) ──
        ItemStack chest = new ItemStack(Material.CHEST);
        ItemMeta cm = chest.getItemMeta();
        cm.setDisplayName("§6Plantillas de Botín");
        cm.setLore(java.util.List.of("§7Crea y edita plantillas de loot"));
        chest.setItemMeta(cm);
        inventory.setItem(11, chest);

        // ── Palo de Botín ──
        ItemStack lootStick = ItemRegistry.createLootStick();
        inventory.setItem(12, lootStick);

        // ── Palo de Spawn ──
        ItemStack spawnStick = ItemRegistry.createSpawnStick();
        inventory.setItem(14, spawnStick);

        // ── Configurar Mini Aldeanos (huevo de aldeano) ──
        ItemStack villagerEgg = new ItemStack(Material.VILLAGER_SPAWN_EGG);
        ItemMeta villagerMeta = villagerEgg.getItemMeta();
        villagerMeta.setDisplayName("§dConseguir Vidas");
        villagerMeta.setLore(java.util.List.of(
            "§7Configura los mini aldeanos",
            "§7para conseguir vidas extra:",
            "§7- Vida (corazones)",
            "§7- Velocidad (niveles)",
            "§7- % de aparición por ronda"
        ));
        villagerEgg.setItemMeta(villagerMeta);
        inventory.setItem(15, villagerEgg);

        // ── Configurar Dificultad de Enemigos (cabeza de zombie) ──
        ItemStack zombieHead = new ItemStack(Material.ZOMBIE_HEAD);
        ItemMeta zombieMeta = zombieHead.getItemMeta();
        zombieMeta.setDisplayName("§cDificultad de Enemigos");
        zombieMeta.setLore(java.util.List.of(
            "§7Configura la cantidad máxima de",
            "§7zombies adicionales por punto de spawn",
            "§7usando vidrios de colores para",
            "§7aumentar o disminuir la dificultad"
        ));
        zombieHead.setItemMeta(zombieMeta);
        inventory.setItem(16, zombieHead);

        // ── Configurar Vidas (papel) ──
        ItemStack paper = new ItemStack(Material.PAPER);
        ItemMeta pm = paper.getItemMeta();
        pm.setDisplayName("§bConfigurar Vidas");
        pm.setLore(java.util.List.of("§7Define las vidas por partida"));
        paper.setItemMeta(pm);
        inventory.setItem(20, paper);

        // ── Botón de Umbral de Últimos Enemigos (cristal) ──
        ItemStack crystal = new ItemStack(Material.AMETHYST_SHARD);
        ItemMeta crystalMeta = crystal.getItemMeta();
        int currentThreshold = GameManager.getInstance().getLastEnemiesThreshold();
        crystalMeta.setDisplayName("§dUmbral de Últimos Enemigos: §e" + currentThreshold + "%");
        crystalMeta.setLore(java.util.List.of(
            "§7Define el porcentaje de enemigos restantes",
            "§7para activar efectos visuales y mensajes",
            "§7- Click izquierdo: aumentar (+1%)",
            "§7- Click derecho: disminuir (-1%)"
        ));
        crystal.setItemMeta(crystalMeta);
        inventory.setItem(21, crystal);

        // ── Duelos por Equipos (tinte rosa) ──
        ItemStack teamDuel = new ItemStack(Material.PINK_DYE);
        ItemMeta teamMeta = teamDuel.getItemMeta();
        teamMeta.setDisplayName("§d§lDuelos por Equipos");
        teamMeta.setLore(java.util.List.of(
            "§7Gestiona equipos y duelos:",
            "§7- Crear equipos",
            "§7- Configurar puntos de spawn",
            "§7- Iniciar/finalizar duelos"
        ));
        teamDuel.setItemMeta(teamMeta);
        inventory.setItem(24, teamDuel);

        // ── Estado de Partida ──
        GameManager.GameState state = GameManager.getInstance().getState();
        String stateText = "§aEsperando";

        if (state == GameManager.GameState.STARTING) {
            stateText = "§eIniciando...";
        } else if (state == GameManager.GameState.ACTIVE) {
            stateText = "§cEn curso (Ronda " + GameManager.getInstance().getCurrentRound() + ")";
        } else if (state == GameManager.GameState.ENDING) {
            stateText = "§6Terminando...";
        }

        ItemStack status = new ItemStack(Material.CLOCK);
        ItemMeta statusMeta = status.getItemMeta();
        statusMeta.setDisplayName("§6Estado: " + stateText);
        // Mostrar mobs restantes solo si la partida está en curso
        if (state == GameManager.GameState.ACTIVE) {
            int mobsRestantes = GameManager.getInstance().getTotalMobsToSpawn() - GameManager.getInstance().getMobsKilled();
            if (mobsRestantes < 0) mobsRestantes = 0;
            statusMeta.setLore(java.util.List.of("§7Mobs restantes: §e" + mobsRestantes));
        } else {
            statusMeta.setLore(null);
        }
        status.setItemMeta(statusMeta);
        inventory.setItem(4, status);

        // ── Botón para Saltar Ronda (junto al reloj, solo visible cuando la partida está activa) ──
        if (state == GameManager.GameState.ACTIVE) {
            ItemStack skipRound = new ItemStack(Material.ENDER_EYE);
            ItemMeta skipMeta = skipRound.getItemMeta();
            skipMeta.setDisplayName("§dSaltar Ronda");
            skipMeta.setLore(java.util.List.of(
                "§7¿No encuentras el último enemigo?",
                "§7Haz clic para saltar la ronda actual."
            ));
            skipRound.setItemMeta(skipMeta);
            inventory.setItem(13, skipRound);
        }
    }

    @Override
    public void click(InventoryClickEvent e) {
        e.setCancelled(true);
        int slot = e.getSlot();

        if (slot == 11) { // Plantillas de Botín (cofre)
            new LootTemplatesMenu(player, 0).open(); // Abrimos en la página 0 (primera página)
        } else if (slot == 12) { // Palo de Botín
            player.getInventory().addItem(ItemRegistry.createLootStick());
            player.sendMessage("§aSe te ha entregado un palo de botín.");
        } else if (slot == 14) { // Palo de Spawn
            player.getInventory().addItem(ItemRegistry.createSpawnStick());
            player.sendMessage("§aSe te ha entregado un palo de spawn.");
        } else if (slot == 15) { // Mini Aldeanos
            new MiniVillagerConfigMenu(player).open();
        } else if (slot == 16) { // Dificultad Enemigos
            new EnemyDifficultyConfigMenu(player).open();
        } else if (slot == 20) { // Configurar Vidas
            new LivesConfigMenu(player).open();
        } else if (slot == 21) { // Umbral Últimos Enemigos
            if (e.isLeftClick()) {
                int threshold = GameManager.getInstance().getLastEnemiesThreshold();
                GameManager.getInstance().setLastEnemiesThreshold(Math.min(threshold + 1, 100));
            } else if (e.isRightClick()) {
                int threshold = GameManager.getInstance().getLastEnemiesThreshold();
                GameManager.getInstance().setLastEnemiesThreshold(Math.max(threshold - 1, 1));
            }
            draw(); // Redibujar para actualizar la información
        } else if (slot == 24) { // Duelos por Equipos (tinte rosa)
            new TeamManagerMenu(player).open();
        } else if (slot == 4 && e.getCurrentItem() != null && e.getCurrentItem().getType() == Material.CLOCK) {
            // Botón Estado de Partida (reloj)
            GameManager.GameState state = GameManager.getInstance().getState();
            if (state == GameManager.GameState.WAITING) {
                player.closeInventory();
                GameManager.getInstance().startGame();
            } else if (state == GameManager.GameState.ACTIVE) {
                player.closeInventory();
                GameManager.getInstance().endGame();
            }
        } else if (e.getSlot() == 13 && e.getCurrentItem() != null && e.getCurrentItem().getType() == Material.ENDER_EYE) {
            // Boton de Saltar Ronda (solo visible cuando la partida está activa)
            player.closeInventory();
            GameManager.getInstance().skipCurrentRound();
        }
    }
}
