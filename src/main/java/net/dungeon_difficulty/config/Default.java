package net.dungeon_difficulty.config;

import java.util.List;

public class Default {
    public static Config config = createDefaultConfig();

    private static Config createDefaultConfig() {
        // Difficulty types
        var normalDifficulty = new Config.DifficultyType("normal");
        normalDifficulty.entities = List.of(
                createEntityModifier(Regex.ANY,
                        new Config.AttributeModifier[]{
                                createDamageMultiplier(0.25F, 0),
                                createArmorBonus(1),
                                createHealthMultiplier(0.25F, 0.1F)
                        },
                        null,
                        0.2F)
        );

        var dungeonDifficulty = new Config.DifficultyType("dungeon");
        dungeonDifficulty.parent = normalDifficulty.name;

        var dungeonSpawners = new Config.SpawnerModifier();
        dungeonSpawners = new Config.SpawnerModifier();
        dungeonSpawners.min_spawn_delay_multiplier = -0.2F;
        dungeonSpawners.max_spawn_delay_multiplier = -0.2F;
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
                        createArmorMultiplier(0.1F),
                        createHealthBonus(1)
                })
        );
        dungeonDifficulty.rewards.weapons = List.of(
                createItemModifier(new Config.AttributeModifier[]{
                        createDamageMultiplier(0.15F, 0.05F),
                        createPowerMultiplier(0.15F, 0.05F)
                })
        );

        // Per Player Difficulty
        var perPlayerDifficulty = new Config.PerPlayerDifficulty();
        var perPlayerEntityModifier = new Config.EntityModifier();
        perPlayerEntityModifier.entity_matches.entity_id_regex = "^(?!the_bumblezone:cosmic_crystal_entity).*$";
        perPlayerEntityModifier.attributes = new Config.AttributeModifier[] {
                createDamageMultiplier(0.2F, 0),
                createHealthMultiplier(0.2F, 0F)
        };
        perPlayerDifficulty.entities = new Config.EntityModifier[] {
                perPlayerEntityModifier
        };

        // Surface
        var overworld = new Config.Dimension();
        overworld.world_matches.dimension_regex = "minecraft:overworld";
        overworld.zones = new Config.Zone[] {
                structure("stronghold", dungeonDifficulty.name, 4),
                structure("monument", dungeonDifficulty.name, 2),
                structure("desert_pyramid", dungeonDifficulty.name, 2),
                structure("jungle_pyramid", dungeonDifficulty.name, 2),
                structure("pillager_outpost", normalDifficulty.name, 2),
                biome("desert", normalDifficulty.name, 1),
                biome("frozen|snowy|ice", normalDifficulty.name, 1),
                biome("jungle", normalDifficulty.name, 1),
        };

        var nether = new Config.Dimension();
        nether.world_matches.dimension_regex = "minecraft:the_nether";
        nether.difficulty = new Config.DifficultyReference(normalDifficulty.name, 3);
        nether.zones = new Config.Zone[] {
                structure("fortress", dungeonDifficulty.name, 4),
                structure("bastion_remnant", dungeonDifficulty.name, 4)
        };


        var end = new Config.Dimension();
        end.world_matches.dimension_regex = "minecraft:the_end";
        end.difficulty = new Config.DifficultyReference(normalDifficulty.name, 5);
        end.zones = new Config.Zone[] {
                structure("end_city", dungeonDifficulty.name, 6)
        };

        var config = new Config();
        config.difficulty_types = new Config.DifficultyType[] { normalDifficulty, dungeonDifficulty };
        config.dimensions = new Config.Dimension[] { overworld, nether, end };
        config.perPlayerDifficulty = perPlayerDifficulty;
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
        var modifier = new Config.AttributeModifier("projectile_damage:generic", value);
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

    private static Config.Zone biome(String regex, String difficulty, int level) {
        var zone = new Config.Zone();
        zone.zone_matches.biome_regex = regex;
        zone.difficulty = new Config.DifficultyReference(difficulty, level);
        return zone;
    }

    private static Config.Zone structure(String id, String difficulty, int level) {
        var zone = new Config.Zone();
        zone.zone_matches.structure_id = id;
        zone.difficulty = new Config.DifficultyReference(difficulty, level);
        return zone;
    }
}
