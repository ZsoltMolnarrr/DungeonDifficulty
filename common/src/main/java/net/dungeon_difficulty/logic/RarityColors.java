package net.dungeon_difficulty.logic;

import com.mojang.logging.LogUtils;
import net.dungeon_difficulty.DungeonDifficulty;
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

    /**
     * Resolves the configured overrides against the `Rarity` constants present at runtime.
     * Rarities added by other mods are extended into the enum when `Rarity` is class loaded,
     * so this has to run after mod loading.
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
