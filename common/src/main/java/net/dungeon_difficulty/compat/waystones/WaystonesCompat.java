package net.dungeon_difficulty.compat.waystones;

import com.mojang.logging.LogUtils;
import net.dungeon_difficulty.Platform;
import org.slf4j.Logger;

/// Optional Waystones integration: village waystones take the name of their village.
/// Treated as fragile: this class never references Waystones or Balm types, and any failure
/// (missing mods, changed API) only disables the integration with an error log.
public class WaystonesCompat {
    static final Logger LOGGER = LogUtils.getLogger();

    public static void init() {
        if (!Platform.util().isModLoaded("waystones") || !Platform.util().isModLoaded("balm")) {
            return;
        }
        try {
            WaystonesNameHandler.register();
        } catch (Throwable throwable) {
            LOGGER.error("Dungeon Difficulty: failed to set up Waystones integration, it is disabled", throwable);
        }
    }
}
