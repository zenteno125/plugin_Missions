package com.zenteno125.items;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.List;

public final class ItemRegistry {

    /* ─── NamespacedKeys ─── */
    private static NamespacedKey LOOT_STICK;
    private static NamespacedKey SPAWN_STICK;
    private static NamespacedKey CAPTURED_MOBS;
    private static NamespacedKey PLAYER_SPAWN_STICK;

    /* ─── Init (call in onEnable) ─── */
    public static void init(Plugin plugin) {
        LOOT_STICK         = new NamespacedKey(plugin, "loot-stick");
        SPAWN_STICK        = new NamespacedKey(plugin, "spawn-stick");
        CAPTURED_MOBS      = new NamespacedKey(plugin, "captured-mobs");
        PLAYER_SPAWN_STICK = new NamespacedKey(plugin, "player-spawn-stick");
    }

    /* ─── Palo de Botín ─── */
    public static ItemStack createLootStick() {
        ItemStack item = new ItemStack(Material.STICK);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;                // fallback improbable

        meta.setDisplayName("§aPalo de Botín");
        meta.setLore(List.of("§7Click cofres para asignar"));
        meta.getPersistentDataContainer().set(LOOT_STICK, PersistentDataType.INTEGER, 1);

        item.setItemMeta(meta);
        return item;
    }

    /* ─── Palo de Spawn (ya existente) ─── */
    public static ItemStack createSpawnStick() {
        ItemStack item = new ItemStack(Material.BLAZE_ROD);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§ePalo de Spawn");
        meta.getPersistentDataContainer().set(SPAWN_STICK, PersistentDataType.INTEGER, 1);
        meta.getPersistentDataContainer().set(CAPTURED_MOBS, PersistentDataType.STRING, "[]");
        item.setItemMeta(meta);
        return item;
    }

    /* ─── Palo de Respawn (ya existente) ─── */
    public static ItemStack createPlayerSpawnStick() {
        ItemStack item = new ItemStack(Material.STICK);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§bPalo de Respawn");
        meta.getPersistentDataContainer().set(PLAYER_SPAWN_STICK, PersistentDataType.INTEGER, 1);
        item.setItemMeta(meta);
        return item;
    }

    /* ─── Helpers de detección ─── */
    public static boolean isLootStick(ItemStack it)        { return hasKey(it, LOOT_STICK); }
    public static boolean isSpawnStick(ItemStack it)       { return hasKey(it, SPAWN_STICK); }
    public static boolean isPlayerSpawnStick(ItemStack it) { return hasKey(it, PLAYER_SPAWN_STICK); }

    private static boolean hasKey(ItemStack it, NamespacedKey key) {
        return it != null && it.hasItemMeta() &&
                it.getItemMeta().getPersistentDataContainer().has(key, PersistentDataType.INTEGER);
    }

    /* ─── Getters usados por otras clases ─── */
    public static NamespacedKey getCapturedMobsKey() { return CAPTURED_MOBS; }
    public static NamespacedKey getSpawnStickKey()   { return SPAWN_STICK; }

    /* ─── Lore dinámico para Palo de Spawn ─── */
    public static void updateSpawnStickLore(ItemStack stick, int count) {
        if (!isSpawnStick(stick)) return;
        ItemMeta meta = stick.getItemMeta();
        meta.setLore(List.of("§7Puntos guardados: §e" + count + "/100"));
        stick.setItemMeta(meta);
    }

    /* ─── Refrescar lore en todos los palos (opcional) ─── */
    // public static void refreshAllSpawnStickLore(int currentCount) { … }
}
