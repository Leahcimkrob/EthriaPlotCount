package de.leahcimkrob.ethriaPlotCount.util;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class PermissionManager {

    // === ADMIN BERECHTIGUNGEN ===
    public static boolean hasAdminPermission(Player player) {
        return player.hasPermission("ethriaplotcount.admin");
    }

    // === PLOT-ZUGRIFF ===
    public static boolean canCountOnOwnPlots(Player player) {
        return player.hasPermission("ethriaplotcount.own");
    }

    public static boolean canCountOnOtherPlots(Player player) {
        return player.hasPermission("ethriaplotcount.other");
    }

    // === ENTITY-BERECHTIGUNGEN ===
    public static boolean canCountEntity(Player player, EntityType entityType) {
        // Admin kann alles
        if (hasAdminPermission(player)) {
            return true;
        }

        String entityName = entityType.name().toLowerCase();

        // Spezifische Entity-Berechtigung prüfen
        if (player.hasPermission("ethriaplotcount.entity." + entityName) ||
            player.hasPermission("ethriaplotcount.entity.*")) {
            return true;
        }

        // Gruppen-Berechtigung prüfen
        String group = EntityGroupManager.getEntityGroup(entityType);
        return player.hasPermission("ethriaplotcount.group." + group) ||
               player.hasPermission("ethriaplotcount.group.*");
    }

    public static boolean canCountGroup(Player player, String group) {
        // Admin kann alles
        if (hasAdminPermission(player)) {
            return true;
        }

        // Gruppen-Berechtigung prüfen
        return player.hasPermission("ethriaplotcount.group." + group.toLowerCase()) ||
               player.hasPermission("ethriaplotcount.group.*");
    }

    // === BASIS-BERECHTIGUNGEN ===
    public static boolean hasBasePermission(Player player) {
        return player.hasPermission("ethriaplotcount.use");
    }

    public static boolean canReload(Player player) {
        return player.hasPermission("ethriaplotcount.reload") ||
               hasAdminPermission(player);
    }

    // === HILFSMETHODEN ===
    public static String getPermissionError(Player player, EntityType entityType) {
        String entityName = entityType.name().toLowerCase();
        String group = EntityGroupManager.getEntityGroup(entityType);

        return "Du benötigst eine der folgenden Berechtigungen: " +
               "ethriaplotcount.entity." + entityName + ", " +
               "ethriaplotcount.group." + group + " oder " +
               "ethriaplotcount.admin";
    }

    public static String getPlotAccessError(Player player) {
        if (!canCountOnOwnPlots(player) && !canCountOnOtherPlots(player)) {
            return "Du benötigst ethriaplotcount.own oder ethriaplotcount.other Berechtigung!";
        }
        return "Du kannst nur auf deinen eigenen/getrusted/added Plots zählen (wenn der Owner online ist)!";
    }
}
