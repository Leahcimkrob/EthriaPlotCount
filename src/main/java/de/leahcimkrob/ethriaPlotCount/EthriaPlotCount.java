package de.leahcimkrob.ethriaPlotCount;

import com.plotsquared.core.plot.Plot;
import de.leahcimkrob.ethriaPlotCount.config.ConfigManager;
import de.leahcimkrob.ethriaPlotCount.integration.PlotSquaredIntegration;
import de.leahcimkrob.ethriaPlotCount.lang.MessageManager;
import de.leahcimkrob.ethriaPlotCount.util.DebugLogger;
import de.leahcimkrob.ethriaPlotCount.util.EntityCounter;
import de.leahcimkrob.ethriaPlotCount.util.EntityGroupManager;
import de.leahcimkrob.ethriaPlotCount.util.PermissionManager;
import de.leahcimkrob.ethriaPlotCount.util.PlotCountTabCompleter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class EthriaPlotCount extends JavaPlugin {
    private ConfigManager configManager;
    private MessageManager messageManager;
    private DebugLogger debugLogger;

    @Override
    public void onEnable() {
        // Plugin startup logic
        getLogger().info("EthriaPlotCount wird gestartet...");

        // Initialisiere Config Manager
        configManager = new ConfigManager(this);

        // Initialisiere Message Manager mit der konfigurierten Sprache
        messageManager = new MessageManager(this, configManager.getLanguage());

        // Initialisiere Debug Logger
        debugLogger = new DebugLogger(this);

        // Aktiviere Debug-Modus basierend auf Config
        debugLogger.setEnabled(configManager.isDebugEnabled());

        // Lösche alte debug.log bei Plugin-Start
        if (configManager.isDebugEnabled()) {
            debugLogger.clearLog();
            debugLogger.debug("=== EthriaPlotCount Debug-Session gestartet ===");
            debugLogger.debug("Debug-Modus aktiviert, alle Debug-Meldungen werden in debug.log geschrieben");
        }

        // Check if PlotSquared is available
        if (getServer().getPluginManager().getPlugin("PlotSquared") == null) {
            getLogger().severe(messageManager.getMessage("plotsquared_not_found"));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Registriere Command-Aliases aus der Config
        registerCommandAliases();

        // Registriere Tab-Completer für Hauptbefehl
        Objects.requireNonNull(this.getCommand("plotcount")).setTabCompleter(new PlotCountTabCompleter(messageManager));

        getLogger().info(messageManager.getMessage("plotsquared_found"));
        getLogger().info(messageManager.getMessage("plugin_enabled"));
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info(messageManager.getMessage("plugin_disabled"));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Accept main command and all configured aliases
        String commandName = command.getName().toLowerCase();
        List<String> validCommands = configManager.getCommandAliases();

        if (!commandName.equals("plotcount") &&
            (validCommands == null || !validCommands.contains(commandName))) {
            return false;
        }

        // Check if sender is player
        if (!(sender instanceof Player)) {
            sender.sendMessage(messageManager.getMessage("player_only"));
            return true;
        }

        Player player = (Player) sender;

        // Check basic permission
        if (!PermissionManager.hasBasePermission(player)) {
            player.sendMessage(messageManager.getMessage("no_permission"));
            return true;
        }

        // Handle different subcommands
        if (args.length == 0) {
            sendHelpMessage(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        // Handle reload command
        if (subCommand.equals("reload")) {
            if (!PermissionManager.canReload(player)) {
                player.sendMessage(messageManager.getMessage("no_permission"));
                return true;
            }

            reloadConfigs();
            player.sendMessage(messageManager.getMessage("config_reloaded"));
            return true;
        }

        // Handle help command
        if (subCommand.equals("help") || subCommand.equals("?")) {
            sendHelpMessage(player);
            return true;
        }

        // Handle all/star command (nur für Admins)
        if (subCommand.equals("all") || subCommand.equals("*")) {
            if (!PermissionManager.hasAdminPermission(player)) {
                player.sendMessage(messageManager.getMessage("no_permission"));
                return true;
            }

            return handleCountAllEntities(player);
        }

        // Handle entity counting
        return handleEntityCount(player, subCommand);
    }

    private boolean handleEntityCount(Player player, String entityTypeStr) {
        // Debug: Prüfe ob Spieler auf einem Plot ist
        if (configManager.isDebugEnabled()) {
            debugLogger.debug("Prüfe Plot-Zugriff für Spieler: %s (Entity: %s)", player.getName(), entityTypeStr);
        }

        com.plotsquared.core.plot.Plot plot = PlotSquaredIntegration.getPlayerPlot(player);
        if (plot == null) {
            player.sendMessage(messageManager.getMessage("not_on_plot"));
            return true;
        }

        // Debug: Zeige Plot-Info
        if (configManager.isDebugEnabled()) {
            debugLogger.debug("Plot gefunden: %s in Welt %s", plot.getId().toString(), plot.getWorldName());
        }

        // Prüfe Plot-Zugriff
        if (!PlotSquaredIntegration.hasPlotAccess(player, plot)) {
            player.sendMessage(PermissionManager.getPlotAccessError(player));
            return true;
        }

        // NEUE LOGIK: Versuche deutsche Namen zu englischen zu konvertieren
        String originalInput = entityTypeStr;
        String englishEntityType = messageManager.getEnglishEntityName(entityTypeStr);

        // Nur bei Debug-Modus loggen
        if (configManager.isDebugEnabled()) {
            debugLogger.debug("Input: '%s' -> Konvertiert zu: '%s'", originalInput, englishEntityType);
        }

        // Validiere Entity-Typ (mit englischem Namen)
        EntityType entityType;
        try {
            entityType = EntityType.valueOf(englishEntityType.toUpperCase());
        } catch (IllegalArgumentException e) {
            Map<String, String> replacements = new HashMap<>();
            replacements.put("entityType", originalInput);
            player.sendMessage(messageManager.getMessage("invalid_entity_type", replacements));
            return true;
        }

        // Prüfe Entity-Berechtigung
        if (!PermissionManager.canCountEntity(player, entityType)) {
            player.sendMessage(PermissionManager.getPermissionError(player, entityType));
            return true;
        }

        // Hole alle Plots die gezählt werden sollen
        Set<Plot> plotsToCount = PlotSquaredIntegration.getPlotsToCount(
            plot, configManager.shouldIncludeMergedPlots()
        );

        // Debug: Merge-Informationen loggen (nur einmal am Anfang)
        if (configManager.isDebugEnabled()) {
            debugLogger.debug("Spieler %s verwendet Befehl '/plotcount %s' auf Plot %s",
                    player.getName(), entityTypeStr, plot.getId().toString());
            PlotSquaredIntegration.debugMergeInfo(plotsToCount, debugLogger);

            // Debug: Zeige Merged-Plot Info
            if (configManager.shouldIncludeMergedPlots() && plotsToCount.size() > 1) {
                debugLogger.debug("Inklusive %d gemergete(r) Plot(s)", plotsToCount.size() - 1);
            }
        }

        // Zeige angemessene Zähl-Nachricht
        // Verwende deutsche Übersetzung für Anzeige (vom englischen Namen)
        String entityName = messageManager.getEntityName(englishEntityType);
        Map<String, String> countingMsg = new HashMap<>();
        countingMsg.put("entityType", entityName);

        if (plotsToCount.size() > 1) {
            countingMsg.put("plotCount", String.valueOf(plotsToCount.size()));
            player.sendMessage(messageManager.getMessage("counting_entities_large", countingMsg));
        } else {
            player.sendMessage(messageManager.getMessage("counting_entities", countingMsg));
        }

        // Führe Zählung durch (synchron, da Entity-Zugriff Main-Thread erfordert)
        EntityCounter.CountResult result = EntityCounter.countWithDetails(
            plotsToCount, entityType, configManager, debugLogger
        );

        // Zeige Ergebnis
        displayCountResult(player, result, entityName, plotsToCount.size());

        return true;
    }

    /**
     * Zählt alle Entities für Admins
     */
    private boolean handleCountAllEntities(Player player) {
        // Debug: Prüfe ob Spieler auf einem Plot ist
        if (configManager.isDebugEnabled()) {
            debugLogger.debug("Prüfe Plot-Zugriff für Spieler: %s (plotcount all)", player.getName());
        }

        com.plotsquared.core.plot.Plot plot = PlotSquaredIntegration.getPlayerPlot(player);
        if (plot == null) {
            player.sendMessage(messageManager.getMessage("not_on_plot"));
            return true;
        }

        // Debug: Zeige Plot-Info
        if (configManager.isDebugEnabled()) {
            debugLogger.debug("Plot gefunden für plotcount all: %s in Welt %s", plot.getId().toString(), plot.getWorldName());
        }

        // Hole alle Plots die gezählt werden sollen
        Set<com.plotsquared.core.plot.Plot> plotsToCount = PlotSquaredIntegration.getPlotsToCount(
            plot, configManager.shouldIncludeMergedPlots()
        );

        // Debug: Zeige Merged-Plot Info
        if (configManager.shouldIncludeMergedPlots() && plotsToCount.size() > 1) {
            if (configManager.isDebugEnabled()) {
                debugLogger.debug("plotcount all: Inklusive %d gemergete(r) Plot(s)", plotsToCount.size() - 1);
            }
        }

        // Zeige entsprechenden Header basierend auf Plot-Count
        if (configManager.shouldIncludeMergedPlots() && plotsToCount.size() > 1) {
            Map<String, String> headerReplacements = new HashMap<>();
            headerReplacements.put("plotCount", String.valueOf(plotsToCount.size()));
            player.sendMessage(messageManager.getMessage("counting_all_header_merged", headerReplacements));
        } else {
            player.sendMessage(messageManager.getMessage("counting_all_header"));
        }

        // Zähle alle Entity-Typen
        Map<EntityType, Integer> entityCounts = new HashMap<>();
        int totalEntities = 0;

        // Durchsuche alle EntityTypes
        for (EntityType entityType : EntityType.values()) {
            // Überspringe technische Entities
            if (isSkippableEntityType(entityType)) {
                continue;
            }

            try {
                EntityCounter.CountResult result = EntityCounter.countWithDetails(
                    plotsToCount, entityType, configManager, debugLogger
                );

                int count = result.getTotalCount();
                if (count > 0) {
                    entityCounts.put(entityType, count);
                    totalEntities += count;
                }
            } catch (Exception e) {
                // Stille Fehlerbehandlung für einzelne Entity-Typen
                getLogger().fine("Fehler beim Zählen von " + entityType + ": " + e.getMessage());
            }
        }

        // Zeige Ergebnisse
        if (entityCounts.isEmpty()) {
            player.sendMessage(messageManager.getMessage("counting_all_no_entities"));
        } else {
            // Sortiere nach Anzahl (absteigend)
            entityCounts.entrySet().stream()
                .sorted(Map.Entry.<EntityType, Integer>comparingByValue().reversed())
                .forEach(entry -> {
                    EntityType type = entry.getKey();
                    int count = entry.getValue();
                    String deutscherName = messageManager.getEntityName(type.name().toLowerCase());

                    sendPrefixedMessage(player, String.format("§e%s: §f%d", deutscherName, count));
                });

            Map<String, String> totalReplacements = new HashMap<>();
            totalReplacements.put("total", String.valueOf(totalEntities));
            player.sendMessage(messageManager.getMessage("counting_all_total", totalReplacements));
        }

        return true;
    }

    /**
     * Prüft ob ein EntityType übersprungen werden soll
     */
    private boolean isSkippableEntityType(EntityType entityType) {
        // Überspringe Player und technische/problematische Entity-Typen
        String name = entityType.name();
        return name.equals("PLAYER") ||
               name.equals("UNKNOWN") ||
               name.equals("FISHING_HOOK") ||
               name.equals("LIGHTNING") ||
               name.equals("WEATHER") ||
               name.equals("COMPLEX_PART") ||
               name.equals("OTHER");
    }

    private void sendHelpMessage(Player player) {
        player.sendMessage(messageManager.getMessage("help_header"));
        player.sendMessage(messageManager.getMessage("help_count"));

        // Admin-Befehle
        if (PermissionManager.hasAdminPermission(player)) {
            player.sendMessage(messageManager.getMessage("help_admin_all"));
            player.sendMessage(messageManager.getMessage("help_admin_star"));
        }

        if (PermissionManager.canReload(player)) {
            player.sendMessage(messageManager.getMessage("help_reload"));
        }

        // Show available aliases
        List<String> aliases = configManager.getCommandAliases();
        if (aliases != null && !aliases.isEmpty()) {
            Map<String, String> aliasReplacements = new HashMap<>();
            aliasReplacements.put("aliases", String.join(", ", aliases));
            player.sendMessage(messageManager.getMessage("available_aliases", aliasReplacements));
        }

        // Show available entity groups based on permissions
        StringBuilder availableGroups = new StringBuilder();
        for (String group : EntityGroupManager.getAllGroups()) {
            if (!"all".equals(group) && PermissionManager.canCountGroup(player, group)) {
                if (availableGroups.length() > 0) {
                    availableGroups.append(", ");
                }
                availableGroups.append(messageManager.getGroupName(group));
            }
        }

        if (availableGroups.length() == 0) {
            availableGroups.append("Keine (fehlende Berechtigungen)");
        }

        Map<String, String> replacements = new HashMap<>();
        replacements.put("types", availableGroups.toString());
        player.sendMessage(messageManager.getMessage("help_footer", replacements));

        // Tab-Completion Hinweis
        player.sendMessage(messageManager.getMessage("help_tab_completion"));
    }

    private void reloadConfigs() {
        configManager.reloadConfig();
        messageManager.setLanguage(configManager.getLanguage());

        // Re-register aliases after config reload
        registerCommandAliases();
    }

    private void registerCommandAliases() {
        try {
            List<String> aliases = configManager.getCommandAliases();
            if (aliases != null && !aliases.isEmpty()) {
                // Register each alias as a separate command that delegates to the main command
                for (String alias : aliases) {
                    if (alias != null && !alias.trim().isEmpty()) {
                        // Use reflection to register command dynamically
                        registerDynamicCommand(alias.trim());
                    }
                }
                getLogger().info("Registrierte " + aliases.size() + " Command-Aliases: " + String.join(", ", aliases));
            }
        } catch (Exception e) {
            getLogger().warning("Fehler beim Registrieren der Command-Aliases: " + e.getMessage());
        }
    }

    private void registerDynamicCommand(String alias) {
        try {
            // Create a simple command that delegates to our onCommand method
            org.bukkit.command.Command aliasCommand = new org.bukkit.command.Command(alias) {
                @Override
                public boolean execute(org.bukkit.command.CommandSender sender, String commandLabel, String[] args) {
                    // Delegate to our main onCommand method
                    return onCommand(sender, this, commandLabel, args);
                }

                @Override
                public List<String> tabComplete(org.bukkit.command.CommandSender sender, String commandLabel, String[] args) {
                    // Delegate to our TabCompleter
                    PlotCountTabCompleter tabCompleter = new PlotCountTabCompleter(messageManager);
                    return tabCompleter.onTabComplete(sender, this, commandLabel, args);
                }
            };

            // Set properties
            aliasCommand.setDescription("Alias für /plotcount");
            aliasCommand.setUsage("/" + alias + " <entitytype>");
            aliasCommand.setPermission("ethriaplotcount.use");

            // Register the command
            getServer().getCommandMap().register("ethriaplotcount", aliasCommand);

        } catch (Exception e) {
            getLogger().warning("Fehler beim Registrieren des Alias '" + alias + "': " + e.getMessage());
        }
    }

    private void displayCountResult(Player player, EntityCounter.CountResult result, String entityName, int plotCount) {
        Map<String, String> replacements = new HashMap<>();

        // Berechne gefilterte Anzahl basierend auf Config
        int displayCount = result.getFilteredCount(configManager);

        replacements.put("count", String.valueOf(displayCount));
        replacements.put("entityName", entityName);

        // Prüfe ob Limit erreicht wurde
        if (result.isLimitReached()) {
            replacements.put("limit", String.valueOf(configManager.getMaxCountLimit()));
            player.sendMessage(messageManager.getMessage("count_limit_reached", replacements));
        }

        // Hauptergebnis
        if (configManager.shouldIncludeMergedPlots() && plotCount > 1) {
            replacements.put("plotCount", String.valueOf(plotCount));
            player.sendMessage(messageManager.getMessage("count_result_merged", replacements));
        } else {
            player.sendMessage(messageManager.getMessage("count_result", replacements));
        }

        // Detaillierte Aufschlüsselung (nur wenn interessant)
        if (result.getInvisibleCount() > 0 || result.getFixedCount() > 0) {
            Map<String, String> detailReplacements = new HashMap<>();
            detailReplacements.put("count", String.valueOf(result.getTotalCount()));
            detailReplacements.put("entityName", entityName);
            detailReplacements.put("visible", String.valueOf(result.getVisibleCount()));
            detailReplacements.put("invisible", String.valueOf(result.getInvisibleCount()));
            detailReplacements.put("fixed", String.valueOf(result.getFixedCount()));

            player.sendMessage(messageManager.getMessage("count_result_detailed", detailReplacements));
        }
    }

    /**
     * Zählt Entities asynchron mit periodischen Main-Thread-Updates für große Plots
     */
    private void countEntitiesAsync(Player player, Set<com.plotsquared.core.plot.Plot> plots,
                                   EntityType entityType, String entityName) {

        // Verwende einen BukkitRunnable für kontrollierten asynchronen Zugriff
        new org.bukkit.scheduler.BukkitRunnable() {
            private EntityCounter.CountResult result = null;

            @Override
            public void run() {
                try {
                    // Diese Zählung läuft im Main-Thread (scheduler.runTask)
                    result = EntityCounter.countWithDetails(plots, entityType, configManager, debugLogger);

                    // Zeige Ergebnis sofort
                    displayCountResult(player, result, entityName, plots.size());

                } catch (Exception e) {
                    // Fallback bei Fehlern
                    Map<String, String> errorMsg = new HashMap<>();
                    errorMsg.put("error", e.getMessage());
                    player.sendMessage(messageManager.getMessage("error_occurred", errorMsg));
                    getLogger().severe("Fehler bei Entity-Zählung: " + e.getMessage());
                    e.printStackTrace();
                }
            }

        }.runTask(this); // Läuft im Main-Thread
    }

    // Getter für andere Klassen
    public ConfigManager getConfigManager() {
        return configManager;
    }

    public MessageManager getMessageManager() {
        return messageManager;
    }

    /**
     * Getter für DebugLogger
     */
    public DebugLogger getDebugLogger() {
        return debugLogger;
    }

    /**
     * Sendet eine Nachricht mit Prefix an den Player
     */
    private void sendPrefixedMessage(Player player, String message) {
        String prefix = messageManager.getMessage("prefix");
        player.sendMessage(prefix + org.bukkit.ChatColor.translateAlternateColorCodes('&', message));
    }
}
