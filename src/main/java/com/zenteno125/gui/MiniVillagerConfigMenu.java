package com.zenteno125.gui;

import com.zenteno125.game.MiniVillagerManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * Menú para configurar los mini aldeanos
 */
public class MiniVillagerConfigMenu extends AbstractMenu {

    private final MiniVillagerManager villagerManager;

    public MiniVillagerConfigMenu(Player player) {
        super(player, 5, ChatColor.GOLD + "Configuración de Mini Aldeanos");
        this.villagerManager = MiniVillagerManager.getInstance();
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

        // Item informativo - Villager Egg
        ItemStack villagerEgg = new ItemStack(Material.VILLAGER_SPAWN_EGG);
        ItemMeta eggMeta = villagerEgg.getItemMeta();
        eggMeta.setDisplayName(ChatColor.GOLD + "Mini Aldeanos");

        List<String> eggLore = new ArrayList<>();
        eggLore.add(ChatColor.GRAY + "Los mini aldeanos aparecen");
        eggLore.add(ChatColor.GRAY + "durante las rondas con una");
        eggLore.add(ChatColor.GRAY + "probabilidad que aumenta");
        eggLore.add(ChatColor.GRAY + "con cada ronda.");
        eggLore.add("");
        eggLore.add(ChatColor.YELLOW + "% Inicial: " + ChatColor.WHITE + villagerManager.getSpawnPercentage() + "%");
        eggLore.add(ChatColor.YELLOW + "Incremento por ronda: " + ChatColor.WHITE + "1%");
        eggMeta.setLore(eggLore);
        villagerEgg.setItemMeta(eggMeta);
        inventory.setItem(4, villagerEgg);

        // Configuración de Vida
        setupHealthItems();

        // Configuración de Velocidad
        setupSpeedItems();

        // Configuración de % de Spawn
        setupSpawnPercentItems();

        // Botón Guardar y Salir
        ItemStack emerald = new ItemStack(Material.EMERALD);
        ItemMeta emeraldMeta = emerald.getItemMeta();
        emeraldMeta.setDisplayName(ChatColor.GREEN + "Guardar y Salir");
        emerald.setItemMeta(emeraldMeta);
        inventory.setItem(40, emerald);
    }

    private void setupHealthItems() {
        // Mostrar valor actual
        ItemStack heart = new ItemStack(Material.GOLDEN_APPLE);
        ItemMeta heartMeta = heart.getItemMeta();
        heartMeta.setDisplayName(ChatColor.RED + "Vida: " + villagerManager.getHealthCorazones() + " corazones");

        List<String> healthLore = new ArrayList<>();
        healthLore.add(ChatColor.GRAY + "Establece los puntos de vida");
        healthLore.add(ChatColor.GRAY + "de los mini aldeanos.");
        heartMeta.setLore(healthLore);
        heart.setItemMeta(heartMeta);
        inventory.setItem(11, heart);

        // Opciones para aumentar
        ItemStack plus1 = createButton(Material.RED_DYE, ChatColor.RED + "+1 Corazón");
        ItemStack plus5 = createButton(Material.REDSTONE, ChatColor.RED + "+5 Corazones");
        ItemStack plus20 = createButton(Material.REDSTONE_BLOCK, ChatColor.RED + "+20 Corazones");

        // Opciones para disminuir
        ItemStack minus1 = createButton(Material.LIGHT_BLUE_DYE, ChatColor.AQUA + "-1 Corazón");
        ItemStack minus5 = createButton(Material.LAPIS_LAZULI, ChatColor.AQUA + "-5 Corazones");

        inventory.setItem(10, minus5);
        inventory.setItem(19, minus1);
        inventory.setItem(20, plus1);
        inventory.setItem(21, plus5);
        inventory.setItem(12, plus20);
    }

    private void setupSpeedItems() {
        // Mostrar valor actual
        ItemStack feather = new ItemStack(Material.FEATHER);
        ItemMeta featherMeta = feather.getItemMeta();
        featherMeta.setDisplayName(ChatColor.AQUA + "Velocidad: " + getRomanNumeral(villagerManager.getSpeedLevel()));

        List<String> speedLore = new ArrayList<>();
        speedLore.add(ChatColor.GRAY + "Establece la velocidad");
        speedLore.add(ChatColor.GRAY + "de los mini aldeanos.");
        featherMeta.setLore(speedLore);
        feather.setItemMeta(featherMeta);
        inventory.setItem(15, feather);

        // Opciones para aumentar
        ItemStack plus1 = createButton(Material.WHITE_DYE, ChatColor.WHITE + "+I Velocidad");
        ItemStack plus5 = createButton(Material.SUGAR, ChatColor.WHITE + "+V Velocidad");
        ItemStack plus10 = createButton(Material.RABBIT_FOOT, ChatColor.WHITE + "+X Velocidad");

        // Opciones para disminuir
        ItemStack minus1 = createButton(Material.BLACK_DYE, ChatColor.DARK_GRAY + "-I Velocidad");
        ItemStack minus5 = createButton(Material.COAL, ChatColor.DARK_GRAY + "-V Velocidad");

        inventory.setItem(14, minus5);
        inventory.setItem(23, minus1);
        inventory.setItem(24, plus1);
        inventory.setItem(25, plus5);
        inventory.setItem(16, plus10);
    }

    private void setupSpawnPercentItems() {
        // Mostrar valor actual
        ItemStack compass = new ItemStack(Material.COMPASS);
        ItemMeta compassMeta = compass.getItemMeta();
        compassMeta.setDisplayName(ChatColor.GOLD + "Spawn: " + villagerManager.getSpawnPercentage() + "%");

        List<String> spawnLore = new ArrayList<>();
        spawnLore.add(ChatColor.GRAY + "Establece el porcentaje inicial");
        spawnLore.add(ChatColor.GRAY + "de aparición de mini aldeanos.");
        spawnLore.add(ChatColor.GRAY + "Este % aumentará en 1 por cada ronda.");
        compassMeta.setLore(spawnLore);
        compass.setItemMeta(compassMeta);
        inventory.setItem(31, compass);

        // Opciones para aumentar
        ItemStack plus1 = createButton(Material.LIME_DYE, ChatColor.GREEN + "+1%");
        ItemStack plus2 = createButton(Material.GREEN_DYE, ChatColor.GREEN + "+2%");
        ItemStack plus5 = createButton(Material.SLIME_BALL, ChatColor.GREEN + "+5%");

        // Opciones para disminuir
        ItemStack minus1 = createButton(Material.PINK_DYE, ChatColor.RED + "-1%");
        ItemStack minus2 = createButton(Material.RED_DYE, ChatColor.RED + "-2%");
        ItemStack minus5 = createButton(Material.REDSTONE, ChatColor.RED + "-5%");

        inventory.setItem(29, minus5);
        inventory.setItem(30, minus2);
        inventory.setItem(28, minus1);
        inventory.setItem(32, plus1);
        inventory.setItem(33, plus2);
        inventory.setItem(34, plus5);
    }

    private ItemStack createButton(Material material, String displayName) {
        ItemStack button = new ItemStack(material);
        ItemMeta meta = button.getItemMeta();
        meta.setDisplayName(displayName);
        button.setItemMeta(meta);
        return button;
    }

    private String getRomanNumeral(int level) {
        switch (level) {
            case 0: return "0";
            case 1: return "I";
            case 2: return "II";
            case 3: return "III";
            case 4: return "IV";
            case 5: return "V";
            case 6: return "VI";
            case 7: return "VII";
            case 8: return "VIII";
            case 9: return "IX";
            case 10: return "X";
            default:
                if (level > 10) return "X+";
                return "0";
        }
    }

    @Override
    public void click(InventoryClickEvent e) {
        e.setCancelled(true);
        int slot = e.getRawSlot();

        switch (slot) {
            // === VIDA ===
            case 10: // -5 corazones
                if (villagerManager.getHealthCorazones() > 5) {
                    villagerManager.setHealthCorazones(villagerManager.getHealthCorazones() - 5);
                    open(); // Cambiado de redraw() a open()
                }
                break;
            case 19: // -1 corazón
                if (villagerManager.getHealthCorazones() > 1) {
                    villagerManager.setHealthCorazones(villagerManager.getHealthCorazones() - 1);
                    open(); // Cambiado de redraw() a open()
                }
                break;
            case 20: // +1 corazón
                villagerManager.increaseHealth(1);
                open(); // Cambiado de redraw() a open()
                break;
            case 21: // +5 corazones
                villagerManager.increaseHealth(5);
                open(); // Cambiado de redraw() a open()
                break;
            case 12: // +20 corazones
                villagerManager.increaseHealth(20);
                open(); // Cambiado de redraw() a open()
                break;

            // === VELOCIDAD ===
            case 14: // -5 velocidad
                if (villagerManager.getSpeedLevel() > 5) {
                    villagerManager.setSpeedLevel(villagerManager.getSpeedLevel() - 5);
                    open(); // Cambiado de redraw() a open()
                }
                break;
            case 23: // -1 velocidad
                if (villagerManager.getSpeedLevel() > 1) {
                    villagerManager.setSpeedLevel(villagerManager.getSpeedLevel() - 1);
                    open(); // Cambiado de redraw() a open()
                }
                break;
            case 24: // +1 velocidad
                villagerManager.increaseSpeed(1);
                open(); // Cambiado de redraw() a open()
                break;
            case 25: // +5 velocidad
                villagerManager.increaseSpeed(5);
                open(); // Cambiado de redraw() a open()
                break;
            case 16: // +10 velocidad
                villagerManager.increaseSpeed(10);
                open(); // Cambiado de redraw() a open()
                break;

            // === SPAWN PERCENTAGE ===
            case 28: // -1%
                if (villagerManager.getSpawnPercentage() > 1) {
                    villagerManager.setSpawnPercentage(villagerManager.getSpawnPercentage() - 1);
                    open(); // Cambiado de redraw() a open()
                }
                break;
            case 30: // -2%
                if (villagerManager.getSpawnPercentage() > 2) {
                    villagerManager.setSpawnPercentage(villagerManager.getSpawnPercentage() - 2);
                    open(); // Cambiado de redraw() a open()
                }
                break;
            case 29: // -5%
                if (villagerManager.getSpawnPercentage() > 5) {
                    villagerManager.setSpawnPercentage(villagerManager.getSpawnPercentage() - 5);
                    open(); // Cambiado de redraw() a open()
                }
                break;
            case 32: // +1%
                villagerManager.increaseSpawnPercentage(1);
                open(); // Cambiado de redraw() a open()
                break;
            case 33: // +2%
                villagerManager.increaseSpawnPercentage(2);
                open(); // Cambiado de redraw() a open()
                break;
            case 34: // +5%
                villagerManager.increaseSpawnPercentage(5);
                open(); // Cambiado de redraw() a open()
                break;

            // === GUARDAR Y SALIR ===
            case 40: // Guardar y salir
                player.sendMessage(ChatColor.GREEN + "¡Configuración de mini aldeanos guardada!");
                new MainMenu(player).open();
                break;
        }
    }
}
