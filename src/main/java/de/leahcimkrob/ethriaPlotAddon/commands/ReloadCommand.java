package de.leahcimkrob.ethriaPlotAddon.commands;

import de.leahcimkrob.ethriaPlotAddon.EthriaPlotAddon;
import de.leahcimkrob.ethriaPlotAddon.util.PermissionManager;
import org.bukkit.entity.Player;

/**
 * Subcommand zum Neuladen der Plugin-Konfiguration
 */
public class ReloadCommand implements SubcommandExecutor {

    private final EthriaPlotAddon plugin;

    public ReloadCommand(EthriaPlotAddon plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean execute(Player player, String[] args) {
        // Config neu laden
        plugin.getConfigManager().reloadConfig();
        plugin.getMessageManager().setLanguage(plugin.getConfigManager().getLanguage());

        // Re-register aliases after config reload
        // Hier könnten wir eine Methode zum Neuladen der Aliases aufrufen

        player.sendMessage(plugin.getMessageManager().getMessage("config_reloaded"));

        // Debug-Nachricht falls Debug aktiviert ist
        if (plugin.getConfigManager().isDebugEnabled()) {
            plugin.getDebugLogger().debug("Konfiguration neu geladen durch Spieler: %s", player.getName());
        }

        return true;
    }

    @Override
    public boolean hasPermission(Player player) {
        return PermissionManager.canReload(player) || PermissionManager.hasAdminPermission(player);
    }

    @Override
    public String getDescription() {
        return "Lädt die Plugin-Konfiguration neu";
    }

    @Override
    public String getUsage() {
        return "/plotaddon reload";
    }
}
