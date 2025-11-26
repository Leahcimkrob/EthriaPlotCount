package de.leahcimkrob.ethriaPlotCount.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class ConfigManager {
    private final JavaPlugin plugin;
    private FileConfiguration config;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        config = plugin.getConfig();
    }

    public void reloadConfig() {
        loadConfig();
    }

    // Sprache
    public String getLanguage() {
        return config.getString("language", "de-de");
    }

    // Command-Aliases
    public List<String> getCommandAliases() {
        return config.getStringList("command_aliases");
    }

    // Einstellungen
    public boolean shouldCountInvisible() {
        return config.getBoolean("settings.count_invisible", true);
    }

    public boolean shouldCountFixed() {
        return config.getBoolean("settings.count_fixed", true);
    }

    public boolean shouldIncludeMergedPlots() {
        return config.getBoolean("settings.include_merged_plots", true);
    }

    public int getMaxCountLimit() {
        return config.getInt("settings.max_count_limit", 1000);
    }

    public boolean shouldCountDroppedItems() {
        return config.getBoolean("settings.count_dropped_items", false);
    }

    public boolean isDebugBoundaries() {
        return config.getBoolean("settings.debug_boundaries", false);
    }

    public boolean isDebugEnabled() {
        return config.getBoolean("settings.debug_enabled", false);
    }

    // Berechtigungen
    public String getBasePermission() {
        return config.getString("permissions.base_permission", "ethriaplotcount.use");
    }
}
