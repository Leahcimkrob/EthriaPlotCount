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
     * Verwendet chunk-basierte Zählung für bessere Performance
     * Mit verbesserter Grenzbehandlung für Plot-Ränder
     */
    public static CountResult countWithDetails(
            Set<com.plotsquared.core.plot.Plot> plots,
            EntityType entityType,
            ConfigManager config,
            DebugLogger debugLogger) {

        int totalCount = 0;
        int allFoundCount = 0; // Alle gefundenen entities (vor Filterung)
        int visibleCount = 0;
        int invisibleCount = 0;
        int fixedCount = 0;
        int maxLimit = config.getMaxCountLimit();
        boolean limitReached = false;

        // Debug-Informationen
        int totalEntitiesChecked = 0;
        int entitiesInBounds = 0;
        int chunksChecked = 0;

        // Verwende eine Set um bereits geprüfte Entities zu verfolgen
        Set<Entity> alreadyCounted = new HashSet<>();

        for (com.plotsquared.core.plot.Plot plot : plots) {
            if (totalCount >= maxLimit) {
                limitReached = true;
                break;
            }

            PlotSquaredIntegration.PlotBounds bounds = PlotSquaredIntegration.getPlotBounds(plot);
            World world = org.bukkit.Bukkit.getWorld(bounds.getWorldName());

            if (world == null) continue;

            // Berechne chunk-Bereiche für den Plot mit erweiterten Grenzen
            int minChunkX = (bounds.getMinX() - 1) >> 4; // Erweitere um 1 Block
            int maxChunkX = (bounds.getMaxX() + 1) >> 4;
            int minChunkZ = (bounds.getMinZ() - 1) >> 4;
            int maxChunkZ = (bounds.getMaxZ() + 1) >> 4;

            // Debug-Info nur bei aktiviertem Debug-Modus
            if (config.isDebugBoundaries() && debugLogger != null) {
                debugLogger.debug("Plot %s: Bounds[%d,%d,%d,%d] Chunks[%d,%d-%d,%d]",
                    plot.getId(), bounds.getMinX(), bounds.getMinZ(), bounds.getMaxX(), bounds.getMaxZ(),
                    minChunkX, minChunkZ, maxChunkX, maxChunkZ);
            }

            // Iteriere durch alle Chunks die den Plot überlappen
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

                        // Verhindere Doppelzählung
                        if (alreadyCounted.contains(entity)) {
                            continue;
                        }

                        // Verbesserte, aber bewährte Grenzprüfung
                        boolean inBounds = bounds.contains(entity.getLocation());

                        if (inBounds) {
                            entitiesInBounds++;

                            // Debug für Grenzfälle nur bei aktiviertem Debug-Modus
                            if (config.isDebugBoundaries() && debugLogger != null) {
                                org.bukkit.Location loc = entity.getLocation();
                                if (isNearBorder(loc, bounds)) {
                                    debugLogger.debug("Entity gefunden: %s bei %.2f,%.2f,%.2f auf Plot %s",
                                        entityType, loc.getX(), loc.getY(), loc.getZ(), plot.getId());
                                }
                            }
                        }

                        if (!inBounds) continue;

                        // Kategorisiere Entity für Statistiken
                        boolean isEntityInvisible = isInvisible(entity);
                        boolean isEntityFixed = isFixed(entity);

                        // Zähle alle gefundenen entities (für Statistiken)
                        alreadyCounted.add(entity);
                        allFoundCount++;
                        if (isEntityInvisible) {
                            invisibleCount++;
                        } else {
                            visibleCount++;
                        }
                        if (isEntityFixed) {
                            fixedCount++;
                        }

                        // Prüfe ob Entity gezählt werden soll basierend auf Config
                        if (shouldCountEntity(entity, config)) {
                            totalCount++;
                        }
                    }
                }
                if (limitReached) break;
            }
        }

        // Debug-Ausgabe nur bei aktiviertem Debug-Modus
        if (config.isDebugBoundaries() && debugLogger != null) {
            debugLogger.debug("Zählstatistik: %d Chunks geprüft, %d Entities untersucht, %d in Grenzen, %d gezählt",
                chunksChecked, totalEntitiesChecked, entitiesInBounds, totalCount);
        }

        return new CountResult(
            totalCount,        // Gefilterte Anzahl
            allFoundCount,     // Alle gefundenen
            visibleCount,
            invisibleCount,
            fixedCount,
            limitReached,
            plots.size()
        );
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
