package net.powerscale.config;

public class Default {
    public static Config config = createDefaultConfig();

    private static Config createDefaultConfig() {
        // Surface
        var overworld = new Config.Dimension();
        overworld.world_matches = new Config.Dimension.Filters();
        overworld.world_matches.dimension_regex = "minecraft:overworld";
        overworld.rewards = new Config.Rewards();
        overworld.rewards.weapons = new Config.ItemModifier[]{
                createItemModifier(new Config.AttributeModifier[]{
                        createDamageMultiplier(1.5F),
                        createProjectileMultiplier(1.5F)
                }),
        };
        overworld.rewards.armor = new Config.ItemModifier[]{
                createItemModifier(new Config.AttributeModifier[]{
                        createArmorMultiplier(1.5F)
                }),
        };
        overworld.entities = new Config.EntityModifier[] {
                createEntityModifier("zombi|creeper|skeleton", new Config.AttributeModifier[]{
                        createHealthMultiplier(2F),
                        createArmorBonus(2F),
                        createMaxHealthBonus(10F),
                        createArmorMultiplier(2F),
                        createDamageMultiplier(2)
                })
        };

        // Nether
        var nether = new Config.Dimension();
        nether.world_matches = new Config.Dimension.Filters();
        nether.world_matches.dimension_regex = "minecraft:the_nether";
        nether.rewards = new Config.Rewards();
        nether.rewards.weapons = new Config.ItemModifier[]{
                createItemModifier(new Config.AttributeModifier[]{
                        createDamageMultiplier(2),
                        createProjectileMultiplier(2)
                }),
        };
        nether.rewards.armor = new Config.ItemModifier[]{
                createItemModifier(new Config.AttributeModifier[]{
                        createArmorMultiplier(2)
                }),
        };

        var blazeModifier = createEntityModifier("blaze", new Config.AttributeModifier[]{});
        blazeModifier.spawners = new Config.SpawnerModifier();
        blazeModifier.spawners.min_spawn_delay_multiplier = 0.5F;
        blazeModifier.spawners.max_spawn_delay_multiplier = 0.5F;
        blazeModifier.spawners.spawn_count_multiplier = 2F;
        nether.entities =  new Config.EntityModifier[] {
                blazeModifier
        };

        var config = new Config();
        config.dimensions = new Config.Dimension[] { overworld, nether };

        return config;
    }

    private static Config.ItemModifier createItemModifier(Config.AttributeModifier[] attributeModifiers) {
        var itemModifier = new Config.ItemModifier();
        itemModifier.item_matches = new Config.ItemModifier.Filters();
        itemModifier.attributes = attributeModifiers;
        return itemModifier;
    }

    private static Config.AttributeModifier createDamageMultiplier(float value) {
        return new Config.AttributeModifier("generic.attack_damage", value);
    }

    private static Config.AttributeModifier createProjectileMultiplier(float value) {
        return new Config.AttributeModifier("generic.projectile_damage", value);
    }

    private static Config.AttributeModifier createArmorMultiplier(float value) {
        return new Config.AttributeModifier("generic.armor", value);
    }

    private static Config.AttributeModifier createArmorBonus(float value) {
        var modifier = new Config.AttributeModifier("generic.armor", value);
        modifier.operation = Config.Operation.ADD;
        return modifier;
    }

    private static Config.AttributeModifier createHealthMultiplier(float value) {
        return new Config.AttributeModifier("generic.max_health", value);
    }

    private static Config.AttributeModifier createMaxHealthBonus(float value) {
        var modifier = new Config.AttributeModifier("generic.max_health", value);
        modifier.operation = Config.Operation.ADD;
        return modifier;
    }

    private static Config.EntityModifier createEntityModifier(String idRegex, Config.AttributeModifier[] attributeModifiers) {
        var entityModifier = new Config.EntityModifier();
        entityModifier.entity_matches = new Config.EntityModifier.Filters();
        entityModifier.entity_matches.entity_id_regex = idRegex;
        entityModifier.attributes = attributeModifiers;
        return entityModifier;
    }
}
