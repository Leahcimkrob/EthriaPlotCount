package de.leahcimkrob.ethriaPlotAddon.commands;

import de.leahcimkrob.ethriaPlotAddon.EthriaPlotAddon;
import de.leahcimkrob.ethriaPlotAddon.util.EntityGroupManager;
import de.leahcimkrob.ethriaPlotAddon.util.PermissionManager;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Set;

/**
 * Subcommand für die Hilfe-Anzeige
 */
public class HelpCommand implements SubcommandExecutor {

    private final EthriaPlotAddon plugin;
    private final SubcommandManager subcommandManager;

    public HelpCommand(EthriaPlotAddon plugin, SubcommandManager subcommandManager) {
        this.plugin = plugin;
        this.subcommandManager = subcommandManager;
    }

    @Override
    public boolean execute(Player player, String[] args) {
        player.sendMessage("§6=== EthriaPlotAddon Help ===");

        // Zeige nur verfügbare Befehle
        boolean hasAnyPermission = false;

        // Dynamisch alle verfügbaren Subcommands anzeigen
        Map<String, SubcommandExecutor> availableCommands = subcommandManager.getAvailableSubcommands(player);

        for (Map.Entry<String, SubcommandExecutor> entry : availableCommands.entrySet()) {
            String commandName = entry.getKey();
            SubcommandExecutor executor = entry.getValue();

            // Überspringe Help selbst um Endlosschleife zu vermeiden
            if ("help".equals(commandName)) {
                continue;
            }

            String usage = executor.getUsage().replace("/plotaddon", "§e/plotaddon");
            String description = "§7- " + executor.getDescription();
            player.sendMessage(usage + " " + description);
            hasAnyPermission = true;
        }

        // Zeige auch plotcount Alias wenn Count-Berechtigung vorhanden
        if (subcommandManager.hasPermissionForSubcommand("count", player)) {
            player.sendMessage("§e/plotcount <entity> §7- Alias für /plotaddon count");
        }

        // Zeige konfigurierte Aliases (nur die für verfügbare Befehle)
        Set<String> aliases = plugin.getConfigManager().getAllAliases();
        if (!aliases.isEmpty()) {
            boolean hasAliases = false;
            for (String alias : aliases) {
                String target = plugin.getConfigManager().getAliasTarget(alias);
                boolean showAlias = false;

                // Prüfe ob der Spieler Berechtigung für das Ziel-Subcommand hat
                if (subcommandManager.hasPermissionForSubcommand(target, player)) {
                    showAlias = true;
                }

                if (showAlias) {
                    if (!hasAliases) {
                        player.sendMessage("§7Verfügbare Aliases:");
                        hasAliases = true;
                    }
                    player.sendMessage("§e/" + alias + " §7- Alias für /plotaddon " + target);
                }
            }
        }

        // Help ist immer verfügbar
        player.sendMessage("§e/plotaddon help §7- Zeige diese Hilfe");

        // Zeige verfügbare Entity-Gruppen wenn Count-Berechtigung vorhanden
        if (subcommandManager.hasPermissionForSubcommand("count", player)) {
            showEntityGroups(player);
        }

        // Wenn keine Berechtigung für irgendetwas vorhanden ist
        if (!hasAnyPermission) {
            player.sendMessage("§cKeine Berechtigungen für verfügbare Befehle gefunden.");
        }

        return true;
    }

    @Override
    public boolean hasPermission(Player player) {
        // Help ist für alle verfügbar
        return true;
    }

    @Override
    public String getDescription() {
        return "Zeigt diese Hilfe";
    }

    @Override
    public String getUsage() {
        return "/plotaddon help";
    }

    private void showEntityGroups(Player player) {
        // Show available entity groups based on permissions
        StringBuilder availableGroups = new StringBuilder();
        for (String group : EntityGroupManager.getAllGroups()) {
            if (!"all".equals(group) && PermissionManager.canCountGroup(player, group)) {
                if (!availableGroups.isEmpty()) {
                    availableGroups.append(", ");
                }
                availableGroups.append(plugin.getMessageManager().getGroupName(group));
            }
        }

        if (!availableGroups.isEmpty()) {
            player.sendMessage("§7Verfügbare Entity-Gruppen: §f" + availableGroups);
            player.sendMessage("§7Tipp: Nutze Tab-Completion für verfügbare Entities!");
        }
    }
}
