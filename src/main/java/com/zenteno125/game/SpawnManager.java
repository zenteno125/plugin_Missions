package com.zenteno125.game;

import com.zenteno125.MissionsPlugin;
import com.zenteno125.gui.SpawnPointManager;
import com.zenteno125.items.SpawnStickData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

/**
 * Gestiona los spawns de mobs según las rondas
 */
public class SpawnManager implements Listener {

    private static SpawnManager instance;
    private final Map<UUID, EntityType> trackedMobs = new HashMap<>();
    private final List<SpawnPoint> activeSpawnPoints = new ArrayList<>();

    private int totalMobsToKill = 0;
    private int mobsKilled = 0;

    // Task ID para el verificador periódico
    private int lastMobsCheckerTaskId = -1;

    // Variable para controlar la frecuencia de los mensajes
    private long lastMessageTime = 0;

    // Array de frases aleatorias para los últimos mobs
    private final String[] mobLastWordsMessages = {
            "¡Me pica el ano!",
            "¡venganse, peo a sentones",
            "¡Apoco si muay mampos",
            "¿Manpoloides o como?!",
            "¡Lameloide: mi pilin brilla?",
            "¡Taen el poder de la vergonia!",
            "¡son gays? o por que tan putos!",
            "¡Apoco?!",
            "¡Les pago el uber!",
            "¡Para esas mamadas vengan!",
            "¡Tan rápido?!",
            "¡Nmms espérense!",
            "¡Ni ha empezado no mames!",
            "¡zun zun zun zaur!",
            "¡Tic toc, el ano del omar ya no es virgen!"
    };

    /**
     * Obtiene un mensaje aleatorio del array de mensajes para los últimos mobs
     */
    private String getRandomLastMobMessage() {
        int index = new Random().nextInt(mobLastWordsMessages.length);
        return mobLastWordsMessages[index];
    }

    public static class SpawnPoint {
        private final Location location;
        private final List<EntityType> mobTypes;

        public SpawnPoint(Location location, List<EntityType> mobTypes) {
            this.location = location;
            this.mobTypes = new ArrayList<>(mobTypes);
        }

        public Location getLocation() {
            return location;
        }

        public List<EntityType> getMobTypes() {
            return mobTypes;
        }
    }

    private SpawnManager() {}

    public static SpawnManager getInstance() {
        if (instance == null) {
            instance = new SpawnManager();
        }
        return instance;
    }

    /**
     * Activa los puntos de spawn según la ronda actual
     */
    public void activateSpawnPoints(int round) {
        // Limpiar puntos anteriores
        clearSpawnPoints();

        // Cargar todos los puntos existentes
        SpawnPointManager spawnManager = SpawnPointManager.getInstance();
        List<Location> allPointLocations = spawnManager.getAllPoints();

        if (allPointLocations.isEmpty()) {
            Bukkit.broadcastMessage(ChatColor.RED + "No hay puntos de spawn configurados.");
            return;
        }

        // Calcular cuántos puntos activar según el porcentaje para esta ronda
        GameManager gameManager = GameManager.getInstance();
        int percentToActivate;

        // Rondas 1-4: 20%, 40%, 60%, 80%
        if (round <= 4) {
            percentToActivate = round * 20;
        } else {
            // Ronda 5+: 100%
            percentToActivate = 100;
        }

        int pointsToActivate = Math.max(1, (allPointLocations.size() * percentToActivate) / 100);

        // Mezclar aleatoriamente los puntos
        Collections.shuffle(allPointLocations);

        int activated = 0;
        for (Location loc : allPointLocations) {
            if (activated >= pointsToActivate) break;

            // Obtener los tipos de mobs configurados en este punto
            Optional<List<EntityType>> mobs = spawnManager.get(loc);
            if (mobs.isEmpty() || mobs.get().isEmpty()) continue;

            activeSpawnPoints.add(new SpawnPoint(loc, mobs.get()));
            activated++;
        }

        Bukkit.broadcastMessage(ChatColor.GOLD + "Se han activado " + activated + " de " + allPointLocations.size() +
                " puntos de spawn (" + percentToActivate + "%)");

        // Iniciar el spawneo después del delay
        scheduleSpawn();
    }

    /**
     * Programa el spawneo de mobs en los puntos activos
     */
    private void scheduleSpawn() {
        // Calcular cuántos mobs adicionales aparecen según la ronda
        final int additionalMobs = GameManager.getInstance().calculateAdditionalMobsPerPoint();

        // Calcular el total de mobs que spawneará esta ronda (anticipado)
        // Este es un estimado inicial que se ajustará después del spawn real
        int estimatedMobs = 0;
        for (SpawnPoint point : activeSpawnPoints) {
            // 1 mob base + mobs adicionales por ronda
            estimatedMobs += point.getMobTypes().size() * (1 + additionalMobs);
        }

        // Resetear contadores
        totalMobsToKill = 0; // Se ajustará después del spawn real
        mobsKilled = 0;

        Bukkit.broadcastMessage(ChatColor.RED + "¡Aproximadamente " + estimatedMobs + " enemigos aparecerán pronto!");

        // Iniciar spawneo con delay
        new BukkitRunnable() {
            @Override
            public void run() {
                spawnMobs(additionalMobs);
            }
        }.runTaskLater(MissionsPlugin.getInstance(), 20L * 10); // 10 segundos después
    }

    /**
     * Spawneaa los mobs en todos los puntos activos
     */
    private void spawnMobs(int additionalMobs) {
        // Intentar generar un mini aldeano al inicio de la ronda (solo uno por ronda)
        trySpawnMiniVillager();

        for (SpawnPoint point : activeSpawnPoints) {
            Location loc = point.getLocation();

            // Verificar si hay jugadores cerca (dentro de 100 bloques)
            boolean playersNearby = false;
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getWorld().equals(loc.getWorld()) &&
                        player.getLocation().distance(loc) <= 100) {
                    playersNearby = true;
                    break;
                }
            }

            if (!playersNearby) continue; // No spawneaar si no hay jugadores cerca

            // Por cada tipo de mob en el punto de spawn
            for (EntityType type : point.getMobTypes()) {
                // Spawneaar 1 mob base
                spawnMobAndTrack(loc, type);

                // Spawneaar mobs adicionales según la ronda
                for (int i = 0; i < additionalMobs; i++) {
                    // Mayor variación en la posición (2-3 bloques a la redonda)
                    Location spawnLoc = loc.clone().add(
                            (Math.random() * 6) - 3,  // -3 a 3 bloques en X
                            0,
                            (Math.random() * 6) - 3   // -3 a 3 bloques en Z
                    );

                    // Asegurarse de que la ubicación es segura para spawneo
                    spawnLoc = findSafeSpawnLocation(spawnLoc);

                    spawnMobAndTrack(spawnLoc, type);
                }
            }
        }

        Bukkit.broadcastMessage(ChatColor.RED + "¡Los enemigos han aparecido! " +
                ChatColor.YELLOW + "Elimina a todos para avanzar de ronda.");
    }

    /**
     * Encuentra una ubicación segura para spawneo cercana a la ubicación dada
     * @param loc Ubicación base
     * @return Ubicación segura para spawneo
     */
    private Location findSafeSpawnLocation(Location loc) {
        // Si la ubicación original es segura, devolverla
        if (isSafeLocation(loc)) {
            return loc;
        }

        // Intentar encontrar ubicación segura en un radio de 3 bloques
        for (int attempts = 0; attempts < 10; attempts++) {
            double offsetX = (Math.random() * 6) - 3;
            double offsetZ = (Math.random() * 6) - 3;

            Location testLoc = loc.clone().add(offsetX, 0, offsetZ);

            // Ajustar la Y para encontrar un bloque sólido debajo
            testLoc.setY(testLoc.getWorld().getHighestBlockYAt(testLoc));

            if (isSafeLocation(testLoc)) {
                return testLoc;
            }
        }

        // Si no pudimos encontrar una ubicación segura, devolver la original
        return loc;
    }

    /**
     * Comprueba si una ubicación es segura para spawneo
     * @param loc Ubicación a comprobar
     * @return true si la ubicación es segura
     */
    private boolean isSafeLocation(Location loc) {
        // Verificar que no hay bloques sólidos en la posición de spawn
        return loc.getBlock().getType().isAir() &&
                loc.clone().add(0, 1, 0).getBlock().getType().isAir();
    }

    /**
     * Intenta spawneaar un mini aldeano en una ubicación aleatoria
     */
    private void trySpawnMiniVillager() {
        int currentRound = GameManager.getInstance().getCurrentRound();
        MiniVillagerManager villagerManager = MiniVillagerManager.getInstance();

        // Verificar si debe aparecer un mini aldeano en esta ronda
        if (villagerManager.shouldSpawnInRound(currentRound) && !activeSpawnPoints.isEmpty()) {
            // Elegir un punto de spawn aleatorio
            SpawnPoint randomPoint = activeSpawnPoints.get((int)(Math.random() * activeSpawnPoints.size()));
            Location spawnLoc = randomPoint.getLocation().clone().add(0, 1, 0);

            // Generar el aldeano
            Villager villager = villagerManager.spawnMiniVillager(spawnLoc);

            // Mensaje a los jugadores
            Bukkit.broadcastMessage(ChatColor.GOLD + "¡Un mini aldeano ha aparecido! Protégelo para recibir recompensas.");
        }
    }

    /**
     * Spawneaa un mob y lo registra para seguimiento
     */
    private void spawnMobAndTrack(Location loc, EntityType type) {
        try {
            // Añadir 1 bloque de altura para que aparezcan sobre la tierra
            Location spawnLoc = loc.clone().add(0, 1, 0);

            // Spawn normal de mob (sin reemplazo por aldeanos)
            LivingEntity entity = (LivingEntity) spawnLoc.getWorld().spawnEntity(spawnLoc, type);

            // Solo si el spawn fue exitoso, añadir al contador y tracking
            if (entity != null) {
                trackedMobs.put(entity.getUniqueId(), type);
                totalMobsToKill++; // Incrementar el contador por cada mob real

                // Actualizar el contador en GameManager también
                GameManager.getInstance().setTotalMobsToSpawn(totalMobsToKill);

                // Iniciar el verificador de últimos mobs si no está activo ya
                startLastMobsChecker();
            }
        } catch (Exception e) {
            Bukkit.getLogger().warning("Error al spawnear mob de tipo: " + type + " - " + e.getMessage());
            // No incrementamos el contador ya que el mob no apareció
        }
    }

    /**
     * Limpia todos los puntos de spawn activos
     */
    public void clearSpawnPoints() {
        // Detener el verificador de últimos mobs
        stopLastMobsChecker();

        // Eliminar todos los mobs registrados
        for (UUID mobId : trackedMobs.keySet()) {
            for (World world : Bukkit.getWorlds()) {
                for (org.bukkit.entity.Entity entity : world.getEntities()) {
                    if (entity.getUniqueId().equals(mobId)) {
                        entity.remove();
                        break;
                    }
                }
            }
        }

        trackedMobs.clear();
        activeSpawnPoints.clear();
        totalMobsToKill = 0;
        mobsKilled = 0;

        // Reiniciar el estado del MiniVillagerManager para la nueva ronda
        MiniVillagerManager.getInstance().resetForNewRound();
    }

    /**
     * Registra cuando un mob es asesinado
     */
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        UUID entityId = event.getEntity().getUniqueId();

        // Si es un mini aldeano, dar una vida al jugador que lo mató
        if (MiniVillagerManager.getInstance().isCurrentVillager(entityId)) {
            // Si es un mini aldeano y lo mató un jugador
            if (event.getEntity().getKiller() instanceof Player) {
                Player killer = event.getEntity().getKiller();
                // Añadir 1 vida al jugador
                GameManager gameManager = GameManager.getInstance();
                int currentLives = gameManager.getPlayerLives(killer);
                gameManager.setPlayerLives(killer, currentLives + 1);

                // Mensaje de recompensa
                killer.sendMessage(ChatColor.GREEN + "¡Has recibido una vida extra! Ahora tienes " +
                        ChatColor.YELLOW + (currentLives + 1) + ChatColor.GREEN + " vidas.");
            }
            return; // Mini aldeano muerto, pero no afecta el conteo de mobs
        }

        if (trackedMobs.containsKey(entityId)) {
            trackedMobs.remove(entityId);
            mobsKilled++;

            // Notificar a GameManager
            GameManager.getInstance().registerMobKill();

            // Verificar si es el último mob
            if (mobsKilled >= totalMobsToKill) {
                // Dar un pequeño delay antes de terminar la ronda
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        GameManager.getInstance().endRound();
                    }
                }.runTaskLater(MissionsPlugin.getInstance(), 40L); // 2 segundos después
            }
        }
    }

    /**
     * Devuelve el progreso de la ronda actual
     */
    public String getProgress() {
        return mobsKilled + "/" + totalMobsToKill;
    }

    /**
     * Inicia un verificador periódico para resaltar los últimos mobs
     */
    private void startLastMobsChecker() {
        if (lastMobsCheckerTaskId != -1) {
            return; // Ya está activo
        }

        lastMobsCheckerTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(
                MissionsPlugin.getInstance(),
                this::checkAndHighlightLastMobs,
                100L, // Esperar 5 segundos antes de la primera ejecución
                60L   // Repetir cada 3 segundos (60 ticks)
        );
    }

    /**
     * Detiene el verificador periódico
     */
    private void stopLastMobsChecker() {
        if (lastMobsCheckerTaskId != -1) {
            Bukkit.getScheduler().cancelTask(lastMobsCheckerTaskId);
            lastMobsCheckerTaskId = -1;
        }
    }

    /**
     * Verifica y resalta los últimos mobs (umbral configurable de enemigos restantes)
     */
    private void checkAndHighlightLastMobs() {
        // Si no hay mobs o ya se mataron todos, no hacer nada
        if (trackedMobs.isEmpty() || totalMobsToKill <= 0) {
            return;
        }

        // Obtener el umbral configurado desde GameManager
        int thresholdPercent = GameManager.getInstance().getLastEnemiesThreshold();

        // Calcular el umbral basado en el porcentaje configurado
        int threshold = Math.max(1, (int)(totalMobsToKill * thresholdPercent / 100.0));
        int remainingMobs = trackedMobs.size();

        // Si quedan pocos mobs (según umbral configurado), aplicar efectos visuales
        if (remainingMobs <= threshold) {
            // Aplicamos los efectos visuales a todos los mobs restantes
            for (UUID mobId : trackedMobs.keySet()) {
                for (World world : Bukkit.getWorlds()) {
                    for (org.bukkit.entity.Entity entity : world.getEntities()) {
                        if (entity.getUniqueId().equals(mobId) && entity instanceof LivingEntity) {
                            LivingEntity livingEntity = (LivingEntity) entity;

                            // Aplicar efectos visuales duraderos para hacerlos más visibles
                            livingEntity.addPotionEffect(
                                new PotionEffect(
                                    PotionEffectType.GLOWING,
                                    100, // Duración de 5 segundos (100 ticks)
                                    1, // Nivel 1
                                    false, // Sin partículas
                                    true // Mostrar iconos
                                )
                            );

                            // El efecto de brillo (GLOWING) hace que la entidad se vea a través de las paredes

                            // Mejorar las partículas: más esparcidas y visibles pero no demasiado grandes
                            Location particleLocation = livingEntity.getLocation().clone().add(0, 1.2, 0); // Justo encima de la cabeza

                            // Partículas de fuego para mayor visibilidad
                            livingEntity.getWorld().spawnParticle(
                                org.bukkit.Particle.FLAME,
                                particleLocation,
                                8, // 8 partículas
                                0.5, 0.3, 0.5, // Dispersión más amplia en XYZ para mayor visibilidad
                                0.01 // Velocidad muy baja para que se queden cerca
                            );

                            // Partícula adicional de corazón roto para hacerlo aún más visible
                            livingEntity.getWorld().spawnParticle(
                                org.bukkit.Particle.VILLAGER_ANGRY,
                                particleLocation,
                                3, // 3 partículas
                                0.25, 0.25, 0.25, // Dispersión moderada
                                0 // Velocidad 0
                            );
                        }
                    }
                }
            }

            // Si estamos en el umbral del 5% o menos, mostrar un mensaje aleatorio
            // Limitamos la frecuencia usando un temporizador (cada 10 segundos)
            long currentTime = System.currentTimeMillis();
            if (remainingMobs <= threshold && currentTime - lastMessageTime > 10000) { // 10 segundos entre mensajes
                // Seleccionar un mensaje aleatorio de la lista
                String randomMessage = getRandomLastMobMessage();
                Bukkit.broadcastMessage(ChatColor.GOLD + "¡Quedan " + remainingMobs +
                                       " enemigos! " + ChatColor.YELLOW + randomMessage);
                lastMessageTime = currentTime;
            }
        }
    }

    /**
     * Elimina todos los mobs que quedan por matar en la ronda actual
     * Esto permite saltar al final de una ronda cuando los jugadores no pueden encontrar al último enemigo
     */
    public void killRemainingMobs() {
        if (trackedMobs.isEmpty()) {
            return; // No hay mobs para eliminar
        }

        int killedCount = 0;

        // Iterar sobre una copia de las claves para evitar ConcurrentModificationException
        for (UUID entityId : new HashSet<>(trackedMobs.keySet())) {
            LivingEntity entity = (LivingEntity) Bukkit.getEntity(entityId);
            if (entity != null && entity.isValid()) {
                // Eliminar la entidad y registrar como matada
                entity.setHealth(0);
                killedCount++;
            } else {
                // Si la entidad ya no existe, actualizamos los contadores
                trackedMobs.remove(entityId);
                mobsKilled++;
            }
        }

        // Actualizar contadores
        mobsKilled = totalMobsToKill;

        if (killedCount > 0) {
            Bukkit.broadcastMessage(ChatColor.YELLOW + "¡Se han eliminado " + killedCount + " enemigos restantes!");
        }

        // Verificar si terminó la ronda (debería ser cierto siempre después de esto)
        checkRoundCompletion();
    }

    /**
     * Verifica si la ronda ha terminado (todos los mobs eliminados)
     */
    private void checkRoundCompletion() {
        if (mobsKilled >= totalMobsToKill) {
            // Cancelar el verificador de últimos mobs si está activo
            stopLastMobsChecker();

            // Terminar la ronda
            GameManager.getInstance().endRound();
        }
    }
}
