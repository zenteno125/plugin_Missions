package com.zenteno125.items;

import com.google.common.reflect.TypeToken;          // Guava OK
import com.zenteno125.util.JsonUtil;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Maneja la lista JSON de mobs capturados dentro del palo de spawn.
 * El formato almacenado es la enum-name: "ZOMBIE", "SPIDER", …
 */
public final class SpawnStickData {

    private static final Type LIST_STRING = new TypeToken<List<String>>(){}.getType();

    /* Acceso rápido al PersistentDataContainer del stick */
    private static PersistentDataContainer pdc(ItemStack stick) {
        if (stick == null || !stick.hasItemMeta()) {
            return null;
        }
        ItemMeta meta = stick.getItemMeta();
        return meta.getPersistentDataContainer();
    }

    /* ─── Lectura / escritura cruda ────────────────────── */
    public static List<String> getCaptured(ItemStack stick) {
        if (stick == null) {
            return new ArrayList<>();
        }
        PersistentDataContainer container = pdc(stick);
        if (container == null) {
            return new ArrayList<>();
        }
        String raw = container.get(ItemRegistry.getCapturedMobsKey(), PersistentDataType.STRING);
        if (raw == null || raw.isEmpty()) return new ArrayList<>();
        return JsonUtil.gson().fromJson(raw, LIST_STRING);
    }

    private static void save(ItemStack stick, List<String> list) {
        ItemMeta meta = stick.getItemMeta();
        meta.getPersistentDataContainer().set(
                ItemRegistry.getCapturedMobsKey(),
                PersistentDataType.STRING,
                JsonUtil.gson().toJson(list));
        stick.setItemMeta(meta);
    }

    /* ─── API pública ─────────────────────────────────── */
    public static void addEntity(ItemStack stick, EntityType type) {
        List<String> list = new ArrayList<>(getCaptured(stick));
        String id = type.name();                // «ZOMBIE»
        if (!list.contains(id)) list.add(id);
        save(stick, list);
    }

    public static void mergeEggList(ItemStack stick, List<EntityType> eggs) {
        List<String> list = new ArrayList<>(getCaptured(stick));
        for (EntityType t : eggs) {
            String id = t.name();
            if (!list.contains(id)) list.add(id);
        }
        save(stick, list);
    }

    public static List<EntityType> asEntityTypes(ItemStack stick) {
        return getCaptured(stick).stream()
                .map(s -> {
                    try { return EntityType.valueOf(s.toUpperCase()); }
                    catch (IllegalArgumentException ignored) { return null; }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
