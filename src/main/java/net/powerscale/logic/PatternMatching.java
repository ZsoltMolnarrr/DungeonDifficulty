package net.powerscale.logic;

import net.minecraft.util.Identifier;
import net.powerscale.config.Config;
import net.powerscale.config.ConfigManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class PatternMatching {
    public static List<Config.ItemModifier> getModifiersForArmor(Identifier item, Identifier dimension) {
        return getModifiersForItem(ModifierSet.ARMOR, item, dimension);
    }

    public static List<Config.ItemModifier> getModifiersForWeapon(Identifier item, Identifier dimension) {
        return getModifiersForItem(ModifierSet.WEAPONS, item, dimension);
    }

    enum ModifierSet {
        ARMOR, WEAPONS
    }

    public static List<Config.ItemModifier> getModifiersForItem(ModifierSet modifierSet, Identifier item, Identifier dimension) {
        var attributeModifiers = new ArrayList<Config.ItemModifier>();
        var dimensions = getDimensionConfigsMatching(dimension);
        for (Config.Dimension dimensionConfig: dimensions) {
            Map<String, Config.ItemModifier[]> modifiers = null; 
            switch (modifierSet) {
                case ARMOR -> {
                    modifiers = dimensionConfig.armor;
                }
                case WEAPONS -> {
                    modifiers = dimensionConfig.weapons;
                }
            }
            for(Map.Entry<String, Config.ItemModifier[]> entry: modifiers.entrySet()) {
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
