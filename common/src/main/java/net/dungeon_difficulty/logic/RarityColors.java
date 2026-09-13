package net.dungeon_difficulty.logic;

import com.mojang.logging.LogUtils;
import net.dungeon_difficulty.DungeonDifficulty;
import net.dungeon_difficulty.mixin.RarityAccessor;
import net.minecraft.util.Formatting;
import net.minecraft.util.Rarity;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class RarityColors {
    static final Logger LOGGER = LogUtils.getLogger();

    // Override color per `Rarity.ordinal()`, empty while the feature is disabled.
    private static volatile Map<Integer, Formatting> overrides = Map.of();
    // The colors the rarities were loaded with, so a config reload can restore them.
    private static final Map<Integer, Formatting> originals = new HashMap<>();

    /**
     * Resolves the configured overrides against the `Rarity` constants present at runtime, and writes them
     * into the constants' `formatting` field (1.20.1 has no getter to hook). Rarities added by other mods
     * (Forge's extensible enum) are only known once those mods created them, so this has to run after mod
     * loading; a config reload re-applies it.
     */
    public static void initialize() {
        var config = DungeonDifficulty.clientConfig.value;
        var rarities = Rarity.values();
        var resolved = new HashMap<Integer, Formatting>();
        if (config.enable_rarity_color_override && config.rarity_color_overrides != null) {
            for (var entry : config.rarity_color_overrides.entrySet()) {
                var ordinal = parseOrdinal(entry.getKey(), rarities.length);
                if (ordinal < 0) {
                    continue;
                }
                var formatting = Formatting.byName(entry.getValue());
                if (formatting == null) {
                    LOGGER.warn("Unknown Formatting `{}` configured for rarity ordinal {}, ignoring it",
                            entry.getValue(), ordinal);
                    continue;
                }
                resolved.put(ordinal, formatting);
            }
        }
        overrides = Map.copyOf(resolved);

        for (var rarity : rarities) {
            var original = originals.computeIfAbsent(rarity.ordinal(), ordinal -> rarity.formatting);
            var override = resolved.get(rarity.ordinal());
            var target = override != null ? override : original;
            if (rarity.formatting != target) {
                ((RarityAccessor) (Object) rarity).dungeon_difficulty$setFormatting(target);
            }
        }
    }

    @Nullable
    public static Formatting override(int ordinal) {
        return overrides.get(ordinal);
    }

    private static int parseOrdinal(String key, int rarityCount) {
        int ordinal;
        try {
            ordinal = Integer.parseInt(key.trim());
        } catch (NumberFormatException exception) {
            LOGGER.warn("Rarity color override key `{}` is not an ordinal, ignoring it", key);
            return -1;
        }
        if (ordinal < 0 || ordinal >= rarityCount) {
            LOGGER.warn("Rarity color override for ordinal {} is out of range, only {} rarities are present",
                    ordinal, rarityCount);
            return -1;
        }
        return ordinal;
    }
}
