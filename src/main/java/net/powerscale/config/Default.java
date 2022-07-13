package net.powerscale.config;

import java.util.HashMap;

public class Default {
    public static Config config = createDefaultConfig();

    private static Config createDefaultConfig() {
        // Surface
        var overworld = new Config.Location();
        overworld.filters = new Config.Location.Filters();
        overworld.filters.dimension_regex = "minecraft:overworld";
        overworld.rewards = new Config.Location.Rewards();
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
                createEntityModifier("zombie|creeper|skeleton", new Config.AttributeModifier[]{
                        createHealthMultiplier(2F),
                        createArmorBonus(2F),
                        createMaxHealthBonus(10F),
                        createArmorMultiplier(2F),
                        createDamageMultiplier(2)
                })
        };

        // Nether
        var nether = new Config.Location();
        nether.filters = new Config.Location.Filters();
        nether.filters.dimension_regex = "minecraft:the_nether";
        nether.rewards = new Config.Location.Rewards();
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

        var config = new Config();
        config.locations = new Config.Location[] { overworld, nether };

        return config;
    }

    private static Config.ItemModifier createItemModifier(Config.AttributeModifier[] attributeModifiers) {
        var itemModifier = new Config.ItemModifier();
        itemModifier.filters = new Config.ItemModifier.Filters();
        itemModifier.modifiers = attributeModifiers;
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
        entityModifier.filters = new Config.EntityModifier.Filters();
        entityModifier.filters.entity_id_regex = idRegex;
        entityModifier.modifiers = attributeModifiers;
        return entityModifier;
    }
}
