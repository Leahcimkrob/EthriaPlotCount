package de.leahcimkrob.ethriaPlotCount.util;

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

    public DebugLogger(JavaPlugin plugin) {
        this.plugin = plugin;
        this.debugFile = new File(plugin.getDataFolder(), "debug.log");
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

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
     * Loggt eine Debug-Nachricht in die debug.log Datei
     */
    public void debug(String message) {
        try (FileWriter writer = new FileWriter(debugFile, true)) {
            String timestamp = dateFormat.format(new Date());
            writer.write(String.format("[%s] [DEBUG] %s%n", timestamp, message));
            writer.flush();
        } catch (IOException e) {
            plugin.getLogger().warning("Konnte nicht in debug.log schreiben: " + e.getMessage());
        }
    }

    /**
     * Loggt eine Debug-Nachricht mit Formatierung
     */
    public void debug(String format, Object... args) {
        debug(String.format(format, args));
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
