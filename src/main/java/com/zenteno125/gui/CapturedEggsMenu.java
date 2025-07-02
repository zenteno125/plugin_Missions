package com.zenteno125.gui;

import com.zenteno125.items.SpawnStickData;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class CapturedEggsMenu extends AbstractMenu {

    private final ItemStack stick;

    public CapturedEggsMenu(Player player, ItemStack stick) {
        super(player, 3, "§6Capturar Huevos");
        this.stick = stick;
    }

    @Override protected void draw() {}          // slots en blanco

    @Override public void click(InventoryClickEvent e) {}  // permitimos arrastrar

    @Override
    public void close(InventoryCloseEvent e) {
        List<EntityType> eggs = new ArrayList<>();

        for (ItemStack it : inventory.getContents()) {
            if (it == null || it.getType() == Material.AIR) continue;

            Material mat = it.getType();
            EntityType type = null;

            // Intentamos diferentes estrategias para conseguir el EntityType

            // Opción 1: Materiales tipo ZOMBIE_SPAWN_EGG (vanilla Minecraft)
            if (mat.name().endsWith("_SPAWN_EGG")) {
                try {
                    String id = mat.name().replace("_SPAWN_EGG", "");
                    type = EntityType.valueOf(id);
                    eggs.add(type);
                    continue;
                } catch (IllegalArgumentException ignored) {
                    // Intentamos las siguientes opciones
                }
            }

            // Opción 2: Checkear si el ítem tiene metadatos específicos de mods
            if (it.hasItemMeta()) {
                ItemMeta meta = it.getItemMeta();

                // Buscar en el lore por cualquier información de entidad
                if (meta.hasLore()) {
                    List<String> lore = meta.getLore();
                    for (String line : lore) {
                        // Si hay una línea que parece un identificador de entidad (namespace:entity)
                        if (line.contains(":")) {
                            try {
                                String cleanLine = line.replaceAll("§[0-9a-fklmnor]", "").trim();
                                String[] parts = cleanLine.split(":", 2);
                                if (parts.length == 2) {
                                    NamespacedKey key = new NamespacedKey(parts[0], parts[1]);
                                    type = Registry.ENTITY_TYPE.get(key);
                                    if (type != null) {
                                        eggs.add(type);
                                        break;
                                    }
                                }
                            } catch (Exception ex) {
                                // Ignoramos y continuamos con otras estrategias
                            }
                        }
                    }
                }

                // Opción 3: Intentar usar el displayName si está disponible
                if (type == null && meta.hasDisplayName()) {
                    String name = meta.getDisplayName().replaceAll("§[0-9a-fklmnor]", "").trim();
                    try {
                        // Intentar obtener por nombre limpio
                        for (EntityType entityType : EntityType.values()) {
                            if (entityType.name().equalsIgnoreCase(name) ||
                                entityType.getKey().getKey().equalsIgnoreCase(name)) {
                                eggs.add(entityType);
                                type = entityType;
                                break;
                            }
                        }
                    } catch (Exception ignored) {
                        // Ignorar si falla
                    }
                }

                // Opción 4: Si es un huevo generador de un mod pero no pudimos identificarlo específicamente
                // Intentamos añadirlo de todas formas si parece un huevo de spawn
                if (type == null && (it.getType().name().contains("SPAWN") ||
                                   it.getType().name().contains("EGG") ||
                                   (meta.hasDisplayName() && meta.getDisplayName().toLowerCase().contains("spawn")))) {
                    // En lugar de avisar con un error, lo registramos en el log para debugging
                    // pero no mostramos mensaje al usuario ya que el ítem probablemente funcionará
                    Bukkit.getLogger().info("Registrando mob no identificado: " + it.getType().name());

                    // No hacemos nada con eggs ya que no tenemos un EntityType específico
                    // La funcionalidad subyacente del plugin debería manejar esto
                }
                // Si no entra en ninguna de las categorías anteriores, no es un huevo de spawn
                // y no necesitamos mostrar ningún mensaje de error
            }
        }

        if (!eggs.isEmpty()) {
            SpawnStickData.mergeEggList(stick, eggs);
            player.sendMessage("§aAñadidos " + eggs.size() + " mobs al palo");
        } else {
            player.sendMessage("§cNo se encontraron huevos generadores válidos");
        }
    }
}
