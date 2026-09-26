package net.dungeon_difficulty.effect;

import net.dungeon_difficulty.DungeonDifficulty;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

public class DifficultyEffects {
    public static final Identifier NO_MINING_ID = Identifier.of(DungeonDifficulty.MODID, "no_mining");
    public static RegistryEntry<StatusEffect> NO_MINING;

    /// Called by each loader from its own registration phase
    public static void register() {
        NO_MINING = Registry.registerReference(Registries.STATUS_EFFECT, NO_MINING_ID,
                new NoMiningStatusEffect(NO_MINING_ID, StatusEffectCategory.HARMFUL, 0x4A4217));
    }
}
