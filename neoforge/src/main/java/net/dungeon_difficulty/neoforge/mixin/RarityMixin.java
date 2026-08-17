package net.dungeon_difficulty.neoforge.mixin;

import net.dungeon_difficulty.logic.RarityColors;
import net.minecraft.text.Style;
import net.minecraft.util.Rarity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.UnaryOperator;

/**
 * NeoForge gives `Rarity` a style modifier, and reads that in tooltips, hover names and the held
 * item name, instead of the plain formatting the common `RarityMixin` covers. Only the color is
 * replaced here, so any other styling a modded rarity applies is kept.
 */
@Mixin(Rarity.class)
public class RarityMixin {
    // Optional, other mods are known to replace rarity coloring, and this is a cosmetic feature
    @Inject(method = "getStyleModifier", at = @At("RETURN"), cancellable = true, require = 0)
    private void injected(CallbackInfoReturnable<UnaryOperator<Style>> cir) {
        var override = RarityColors.override(((Rarity) (Object) this).ordinal());
        if (override != null) {
            var original = cir.getReturnValue();
            cir.setReturnValue(style -> original.apply(style).withColor(override));
        }
    }
}
