// CAMBIOS PARA SpawnManager.java

// 1. Modificación del método spawnMobAndTrack para añadir tags y evitar despawneo:

private void spawnMobAndTrack(Location loc, EntityType type) {
    try {
        // Añadir 1 bloque de altura para que aparezcan sobre la tierra
        Location spawnLoc = loc.clone().add(0, 1, 0);

        // Spawn normal de mob
        LivingEntity entity = (LivingEntity) spawnLoc.getWorld().spawnEntity(spawnLoc, type);

        // Solo si el spawn fue exitoso, añadir al contador y tracking
        if (entity != null) {
            // Aplicar tag personalizado para evitar despawneo
            String entityName = type.toString().toLowerCase().replace("_", "");
            entity.addScoreboardTag("missions_enemy_" + entityName);

            // Establecer que no desaparezca
            entity.setPersistent(true);
            entity.setRemoveWhenFarAway(false);

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

// 2. Modificación del método checkAndHighlightLastMobs para usar correctamente el umbral del 5%:

private void checkAndHighlightLastMobs() {
    // Si no hay mobs o ya se mataron todos, no hacer nada
    if (trackedMobs.isEmpty() || totalMobsToKill <= 0) {
        return;
    }

    // Obtener el umbral configurado desde GameManager
    int thresholdPercent = GameManager.getInstance().getLastEnemiesThreshold();

    // Calcular el umbral basado en el porcentaje configurado (por defecto 5%)
    int threshold = Math.max(1, (int)(totalMobsToKill * thresholdPercent / 100.0));
    int remainingMobs = trackedMobs.size();

    // Si quedan pocos mobs (según umbral configurado), aplicar efectos visuales
    if (remainingMobs <= threshold) {
        // Evitar spam de mensajes (solo uno cada 15 segundos)
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastMessageTime > 15000) {
            lastMessageTime = currentTime;
            Bukkit.broadcastMessage(ChatColor.RED + "¡Quedan " + remainingMobs + " enemigos! " +
                    ChatColor.YELLOW + getRandomLastMobMessage());
        }

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
                                false, // Sin ambiente
                                true  // Mostrar partículas
                            )
                        );
                    }
                }
            }
        }
    }
}

// 3. Modificación del método killRemainingMobs para asegurar que todos los enemigos se eliminen:

public void killRemainingMobs() {
    if (trackedMobs.isEmpty()) {
        return; // No hay mobs para eliminar
    }

    int killedCount = 0;

    // Crear una copia del conjunto para evitar problemas de concurrencia
    Set<UUID> mobsToRemove = new HashSet<>(trackedMobs.keySet());

    // Buscar en todos los mundos para asegurar que encontramos todas las entidades
    for (UUID mobId : mobsToRemove) {
        boolean removed = false;

        for (World world : Bukkit.getWorlds()) {
            for (org.bukkit.entity.Entity entity : world.getEntities()) {
                if (entity.getUniqueId().equals(mobId)) {
                    // Eliminar la entidad directamente
                    entity.remove();
                    killedCount++;
                    removed = true;
                    break;
                }
            }

            if (removed) {
                break;
            }
        }

        // Si la entidad fue encontrada y eliminada, o si ya no existe
        trackedMobs.remove(mobId);
        mobsKilled++;
    }

    // Limpiar cualquier referencia restante
    trackedMobs.clear();

    // Informar del resultado
    if (killedCount > 0) {
        Bukkit.broadcastMessage(ChatColor.RED + "Se han eliminado " + killedCount + " enemigos restantes.");
    }

    // Detener el verificador de últimos mobs
    stopLastMobsChecker();
}
