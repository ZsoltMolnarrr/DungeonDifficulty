package net.dungeon_difficulty.neoforge;

import net.dungeon_difficulty.DungeonDifficulty;
import net.minecraft.registry.RegistryKeys;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.RegisterEvent;

@Mod(DungeonDifficulty.MODID)
public final class NeoForgeMod {
    public NeoForgeMod(IEventBus modBus) {
        // Run our common setup.
        DungeonDifficulty.init();

        modBus.addListener(RegisterEvent.class, NeoForgeMod::register);
    }

    public static void register(RegisterEvent event) {
        event.register(RegistryKeys.LOOT_FUNCTION_TYPE, reg -> {
            DungeonDifficulty.registerLootFunctions();
        });
    }
}
