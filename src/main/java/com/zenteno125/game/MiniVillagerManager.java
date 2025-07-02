package com.zenteno125.game;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.attribute.Attribute;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.util.UUID;

/**
 * Gestiona la creación y modificación de mini aldeanos
 */
public class MiniVillagerManager {
    private static MiniVillagerManager instance;

    // Configuración predeterminada
    private int healthCorazones = 10; // 10 corazones (20 HP)
    private int speedLevel = 1; // Nivel de velocidad
    private int spawnPercentage = 5; // Porcentaje inicial de spawn

    // Control de spawn
    private boolean hasSpawnedInCurrentRound = false;
    private UUID currentVillagerUUID = null;

    private MiniVillagerManager() {
        // Constructor privado para singleton
    }

    public static MiniVillagerManager getInstance() {
        if (instance == null) instance = new MiniVillagerManager();
        return instance;
    }

    /**
     * Genera un mini aldeano en la ubicación especificada
     * @param location Ubicación donde aparecerá
     * @return El aldeano generado
     */
    public Villager spawnMiniVillager(Location location) {
        Villager villager = (Villager) location.getWorld().spawnEntity(location, EntityType.VILLAGER);

        // Configurar como bebé
        villager.setBaby();

        // Establecer atributos
        applyAttributes(villager);

        // Agregar efectos visuales
        applyEffects(villager);

        // Nombre
        villager.setCustomName(ChatColor.GOLD + "Penetrame si puedes");
        villager.setCustomNameVisible(true);

        // Marcar como generado para esta ronda
        hasSpawnedInCurrentRound = true;
        currentVillagerUUID = villager.getUniqueId();

        return villager;
    }

    /**
     * Aplica los atributos configurados al aldeano
     * @param villager El aldeano a modificar
     */
    public void applyAttributes(LivingEntity villager) {
        // Establecer vida máxima (2 HP = 1 corazón)
        double healthPoints = healthCorazones * 2.0;
        villager.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(healthPoints);
        villager.setHealth(healthPoints);

        // Establecer velocidad según el nivel configurado
        double speedValue = 0.2 + (speedLevel * 0.02); // Velocidad base + incremento por nivel
        villager.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(speedValue);
    }

    /**
     * Aplica efectos visuales al aldeano
     * @param villager El aldeano a modificar
     */
    private void applyEffects(LivingEntity villager) {
        // Si tiene velocidad alta, agregar efecto de partículas
        if (speedLevel >= 3) {
            villager.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, Integer.MAX_VALUE, 0, false, false));
        }
    }

    /**
     * Determina si un mini aldeano debe aparecer en esta ronda
     * @param currentRound Ronda actual
     * @return true si debe aparecer
     */
    public boolean shouldSpawnInRound(int currentRound) {
        // Si ya spawneó un aldeano en esta ronda, no generar otro
        if (hasSpawnedInCurrentRound) {
            return false;
        }

        // El porcentaje aumenta en 1% por cada ronda
        int currentPercentage = Math.min(100, spawnPercentage + (currentRound - 1));

        // Comprobar probabilidad
        return Math.random() * 100 < currentPercentage;
    }

    /**
     * Reinicia el estado de spawn para una nueva ronda
     */
    public void resetForNewRound() {
        hasSpawnedInCurrentRound = false;
        currentVillagerUUID = null;
    }

    /**
     * Verifica si el UUID corresponde a un mini aldeano actual
     * @param uuid UUID a comprobar
     * @return true si es un mini aldeano actual
     */
    public boolean isCurrentVillager(UUID uuid) {
        return uuid != null && uuid.equals(currentVillagerUUID);
    }

    // Getters y setters

    public int getHealthCorazones() {
        return healthCorazones;
    }

    public void setHealthCorazones(int healthCorazones) {
        this.healthCorazones = healthCorazones;
    }

    public int getSpeedLevel() {
        return speedLevel;
    }

    public void setSpeedLevel(int speedLevel) {
        this.speedLevel = speedLevel;
    }

    public int getSpawnPercentage() {
        return spawnPercentage;
    }

    public void setSpawnPercentage(int spawnPercentage) {
        this.spawnPercentage = Math.min(100, Math.max(0, spawnPercentage));
    }

    /**
     * Incrementa la vida en la cantidad especificada
     * @param amount Cantidad a incrementar
     */
    public void increaseHealth(int amount) {
        healthCorazones += amount;
    }

    /**
     * Incrementa la velocidad en la cantidad especificada
     * @param amount Niveles a incrementar
     */
    public void increaseSpeed(int amount) {
        speedLevel += amount;
    }

    /**
     * Incrementa el porcentaje de spawn en la cantidad especificada
     * @param amount Porcentaje a incrementar
     */
    public void increaseSpawnPercentage(int amount) {
        spawnPercentage = Math.min(100, spawnPercentage + amount);
    }
}
