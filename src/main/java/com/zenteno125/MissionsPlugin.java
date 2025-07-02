package com.zenteno125;
import com.zenteno125.commands.MissionsCommand;
import com.zenteno125.game.GameManager;
import com.zenteno125.game.SpawnManager;
import com.zenteno125.items.ItemRegistry;
import com.zenteno125.items.LootTemplateManager;
import com.zenteno125.listeners.InventoryListener;
import com.zenteno125.listeners.PlayerInteractListener;
import com.zenteno125.listeners.LootStickListener;
import com.zenteno125.listeners.PlayerListener;
import org.bukkit.plugin.java.JavaPlugin;

public final class MissionsPlugin extends JavaPlugin {

    private static MissionsPlugin instance;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        // Cargar gestores
        ItemRegistry.init(this);
        LootTemplateManager.getInstance(); // TODO LootTemplates: cargar plantillas
        GameManager.getInstance().loadConfig();

        // Registrar comandos
        getCommand("missions").setExecutor(new MissionsCommand());

        // Registrar listeners
        getServer().getPluginManager().registerEvents(new InventoryListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerInteractListener(), this);
        getServer().getPluginManager().registerEvents(new LootStickListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerListener(), this);
        getServer().getPluginManager().registerEvents(SpawnManager.getInstance(), this);
        getServer().getPluginManager().registerEvents(new com.zenteno125.listeners.TeamBlockListener(), this);

        getLogger().info("Missions habilitado");
        new com.zenteno125.particles.SpawnParticlesTask().runTaskTimer(this, 0L, 20L);

    }

    @Override
    public void onDisable() {
        getLogger().info("Missions deshabilitado");
    }

    public static MissionsPlugin getInstance() {
        return instance;
    }
}
