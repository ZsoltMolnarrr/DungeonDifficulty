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
                createItemModifier("generic.attack_damage", 2),
                createItemModifier("generic.projectile_damage", 2)
        };
        overworld.rewards.armor = new Config.ItemModifier[]{
                createItemModifier("generic.armor", 2),
        };

        // Nether
//        var nether = new Config.Dimension();
//        nether.weapons = new HashMap<String, Config.AttributeModifier[]>() {{
//            put(".*", new Config.AttributeModifier[] {
//                    createDamageMultiplier(3),
//                    createProjectileMultiplier(3)
//            });
//        }};
//        nether.armor = new HashMap<String, Config.AttributeModifier[]>() {{
//            put(".*", new Config.AttributeModifier[] {
//                    createArmorMultiplier(2),
//                    createMaxHealthBonus(2)
//            });
//        }};
//
//        // End
//        var end = new Config.Dimension();
//        end.weapons = new HashMap<String, Config.AttributeModifier[]>() {{
//            put(".*", new Config.AttributeModifier[] {
//                    createDamageMultiplier(4),
//                    createProjectileMultiplier(4)
//            });
//        }};
//        end.armor = new HashMap<String, Config.AttributeModifier[]>() {{
//            put(".*", new Config.AttributeModifier[] {
//                    createArmorMultiplier(4)
//            });
//        }};

        var config = new Config();
        config.locations = new Config.Location[] { overworld };
//        return new Config(new HashMap<String, Config.Dimension>() {{
//            put("minecraft:overworld", overworld );
//            put("minecraft:the_nether", nether );
//            put("minecraft:the_end", end );
//        }});
        return config;
    }

    private static Config.ItemModifier createItemModifier(String attributeName, float value) {
        var itemModifier = new Config.ItemModifier();
        itemModifier.filters = new Config.ItemModifier.Filters();
        itemModifier.modifiers = new Config.AttributeModifier[]{ new Config.AttributeModifier(attributeName, value)};
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

    private static Config.AttributeModifier createMaxHealthBonus(float value) {
        var modifier = new Config.AttributeModifier("generic.max_health", value);
        modifier.operation = Config.Operation.ADD;
        return modifier;
    }
}
