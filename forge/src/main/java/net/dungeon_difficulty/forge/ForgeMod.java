package net.dungeon_difficulty.forge;

import net.dungeon_difficulty.DungeonDifficulty;
import net.dungeon_difficulty.forge.loot.LocalScalingLootModifier;
import net.dungeon_difficulty.logic.LocalScalingLootFunction;
import net.minecraft.registry.RegistryKeys;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;

/// Forge 47 entrypoint (1.20.1 port). NeoForge 1.20.1 is a fork of Forge 47.1 that kept the Forge packages
/// and FML, so this jar loads there unchanged; only APIs present at 47.1 are used.
@Mod(DungeonDifficulty.MODID)
public final class ForgeMod {
    // FMLJavaModLoadingContext.get() is flagged for removal by late 47.x builds, but the
    // constructor-injected replacement doesn't exist on early 47.x; get() works on all of [47,).
    @SuppressWarnings("removal")
    public ForgeMod() {
        // Run our common setup.
        DungeonDifficulty.init();

        var modBus = FMLJavaModLoadingContext.get().getModEventBus();
        // Explicit event classes: Forge 47's plain addListener(Consumer) infers the event type from the
        // lambda via TypeTools, which is fragile; the 4-arg overload takes it directly.
        modBus.addListener(EventPriority.NORMAL, false, RegisterEvent.class, ForgeMod::register);
        MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, RegisterCommandsEvent.class, event ->
                DungeonDifficulty.registerCommands(event.getDispatcher()));
    }

    /// Forge locks every vanilla registry outside its `RegisterEvent` window (and 47.0–47.3 reject a plain
    /// `Registry.register` even inside it), so registration goes through the event's helper.
    public static void register(RegisterEvent event) {
        // Registered so loot tables serialized with the Fabric-side function (shared datapacks) still parse.
        event.register(RegistryKeys.LOOT_FUNCTION_TYPE, helper ->
                helper.register(LocalScalingLootFunction.ID, LocalScalingLootFunction.TYPE));
        // Loot scaling is applied by a global loot modifier, declared in `data/forge/loot_modifiers`
        event.register(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, helper ->
                helper.register(LocalScalingLootModifier.ID, LocalScalingLootModifier.CODEC));
    }
}
