package com.zenteno125.items;

import org.bukkit.Location;
import org.bukkit.block.Block;
import com.zenteno125.util.JsonUtil;
import com.zenteno125.MissionsPlugin;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

/**
 * Gestiona la asignación de plantillas a cofres
 */
public class LootChestManager {
    private static final String FILE_NAME = "loot-chests.yml";
    private static LootChestManager instance;

    // Mapa: "world,x,y,z" → Lista de plantillas asignadas
    private final Map<String, List<String>> chestTemplates = new HashMap<>();

    // Mapa: "world,x,y,z" → plantilla actualmente activa
    private final Map<String, String> activeTemplates = new HashMap<>();

    // Mapa: "world,x,y,z" → valor de rareza personalizado (2-9)
    private final Map<String, Integer> chestRarityValues = new HashMap<>();

    // Último cambio aleatorio de plantillas
    private int lastTemplateRotation = 0;
    private static final int TEMPLATE_ROTATION_INTERVAL = 10; // Cambiar cada 10 rondas

    // Control de cofres recargados en esta partida/ronda
    private final Set<String> refillQueue = new HashSet<>();
    private boolean needRefill = false;

    private LootChestManager() {
        load();
    }

    public static LootChestManager getInstance() {
        if (instance == null) instance = new LootChestManager();
        return instance;
    }

    /**
     * Asigna una plantilla a un cofre específico
     */
    public void assignTemplate(Block chest, String templateName) {
        String key = locationToString(chest.getLocation());
        List<String> templates = chestTemplates.getOrDefault(key, new ArrayList<>());

        // Si no está en la lista, añadirla
        if (!templates.contains(templateName)) {
            templates.add(templateName);
        }

        chestTemplates.put(key, templates);

        // Si es la primera plantilla o no hay plantilla activa, establecerla como activa
        if (!activeTemplates.containsKey(key) || activeTemplates.get(key) == null) {
            activeTemplates.put(key, templateName);
        }

        // Asignar rareza por defecto si no existe
        if (!chestRarityValues.containsKey(key)) {
            chestRarityValues.put(key, 4); // Valor predeterminado
        }

        save();
    }

    /**
     * Elimina una plantilla de un cofre específico
     */
    public void removeTemplate(Block chest, String templateName) {
        String key = locationToString(chest.getLocation());
        if (chestTemplates.containsKey(key)) {
            List<String> templates = chestTemplates.get(key);
            templates.remove(templateName);

            // Si la plantilla eliminada era la activa, establecer otra si hay disponibles
            if (activeTemplates.getOrDefault(key, "").equals(templateName)) {
                if (!templates.isEmpty()) {
                    activeTemplates.put(key, templates.get(0));
                } else {
                    activeTemplates.remove(key);
                }
            }

            // Si no quedan plantillas, eliminar el cofre
            if (templates.isEmpty()) {
                chestTemplates.remove(key);
                activeTemplates.remove(key);
                chestRarityValues.remove(key);
            }

            save();
        }
    }

    /**
     * Elimina todas las asignaciones de un cofre
     */
    public void removeAllTemplates(Block chest) {
        String key = locationToString(chest.getLocation());
        chestTemplates.remove(key);
        activeTemplates.remove(key);
        chestRarityValues.remove(key);
        save();
    }

    /**
     * Para compatibilidad con código existente
     */
    public boolean removeTemplate(Block chest) {
        removeAllTemplates(chest);
        return true;
    }

    /**
     * Obtiene todas las plantillas asignadas a un cofre
     * @return lista de nombres de plantillas o lista vacía si no tiene
     */
    public List<String> getAssignedTemplates(Block chest) {
        String key = locationToString(chest.getLocation());
        return chestTemplates.getOrDefault(key, new ArrayList<>());
    }

    /**
     * Obtiene la plantilla actualmente activa para un cofre
     * @return nombre de la plantilla activa o null si no tiene
     */
    public String getActiveTemplate(Block chest) {
        return activeTemplates.get(locationToString(chest.getLocation()));
    }

    /**
     * Para compatibilidad con código existente
     * @return plantilla activa del cofre
     */
    public String getAssignedTemplate(Block chest) {
        return getActiveTemplate(chest);
    }

    /**
     * Establece el valor de rareza para un cofre (2-9)
     */
    public void setRarityValue(Block chest, int value) {
        if (value < 2) value = 2;
        if (value > 9) value = 9;
        chestRarityValues.put(locationToString(chest.getLocation()), value);
        save();
    }

    /**
     * Obtiene el valor de rareza para un cofre (2-9)
     */
    public int getRarityValue(Block chest) {
        String key = locationToString(chest.getLocation());
        return chestRarityValues.getOrDefault(key, 4); // Valor predeterminado
    }

    /**
     * Rota aleatoriamente las plantillas activas de todos los cofres
     */
    public void rotateActiveTemplates() {
        Random rand = new Random();
        for (String locationKey : chestTemplates.keySet()) {
            List<String> templates = chestTemplates.get(locationKey);
            if (templates.size() > 1) {
                // Seleccionar una plantilla aleatoria diferente a la actual
                String currentActive = activeTemplates.get(locationKey);
                String newActive;
                do {
                    newActive = templates.get(rand.nextInt(templates.size()));
                } while (newActive.equals(currentActive) && templates.size() > 1);

                activeTemplates.put(locationKey, newActive);
            }
        }
        save();
    }

    /**
     * Verifica si es momento de rotar las plantillas basado en la ronda actual
     */
    public void checkTemplateRotation(int currentRound) {
        if (currentRound % TEMPLATE_ROTATION_INTERVAL == 0 && currentRound != lastTemplateRotation) {
            lastTemplateRotation = currentRound;
            rotateActiveTemplates();
            Bukkit.broadcastMessage(ChatColor.GOLD + "¡Las plantillas de los cofres han rotado!");
        }
    }

    /**
     * Verifica si un cofre tiene al menos una plantilla asignada
     */
    public boolean hasTemplate(Block chest) {
        String key = locationToString(chest.getLocation());
        return chestTemplates.containsKey(key) && !chestTemplates.get(key).isEmpty();
    }

    /**
     * Convierte una ubicación a formato string para usarse como clave
     */
    private String locationToString(Location loc) {
        return loc.getWorld().getName() + "," +
               loc.getBlockX() + "," +
               loc.getBlockY() + "," +
               loc.getBlockZ();
    }

    /**
     * Marca todos los cofres para ser recargados (solo al inicio de la partida)
     */
    public void queueAllChestsForRefill() {
        refillQueue.clear();
        needRefill = true;
    }

    /**
     * Marca los cofres para ser recargados según la ronda actual y la rareza personalizada
     * @param currentRound Ronda actual del juego
     */
    public void queueChestsForRefillByRarity(int currentRound) {
        refillQueue.clear();
        needRefill = true;

        // Verificar si es momento de rotar las plantillas
        checkTemplateRotation(currentRound);

        // Si es ronda 1, ya se rellenaron todos al inicio de la partida
        if (currentRound <= 1) {
            return;
        }

        // Registrar qué cofres ya han sido procesados (que no deben recargarse)
        Set<String> processedChests = new HashSet<>();

        // Para cada cofre con plantilla asignada
        for (String chestKey : chestTemplates.keySet()) {
            int rarityValue = chestRarityValues.getOrDefault(chestKey, 4);

            // Si la ronda actual no es múltiplo del valor de rareza, no recargar
            if (currentRound % rarityValue != 0) {
                processedChests.add(chestKey);
            }
        }

        // Añadir los cofres que no deben recargarse a la cola para evitar su recarga
        refillQueue.addAll(processedChests);

        // Mensaje informativo
        StringBuilder message = new StringBuilder(ChatColor.GOLD + "A lootear de nuevo putos!");

        Bukkit.broadcastMessage(message.toString());
    }

    /**
     * Verifica si un cofre debe ser recargado y lo marca como ya recargado
     * @return true si debe recargarse, false si ya fue recargado
     */
    public boolean shouldRefillChest(Block chest) {
        if (!needRefill) return false;

        String key = locationToString(chest.getLocation());
        if (refillQueue.contains(key)) return false;

        refillQueue.add(key);
        return true;
    }

    /**
     * Desactiva la recarga de cofres (después de iniciar una ronda)
     */
    public void disableRefill() {
        needRefill = false;
    }

    /**
     * Carga las asignaciones desde el archivo
     */
    public void load() {
        chestTemplates.clear();
        activeTemplates.clear();
        chestRarityValues.clear();
        File file = new File(MissionsPlugin.getInstance().getDataFolder(), FILE_NAME);
        if (!file.exists()) return;

        try (FileReader reader = new FileReader(file)) {
            Map<String, Object> loaded = JsonUtil.gson().fromJson(reader, Map.class);
            if (loaded != null) {
                for (String key : loaded.keySet()) {
                    // Compatibilidad con formato antiguo
                    if (loaded.get(key) instanceof String) {
                        String templateName = (String) loaded.get(key);
                        List<String> templates = new ArrayList<>();
                        templates.add(templateName);
                        chestTemplates.put(key, templates);
                        activeTemplates.put(key, templateName);
                        chestRarityValues.put(key, 4); // Valor predeterminado
                    }
                    // Nuevo formato con múltiples plantillas y rareza personalizada
                    else if (loaded.get(key) instanceof Map) {
                        Map<String, Object> data = (Map<String, Object>) loaded.get(key);
                        chestTemplates.put(key, (List<String>) data.get("templates"));
                        activeTemplates.put(key, (String) data.get("active"));

                        // La rareza puede estar almacenada como Double o Integer
                        Object rarityObj = data.get("rarity");
                        int rarity = 4; // valor predeterminado
                        if (rarityObj instanceof Double) {
                            rarity = ((Double) rarityObj).intValue();
                        } else if (rarityObj instanceof Integer) {
                            rarity = (Integer) rarityObj;
                        }
                        chestRarityValues.put(key, rarity);
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Guarda las asignaciones a un archivo
     */
    public void save() {
        Map<String, Map<String, Object>> toSave = new HashMap<>();

        // Convertir datos a formato guardable
        for (String key : chestTemplates.keySet()) {
            Map<String, Object> data = new HashMap<>();
            data.put("templates", chestTemplates.get(key));
            data.put("active", activeTemplates.getOrDefault(key, chestTemplates.get(key).get(0)));
            data.put("rarity", chestRarityValues.getOrDefault(key, 4));
            toSave.put(key, data);
        }

        try {
            File dataFolder = MissionsPlugin.getInstance().getDataFolder();
            if (!dataFolder.exists()) dataFolder.mkdirs();
            File file = new File(dataFolder, FILE_NAME);

            try (FileWriter writer = new FileWriter(file)) {
                JsonUtil.gson().toJson(toSave, writer);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Verifica si un cofre debe aparecer en la ronda actual según su valor de rareza
     */
    public boolean shouldChestAppearInRound(Block chest, int currentRound) {
        int rarityValue = getRarityValue(chest);
        return currentRound % rarityValue == 0;
    }

    /**
     * Establece una plantilla específica como activa para un cofre
     * @param chest el cofre a modificar
     * @param templateName nombre de la plantilla a activar
     * @return true si se pudo establecer, false si no existe la plantilla
     */
    public boolean setActiveTemplate(Block chest, String templateName) {
        String key = locationToString(chest.getLocation());
        List<String> templates = chestTemplates.getOrDefault(key, new ArrayList<>());

        // Verificar que la plantilla exista en la lista de plantillas del cofre
        if (templates.contains(templateName)) {
            activeTemplates.put(key, templateName);
            save();
            return true;
        }
        return false;
    }
}
