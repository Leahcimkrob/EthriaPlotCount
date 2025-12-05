package de.leahcimkrob.ethriaPlotAddon.commands;

import org.bukkit.entity.Player;

/**
 * Interface für alle Subcommand-Executoren
 */
public interface SubcommandExecutor {
    
    /**
     * Führt das Subcommand aus
     * @param player Der Spieler, der den Befehl ausführt
     * @param args Die Argumente des Subcommands
     * @return true wenn der Befehl erfolgreich verarbeitet wurde
     */
    boolean execute(Player player, String[] args);
    
    /**
     * Prüft ob der Spieler berechtigt ist, dieses Subcommand zu verwenden
     * @param player Der zu prüfende Spieler
     * @return true wenn berechtigt
     */
    boolean hasPermission(Player player);
    
    /**
     * Gibt die Beschreibung des Subcommands zurück
     * @return Beschreibung für die Hilfe
     */
    String getDescription();
    
    /**
     * Gibt die Usage-Syntax zurück
     * @return Usage-String
     */
    String getUsage();
}
