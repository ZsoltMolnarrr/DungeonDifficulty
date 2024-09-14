package net.dungeon_difficulty.logic;

import net.minecraft.util.Rarity;

public class RarityHelper {
    public static int getRarityIndex(Rarity rarity) {
        return switch (rarity) {
            case COMMON -> 0;
            case UNCOMMON -> 1;
            case RARE -> 2;
            case EPIC -> 3;
            default -> 0;
        };
    }

    public static Rarity getRarityByIndex(int index) {
        return switch (index) {
            case 0 -> Rarity.COMMON;
            case 1 -> Rarity.UNCOMMON;
            case 2 -> Rarity.RARE;
            case 3 -> Rarity.EPIC;
            default -> Rarity.COMMON;
        };
    }

    public static Rarity increasedRarity(Rarity rarity, int bonus) {
        var nextIndex = rarity.ordinal() + bonus;
        var maxIndex = Rarity.values().length - 1;
        if (nextIndex <= maxIndex) {
            return Rarity.values()[nextIndex];
        } else {
            return Rarity.values()[maxIndex];
        }
    }
}
