package net.dungeon_difficulty.fabric;

import net.dungeon_difficulty.DungeonDifficulty;
import net.dungeon_difficulty.logic.LocalScalingLootFunction;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;

import java.util.List;

public final class FabricMod implements ModInitializer {
    @Override
    public void onInitialize() {
        DungeonDifficulty.init();
        DungeonDifficulty.registerLootFunctions();

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                DungeonDifficulty.registerCommands(dispatcher));

        // Every loot table gets the local scaling function, which scales items by the table's id and drop location
        LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) ->
                tableBuilder.apply(new LocalScalingLootFunction(List.of(), key.getValue())));
    }
}
