package net.dungeon_difficulty.mixin;

import net.dungeon_difficulty.effect.DifficultyEffects;
import net.dungeon_difficulty.effect.NoMiningStatusEffect;
import net.minecraft.block.AbstractBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Block state level (not `Block`), so blocks overriding the calculation are covered too.
// Runs on both sides: client breaking progress and server validation must match.
@Mixin(AbstractBlock.AbstractBlockState.class)
public abstract class BlockStateMixin {
    @Inject(method = "calcBlockBreakingDelta", at = @At("RETURN"), cancellable = true)
    private void calcBlockBreakingDelta_NoMiningExemption(PlayerEntity player, BlockView world, BlockPos pos, CallbackInfoReturnable<Float> cir) {
        var noMining = DifficultyEffects.NO_MINING;
        if (noMining == null || !player.hasStatusEffect(noMining)) {
            return;
        }
        var state = (AbstractBlock.AbstractBlockState) (Object) this;
        if (!state.isIn(NoMiningStatusEffect.EXEMPT_BLOCKS)) {
            return;
        }
        var factor = ((NoMiningStatusEffect) noMining.value()).exemptionFactor(player);
        if (factor != 1F) {
            cir.setReturnValue(cir.getReturnValue() * factor);
        }
    }
}
