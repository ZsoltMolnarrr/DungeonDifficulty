package net.dungeon_difficulty.fabric;

import net.dungeon_difficulty.DungeonDifficulty;
import net.fabricmc.api.ModInitializer;

public final class FabricMod implements ModInitializer {
    @Override
    public void onInitialize() {
        DungeonDifficulty.init();
    }
}
