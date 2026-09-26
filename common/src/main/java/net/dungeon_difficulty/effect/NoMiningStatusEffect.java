package net.dungeon_difficulty.effect;

import net.dungeon_difficulty.DungeonDifficulty;
import net.minecraft.block.Block;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

import java.util.function.BiConsumer;

/// Slows down block breaking the same way as Mining Fatigue, without the attack speed penalty.
/// Vanilla Mining Fatigue hardcodes its break speed multipliers, so the same values are applied here
/// via the `player.block_break_speed` attribute instead.
public class NoMiningStatusEffect extends StatusEffect {
    /// Blocks breaking at normal speed despite the effect. Block tags are synced to clients,
    /// so both sides agree on breaking progress.
    public static final TagKey<Block> EXEMPT_BLOCKS = TagKey.of(RegistryKeys.BLOCK,
            Identifier.of(DungeonDifficulty.MODID, "no_mining_exempt"));

    private final Identifier modifierId;

    public NoMiningStatusEffect(Identifier id, StatusEffectCategory category, int color) {
        super(category, color);
        this.modifierId = id;
        // Registered so the vanilla `onRemoved` finds and removes the modifier by id
        addAttributeModifier(EntityAttributes.PLAYER_BLOCK_BREAK_SPEED, id, amount(0),
                EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    }

    // Same as vanilla Mining Fatigue in `PlayerEntity.getBlockBreakingSpeed`
    private static float multiplier(int amplifier) {
        return switch (amplifier) {
            case 0 -> 0.3F;
            case 1 -> 0.09F;
            case 2 -> 0.0027F;
            default -> 8.1E-4F;
        };
    }

    private static double amount(int amplifier) {
        return multiplier(amplifier) - 1.0;
    }

    private EntityAttributeModifier createModifier(int amplifier) {
        return new EntityAttributeModifier(modifierId, amount(amplifier), EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    }

    @Override
    public void forEachAttributeModifier(int amplifier, BiConsumer<RegistryEntry<EntityAttribute>, EntityAttributeModifier> consumer) {
        consumer.accept(EntityAttributes.PLAYER_BLOCK_BREAK_SPEED, createModifier(amplifier));
    }

    @Override
    public void onApplied(AttributeContainer attributeContainer, int amplifier) {
        var instance = attributeContainer.getCustomInstance(EntityAttributes.PLAYER_BLOCK_BREAK_SPEED);
        if (instance != null) {
            instance.removeModifier(modifierId);
            instance.addPersistentModifier(createModifier(amplifier));
        }
    }

    /// Multiplier restoring the block breaking speed the player would have without this effect.
    /// Recomputes the attribute without our modifier (same order and clamping as vanilla),
    /// rather than dividing by the multiplier, to stay exact near the attribute's bounds.
    public float exemptionFactor(PlayerEntity player) {
        var instance = player.getAttributeInstance(EntityAttributes.PLAYER_BLOCK_BREAK_SPEED);
        if (instance == null || !instance.hasModifier(modifierId)) {
            return 1F;
        }
        var withModifier = instance.getValue();
        if (withModifier <= 0) {
            // Zeroed by something else, nothing to restore
            return 1F;
        }

        double base = instance.getBaseValue();
        double addMultipliedBase = 0;
        double total = 1;
        for (var modifier : instance.getModifiers()) {
            if (modifier.id().equals(modifierId)) {
                continue;
            }
            switch (modifier.operation()) {
                case ADD_VALUE -> base += modifier.value();
                case ADD_MULTIPLIED_BASE -> addMultipliedBase += modifier.value();
                case ADD_MULTIPLIED_TOTAL -> total *= 1.0 + modifier.value();
            }
        }
        var withoutModifier = instance.getAttribute().value().clamp((base + base * addMultipliedBase) * total);
        return (float) (withoutModifier / withModifier);
    }
}
