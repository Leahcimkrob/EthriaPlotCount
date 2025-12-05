package de.leahcimkrob.ethriaPlotAddon.util;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import java.util.*;

public class EntityGroupManager {

    private static final Set<EntityType> ANIMALS = EnumSet.of(
        EntityType.COW, EntityType.SHEEP, EntityType.PIG, EntityType.CHICKEN,
        EntityType.HORSE, EntityType.DONKEY, EntityType.MULE, EntityType.LLAMA,
        EntityType.TRADER_LLAMA, EntityType.WOLF, EntityType.CAT, EntityType.OCELOT,
        EntityType.RABBIT, EntityType.FOX, EntityType.BEE, EntityType.TURTLE,
        EntityType.PANDA, EntityType.POLAR_BEAR, EntityType.GOAT, EntityType.AXOLOTL,
        EntityType.FROG, EntityType.TADPOLE, EntityType.VILLAGER, EntityType.IRON_GOLEM,
        EntityType.SNOW_GOLEM
    );

    private static final Set<EntityType> MOBS = EnumSet.of(
        EntityType.ZOMBIE, EntityType.SKELETON, EntityType.CREEPER, EntityType.SPIDER,
        EntityType.CAVE_SPIDER, EntityType.ENDERMAN, EntityType.WITCH, EntityType.SLIME,
        EntityType.MAGMA_CUBE, EntityType.BLAZE, EntityType.GHAST, EntityType.PHANTOM,
        EntityType.DROWNED, EntityType.HUSK, EntityType.STRAY, EntityType.WITHER_SKELETON,
        EntityType.ZOMBIFIED_PIGLIN, EntityType.PIGLIN, EntityType.PIGLIN_BRUTE,
        EntityType.HOGLIN, EntityType.ZOGLIN, EntityType.ENDERMITE, EntityType.SILVERFISH,
        EntityType.GUARDIAN, EntityType.ELDER_GUARDIAN, EntityType.SHULKER, EntityType.VEX,
        EntityType.EVOKER, EntityType.VINDICATOR, EntityType.PILLAGER, EntityType.RAVAGER,
        EntityType.WITHER, EntityType.ENDER_DRAGON, EntityType.WARDEN
    );

    private static final Set<EntityType> ENTITIES = EnumSet.of(
        EntityType.ARMOR_STAND, EntityType.ITEM_FRAME, EntityType.GLOW_ITEM_FRAME,
        EntityType.PAINTING
    );

    private static final Set<EntityType> VEHICLES = EnumSet.of(
        EntityType.MINECART, EntityType.CHEST_MINECART, EntityType.HOPPER_MINECART,
        EntityType.TNT_MINECART, EntityType.FURNACE_MINECART
        // BOAT und CHEST_BOAT werden zur Laufzeit geprüft da sie in verschiedenen Versionen unterschiedlich heißen
    );

    private static final Set<EntityType> ITEMS = EnumSet.of(
        EntityType.ITEM, EntityType.EXPERIENCE_ORB
    );

    private static final Set<EntityType> PROJECTILES = EnumSet.of(
        EntityType.ARROW, EntityType.SPECTRAL_ARROW, EntityType.TRIDENT,
        EntityType.SNOWBALL, EntityType.EGG, EntityType.ENDER_PEARL,
        EntityType.SPLASH_POTION, EntityType.LINGERING_POTION,
        EntityType.FIREWORK_ROCKET, EntityType.FIREBALL, EntityType.SMALL_FIREBALL,
        EntityType.DRAGON_FIREBALL, EntityType.WITHER_SKULL
    );

    public static boolean isInGroup(EntityType entityType, String group) {
        switch (group.toLowerCase()) {
            case "all":
                return true;
            case "animals":
                return ANIMALS.contains(entityType);
            case "mobs":
                return MOBS.contains(entityType);
            case "entities":
                return ENTITIES.contains(entityType);
            case "vehicles":
                if (VEHICLES.contains(entityType)) return true;
                // Prüfe zusätzliche Fahrzeug-Typen zur Laufzeit
                String name = entityType.name();
                return name.equals("BOAT") || name.equals("CHEST_BOAT");
            case "items":
                return ITEMS.contains(entityType);
            case "projectiles":
                return PROJECTILES.contains(entityType);
            default:
                return false;
        }
    }

    public static boolean isInAnyGroup(EntityType entityType, List<String> groups) {
        for (String group : groups) {
            if (isInGroup(entityType, group)) {
                return true;
            }
        }
        return false;
    }

    public static String getEntityGroup(EntityType entityType) {
        if (ANIMALS.contains(entityType)) return "animals";
        if (MOBS.contains(entityType)) return "mobs";
        if (ENTITIES.contains(entityType)) return "entities";
        if (VEHICLES.contains(entityType)) return "vehicles";
        if (ITEMS.contains(entityType)) return "items";
        if (PROJECTILES.contains(entityType)) return "projectiles";
        return "unknown";
    }

    public static Set<EntityType> getGroupTypes(String group) {
        switch (group.toLowerCase()) {
            case "animals":
                return new HashSet<>(ANIMALS);
            case "mobs":
                return new HashSet<>(MOBS);
            case "entities":
                return new HashSet<>(ENTITIES);
            case "vehicles":
                Set<EntityType> vehicles = new HashSet<>(VEHICLES);
                // Füge Boote hinzu falls sie existieren
                try {
                    vehicles.add(EntityType.valueOf("BOAT"));
                } catch (IllegalArgumentException ignored) {}
                try {
                    vehicles.add(EntityType.valueOf("CHEST_BOAT"));
                } catch (IllegalArgumentException ignored) {}
                return vehicles;
            case "items":
                return new HashSet<>(ITEMS);
            case "projectiles":
                return new HashSet<>(PROJECTILES);
            case "all":
                Set<EntityType> allTypes = new HashSet<>();
                allTypes.addAll(ANIMALS);
                allTypes.addAll(MOBS);
                allTypes.addAll(ENTITIES);
                allTypes.addAll(getGroupTypes("vehicles")); // Verwende die erweiterte Vehicles-Liste
                allTypes.addAll(ITEMS);
                allTypes.addAll(PROJECTILES);
                return allTypes;
            default:
                return new HashSet<>();
        }
    }

    public static List<String> getAllGroups() {
        return Arrays.asList("all", "animals", "mobs", "entities", "vehicles", "items", "projectiles");
    }
}
