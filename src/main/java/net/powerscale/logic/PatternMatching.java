package net.powerscale.logic;

import net.minecraft.util.Identifier;
import net.powerscale.config.Config;
import net.powerscale.config.ConfigManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PatternMatching {
    public static List<Config.AttributeModifier> getModifiersForArmor(Identifier item, Identifier dimension) {
        return getModifiersForItem(ModifierSet.ARMOR, item, dimension);
    }

    public static List<Config.AttributeModifier> getModifiersForWeapon(Identifier item, Identifier dimension) {
        return getModifiersForItem(ModifierSet.WEAPONS, item, dimension);
    }

    enum ModifierSet {
        ARMOR, WEAPONS
    }

    public static List<Config.AttributeModifier> getModifiersForItem(ModifierSet modifierSet, Identifier itemId, Identifier dimensionId) {
        var attributeModifiers = new ArrayList<Config.AttributeModifier>();
        var locations = getLocationConfigsMatching(dimensionId);
        for (var location: locations) {
            if (location.rewards != null) {
                Config.ItemModifier[] itemModifiers = null;
                switch (modifierSet) {
                    case ARMOR -> {
                        itemModifiers = location.rewards.armor;
                    }
                    case WEAPONS -> {
                        itemModifiers = location.rewards.weapons;
                    }
                }
                if (itemModifiers == null) {
                    continue;
                }
                for(var entry: itemModifiers) {
                    if (entry.filters != null) {
                        if (matches(itemId.toString(), entry.filters.item_id_regex)) {
                            System.out.println("PM: " + itemId + " matches: " + entry.filters.item_id_regex);
                            attributeModifiers.addAll(Arrays.asList(entry.modifiers));
                        }
                    } else {
                        attributeModifiers.addAll(Arrays.asList(entry.modifiers));
                    }
                }
            }

        }
        return attributeModifiers;
    }

    public static List<Config.Location> getLocationConfigsMatching(Identifier dimension) {
        var dimensionConfigs = new ArrayList<Config.Location>();
        for (var entry : ConfigManager.currentConfig.locations) {
            var dimensionRegex = getDimensionRegex(entry);
            if (matches(dimension.toString(), dimensionRegex)) {
                System.out.println("PM: " + dimension + " matches dimension_regex: " + dimensionRegex);
                dimensionConfigs.add(entry);
            } else {
                System.out.println("PM: " + dimension + " does not match: dimension_regex: " + dimensionRegex);
            }
        }
        return dimensionConfigs;
    }

    private static String getDimensionRegex(Config.Location location) {
        if (location.filters != null) {
            return location.filters.dimension_regex;
        }
        return null;
    }

    private static boolean matches(String subject, String nullableRegex) {
//        return nullableRegex == null || nullableRegex.isEmpty() || subject.matches(nullableRegex);
        if (nullableRegex == null || nullableRegex.isEmpty()) {
            return true;
        }
        Pattern pattern = Pattern.compile(nullableRegex, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(subject);
        return matcher.find();
    }
}
