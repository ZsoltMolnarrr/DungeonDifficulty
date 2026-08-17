package net.dungeon_difficulty.mixin;

import net.dungeon_difficulty.logic.RarityColors;
import net.minecraft.util.Formatting;
import net.minecraft.util.Rarity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Rarity.class)
public class RarityMixin {
    // Optional, other mods are known to replace rarity coloring, and this is a cosmetic feature
    @Inject(method = "getFormatting", at = @At("RETURN"), cancellable = true, require = 0)
    private void injected(CallbackInfoReturnable<Formatting> cir) {
        var override = RarityColors.override(((Rarity) (Object) this).ordinal());
        if (override != null && override != cir.getReturnValue()) {
            cir.setReturnValue(override);
        }
    }
}
