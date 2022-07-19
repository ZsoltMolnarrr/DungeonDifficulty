package net.powerscale.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.powerscale.logic.ExperienceScaling;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(MobEntity.class)
public class MobEntityMixin {
    @ModifyVariable(method = "getXpToDrop", at = @At(value = "RETURN", ordinal = 0))
    private int modifyDroppedXP(int xp) {
        var entity = (LivingEntity) (Object) this;
        return ExperienceScaling.scale(entity.getWorld(), entity, xp);
    }
}
