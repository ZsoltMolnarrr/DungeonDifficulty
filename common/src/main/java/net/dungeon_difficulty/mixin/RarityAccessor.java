package net.dungeon_difficulty.mixin;

import net.minecraft.util.Formatting;
import net.minecraft.util.Rarity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

/// 1.20.1 has no `Rarity#getFormatting` to hook: vanilla reads the `formatting` field directly, so the color
/// override rewrites the enum constant's field (see `RarityColors`). Forge additionally routes through
/// `getStyleModifier`, covered by the forge module's `RarityMixin`.
@Mixin(Rarity.class)
public interface RarityAccessor {
    @Mutable
    @Accessor("formatting")
    void dungeon_difficulty$setFormatting(Formatting formatting);
}
