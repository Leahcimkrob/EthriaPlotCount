package de.leahcimkrob.ethriaPlotAddon.util;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DebugLogger {

    private final JavaPlugin plugin;
    private final File debugFile;
    private final SimpleDateFormat dateFormat;
    private boolean isEnabled;

    public DebugLogger(JavaPlugin plugin) {
        this.plugin = plugin;
        this.debugFile = new File(plugin.getDataFolder(), "debug.log");
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        this.isEnabled = true; // Standardmäßig aktiviert

        // Erstelle debug.log falls sie nicht existiert
        try {
            if (!debugFile.exists()) {
                debugFile.getParentFile().mkdirs();
                debugFile.createNewFile();
            }
        } catch (IOException e) {
            plugin.getLogger().warning("Konnte debug.log nicht erstellen: " + e.getMessage());
        }
    }

    /**
     * Aktiviert oder deaktiviert den Debug Logger
     */
    public void setEnabled(boolean enabled) {
        this.isEnabled = enabled;
        if (enabled) {
            debug("Debug Logger aktiviert");
        }
    }

    /**
     * Prüft ob Debug aktiviert ist
     */
    public boolean isEnabled() {
        return isEnabled;
    }

    /**
     * Loggt eine Debug-Nachricht in die debug.log Datei
     */
    public void debug(String message) {
        if (!isEnabled) {
            return;
        }

        try (FileWriter writer = new FileWriter(debugFile, true)) {
            String timestamp = dateFormat.format(new Date());
            writer.write(String.format("[%s] [DEBUG] %s%n", timestamp, message));
            writer.flush();
        } catch (IOException e) {
            plugin.getLogger().warning("Konnte nicht in debug.log schreiben: " + e.getMessage());
            // Fallback: Schreibe in die normale Console
            plugin.getLogger().info("[DEBUG] " + message);
        }
    }

    /**
     * Loggt eine Debug-Nachricht mit Formatierung
     */
    public void debug(String format, Object... args) {
        if (!isEnabled) {
            return;
        }
        debug(String.format(format, args));
    }

    /**
     * Loggt Plot-Informationen mit Koordinaten
     */
    public void debugPlotInfo(String plotId, int minX, int minZ, int maxX, int maxZ, String worldName) {
        debug("Plot %s [Welt: %s] | X: %d bis %d (%d Blöcke breit) | Z: %d bis %d (%d Blöcke tief)",
              plotId, worldName, minX, maxX, (maxX - minX + 1), minZ, maxZ, (maxZ - minZ + 1));
    }

    /**
     * Löscht die debug.log Datei (für Neustart)
     */
    public void clearLog() {
        try {
            if (debugFile.exists()) {
                debugFile.delete();
                debugFile.createNewFile();
            }
        } catch (IOException e) {
            plugin.getLogger().warning("Konnte debug.log nicht zurücksetzen: " + e.getMessage());
        }
    }
}

