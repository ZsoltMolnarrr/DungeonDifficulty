package net.dungeon_difficulty.logic;

import net.minecraft.util.Rarity;

public class RarityHelper {
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
