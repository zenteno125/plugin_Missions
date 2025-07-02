package com.zenteno125.gui;

import com.zenteno125.MissionsPlugin;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;

import java.io.File;
import java.util.*;

/**
 * Gestiona la persistencia y consulta de puntos de spawn.
 * Guarda las IDs como enum-name (e.g. "ZOMBIE") para evitar problemas con namespaces.
 */
public class SpawnPointManager {

    private static SpawnPointManager instance;

    /** clave: Location del bloque | valor: lista de enum-name */
    private final Map<Location, List<String>> map = new HashMap<>();

    private final File file;
    private YamlConfiguration yaml;

    /* ──────────────────────────── Singleton ──────────────────────────── */
    private SpawnPointManager() {
        file = new File(MissionsPlugin.getInstance().getDataFolder(), "spawns.yml");
        reload();
    }

    public static SpawnPointManager getInstance() {
        if (instance == null) instance = new SpawnPointManager();
        return instance;
    }

    /* ──────────────────────────── API pública ──────────────────────────── */

    public void addPoint(Location loc, List<EntityType> mobs) {
        List<String> ids = mobs.stream().map(EntityType::name).toList();  // ZOMBIE, SPIDER…
        map.put(loc.clone(), ids);
        save();
    }

    public Optional<List<EntityType>> get(Location loc) {
        List<String> ids = map.get(loc);
        if (ids == null) return Optional.empty();
        return Optional.of(
                ids.stream()
                        .map(s -> { try { return EntityType.valueOf(s); } catch (Exception ignored) { return null; } })
                        .filter(Objects::nonNull)
                        .toList()
        );
    }

    /** Devuelve TODAS las locations guardadas (copia defensiva). */
    public List<Location> getAllPoints() {
        return new ArrayList<>(map.keySet());
    }

    /** Cantidad total de puntos. */
    public int size() { return map.size(); }

    /**
     * Elimina un punto de spawn
     * @param loc Ubicación del punto
     * @return true si se eliminó correctamente
     */
    public boolean removePoint(Location loc) {
        boolean removed = map.remove(loc) != null;
        if (removed) {
            save();
        }
        return removed;
    }

    /**
     * Actualiza los mobs asignados a un punto existente
     * @param loc Ubicación del punto
     * @param mobs Nueva lista de mobs
     * @return true si se actualizó correctamente
     */
    public boolean updatePoint(Location loc, List<EntityType> mobs) {
        if (!map.containsKey(loc)) {
            return false;
        }

        List<String> ids = mobs.stream().map(EntityType::name).toList();
        map.put(loc, ids);
        save();
        return true;
    }

    /** Borra todos los puntos y devuelve cuántos había. */
    public int resetAll() {
        int size = map.size();
        map.clear();
        save();
        return size;
    }

    /* ──────────────────────────── Persistencia ──────────────────────────── */

    private void reload() {
        yaml = YamlConfiguration.loadConfiguration(file);
        map.clear();

        if (yaml.contains("points")) {
            for (String worldUID : yaml.getConfigurationSection("points").getKeys(false)) {
                UUID uid = UUID.fromString(worldUID);
                for (String key : yaml.getConfigurationSection("points." + worldUID).getKeys(false)) {
                    String[] p = key.split(":");
                    Location loc = new Location(
                            MissionsPlugin.getInstance().getServer().getWorld(uid),
                            Double.parseDouble(p[0]),
                            Double.parseDouble(p[1]),
                            Double.parseDouble(p[2]));

                    List<String> ids = yaml.getStringList("points." + worldUID + "." + key);
                    map.put(loc, ids);
                }
            }
        }
    }

    private void save() {
        yaml.set("points", null);

        for (Map.Entry<Location, List<String>> entry : map.entrySet()) {
            Location l = entry.getKey();
            String path = "points." + l.getWorld().getUID() + "." +
                    l.getBlockX() + ":" + l.getBlockY() + ":" + l.getBlockZ();
            yaml.set(path, entry.getValue());             // lista de enum-name
        }
        try { yaml.save(file); } catch (Exception ignored) {}
    }
}
