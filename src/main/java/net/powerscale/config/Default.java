package net.powerscale.config;

import java.util.HashMap;

public class Default {
    public static Config config = createDefaultConfig();

    private static Config createDefaultConfig() {
        // Surface
        var overworldItems = new HashMap<String, Config.ItemModifier[]>() {{
            put(".*", new Config.ItemModifier[] {
                createDamageMultiplier(2)
            });
        }};
        var overworld = new Config.Dimension(overworldItems);
        // Nether
        var netherItems = new HashMap<String, Config.ItemModifier[]>() {{
            put(".*", new Config.ItemModifier[] {
                createDamageMultiplier(3),
                createArmorMultiplier(3)
            });
        }};
        var nether = new Config.Dimension(netherItems);

        // End
        var endItems = new HashMap<String, Config.ItemModifier[]>() {{
            put(".*", new Config.ItemModifier[] {
                createDamageMultiplier(4),
                createArmorMultiplier(4)
            });
        }};
        var end = new Config.Dimension(endItems);

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
}
