package net.powerscale.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.powerscale.logic.EntityScaling;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin {
    @Inject(method = "spawnEntity", at = @At("HEAD"))
    private void pre_spawnEntity(Entity entity, CallbackInfoReturnable<Boolean> info) {
        var world = (ServerWorld) ((Object)this);
        EntityScaling.scale(entity, world);
    }
}
