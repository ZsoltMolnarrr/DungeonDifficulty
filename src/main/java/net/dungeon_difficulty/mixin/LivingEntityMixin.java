package net.dungeon_difficulty.mixin;

import net.dungeon_difficulty.logic.EntityScalable;
import net.dungeon_difficulty.logic.ExperienceScaling;
import net.dungeon_difficulty.logic.PatternMatching;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class LivingEntityMixin implements EntityScalable {

    // MARK: Rescaling safeguard

    private static final String modifiedKey = "dd_scaled";

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void writeCustomDataToNbt_DungeonDifficulty(NbtCompound nbt, CallbackInfo ci) {
        nbt.putBoolean(modifiedKey, isAlreadyScaled_DungeonDifficulty);
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void readCustomDataFromNbt_DungeonDifficulty(NbtCompound nbt, CallbackInfo ci) {
        isAlreadyScaled_DungeonDifficulty = nbt.getBoolean(modifiedKey);
    }

    // MARK: Experience scaling

    @ModifyArg(method = "dropXp", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/ExperienceOrbEntity;spawn(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/util/math/Vec3d;I)V"), index = 2)
    private int modifyDroppedXp_DungeonDifficulty(int xp) {
        var entity = (LivingEntity) (Object) this;
        return ExperienceScaling.scale(entity.getWorld(), entity, xp);
    }

    // MARK: EntityScalable

    private boolean isAlreadyScaled_DungeonDifficulty = false;
    @Override
    public void markAlreadyScaled() {
        isAlreadyScaled_DungeonDifficulty = true;
    }

    @Override
    public boolean isAlreadyScaled() {
        return isAlreadyScaled_DungeonDifficulty;
    }

    private PatternMatching.LocationData locationData_DungeonDifficulty;

    @Override
    public PatternMatching.LocationData getLocationData() {
        return locationData_DungeonDifficulty;
    }

    @Override
    public void setLocationData(PatternMatching.LocationData data) {
        locationData_DungeonDifficulty = data;
    }

}
