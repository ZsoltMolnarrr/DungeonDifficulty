package net.powerscale.config;

public class Default {
    public static Config config = createDefaultConfig();

    private static Config createDefaultConfig() {
        // Surface
        var overworld = new Config.Dimension();
        overworld.world_matches.dimension_regex = "minecraft:overworld";
//        overworld.rewards.weapons = new Config.ItemModifier[]{
//                createItemModifier(new Config.AttributeModifier[]{
//                        createDamageMultiplier(1.5F),
//                        createProjectileMultiplier(1.5F)
//                }),
//        };
//        overworld.rewards.armor = new Config.ItemModifier[]{
//                createItemModifier(new Config.AttributeModifier[]{
//                        createArmorMultiplier(1.5F)
//                }),
//        };
//        overworld.entities = new Config.EntityModifier[] {
//                createEntityModifier("zombi|creeper|skeleton", new Config.AttributeModifier[]{
//                        createHealthMultiplier(2F),
//                        createArmorBonus(2F),
//                        createDamageMultiplier(2)
//                })
//        };
//        var desert = new Config.Zone();
//        desert.zone_matches.biome_regex = "desert";
//        desert.rewards.weapons = new Config.ItemModifier[]{
//                createItemModifier(new Config.AttributeModifier[]{
//                        createDamageMultiplier(1.5F),
//                        createProjectileMultiplier(1.5F)
//                }),
//        };
//        desert.rewards.armor = new Config.ItemModifier[]{
//                createItemModifier(new Config.AttributeModifier[]{
//                        createMaxHealthBonus(2)
//                }),
//        };
//        desert.entities = new Config.EntityModifier[] {
//                createEntityModifier("zombi|creeper|skeleton", new Config.AttributeModifier[]{
//                        createArmorMultiplier(2F),
//                        createMaxHealthBonus(10F)
//                })
//        };
//        overworld.zones = new Config.Zone[] { desert };

        // Nether
        var nether = new Config.Dimension();
        nether.world_matches.dimension_regex = "minecraft:the_nether";
        nether.rewards.weapons = new Config.ItemModifier[]{
                createItemModifier(new Config.AttributeModifier[]{
                        createDamageMultiplier(1.25F, 0.25F),
                        createProjectileMultiplier(1.25F, 0.25F)
                }),
        };
        nether.rewards.armor = new Config.ItemModifier[]{
                createItemModifier(new Config.AttributeModifier[]{
                        createArmorMultiplier(1.2F),
                        createHealthBonus(2)
                }),
        };
        var blazeSpawners = new Config.SpawnerModifier();
        blazeSpawners = new Config.SpawnerModifier();
        blazeSpawners.min_spawn_delay_multiplier = 0.5F;
        blazeSpawners.max_spawn_delay_multiplier = 0.5F;
        blazeSpawners.spawn_count_multiplier = 2F;
        blazeSpawners.max_nearby_entities_multiplier = 3F;
        nether.entities = new Config.EntityModifier[] {
            createEntityModifier(".*",
                    new Config.AttributeModifier[]{
                        createDamageMultiplier(1.5F, 0),
                        createArmorBonus(2),
                        createHealthMultiplier(1.4F, 0.1F)
                    },
                    null),
            createEntityModifier("blaze", new Config.AttributeModifier[]{}, blazeSpawners)
        };
//
//        var blazeModifier = createEntityModifier("blaze", new Config.AttributeModifier[]{});
//        blazeModifier.spawners = new Config.SpawnerModifier();
//        blazeModifier.spawners.min_spawn_delay_multiplier = 0.5F;
//        blazeModifier.spawners.max_spawn_delay_multiplier = 0.5F;
//        blazeModifier.spawners.spawn_count_multiplier = 2F;
//        nether.entities =  new Config.EntityModifier[] {
//                blazeModifier
//        };
//

        var end = new Config.Dimension();
        end.world_matches.dimension_regex = "minecraft:the_end";

        var config = new Config();
        config.dimensions = new Config.Dimension[] { overworld, nether, end };
        return config;
    }

    private static Config.ItemModifier createItemModifier(Config.AttributeModifier[] attributeModifiers) {
        var itemModifier = new Config.ItemModifier();
        itemModifier.item_matches = new Config.ItemModifier.Filters();
        itemModifier.attributes = attributeModifiers;
        return itemModifier;
    }

    private static Config.AttributeModifier createDamageMultiplier(float value, float randomness) {
        var modifier = new Config.AttributeModifier("generic.attack_damage", value);
        modifier.randomness = randomness;
        return modifier;
    }

    private static Config.AttributeModifier createProjectileMultiplier(float value, float randomness) {
        var modifier = new Config.AttributeModifier("generic.projectile_damage", value);
        modifier.randomness = randomness;
        return modifier;
    }

    private static Config.AttributeModifier createArmorMultiplier(float value) {
        return new Config.AttributeModifier("generic.armor", value);
    }

    private static Config.AttributeModifier createArmorBonus(float value) {
        var modifier = new Config.AttributeModifier("generic.armor", value);
        modifier.operation = Config.Operation.ADD;
        return modifier;
    }

    private static Config.AttributeModifier createHealthMultiplier(float value, float randomness) {
        var modifier = new Config.AttributeModifier("generic.max_health", value);
        modifier.randomness = randomness;
        return modifier;
    }

    private static Config.AttributeModifier createHealthBonus(float value) {
        var modifier = new Config.AttributeModifier("generic.max_health", value);
        modifier.operation = Config.Operation.ADD;
        return modifier;
    }

    private static Config.EntityModifier createEntityModifier(String idRegex, Config.AttributeModifier[] attributeModifiers, Config.SpawnerModifier spawnerModifier) {
        var entityModifier = new Config.EntityModifier();
        entityModifier.entity_matches = new Config.EntityModifier.Filters();
        entityModifier.entity_matches.entity_id_regex = idRegex;
        entityModifier.attributes = attributeModifiers;
        entityModifier.spawners = spawnerModifier;
        return entityModifier;
    }
}
