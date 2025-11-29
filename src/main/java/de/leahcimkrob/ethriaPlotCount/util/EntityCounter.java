package de.leahcimkrob.ethriaPlotCount.util;

import de.leahcimkrob.ethriaPlotCount.config.ConfigManager;
import de.leahcimkrob.ethriaPlotCount.integration.PlotSquaredIntegration;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.inventory.InventoryHolder;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class EntityCounter {

    /**
     * Zählt Entities eines bestimmten Typs auf den angegebenen Plots
     * Verwendet eine einzige optimierte Schleife um Dopplungen zu vermeiden
     */
    public static int countEntitiesOnPlots(
            Set<com.plotsquared.core.plot.Plot> plots,
            EntityType entityType,
            ConfigManager config) {

        AtomicInteger totalCount = new AtomicInteger(0);
        int maxLimit = config.getMaxCountLimit();

        // Verwende eine Set um bereits geprüfte Entities zu verfolgen
        Set<Entity> alreadyCounted = new HashSet<>();

        for (com.plotsquared.core.plot.Plot plot : plots) {
            // Stoppe wenn Limit erreicht
            if (totalCount.get() >= maxLimit) {
                break;
            }

            PlotSquaredIntegration.PlotBounds bounds = PlotSquaredIntegration.getPlotBounds(plot);
            World world = org.bukkit.Bukkit.getWorld(bounds.getWorldName());

            if (world == null) {
                continue;
            }

            // Berechne chunk-Bereiche für den Plot mit erweiterten Grenzen
            int minChunkX = (bounds.getMinX() - 1) >> 4; // Erweitere um 1 Block
            int maxChunkX = (bounds.getMaxX() + 1) >> 4;
            int minChunkZ = (bounds.getMinZ() - 1) >> 4;
            int maxChunkZ = (bounds.getMaxZ() + 1) >> 4;

            // Iteriere durch alle Chunks die den Plot überlappen
            for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
                for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                    // Stoppe wenn Limit erreicht
                    if (totalCount.get() >= maxLimit) {
                        break;
                    }

                    // Prüfe ob Chunk geladen ist
                    if (!world.isChunkLoaded(chunkX, chunkZ)) {
                        continue;
                    }

                    org.bukkit.Chunk chunk = world.getChunkAt(chunkX, chunkZ);

                    // Zähle Entities in diesem Chunk
                    for (Entity entity : chunk.getEntities()) {
                        // Stoppe wenn Limit erreicht
                        if (totalCount.get() >= maxLimit) {
                            break;
                        }

                        // Prüfe Entity-Typ
                        if (entity.getType() != entityType) {
                            continue;
                        }

                        // Verhindere Doppelzählung
                        if (alreadyCounted.contains(entity)) {
                            continue;
                        }

                        // Prüfe ob Entity in Plot-Grenzen ist
                        if (!bounds.contains(entity.getLocation())) {
                            continue;
                        }

                        // Filtere basierend auf Config-Einstellungen
                        if (!shouldCountEntity(entity, config)) {
                            continue;
                        }

                        alreadyCounted.add(entity);
                        totalCount.incrementAndGet();
                    }
                }
            }
        }

        return totalCount.get();
    }

    /**
     * Prüft ob eine Entity gezählt werden soll basierend auf Config-Einstellungen
     */
    private static boolean shouldCountEntity(Entity entity, ConfigManager config) {
        // Items auf dem Boden
        if (entity instanceof Item) {
            return config.shouldCountDroppedItems();
        }

        // Unsichtbare Entities
        if (!config.shouldCountInvisible() && isInvisible(entity)) {
            return false;
        }

        // Fixierte Entities
        if (!config.shouldCountFixed() && isFixed(entity)) {
            return false;
        }

        return true;
    }

    /**
     * Prüft ob eine Entity unsichtbar ist
     */
    private static boolean isInvisible(Entity entity) {
        // ArmorStands können unsichtbar sein
        if (entity instanceof ArmorStand) {
            return !((ArmorStand) entity).isVisible();
        }

        // Andere Living Entities mit Unsichtbarkeits-Potion
        if (entity instanceof LivingEntity) {
            LivingEntity living = (LivingEntity) entity;
            return living.hasPotionEffect(org.bukkit.potion.PotionEffectType.INVISIBILITY);
        }

        return false;
    }

    /**
     * Prüft ob eine Entity fixiert ist
     */
    private static boolean isFixed(Entity entity) {
        // ItemFrames können fixiert sein
        if (entity instanceof ItemFrame) {
            return ((ItemFrame) entity).isFixed();
        }

        // ArmorStands können fixiert sein
        if (entity instanceof ArmorStand) {
            ArmorStand stand = (ArmorStand) entity;
            return !stand.hasArms() && !stand.hasBasePlate() && stand.isMarker();
        }

        return false;
    }

    /**
     * Erstellt eine detaillierte Zähl-Statistik
     * Verwendet optimierte Merge-Area-Berechnung für bessere Abdeckung der Straßen
     */
    public static CountResult countWithDetails(
            Set<com.plotsquared.core.plot.Plot> plots,
            EntityType entityType,
            ConfigManager config,
            DebugLogger debugLogger) {

        if (plots.isEmpty()) {
            return new CountResult(0, 0, 0, 0, 0, false, 0);
        }

        // Neue Logik: Bei mehreren Plots verwende eine einzige Merge-Area
        if (plots.size() > 1) {
            // Für Merge: Verwende eine große Bounding Box die alle Plots und Straßen umfasst
            PlotSquaredIntegration.PlotBounds mergeBounds = PlotSquaredIntegration.getTotalMergeBounds(plots);

            if (mergeBounds == null) {
                return new CountResult(0, 0, 0, 0, 0, false, 0);
            }

            // Debug: Merge-Area Information nur einmal ausgeben
            if (config.isDebugEnabled() && debugLogger != null) {
                debugLogger.debug("=== MERGE AREA COUNTING ===");
                debugLogger.debug("Merge-Bereich: %s", mergeBounds.toString());
                debugLogger.debug("Anzahl Plots im Merge: %d", plots.size());
            }

            // Zähle in der gesamten Merge-Area
            CountResult mergeResult = countInArea(mergeBounds, entityType, config, debugLogger);

            if (config.isDebugEnabled() && debugLogger != null) {
                debugLogger.debug("=== MERGE COUNTING ABGESCHLOSSEN ===");
                debugLogger.debug("Gesamtergebnis: %d Entities gefunden", mergeResult.getTotalCount());
            }

            return new CountResult(
                mergeResult.getTotalCount(),
                mergeResult.getAllFoundCount(),
                mergeResult.getVisibleCount(),
                mergeResult.getInvisibleCount(),
                mergeResult.getFixedCount(),
                mergeResult.isLimitReached(),
                plots.size()
            );

        } else {
            // Für einzelne Plots: Verwende die alte Methode
            com.plotsquared.core.plot.Plot singlePlot = plots.iterator().next();
            PlotSquaredIntegration.PlotBounds bounds = PlotSquaredIntegration.getPlotBounds(singlePlot);

            // Debug: Einzelplot-Information nur einmal ausgeben
            if (config.isDebugEnabled() && debugLogger != null) {
                debugLogger.debug("=== EINZELPLOT COUNTING ===");
                debugLogger.debug("Plot-Bereich: %s", bounds.toString());
            }

            CountResult singleResult = countInArea(bounds, entityType, config, debugLogger);

            if (config.isDebugEnabled() && debugLogger != null) {
                debugLogger.debug("=== EINZELPLOT COUNTING ABGESCHLOSSEN ===");
                debugLogger.debug("Ergebnis: %d Entities gefunden", singleResult.getTotalCount());
            }

            return new CountResult(
                singleResult.getTotalCount(),
                singleResult.getAllFoundCount(),
                singleResult.getVisibleCount(),
                singleResult.getInvisibleCount(),
                singleResult.getFixedCount(),
                singleResult.isLimitReached(),
                1
            );
        }
    }

    /**
     * Zählt Entities in einem bestimmten Bereich
     */
    private static CountResult countInArea(
            PlotSquaredIntegration.PlotBounds bounds,
            EntityType entityType,
            ConfigManager config,
            DebugLogger debugLogger) {

        int totalCount = 0;
        int allFoundCount = 0;
        int visibleCount = 0;
        int invisibleCount = 0;
        int fixedCount = 0;
        int maxLimit = config.getMaxCountLimit();
        boolean limitReached = false;

        World world = org.bukkit.Bukkit.getWorld(bounds.getWorldName());
        if (world == null) {
            return new CountResult(0, 0, 0, 0, 0, false, 0);
        }

        // Berechne chunk-Bereiche für den Bereich mit erweiterten Grenzen
        int minChunkX = (bounds.getMinX() - 1) >> 4;
        int maxChunkX = (bounds.getMaxX() + 1) >> 4;
        int minChunkZ = (bounds.getMinZ() - 1) >> 4;
        int maxChunkZ = (bounds.getMaxZ() + 1) >> 4;

        // Debug: Chunk-Bereiche loggen
        if (config.isDebugEnabled() && debugLogger != null) {
            debugLogger.debug("Chunk-Bereich: X=%d bis %d, Z=%d bis %d", minChunkX, maxChunkX, minChunkZ, maxChunkZ);
        }

        int chunksChecked = 0;
        int totalEntitiesChecked = 0;

        // Iteriere durch alle Chunks die den Bereich überlappen
        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                if (totalCount >= maxLimit) {
                    limitReached = true;
                    break;
                }

                // Prüfe ob Chunk geladen ist
                if (!world.isChunkLoaded(chunkX, chunkZ)) {
                    continue;
                }

                chunksChecked++;
                org.bukkit.Chunk chunk = world.getChunkAt(chunkX, chunkZ);

                for (Entity entity : chunk.getEntities()) {
                    totalEntitiesChecked++;

                    if (totalCount >= maxLimit) {
                        limitReached = true;
                        break;
                    }

                    if (entity.getType() != entityType) continue;

                    allFoundCount++;

                    // Prüfe ob Entity in Bounds ist
                    if (!bounds.contains(entity.getLocation())) {
                        continue;
                    }

                    // Kategorisiere Entity
                    boolean isEntityInvisible = isInvisible(entity);
                    boolean isEntityFixed = isFixed(entity);

                    if (isEntityInvisible) {
                        invisibleCount++;
                    } else {
                        visibleCount++;
                    }

                    if (isEntityFixed) {
                        fixedCount++;
                    }

                    // Prüfe ob Entity gezählt werden soll
                    if (shouldCountEntity(entity, config)) {
                        totalCount++;

                        // Debug: Entity-Position loggen
                        if (config.isDebugEnabled() && debugLogger != null) {
                            org.bukkit.Location loc = entity.getLocation();
                            debugLogger.debug("Entity %s gefunden bei (%.2f, %.2f, %.2f)",
                                    entity.getType(), loc.getX(), loc.getY(), loc.getZ());
                        }
                    }
                }

                if (limitReached) break;
            }
            if (limitReached) break;
        }

        // Debug-Ausgabe
        if (config.isDebugEnabled() && debugLogger != null) {
            debugLogger.debug("Zählstatistik: %d Chunks geprüft, %d Entities untersucht, %d gefunden, %d gezählt",
                chunksChecked, totalEntitiesChecked, allFoundCount, totalCount);
            if (limitReached) {
                debugLogger.debug("WARNUNG: Zähl-Limit von %d erreicht!", maxLimit);
            }
        }

        return new CountResult(totalCount, allFoundCount, visibleCount, invisibleCount, fixedCount, limitReached, 0);
    }

    /**
     * Prüft ob eine Entity nahe an Plot-Grenzen ist (für Debug)
     */
    private static boolean isNearBorder(org.bukkit.Location loc, PlotSquaredIntegration.PlotBounds bounds) {
        double x = loc.getX();
        double z = loc.getZ();
        double margin = 1.0; // 1 Block Rand

        return x <= bounds.getMinX() + margin || x >= bounds.getMaxX() - margin ||
               z <= bounds.getMinZ() + margin || z >= bounds.getMaxZ() - margin;
    }


    /**
     * Prüft ob eine Entity auf irgendeinem der angegebenen Plots steht
     */
    private static boolean isEntityOnAnyPlot(Entity entity, Set<com.plotsquared.core.plot.Plot> plots) {
        org.bukkit.Location entityLoc = entity.getLocation();

        for (com.plotsquared.core.plot.Plot plot : plots) {
            PlotSquaredIntegration.PlotBounds bounds = PlotSquaredIntegration.getPlotBounds(plot);
            if (bounds.contains(entityLoc)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Ergebnis einer detaillierten Zählung
     */
    public static class CountResult {
        private final int totalCount;      // Gefilterte Anzahl (nach Config)
        private final int allFoundCount;   // Alle gefundenen (vor Filter)
        private final int visibleCount;
        private final int invisibleCount;
        private final int fixedCount;
        private final boolean limitReached;
        private final int plotCount;

        public CountResult(int totalCount, int allFoundCount, int visibleCount, int invisibleCount,
                          int fixedCount, boolean limitReached, int plotCount) {
            this.totalCount = totalCount;
            this.allFoundCount = allFoundCount;
            this.visibleCount = visibleCount;
            this.invisibleCount = invisibleCount;
            this.fixedCount = fixedCount;
            this.limitReached = limitReached;
            this.plotCount = plotCount;
        }

        // Getters
        public int getTotalCount() { return totalCount; }
        public int getAllFoundCount() { return allFoundCount; }
        public int getVisibleCount() { return visibleCount; }
        public int getInvisibleCount() { return invisibleCount; }
        public int getFixedCount() { return fixedCount; }
        public boolean isLimitReached() { return limitReached; }
        public int getPlotCount() { return plotCount; }

        /**
         * Berechnet die Anzahl basierend auf Config-Einstellungen
         * Die Filterung wurde bereits in countWithDetails() angewendet
         */
        public int getFilteredCount(ConfigManager config) {
            // Die totalCount enthält bereits alle gefilterten Entities
            // basierend auf den Config-Einstellungen
            return totalCount;
        }
    }
}
