package com.zenteno125.game;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.*;

import java.util.*;

public class TeamManager {

    private static TeamManager instance;

    public static TeamManager getInstance() {
        if (instance == null) {
            instance = new TeamManager();
        }
        return instance;
    }

    public enum TeamColor {
        BLUE(Material.BLUE_WOOL, ChatColor.BLUE, "Azul"),
        RED(Material.RED_WOOL, ChatColor.RED, "Rojo"),
        GREEN(Material.LIME_WOOL, ChatColor.GREEN, "Verde"),
        YELLOW(Material.YELLOW_WOOL, ChatColor.YELLOW, "Amarillo");

        private final Material material;
        private final ChatColor chatColor;
        private final String spanish;

        TeamColor(Material material, ChatColor chatColor, String spanish) {
            this.material = material;
            this.chatColor = chatColor;
            this.spanish = spanish;
        }

        public Material getMaterial() {
            return material;
        }

        public ChatColor getChatColor() {
            return chatColor;
        }

        public String getSpanishName() {
            return spanish;
        }

        public static TeamColor fromMaterial(Material material) {
            for (TeamColor color : values()) {
                if (color.getMaterial() == material) {
                    return color;
                }
            }
            return null;
        }
    }

    private final Map<TeamColor, Set<UUID>> teams;
    private final Map<TeamColor, List<Location>> spawnPoints;
    private final Map<UUID, TeamColor> playerTeams;
    private int defaultLives;
    private final Map<TeamColor, Integer> teamLives;
    private boolean duelActive;
    private Scoreboard scoreboard;

    private TeamManager() {
        teams = new HashMap<>();
        playerTeams = new HashMap<>();
        spawnPoints = new HashMap<>();
        teamLives = new HashMap<>();
        defaultLives = 1;
        duelActive = false;

        for (TeamColor color : TeamColor.values()) {
            teams.put(color, new HashSet<>());
            spawnPoints.put(color, new ArrayList<>());
            teamLives.put(color, defaultLives);
        }
    }

    public void addPlayerToTeam(UUID playerUUID, TeamColor color) {
        TeamColor currentTeam = playerTeams.get(playerUUID);
        if (currentTeam != null) {
            teams.get(currentTeam).remove(playerUUID);
        }

        teams.get(color).add(playerUUID);
        playerTeams.put(playerUUID, color);
    }

    public void removePlayerFromTeam(UUID playerUUID) {
        TeamColor color = playerTeams.get(playerUUID);
        if (color != null) {
            teams.get(color).remove(playerUUID);
            playerTeams.remove(playerUUID);
        }
    }

    public TeamColor getPlayerTeam(UUID playerUUID) {
        return playerTeams.get(playerUUID);
    }

    public void addSpawnPoint(TeamColor color, Location location) {
        spawnPoints.get(color).add(location);
    }

    public void removeSpawnPoint(TeamColor color, Location location) {
        spawnPoints.get(color).removeIf(loc -> loc.distance(location) < 1.0);
    }

    public void clearSpawnPoints(TeamColor color) {
        spawnPoints.get(color).clear();
    }

    public void clearAllSpawnPoints() {
        for (TeamColor color : TeamColor.values()) {
            spawnPoints.get(color).clear();
        }
    }

    public Set<UUID> getTeamMembers(TeamColor color) {
        return new HashSet<>(teams.get(color));
    }

    public int getSpawnPointCount(TeamColor color) {
        return spawnPoints.get(color).size();
    }

    public void setDefaultLives(int lives) {
        this.defaultLives = lives;
        for (TeamColor color : TeamColor.values()) {
            teamLives.put(color, lives);
        }
    }

    public int getDefaultLives() {
        return defaultLives;
    }

    public int getTeamLives(TeamColor color) {
        return teamLives.get(color);
    }

    public void decrementTeamLives(TeamColor color) {
        int currentLives = teamLives.get(color);
        if (currentLives > 0) {
            teamLives.put(color, currentLives - 1);
        }
    }

    public void startDuel() {
        if (!hasSufficientTeams()) {
            Bukkit.broadcastMessage("§c¡No hay suficientes equipos con jugadores para iniciar un duelo!");
            return;
        }

        if (!hasSufficientSpawnPoints()) {
            Bukkit.broadcastMessage("§c¡Faltan puntos de spawn para algunos equipos!");
            return;
        }

        duelActive = true;
        setupScoreboard();

        for (TeamColor color : TeamColor.values()) {
            teamLives.put(color, defaultLives);
        }

        for (TeamColor color : TeamColor.values()) {
            List<Location> teamSpawns = spawnPoints.get(color);
            if (teamSpawns.isEmpty()) continue;

            Set<UUID> members = teams.get(color);
            for (UUID memberId : members) {
                Player player = Bukkit.getPlayer(memberId);
                if (player == null || !player.isOnline()) continue;

                Location spawnPoint = teamSpawns.get(new Random().nextInt(teamSpawns.size()));
                player.teleport(spawnPoint);

                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 5 * 20, 4));

                player.sendMessage(color.getChatColor() + "¡Duelo iniciado! Tienes 5 segundos de inmovilidad.");
            }
        }

        Bukkit.broadcastMessage("§a¡El duelo por equipos ha comenzado!");
    }

    public void endDuel() {
        if (!duelActive) return;

        duelActive = false;
        TeamColor winningTeam = getWinningTeam();

        StringBuilder summary = new StringBuilder("§6=== Resumen del Duelo ===\n");

        if (winningTeam != null) {
            summary.append("§6Equipo ganador: ").append(winningTeam.getChatColor()).append(winningTeam.getSpanishName()).append("\n");
            summary.append("§6Miembros: ");

            boolean first = true;
            for (UUID memberId : teams.get(winningTeam)) {
                Player player = Bukkit.getPlayer(memberId);
                if (player != null) {
                    if (!first) summary.append(", ");
                    summary.append(player.getName());
                    first = false;
                }
            }
        } else {
            summary.append("§6No hay equipo ganador, el duelo fue cancelado.\n");
        }

        Bukkit.broadcastMessage(summary.toString());

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
        }
    }

    public void handlePlayerDeath(Player player) {
        if (!duelActive) return;

        UUID playerId = player.getUniqueId();
        TeamColor team = getPlayerTeam(playerId);

        if (team == null) return;

        int lives = teamLives.get(team);
        if (lives > 0) {
            decrementTeamLives(team);
            lives--;

            Bukkit.broadcastMessage(team.getChatColor() + "El equipo " + team.getSpanishName() +
                                   " ha perdido una vida. Vidas restantes: " + lives);

            List<Location> teamSpawns = spawnPoints.get(team);
            if (!teamSpawns.isEmpty()) {
                Location respawnPoint = teamSpawns.get(new Random().nextInt(teamSpawns.size()));
                player.teleport(respawnPoint);
                player.sendMessage("§aHas respawneado en un punto de tu equipo.");

                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 3 * 20, 4));
            }

            updateScoreboard();
        } else {
            player.setGameMode(org.bukkit.GameMode.SPECTATOR);
            player.sendMessage("§c¡Tu equipo no tiene más vidas! Ahora estás en modo espectador.");

            checkForWinner();
        }
    }

    private void checkForWinner() {
        int teamsAlive = 0;
        TeamColor lastTeamAlive = null;

        for (TeamColor color : TeamColor.values()) {
            if (!teams.get(color).isEmpty() && teamLives.get(color) > 0) {
                teamsAlive++;
                lastTeamAlive = color;
            }
        }

        if (teamsAlive <= 1 && duelActive) {
            if (lastTeamAlive != null) {
                Bukkit.broadcastMessage("§a¡El equipo " + lastTeamAlive.getChatColor() + lastTeamAlive.getSpanishName() +
                                       "§a ha ganado el duelo!");
            }
            endDuel();
        }
    }

    private TeamColor getWinningTeam() {
        for (TeamColor color : TeamColor.values()) {
            if (!teams.get(color).isEmpty() && teamLives.get(color) > 0) {
                return color;
            }
        }
        return null;
    }

    private boolean hasSufficientTeams() {
        int teamsWithPlayers = 0;

        for (TeamColor color : TeamColor.values()) {
            if (!teams.get(color).isEmpty()) {
                teamsWithPlayers++;
            }
        }

        return teamsWithPlayers >= 2;
    }

    private boolean hasSufficientSpawnPoints() {
        for (TeamColor color : TeamColor.values()) {
            if (!teams.get(color).isEmpty() && spawnPoints.get(color).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public boolean isDuelActive() {
        return duelActive;
    }

    private void setupScoreboard() {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        scoreboard = manager.getNewScoreboard();

        Objective objective = scoreboard.registerNewObjective("duelScore", "dummy", "§d§lDuelo por Equipos");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        int score = TeamColor.values().length + 1;

        for (TeamColor color : TeamColor.values()) {
            Set<UUID> members = teams.get(color);
            if (members.isEmpty()) continue;

            Score teamScore = objective.getScore(color.getChatColor() + color.getSpanishName() +
                                               ": §f" + teamLives.get(color) + " vidas");
            teamScore.setScore(score--);

            org.bukkit.scoreboard.Team sbTeam = scoreboard.registerNewTeam(color.name());
            sbTeam.setColor(color.getChatColor());
            sbTeam.setPrefix(color.getChatColor().toString());

            for (UUID memberId : members) {
                Player player = Bukkit.getPlayer(memberId);
                if (player != null) {
                    sbTeam.addEntry(player.getName());
                }
            }
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.setScoreboard(scoreboard);
        }
    }

    public void updateScoreboard() {
        if (!duelActive || scoreboard == null) return;

        Objective objective = scoreboard.getObjective("duelScore");
        if (objective == null) {
            setupScoreboard();
            return;
        }

        for (TeamColor color : TeamColor.values()) {
            Set<UUID> members = teams.get(color);
            if (members.isEmpty()) continue;

            for (String entry : scoreboard.getEntries()) {
                if (entry.startsWith(color.getChatColor().toString())) {
                    scoreboard.resetScores(entry);
                }
            }

            Score teamScore = objective.getScore(color.getChatColor() + color.getSpanishName() +
                                               ": §f" + teamLives.get(color) + " vidas");
            teamScore.setScore(TeamColor.values().length - color.ordinal());
        }
    }
}
