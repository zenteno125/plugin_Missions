package com.zenteno125.items;

import org.bukkit.inventory.ItemStack;
import java.util.*;
import java.io.*;
import com.zenteno125.util.JsonUtil;
import com.zenteno125.game.GameManager;

/**
 * Gestor de plantillas de botín. Carga/guarda templates.yml
 */
public class LootTemplateManager {
    private static final String FILE_NAME = "templates.yml";
    private static LootTemplateManager instance;
    private final Map<String, List<ItemStack>> templates = new HashMap<>();
    private final Map<String, LootRarity> templateRarities = new HashMap<>();

    private LootTemplateManager() {
        load();
    }

    public static LootTemplateManager getInstance() {
        if (instance == null) instance = new LootTemplateManager();
        return instance;
    }

    public Set<String> list() {
        return templates.keySet();
    }

    public boolean create(String name) {
        if (templates.containsKey(name)) return false;
        templates.put(name, new ArrayList<>());
        templateRarities.put(name, LootRarity.NORMAL); // Por defecto, rareza normal
        save();
        return true;
    }

    public boolean cloneTemplate(String from, String to) {
        if (!templates.containsKey(from) || templates.containsKey(to)) return false;
        templates.put(to, new ArrayList<>(templates.get(from)));
        templateRarities.put(to, templateRarities.getOrDefault(from, LootRarity.NORMAL));
        save();
        return true;
    }

    public boolean delete(String name) {
        if (!templates.containsKey(name)) return false;
        templates.remove(name);
        templateRarities.remove(name);
        save();
        return true;
    }

    public void setLoot(String name, ItemStack[] loot) {
        templates.put(name, Arrays.asList(loot));
        if (!templateRarities.containsKey(name)) {
            templateRarities.put(name, LootRarity.NORMAL);
        }
        save();
    }

    public List<ItemStack> getLoot(String name) {
        return templates.getOrDefault(name, new ArrayList<>());
    }

    /**
     * Establece la rareza de una plantilla
     * @param name Nombre de la plantilla
     * @param rarity Rareza a establecer
     * @return true si se modificó correctamente
     */
    public boolean setRarity(String name, LootRarity rarity) {
        if (!templates.containsKey(name)) return false;
        templateRarities.put(name, rarity);
        save();
        return true;
    }

    /**
     * Obtiene la rareza de una plantilla
     * @param name Nombre de la plantilla
     * @return Rareza de la plantilla
     */
    public LootRarity getRarity(String name) {
        return templateRarities.getOrDefault(name, LootRarity.NORMAL);
    }

    /**
     * Filtra las plantillas según la ronda actual
     * @param currentRound Número de ronda actual
     * @return Lista de nombres de plantillas disponibles para esta ronda
     */
    public List<String> getAvailableTemplatesForRound(int currentRound) {
        List<String> available = new ArrayList<>();

        for (String templateName : templates.keySet()) {
            LootRarity rarity = templateRarities.getOrDefault(templateName, LootRarity.NORMAL);
            if (rarity.shouldAppearInRound(currentRound)) {
                available.add(templateName);
            }
        }

        return available;
    }

    public void load() {
        templates.clear();
        templateRarities.clear();
        File file = new File("plugins/Misions/templates.yml");
        if (!file.exists()) return;
        try (java.io.FileReader reader = new java.io.FileReader(file)) {
            Map<String, Object> raw = JsonUtil.gson().fromJson(reader, Map.class);
            if (raw != null) {
                for (var entry : raw.entrySet()) {
                    String name = entry.getKey();
                    if (entry.getValue() instanceof String) {
                        // Formato antiguo, solo contiene items
                        String base64 = (String) entry.getValue();
                        List<ItemStack> items = JsonUtil.itemStackListFromBase64(base64);
                        templates.put(name, items);
                        templateRarities.put(name, LootRarity.NORMAL); // Por defecto
                    } else if (entry.getValue() instanceof Map) {
                        // Nuevo formato con rareza e items
                        Map<String, Object> templateData = (Map<String, Object>) entry.getValue();
                        String base64 = (String) templateData.get("items");
                        String rarityName = (String) templateData.getOrDefault("rarity", "NORMAL");

                        List<ItemStack> items = JsonUtil.itemStackListFromBase64(base64);
                        templates.put(name, items);

                        try {
                            LootRarity rarity = LootRarity.valueOf(rarityName);
                            templateRarities.put(name, rarity);
                        } catch (Exception e) {
                            templateRarities.put(name, LootRarity.NORMAL);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void save() {
        File file = new File("plugins/Misions/templates.yml");
        file.getParentFile().mkdirs();
        Map<String, Object> raw = new HashMap<>();

        for (var entry : templates.entrySet()) {
            String name = entry.getKey();
            String base64 = JsonUtil.itemStackListToBase64(entry.getValue());
            LootRarity rarity = templateRarities.getOrDefault(name, LootRarity.NORMAL);

            Map<String, Object> templateData = new HashMap<>();
            templateData.put("items", base64);
            templateData.put("rarity", rarity.name());

            raw.put(name, templateData);
        }

        try (java.io.FileWriter writer = new java.io.FileWriter(file)) {
            JsonUtil.gson().toJson(raw, writer);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
