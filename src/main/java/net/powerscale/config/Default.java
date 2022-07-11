package net.powerscale.config;

import java.util.HashMap;

public class Default {
    public static Config config = createDefaultConfig();

    private static Config createDefaultConfig() {
        // Surface
        var overworld = new Config.Dimension();
        overworld.weapons = new HashMap<String, Config.ItemModifier[]>() {{
            put(".*", new Config.ItemModifier[] {
                    createDamageMultiplier(2)
            });
        }};

        // Nether
        var nether = new Config.Dimension();
        nether.weapons = new HashMap<String, Config.ItemModifier[]>() {{
            put(".*", new Config.ItemModifier[] {
                    createDamageMultiplier(3)
            });
        }};
        nether.armor = new HashMap<String, Config.ItemModifier[]>() {{
            put(".*", new Config.ItemModifier[] {
                    createArmorMultiplier(2),
                    createMaxHealthBonus(2)
            });
        }};

        // End
        var end = new Config.Dimension();
        end.weapons = new HashMap<String, Config.ItemModifier[]>() {{
            put(".*", new Config.ItemModifier[] {
                    createDamageMultiplier(4)
            });
        }};
        end.armor = new HashMap<String, Config.ItemModifier[]>() {{
            put(".*", new Config.ItemModifier[] {
                    createArmorMultiplier(4)
            });
        }};

        return new Config(new HashMap<String, Config.Dimension>() {{
            put("minecraft:overworld", overworld );
            put("minecraft:the_nether", nether );
            put("minecraft:the_end", end );
        }});
    }

    private static Config.ItemModifier createDamageMultiplier(float value) {
        return new Config.ItemModifier("generic.attack_damage", value);
    }

    private static Config.ItemModifier createArmorMultiplier(float value) {
        return new Config.ItemModifier("generic.armor", value);
    }

    private static Config.ItemModifier createMaxHealthBonus(float value) {
        var modifier = new Config.ItemModifier("generic.max_health", value);
        modifier.operation = Config.Operation.ADD;
        return modifier;
    }
}
