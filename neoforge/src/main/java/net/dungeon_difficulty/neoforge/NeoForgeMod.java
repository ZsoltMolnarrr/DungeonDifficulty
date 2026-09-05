package net.dungeon_difficulty.neoforge;

import net.dungeon_difficulty.DungeonDifficulty;
import net.dungeon_difficulty.neoforge.loot.LocalScalingLootModifier;
import net.minecraft.registry.RegistryKeys;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.RegisterEvent;

@Mod(DungeonDifficulty.MODID)
public final class NeoForgeMod {
    public NeoForgeMod(IEventBus modBus) {
        // Run our common setup.
        DungeonDifficulty.init();

        modBus.addListener(RegisterEvent.class, NeoForgeMod::register);
        NeoForge.EVENT_BUS.addListener(RegisterCommandsEvent.class, event ->
                DungeonDifficulty.registerCommands(event.getDispatcher()));
    }

    public static void register(RegisterEvent event) {
        event.register(RegistryKeys.LOOT_FUNCTION_TYPE, reg -> {
            DungeonDifficulty.registerLootFunctions();
        });
        // Loot scaling is applied by a global loot modifier, declared in `data/neoforge/loot_modifiers`
        event.register(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, helper ->
                helper.register(LocalScalingLootModifier.ID, LocalScalingLootModifier.CODEC));
    }
}
