package net.dungeon_difficulty.mixin;

import net.dungeon_difficulty.logic.EntityDifficultyScalable;
import net.dungeon_difficulty.logic.ExperienceScaling;
import net.dungeon_difficulty.logic.PatternMatching;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class LivingEntityMixin implements EntityDifficultyScalable {

    // MARK: Rescaling safeguard

    private static final String modifiedKey = "dd_scaled";
    private static final int NOT_SCALED = 0;

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void writeCustomDataToNbt_DungeonDifficulty(NbtCompound nbt, CallbackInfo ci) {
        nbt.putInt(modifiedKey, scalingLevel_DungeonDifficulty);
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void readCustomDataFromNbt_DungeonDifficulty(NbtCompound nbt, CallbackInfo ci) {
        // Migration: Check NBT type
        if (nbt.getBoolean(modifiedKey)) {
            scalingLevel_DungeonDifficulty = 1;
        } else if (nbt.getInt(modifiedKey) > 0) {  // 3 = INT
            // New format: read directly
            scalingLevel_DungeonDifficulty = nbt.getInt(modifiedKey);
        } else {
            // Missing key: not scaled
            scalingLevel_DungeonDifficulty = NOT_SCALED;
        }
    }

    // MARK: Experience scaling

    @ModifyArg(method = "dropXp", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/ExperienceOrbEntity;spawn(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/util/math/Vec3d;I)V"), index = 2)
    private int modifyDroppedXp_DungeonDifficulty(int xp) {
        var entity = (LivingEntity) (Object) this;
        return ExperienceScaling.scale((ServerWorld) entity.getWorld(), entity, xp);
    }

    // MARK: EntityScalable

    private int scalingLevel_DungeonDifficulty = NOT_SCALED;

    @Override
    public int getScalingLevel() {
        return scalingLevel_DungeonDifficulty;
    }

    @Override
    public void markAlreadyScaled(int level) {
        scalingLevel_DungeonDifficulty = level;  // Backward compatibility
    }

    private PatternMatching.LocationData locationData_DungeonDifficulty;

    @Override
    public PatternMatching.LocationData getScalingLocationData() {
        return locationData_DungeonDifficulty;
    }

    @Override
    public void setScalingLocationData(PatternMatching.LocationData data) {
        locationData_DungeonDifficulty = data;
    }

}
