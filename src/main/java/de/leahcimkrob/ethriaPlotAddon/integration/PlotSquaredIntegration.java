package de.leahcimkrob.ethriaPlotAddon.integration;

import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.player.PlotPlayer;
import de.leahcimkrob.ethriaPlotAddon.util.PermissionManager;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class PlotSquaredIntegration {

    /**
     * Prüft ob ein Spieler sich auf einem Plot befindet
     */
    public static Plot getPlayerPlot(Player player) {
        org.bukkit.Location bukkitLoc = player.getLocation();
        Location plotLocation = Location.at(
            bukkitLoc.getWorld().getName(),
            bukkitLoc.getBlockX(),
            bukkitLoc.getBlockY(),
            bukkitLoc.getBlockZ()
        );

        return plotLocation.getPlot();
    }

    /**
     * Prüft ob ein Spieler Zugriff auf einen Plot hat
     */
    public static boolean hasPlotAccess(Player player, Plot plot) {
        // Admin kann alles
        if (PermissionManager.hasAdminPermission(player)) {
            return true;
        }

        // Andere Plots nur mit entsprechender Berechtigung
        if (PermissionManager.canCountOnOtherPlots(player)) {
            return true;
        }

        // Prüfe ob Spieler Zugriff auf eigene/trusted/added Plots hat
        if (!PermissionManager.canCountOnOwnPlots(player)) {
            return false;
        }

        UUID playerUUID = player.getUniqueId();

        // Ist der Spieler Owner?
        if (plot.getOwners().contains(playerUUID)) {
            return true;
        }

        // Ist der Spieler trusted?
        if (plot.getTrusted().contains(playerUUID)) {
            return true;
        }

        // Ist der Spieler added? (Nur wenn Owner online ist)
        if (plot.getMembers().contains(playerUUID)) {
            // Prüfe ob mindestens ein Owner online ist
            for (UUID ownerUUID : plot.getOwners()) {
                Player owner = player.getServer().getPlayer(ownerUUID);
                if (owner != null && owner.isOnline()) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Holt alle Plots die gezählt werden sollen (einzeln oder mit merged)
     * Verwendet die korrekte PlotSquared-Methode um auch Straßen zwischen Plots zu erfassen
     */
    public static Set<Plot> getPlotsToCount(Plot basePlot, boolean includeMerged) {
        Set<Plot> plotsToCount = new HashSet<>();

        if (includeMerged) {
            // Verwende getConnectedPlots() um alle verbundenen Plots inklusive Straßen zu bekommen
            Set<Plot> connectedPlots = basePlot.getConnectedPlots();
            plotsToCount.addAll(connectedPlots);

            // Stelle sicher, dass der Basis-Plot enthalten ist
            plotsToCount.add(basePlot);
        } else {
            // Nur der einzelne Plot
            plotsToCount.add(basePlot);
        }

        return plotsToCount;
    }

    /**
     * Berechnet die gesamte Bounding Box für alle Plots in einem Merge
     * Dies schließt auch die Straßen zwischen den Plots ein
     */
    public static PlotBounds getTotalMergeBounds(Set<Plot> plots) {
        if (plots.isEmpty()) {
            return null;
        }

        String worldName = null;
        int minX = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
        int minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;

        for (Plot plot : plots) {
            PlotBounds plotBounds = getPlotBounds(plot);

            if (worldName == null) {
                worldName = plotBounds.getWorldName();
            }

            minX = Math.min(minX, plotBounds.getMinX());
            minZ = Math.min(minZ, plotBounds.getMinZ());
            maxX = Math.max(maxX, plotBounds.getMaxX());
            maxZ = Math.max(maxZ, plotBounds.getMaxZ());
            minY = Math.min(minY, plotBounds.getMinY());
            maxY = Math.max(maxY, plotBounds.getMaxY());
        }

        return new PlotBounds(worldName, minX, minZ, maxX, maxZ, minY, maxY);
    }

    /**
     * Loggt Debug-Informationen über alle Plots in einem Merge
     */
    public static void debugMergeInfo(Set<Plot> plots, de.leahcimkrob.ethriaPlotAddon.util.DebugLogger debugLogger) {
        if (debugLogger == null) return;

        debugLogger.debug("=== MERGE DEBUG INFO ===");
        debugLogger.debug("Anzahl Plots im Merge: %d", plots.size());

        int minX = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;

        for (Plot plot : plots) {
            PlotBounds bounds = getPlotBounds(plot);
            debugLogger.debug("  Plot %s: %s", plot.getId().toString(), bounds.toString());

            // Berechne Gesamt-Bounding-Box
            minX = Math.min(minX, bounds.getMinX());
            minZ = Math.min(minZ, bounds.getMinZ());
            maxX = Math.max(maxX, bounds.getMaxX());
            maxZ = Math.max(maxZ, bounds.getMaxZ());
        }

        debugLogger.debug("Gesamt-Bereich des Merges: X=%d bis %d (%d Blöcke), Z=%d bis %d (%d Blöcke)",
                minX, maxX, (maxX - minX + 1), minZ, maxZ, (maxZ - minZ + 1));
        debugLogger.debug("=== ENDE MERGE DEBUG ===");
    }

    /**
     * Holt die Bounding Box für einen Plot
     */
    public static PlotBounds getPlotBounds(Plot plot) {
        com.plotsquared.core.location.Location min = plot.getBottomAbs();
        com.plotsquared.core.location.Location max = plot.getTopAbs();

        return new PlotBounds(
            min.getWorldName(),
            min.getX(),
            min.getZ(),
            max.getX(),
            max.getZ(),
            plot.getArea().getMinGenHeight(),
            plot.getArea().getMaxGenHeight()
        );
    }

    /**
     * Hilfklasse für Plot-Grenzen
     */
    public static class PlotBounds {
        private final String worldName;
        private final int minX, minZ, maxX, maxZ;
        private final int minY, maxY;

        public PlotBounds(String worldName, int minX, int minZ, int maxX, int maxZ, int minY, int maxY) {
            this.worldName = worldName;
            this.minX = minX;
            this.minZ = minZ;
            this.maxX = maxX;
            this.maxZ = maxZ;
            this.minY = minY;
            this.maxY = maxY;
        }

        public String getWorldName() { return worldName; }
        public int getMinX() { return minX; }
        public int getMinZ() { return minZ; }
        public int getMaxX() { return maxX; }
        public int getMaxZ() { return maxZ; }
        public int getMinY() { return minY; }
        public int getMaxY() { return maxY; }

        public boolean contains(org.bukkit.Location location) {
            if (!worldName.equals(location.getWorld().getName())) {
                return false;
            }

            // Verwende Dezimal-Positionen für präzise Grenzprüfung
            double x = location.getX();
            double z = location.getZ();
            double y = location.getY();

            // PlotSquared-kompatible Grenzen: minX/Z inclusive, maxX/Z+1 exclusive
            // Füge kleine Toleranz hinzu für Floating-Point-Präzision
            double epsilon = 0.0001; // Sehr kleine Toleranz

            boolean xInBounds = x >= (minX - epsilon) && x < (maxX + 1 + epsilon);
            boolean zInBounds = z >= (minZ - epsilon) && z < (maxZ + 1 + epsilon);
            boolean yInBounds = y >= minY && y <= maxY;

            return xInBounds && zInBounds && yInBounds;
        }

        @Override
        public String toString() {
            return String.format("PlotBounds[world=%s, x=%d-%d (%d), z=%d-%d (%d), y=%d-%d]",
                    worldName, minX, maxX, (maxX - minX + 1),
                    minZ, maxZ, (maxZ - minZ + 1), minY, maxY);
        }
    }
}

