package net.dungeon_difficulty.neoforge;

import net.dungeon_difficulty.DungeonDifficulty;
import net.neoforged.fml.common.Mod;

@Mod(DungeonDifficulty.MODID)
public final class NeoForgeMod {
    public NeoForgeMod() {
        // Run our common setup.
        DungeonDifficulty.init();
    }
}
