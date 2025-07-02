package com.zenteno125.items;

import org.bukkit.ChatColor;

/**
 * Enumeración que define los diferentes tipos de rareza para las plantillas de botín
 */
public enum LootRarity {
    NORMAL(ChatColor.WHITE + "Normal", 1, "Aparece cada ronda"),
    RARE(ChatColor.BLUE + "Rara", 5, "Aparece cada 5 rondas"),
    EPIC(ChatColor.DARK_PURPLE + "Épica", 10, "Aparece cada 10 rondas");

    private final String displayName;
    private final int roundInterval;
    private final String description;

    LootRarity(String displayName, int roundInterval, String description) {
        this.displayName = displayName;
        this.roundInterval = roundInterval;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getRoundInterval() {
        return roundInterval;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Verifica si la plantilla debe aparecer en la ronda actual
     * @param currentRound Ronda actual
     * @return true si debe aparecer
     */
    public boolean shouldAppearInRound(int currentRound) {
        return currentRound % roundInterval == 0;
    }

    /**
     * Obtiene la siguiente rareza en orden cíclico
     * NORMAL -> RARE -> EPIC -> NORMAL
     * @return La siguiente rareza
     */
    public LootRarity next() {
        switch (this) {
            case NORMAL:
                return RARE;
            case RARE:
                return EPIC;
            case EPIC:
                return NORMAL;
            default:
                return NORMAL;
        }
    }
}
