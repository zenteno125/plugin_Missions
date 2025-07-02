package com.zenteno125.listeners;

import com.zenteno125.game.GameManager;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import com.zenteno125.MissionsPlugin;

/**
 * Listener para eventos de jugadores
 */
public class PlayerListener implements Listener {

    /**
     * Maneja la muerte de jugadores
     */
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        GameManager gameManager = GameManager.getInstance();

        // Verificar si hay un juego activo
        if (gameManager.getState() == GameManager.GameState.ACTIVE ||
            gameManager.getState() == GameManager.GameState.STARTING) {

            // Reducir vidas
            int livesLeft = gameManager.reducePlayerLives(player);

            // Informar al jugador
            String message;
            if (livesLeft > 0) {
                message = ChatColor.RED + "¡Has perdido una vida! Te quedan " +
                          ChatColor.YELLOW + livesLeft + ChatColor.RED + " vidas.";
            } else {
                message = ChatColor.DARK_RED + "¡Has perdido todas tus vidas! " +
                          "Ahora estás en modo espectador hasta que termine la partida.";
            }

            player.sendMessage(message);

            // Verificar si todos los jugadores están muertos
            if (gameManager.allPlayersEliminated()) {
                player.sendMessage(ChatColor.RED + "¡Todos los jugadores han sido eliminados!");
                gameManager.endGame();
            }
        }
    }

    /**
     * Muestra información al jugador cuando vuelve a aparecer
     */
    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        final Player player = event.getPlayer();
        final GameManager gameManager = GameManager.getInstance();

        // Verificar si hay un juego activo
        if (gameManager.getState() == GameManager.GameState.ACTIVE ||
            gameManager.getState() == GameManager.GameState.STARTING) {

            // Informar al jugador sobre sus vidas restantes
            int livesLeft = gameManager.getPlayerLives(player);

            if (livesLeft > 0) {
                player.sendMessage(ChatColor.GOLD + "Ronda actual: " + gameManager.getCurrentRound() +
                                  " - Vidas restantes: " + ChatColor.RED + livesLeft);
            } else {
                // Colocar al jugador en modo espectador en el siguiente tick
                // (no podemos cambiar el GameMode directamente en el evento de respawn)
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        player.setGameMode(GameMode.SPECTATOR);
                        player.sendMessage(ChatColor.RED + "No te quedan vidas. Estás en modo espectador hasta que termine la partida.");
                    }
                }.runTaskLater(MissionsPlugin.getInstance(), 1L);
            }
        }
    }

    /**
     * Maneja la salida de jugadores
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        GameManager gameManager = GameManager.getInstance();
        // Limpia cualquier estado temporal si es necesario (opcional, según lógica interna)
        // Por ejemplo, podrías guardar progreso o limpiar buffs temporales aquí
    }

    /**
     * Muestra información al jugador cuando se conecta durante una partida
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        GameManager gameManager = GameManager.getInstance();

        // Si hay una partida en curso, informar al jugador
        if (gameManager.getState() == GameManager.GameState.ACTIVE ||
            gameManager.getState() == GameManager.GameState.STARTING) {

            // Si el jugador no tiene vidas asignadas, asignarle vidas predeterminadas
            if (!gameManager.hasPlayerLives(player)) {
                gameManager.setPlayerLives(player, gameManager.getDefaultLives());
                player.sendMessage(ChatColor.GREEN + "Te has unido a una partida en curso. " +
                                  "Tienes " + gameManager.getDefaultLives() + " vidas.");

                // Asegurarse de que esté en modo supervivencia
                if (player.getGameMode() == GameMode.SPECTATOR) {
                    player.setGameMode(GameMode.SURVIVAL);
                }
            } else {
                int lives = gameManager.getPlayerLives(player);

                if (lives > 0) {
                    player.sendMessage(ChatColor.GREEN + "Te has reconectado a la partida. " +
                                      "Te quedan " + lives + " vidas.");

                    // Asegurarse de que esté en modo supervivencia
                    if (player.getGameMode() == GameMode.SPECTATOR) {
                        player.setGameMode(GameMode.SURVIVAL);
                    }
                } else {
                    // Si no le quedan vidas, mantenerlo en modo espectador
                    player.setGameMode(GameMode.SPECTATOR);
                    player.sendMessage(ChatColor.RED + "No te quedan vidas. Estás en modo espectador hasta que termine la partida.");
                }
            }
        } else {
            // Si no hay partida en curso, asegurarse de que el jugador esté en modo supervivencia
            if (player.getGameMode() != GameMode.SURVIVAL) {
                player.setGameMode(GameMode.SURVIVAL);
            }
        }
    }
}
