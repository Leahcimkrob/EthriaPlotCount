package de.leahcimkrob.ethriaPlotCount.integration;

import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.player.PlotPlayer;
import de.leahcimkrob.ethriaPlotCount.util.PermissionManager;
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
     */
    public static Set<Plot> getPlotsToCount(Plot basePlot, boolean includeMerged) {
        Set<Plot> plotsToCount = new HashSet<>();
        plotsToCount.add(basePlot);

        if (includeMerged) {
            // Füge alle gemergten Plots hinzu
            Set<Plot> connectedPlots = basePlot.getConnectedPlots();
            plotsToCount.addAll(connectedPlots);
        }

        return plotsToCount;
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
    }
}
