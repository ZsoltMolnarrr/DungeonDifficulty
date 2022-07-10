package net.powerscale.logic;

import net.minecraft.util.Identifier;
import net.powerscale.config.Config;
import net.powerscale.config.ConfigManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class PatternMatching {
    public static List<Config.ItemModifier> getModifiersForItem(Identifier item, Identifier dimension) {
        var attributeModifiers = new ArrayList<Config.ItemModifier>();
        var dimensions = getDimensionConfigsMatching(dimension);
        for (Config.Dimension dimensionConfig: dimensions) {
            for(Map.Entry<String, Config.ItemModifier[]> entry: dimensionConfig.items.entrySet()) {
                if (item.toString().matches(entry.getKey())) {
                    System.out.println("PM: " + item + " matches: " + entry.getKey());
                    attributeModifiers.addAll(Arrays.asList(entry.getValue()));
                }
            }
        }
        return attributeModifiers;
    }

    public static List<Config.Dimension> getDimensionConfigsMatching(Identifier dimension) {
        var dimensionConfigs = new ArrayList<Config.Dimension>();
        for (Map.Entry<String,Config.Dimension> entry : ConfigManager.currentConfig.dimensions.entrySet()) {
            if (dimension.toString().matches(entry.getKey())) {
                System.out.println("PM: " + dimension + " matches: " + entry.getKey());
                dimensionConfigs.add(entry.getValue());
            } else {
                System.out.println("PM: " + dimension + " does not match: " + entry.getKey());
            }
        }
        return dimensionConfigs;
    }
}
