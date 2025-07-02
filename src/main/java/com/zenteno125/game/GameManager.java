package com.zenteno125.game;

import com.zenteno125.MissionsPlugin;
import com.zenteno125.items.LootChestManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public class GameManager {
    private static GameManager instance;

    // Estados del juego
    public enum GameState {
        WAITING, // Esperando para iniciar
        STARTING, // Cuenta regresiva para iniciar
        ACTIVE, // Partida en curso
        ENDING // Partida terminando
    }

    // Configuración
    private int defaultLives = 3;
    private int delayBeforeSpawn = 30; // Cambiado de 60 a 30 segundos
    private int maxAdditionalMobs = 4; // Máximo de mobs adicionales por punto de spawn
    private int lastEnemiesThreshold = 10; // Porcentaje para destacar últimos enemigos (cambiado a 10%)

    // Estado actual
    private GameState state = GameState.WAITING;
    private int currentRound = 0;
    private Map<UUID, Integer> playerLives = new HashMap<>();
    private boolean refillChestsEachRound = false;

    // Mobs por ronda
    private int totalMobsToSpawn = 0;
    private int mobsKilled = 0;

    private GameManager() {
        loadConfig();
    }

    public static GameManager getInstance() {
        if (instance == null) instance = new GameManager();
        return instance;
    }

    /**
     * Carga la configuración desde config.yml
     */
    public void loadConfig() {
        FileConfiguration config = MissionsPlugin.getInstance().getConfig();
        defaultLives = config.getInt("defaultLives", 3);
        delayBeforeSpawn = config.getInt("delayBeforeSpawn", 60);
        refillChestsEachRound = config.getBoolean("refillChestsEachRound", false);
        maxAdditionalMobs = config.getInt("maxAdditionalMobs", 4);
    }

    /**
     * Guarda la configuración en config.yml
     */
    public void saveConfig() {
        FileConfiguration config = MissionsPlugin.getInstance().getConfig();
        config.set("defaultLives", defaultLives);
        config.set("delayBeforeSpawn", delayBeforeSpawn);
        config.set("refillChestsEachRound", refillChestsEachRound);
        config.set("maxAdditionalMobs", maxAdditionalMobs);
        MissionsPlugin.getInstance().saveConfig();
    }

    /**
     * Obtiene el número predeterminado de vidas por jugador
     * @return Número de vidas predeterminado
     */
    public int getDefaultLives() {
        return defaultLives;
    }

    /**
     * Establece el número predeterminado de vidas por jugador
     * @param lives Número de vidas a establecer
     */
    public void setDefaultLives(int lives) {
        this.defaultLives = lives;
        saveConfig();
    }

    /**
     * Obtiene el estado actual del juego
     * @return Estado actual del juego
     */
    public GameState getState() {
        return state;
    }

    /**
     * Obtiene la ronda actual del juego
     * @return Número de la ronda actual
     */
    public int getCurrentRound() {
        return currentRound;
    }

    /**
     * Inicia una nueva partida
     */
    public void startGame() {
        if (state != GameState.WAITING) {
            return;
        }

        state = GameState.STARTING;
        currentRound = 1;
        playerLives.clear();

        // Asignar vidas a todos los jugadores en línea
        for (Player player : Bukkit.getOnlinePlayers()) {
            playerLives.put(player.getUniqueId(), defaultLives);
            player.sendMessage(ChatColor.GREEN + "¡Partida iniciada! Tienes " +
                              ChatColor.YELLOW + defaultLives + ChatColor.GREEN + " vidas.");
        }

        broadcastMessage(ChatColor.GOLD + "¡La partida ha comenzado! Preparando la ronda 1...");

        // Cargar cofres con ítems
        fillAllChests();

        // Iniciar la primera ronda después del delay
        Bukkit.getScheduler().runTaskLater(MissionsPlugin.getInstance(), () -> {
            startRound();
        }, delayBeforeSpawn * 20L); // Convertir segundos a ticks

        // Guardar estado en configuración
        saveConfig();
    }

    /**
     * Inicia una nueva ronda
     */
    public void startRound() {
        if (state != GameState.STARTING && state != GameState.ACTIVE) {
            return;
        }

        state = GameState.ACTIVE;
        mobsKilled = 0;

        // Seleccionar puntos de spawn según la ronda actual
        int percentToActivate = calculateSpawnPointsPercentage();
        broadcastMessage(ChatColor.GOLD + "¡Ronda " + currentRound + " iniciada! " +
                        "Activando " + percentToActivate + "% de los puntos de spawn.");

        // Activar los puntos de spawn correspondientes usando SpawnManager
        SpawnManager.getInstance().activateSpawnPoints(currentRound);

        // Refill chests according to rarity and current round
        LootChestManager chestManager = LootChestManager.getInstance();
        chestManager.queueChestsForRefillByRarity(currentRound);

        // Verificar si es momento de rotar las plantillas de los cofres (cada 10 rondas)
        chestManager.checkTemplateRotation(currentRound);
    }

    /**
     * Calcula el porcentaje de puntos de spawn a activar según la ronda
     */
    private int calculateSpawnPointsPercentage() {
        // Rondas 1-4: 20%, 40%, 60%, 80%
        if (currentRound <= 4) {
            return currentRound * 20;
        }
        // Ronda 5+: 100%
        return 100;
    }

    /**
     * Calcula cuántos mobs adicionales spawneará cada punto según la ronda
     */
    public int calculateAdditionalMobsPerPoint() {
        // Para las rondas 1-5, aumentamos de 1 en 1 (un mob adicional por ronda)
        if (currentRound <= 5) {
            return currentRound - 1; // Ronda 1: 0, Ronda 2: 1, Ronda 3: 2, Ronda 4: 3, Ronda 5: 4
        }

        // A partir de la ronda 6, empezamos a sumar el máximo configurado por el jugador
        // El valor base es 5 mobs (que es lo que tenemos en la ronda 5)
        int baseAmount = 5;

        // Calculamos cuántas veces hemos pasado el umbral de la ronda 6
        int additionalRounds = currentRound - 6;

        // Sumamos el máximo configurado por el jugador por cada ronda adicional
        return baseAmount + (additionalRounds * maxAdditionalMobs);
    }

    /**
     * Termina la ronda actual y avanza a la siguiente
     */
    public void endRound() {
        if (state != GameState.ACTIVE) {
            return;
        }

        broadcastMessage(ChatColor.GREEN + "¡Ronda " + currentRound + " completada!");

        currentRound++;
        broadcastMessage(ChatColor.GOLD + "Preparando ronda " + currentRound + "...");

        // Iniciar la siguiente ronda después de un breve delay
        Bukkit.getScheduler().runTaskLater(MissionsPlugin.getInstance(), () -> {
            startRound();
        }, 200L); // 10 segundos de espera entre rondas
    }

    /**
     * Termina la partida en curso
     */
    public void endGame() {
        if (state == GameState.WAITING) {
            return;
        }

        state = GameState.ENDING;
        broadcastMessage(ChatColor.RED + "¡La partida ha terminado! Rondas jugadas: " + currentRound);

        // Limpiar puntos de spawn activos
        SpawnManager.getInstance().clearSpawnPoints();

        // Restaurar el modo supervivencia para todos los jugadores
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getGameMode() == GameMode.SPECTATOR) {
                player.setGameMode(GameMode.SURVIVAL);
                player.sendMessage(ChatColor.GREEN + "La partida ha terminado. Has vuelto al modo supervivencia.");
            }
        }

        // Reiniciar estado
        state = GameState.WAITING;
        currentRound = 0;
        playerLives.clear();
    }

    /**
     * Registra un mob asesinado
     */
    public void registerMobKill() {
        mobsKilled++;

        // Si ya se mataron todos los mobs de la ronda, SpawnManager se encarga de terminar la ronda
        // No necesitamos accionar nada aquí ya que el SpawnManager llama a endRound()
    }

    /**
     * Establece el total de mobs que aparecerán en la ronda
     */
    public void setTotalMobsToSpawn(int count) {
        this.totalMobsToSpawn = count;
    }

    /**
     * Rellena todos los cofres con sus plantillas asignadas
     */
    private void fillAllChests() {
        // Marcar todos los cofres para recargar
        LootChestManager.getInstance().queueAllChestsForRefill();

        // Enviar mensaje de feedback
        broadcastMessage(ChatColor.GOLD + "¡Los cofres han sido preparados con botín!");
    }

    /**
     * Envía un mensaje a todos los jugadores
     */
    private void broadcastMessage(String message) {
        Bukkit.broadcastMessage(message);
    }

    /**
     * Devuelve el progreso de la ronda actual
     */
    public String getProgress() {
        return mobsKilled + "/" + totalMobsToSpawn;
    }

    /**
     * Obtiene el número total de mobs a aparecer en la ronda actual
     * @return Número total de mobs a aparecer
     */
    public int getTotalMobsToSpawn() {
        return totalMobsToSpawn;
    }

    /**
     * Obtiene el número de mobs eliminados en la ronda actual
     * @return Número de mobs eliminados
     */
    public int getMobsKilled() {
        return mobsKilled;
    }

    /**
     * Salta la ronda actual y pasa a la siguiente
     * Útil cuando los jugadores no pueden encontrar el último enemigo
     */
    public void skipCurrentRound() {
        if (state == GameState.ACTIVE) {
            broadcastMessage(ChatColor.GOLD + "¡Ronda " + currentRound + " saltada manualmente!");

            // Cambiar a estado "STARTING" para evitar que se procesen más acciones de la ronda actual
            state = GameState.STARTING;

            // Eliminar cualquier enemigo restante de forma definitiva
            SpawnManager.getInstance().killRemainingMobs();

            // Avanzar a la siguiente ronda
            broadcastMessage(ChatColor.GOLD + "Preparando ronda " + (currentRound + 1) + "...");

            // Iniciar la siguiente ronda después de un breve delay
            Bukkit.getScheduler().runTaskLater(MissionsPlugin.getInstance(), () -> {
                currentRound++; // Incrementamos la ronda aquí directamente
                state = GameState.STARTING; // Asegurar que estamos en el estado correcto
                startRound();
            }, 200L); // 10 segundos de espera entre rondas
        } else {
            broadcastMessage(ChatColor.RED + "¡No hay ninguna ronda activa para saltar!");
        }
    }

    /**
     * Reduce las vidas de un jugador en 1
     * @param player El jugador que perdió una vida
     * @return El número de vidas restantes
     */
    public int reducePlayerLives(Player player) {
        UUID playerId = player.getUniqueId();
        if (playerLives.containsKey(playerId)) {
            int lives = playerLives.get(playerId) - 1;
            playerLives.put(playerId, Math.max(0, lives));
            return Math.max(0, lives);
        }
        return 0;
    }

    /**
     * Establece las vidas de un jugador
     * @param player El jugador
     * @param lives Número de vidas a establecer
     */
    public void setPlayerLives(Player player, int lives) {
        playerLives.put(player.getUniqueId(), lives);
    }

    /**
     * Obtiene las vidas restantes de un jugador
     * @param player El jugador
     * @return Número de vidas restantes
     */
    public int getPlayerLives(Player player) {
        return playerLives.getOrDefault(player.getUniqueId(), 0);
    }

    /**
     * Verifica si un jugador tiene vidas asignadas
     * @param player El jugador
     * @return true si el jugador tiene vidas asignadas
     */
    public boolean hasPlayerLives(Player player) {
        return playerLives.containsKey(player.getUniqueId());
    }

    /**
     * Verifica si todos los jugadores han sido eliminados
     * @return true si todos los jugadores tienen 0 vidas
     */
    public boolean allPlayersEliminated() {
        // Si no hay jugadores con vidas, todos están eliminados
        if (playerLives.isEmpty()) {
            return true;
        }

        // Verificar si hay algún jugador con vidas
        for (Integer lives : playerLives.values()) {
            if (lives > 0) {
                return false;
            }
        }

        return true;
    }

    /**
     * Obtiene el máximo de mobs adicionales por punto de spawn
     * @return Número máximo de mobs adicionales
     */
    public int getMaxAdditionalMobs() {
        return maxAdditionalMobs;
    }

    /**
     * Establece el máximo de mobs adicionales por punto de spawn
     * @param max Número máximo de mobs adicionales
     */
    public void setMaxAdditionalMobs(int max) {
        this.maxAdditionalMobs = Math.max(0, max);
        saveConfig();
    }

    /**
     * Incrementa el máximo de mobs adicionales en la cantidad especificada
     * @param amount Cantidad a incrementar
     */
    public void increaseMaxAdditionalMobs(int amount) {
        maxAdditionalMobs += amount;
        saveConfig();
    }

    /**
     * Fuerza el avance a la siguiente ronda (elimina todos los mobs y avanza)
     */
    public void forceNextRound() {
        if (state != GameState.ACTIVE) {
            return;
        }

        // Limpiar los mobs actuales
        SpawnManager.getInstance().clearSpawnPoints();

        broadcastMessage(ChatColor.GOLD + "¡Ronda " + currentRound + " finalizada forzosamente!");

        // Avanzar a la siguiente ronda
        currentRound++;
        broadcastMessage(ChatColor.GOLD + "Preparando ronda " + currentRound + "...");

        // Iniciar la siguiente ronda después de un breve delay
        Bukkit.getScheduler().runTaskLater(MissionsPlugin.getInstance(), () -> {
            startRound();
        }, 200L); // 10 segundos de espera entre rondas
    }

    /**
     * Obtiene el porcentaje de umbral para destacar los últimos enemigos
     * @return Porcentaje de umbral (por defecto 5%)
     */
    public int getLastEnemiesThreshold() {
        return lastEnemiesThreshold;
    }

    /**
     * Establece el porcentaje de umbral para destacar los últimos enemigos
     * @param threshold Porcentaje de umbral (1-100)
     */
    public void setLastEnemiesThreshold(int threshold) {
        // Asegurarse de que el umbral esté entre 1 y 100
        this.lastEnemiesThreshold = Math.max(1, Math.min(100, threshold));
        saveConfig();
    }

    /**
     * Incrementa o decrementa el porcentaje de umbral para destacar los últimos enemigos
     * @param amount Cantidad a incrementar (positivo) o decrementar (negativo)
     */
    public void adjustLastEnemiesThreshold(int amount) {
        setLastEnemiesThreshold(lastEnemiesThreshold + amount);
    }
}
