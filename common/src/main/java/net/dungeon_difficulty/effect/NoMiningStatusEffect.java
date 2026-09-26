package net.dungeon_difficulty.effect;

import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

import java.util.function.BiConsumer;

/// Slows down block breaking the same way as Mining Fatigue, without the attack speed penalty.
/// Vanilla Mining Fatigue hardcodes its break speed multipliers, so the same values are applied here
/// via the `player.block_break_speed` attribute instead.
public class NoMiningStatusEffect extends StatusEffect {
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
}
