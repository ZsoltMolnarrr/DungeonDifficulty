package net.dungeon_difficulty.config;

import net.dungeon_difficulty.DungeonDifficulty;
import net.dungeon_difficulty.logic.PatternMatching;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;

import java.util.List;

public class Default {
    public static Config config = createDefaultConfig();

    private static Config createDefaultConfig() {
        // Difficulty types
        var normalDifficulty = new Config.DifficultyType("adventure");
        normalDifficulty.entities = List.of(
                createEntityModifier(Regex.ANY,
                        new Config.AttributeModifier[]{
                                createDamageMultiplier(0.05F, 0),
                                createArmorBonus(0.25),
                                createHealthMultiplier(0.05F, 0.025F)
                        },
                        null,
                        0.2F)
        );

        var dungeonDifficulty = new Config.DifficultyType("dungeon");
        dungeonDifficulty.parent = normalDifficulty.name;

        var dungeonSpawners = new Config.SpawnerModifier();
        dungeonSpawners = new Config.SpawnerModifier();
        dungeonSpawners.min_spawn_delay_multiplier = -0.1F;
        dungeonSpawners.max_spawn_delay_multiplier = -0.1F;
        dungeonSpawners.spawn_count_multiplier = 0.5F;
        dungeonSpawners.max_nearby_entities_multiplier = 1F;

        dungeonDifficulty.entities = List.of(
                createEntityModifier(Regex.ANY,
                        new Config.AttributeModifier[]{ },
                        dungeonSpawners,
                        0)
        );
        dungeonDifficulty.rewards.armor = List.of(
                createItemModifier(new Config.AttributeModifier[]{
                        createArmorMultiplier(0.3F),
                        createHealthBonus(3)
                })
        );
        dungeonDifficulty.rewards.weapons = List.of(
                createItemModifier(new Config.AttributeModifier[]{
                        createDamageMultiplier(0.3F, 0.15F),
                        createPowerMultiplier(0.3F, 0.15F)
                })
        );

        // Per Player Difficulty
        var perPlayerDifficulty = new Config.PerPlayerDifficulty();
        var perPlayerEntityModifier = new Config.EntityModifier();
        perPlayerEntityModifier.entity_matches.entity_id_regex = Regex.ANY;
        if (FabricLoader.getInstance().isModLoaded("the_bumblezone")) {
            perPlayerEntityModifier.entity_matches.entity_id_regex = "^(?!the_bumblezone:cosmic_crystal_entity).*$";
        }

        perPlayerEntityModifier.attributes = new Config.AttributeModifier[] {
                createDamageMultiplier(0.05F, 0),
                createHealthMultiplier(0.05F, 0F)
        };
        perPlayerDifficulty.entities = new Config.EntityModifier[] {
                perPlayerEntityModifier
        };

        // Surface
        var overworld = new Config.Dimension();
        overworld.world_matches.dimension_regex = "minecraft:overworld";
        overworld.zones = List.of(
                structureTag("level_3", dungeonDifficulty.name, 3),
                structureTag("level_2", dungeonDifficulty.name, 2),
                structureTag("level_1", dungeonDifficulty.name, 1),
                biomeRegex("desert|frozen|snowy|ice|jungle", normalDifficulty.name, 1)
        );

        var nether = new Config.Dimension();
        nether.world_matches.dimension_regex = "minecraft:the_nether";
        nether.difficulty = new Config.DifficultyReference(normalDifficulty.name, 3);
        nether.zones = List.of(
                structureTag("level_4", dungeonDifficulty.name, 4)
        );
        nether.entities = List.of(
                entitySpecificMatcher(Identifier.ofVanilla("wither"), dungeonDifficulty.name, 3)
        );

        var end = new Config.Dimension();
        end.world_matches.dimension_regex = "minecraft:the_end";
        end.difficulty = new Config.DifficultyReference(normalDifficulty.name, 4);
        end.zones = List.of(
                structureTag("level_6", dungeonDifficulty.name, 6),
                structureTag("level_5", dungeonDifficulty.name, 5)
        );
        end.entities = List.of(
                entitySpecificMatcher(Identifier.ofVanilla("ender_dragon"), dungeonDifficulty.name, 4)
        );

        var config = new Config();
        config.difficulty_types = List.of(normalDifficulty, dungeonDifficulty);
        config.dimensions = new Config.Dimension[] { overworld, nether, end };
        config.per_player_difficulty = perPlayerDifficulty;
        return config;
    }

    private static Config.ItemModifier createItemModifier(Config.AttributeModifier[] attributeModifiers) {
        return createItemModifier(null, null, attributeModifiers);
    }

    private static Config.ItemModifier createItemModifier(String itemIdRegex, String lootTableRegex, Config.AttributeModifier[] attributeModifiers) {
        var itemModifier = new Config.ItemModifier();
        itemModifier.item_matches = new Config.ItemModifier.Filters();
        if (itemIdRegex != null) {
            itemModifier.item_matches.item_id_regex = itemIdRegex;
        }
        if (lootTableRegex != null) {
            itemModifier.item_matches.loot_table_regex = lootTableRegex;
        }
        itemModifier.attributes = attributeModifiers;
        return itemModifier;
    }

    private static Config.AttributeModifier createDamageMultiplier(float value, float randomness) {
        var modifier = new Config.AttributeModifier("damage", value);
        modifier.randomness = randomness;
        return modifier;
    }

    private static Config.AttributeModifier createPowerMultiplier(float value, float randomness) {
        var modifier = new Config.AttributeModifier("power", value);
        modifier.randomness = randomness;
        return modifier;
    }

    private static Config.AttributeModifier createProjectileMultiplier(float value, float randomness) {
        var modifier = new Config.AttributeModifier("ranged_weapon:damage", value);
        modifier.randomness = randomness;
        return modifier;
    }

    private static Config.AttributeModifier createArmorMultiplier(float value) {
        return new Config.AttributeModifier("generic.armor", value);
    }

    private static Config.AttributeModifier createArmorBonus(float value) {
        var modifier = new Config.AttributeModifier("generic.armor", value);
        modifier.operation = Config.Operation.ADDITION;
        return modifier;
    }

    private static Config.AttributeModifier createHealthMultiplier(float value, float randomness) {
        var modifier = new Config.AttributeModifier("generic.max_health", value);
        modifier.randomness = randomness;
        return modifier;
    }

    private static Config.AttributeModifier createHealthBonus(float value) {
        var modifier = new Config.AttributeModifier("generic.max_health", value);
        modifier.operation = Config.Operation.ADDITION;
        return modifier;
    }

    private static Config.EntityModifier createEntityModifier(String idRegex, Config.AttributeModifier[] attributeModifiers, Config.SpawnerModifier spawnerModifier, float xpMultiplier) {
        var entityModifier = new Config.EntityModifier();
        entityModifier.entity_matches = new Config.EntityModifier.Filters();
        entityModifier.entity_matches.entity_id_regex = idRegex;
        entityModifier.attributes = attributeModifiers;
        entityModifier.spawners = spawnerModifier;
        entityModifier.experience_multiplier = xpMultiplier;
        return entityModifier;
    }

    private static Config.Zone biomeRegex(String regex, String difficulty, int level) {
        var zone = new Config.Zone();
        zone.zone_matches.biome = PatternMatching.REGEX_PREFIX + regex;
        zone.difficulty = new Config.DifficultyReference(difficulty, level);
        return zone;
    }

    private static Config.Zone structureId(String id, String difficulty, int level) {
        var zone = new Config.Zone();
        zone.zone_matches.structure = id;
        zone.difficulty = new Config.DifficultyReference(difficulty, level);
        return zone;
    }

    private static Config.Zone structureTag(String tag, String difficulty, int level) {
        var zone = new Config.Zone();
        zone.zone_matches.structure = "#" + DungeonDifficulty.MODID + ":" + tag;
        zone.difficulty = new Config.DifficultyReference(difficulty, level);
        return zone;
    }

    private static Config.EntityMatcher entityTypeMatcher(String type, String difficulty, int level) {
        var entityMatcher = new Config.EntityMatcher();
        entityMatcher.entity_type = type;
        entityMatcher.difficulty = new Config.DifficultyReference(difficulty, level);
        return entityMatcher;
    }

    private static Config.EntityMatcher entityLootTableMatcher(String lootTable, String difficulty, int level) {
        var entityMatcher = new Config.EntityMatcher();
        entityMatcher.loot_table = lootTable;
        entityMatcher.difficulty = new Config.DifficultyReference(difficulty, level);
        return entityMatcher;
    }

    private static Config.EntityMatcher entitySpecificMatcher(Identifier entityId, String difficulty, int level) {
        var entityMatcher = new Config.EntityMatcher();
        entityMatcher.entity_type = entityId.toString();
        entityMatcher.loot_table = entityId.getNamespace() + ":" + "entities/" + entityId.getPath();
        entityMatcher.difficulty = new Config.DifficultyReference(difficulty, level);
        return entityMatcher;
    }
}
