package net.dungeon_difficulty.mixin;

import net.dungeon_difficulty.logic.EntityScalable;
import net.dungeon_difficulty.logic.ExperienceScaling;
import net.dungeon_difficulty.logic.PatternMatching;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(LivingEntity.class)
public class LivingEntityMixin implements EntityScalable {
    private PatternMatching.LocationData locationData_DungeonDifficulty;

    @Override
    public PatternMatching.LocationData getLocationData() {
        return locationData_DungeonDifficulty;
    }

    @Override
    public void setLocationData(PatternMatching.LocationData data) {
        locationData_DungeonDifficulty = data;
    }

    @ModifyArg(method = "dropXp", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/ExperienceOrbEntity;spawn(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/util/math/Vec3d;I)V"), index = 2)
    private int modifyDroppedXp_DungeonDifficulty(int xp) {
        var entity = (LivingEntity) (Object) this;
        return ExperienceScaling.scale(entity.getWorld(), entity, xp);
    }
}
