package com.zenteno125.gui;

import com.zenteno125.MessagesCode;
import com.zenteno125.items.SpawnStickData;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class SelectMobMenu extends AbstractMenu {

    private final java.util.function.Consumer<List<EntityType>> callback;
    private final ItemStack stick;
    private final List<EntityType> selection = new ArrayList<>();

    public SelectMobMenu(Player player, ItemStack stick,
                         java.util.function.Consumer<List<EntityType>> callback) {
        super(player, 6, "§eSelecciona Mobs");
        this.callback = callback;
        this.stick = stick;
    }

    /* ---------- Helpers ---------- */

    private ItemStack icon(EntityType type) {
        Material mat;
        try {
            // Intentar obtener el huevo generador específico para este tipo de mob
            mat = Material.valueOf(type.name() + "_SPAWN_EGG");
        }
        catch (IllegalArgumentException e) {
            // Si es un mob de mod u otro sin huevo generador específico, usar EGG genérico
            mat = Material.EGG;
        }

        ItemStack egg = new ItemStack(mat);
        ItemMeta meta = egg.getItemMeta();

        // Guardar el nombre legible para mostrar
        String displayName = type.getKey().getKey();
        meta.setDisplayName("§f" + displayName);

        // Solo guardamos el nombre de la entidad en el lore para identificarla después
        meta.setLore(java.util.List.of(
            "§8" + type.getKey().toString(),
            "§7Click para seleccionar"
        ));

        egg.setItemMeta(meta);
        return egg;
    }

    private ItemStack confirmButton() {
        ItemStack ok = new ItemStack(Material.SLIME_BALL);
        ItemMeta m = ok.getItemMeta();
        m.setDisplayName("§a§lCONFIRMAR");
        ok.setItemMeta(m);
        return ok;
    }

    private ItemStack cancelButton() {
        ItemStack no = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta m = no.getItemMeta();
        m.setDisplayName("§cCancelar");
        no.setItemMeta(m);
        return no;
    }

    /* ---------- Draw ---------- */
    @Override
    protected void draw() {
        List<EntityType> captured = SpawnStickData.asEntityTypes(stick);
        int slot = 0;
        for (EntityType type : captured) {
            if (slot >= 45) break;            // deja la última fila libre
            inventory.setItem(slot++, icon(type));
        }

        inventory.setItem(49, confirmButton());   // centro fila inferior
        inventory.setItem(53, cancelButton());    // esquina
    }

    /* ---------- Click ---------- */
    @Override
    public void click(InventoryClickEvent e) {
        e.setCancelled(true);
        int slot = e.getRawSlot();

        if (slot == 49) {                  // Confirmar
            // Ahora solo aplicamos los mobs que el usuario selecciona explícitamente
            // Si la selección está vacía, no aplicamos ningún mob
            List<EntityType> out = new ArrayList<>(selection);

            if (selection.isEmpty()) {
                player.sendMessage("§eNo se asignó ningún mob al punto.");
            } else {
                player.sendMessage("§aEstablecidos " + out.size() + " mobs en el punto.");
            }

            callback.accept(out);
            player.closeInventory();
            return;
        }
        if (slot == 53) {                  // Cancelar
            player.closeInventory();
            return;
        }

        if (slot >= 0 && slot < 45) {      // Íconos de mobs
            ItemStack clicked = e.getCurrentItem();
            if (clicked == null || clicked.getType() == Material.AIR) return;

            if (!clicked.hasItemMeta() || !clicked.getItemMeta().hasLore()) return;

            List<String> lore = clicked.getItemMeta().getLore();
            if (lore == null || lore.isEmpty()) return;

            // Obtener la clave completa de la entidad (namespace:entity) desde el lore
            String fullKey = lore.get(0).replace("§8", "");
            EntityType type = null;

            try {
                // Convertir la cadena a NamespacedKey
                String[] parts = fullKey.split(":", 2);
                if (parts.length == 2) {
                    NamespacedKey namespacedKey = new NamespacedKey(parts[0], parts[1]);
                    type = Registry.ENTITY_TYPE.get(namespacedKey);
                }

                // Si no se pudo obtener el EntityType, intentar reparar el nombre de la entidad
                if (type == null) {
                    // Crear un EntityType basado en la parte del nombre después de ':'
                    String entityName = parts[1].toUpperCase().replace(" ", "_");
                    try {
                        type = EntityType.valueOf(entityName);
                    } catch (IllegalArgumentException ignored) {
                        // Intentar con el nombre que se muestra
                        String displayName = clicked.getItemMeta().getDisplayName().replace("§f", "").toUpperCase().replace(" ", "_");
                        try {
                            type = EntityType.valueOf(displayName);
                        } catch (IllegalArgumentException ignored2) {
                            // Seguimos sin encontrar el tipo
                        }
                    }
                }

                // Si todavía no encontramos el EntityType, creamos uno temporal para la selección
                if (type == null) {
                    // El tipo sigue siendo nulo, pero aún así lo agregamos a la selección
                    // para que el usuario pueda ver que se seleccionó
                    try {
                        for (EntityType existingType : SpawnStickData.asEntityTypes(stick)) {
                            if (existingType.getKey().toString().equals(fullKey)) {
                                type = existingType;
                                break;
                            }
                        }
                    } catch (Exception ignored) {
                        // En caso de error, continuamos sin mostrar mensajes
                    }
                }

                // Si encontramos un tipo, o si usaremos el que ya estaba en el stick
                if (type != null) {
                    ItemMeta meta = clicked.getItemMeta();

                    if (selection.contains(type)) {
                        selection.remove(type);
                        player.sendMessage("§cQuitado §f" + (type.getKey() != null ? type.getKey().getKey() : fullKey));
                        meta.removeEnchant(org.bukkit.enchantments.Enchantment.DURABILITY);
                    } else {
                        selection.add(type);
                        player.sendMessage("§aAñadido §f" + (type.getKey() != null ? type.getKey().getKey() : fullKey));
                        meta.addEnchant(org.bukkit.enchantments.Enchantment.DURABILITY, 1, true);
                    }

                    // Aplicar los metadatos actualizados al ítem
                    clicked.setItemMeta(meta);
                } else {
                    // Como último recurso, no mostramos error pero registramos en el log
                    Bukkit.getLogger().warning("No se pudo identificar completamente la entidad: " + fullKey);
                }
            } catch (Exception ex) {
                Bukkit.getLogger().warning("Error al procesar entidad de mod: " + ex.getMessage());
                // No mostramos el error al usuario para no confundirlo
            }
        }
    }
}
