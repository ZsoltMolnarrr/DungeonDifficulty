package net.dungeon_difficulty.mixin;

import net.dungeon_difficulty.logic.EntityScaling;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerEntityManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.entity.EntityLike;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerEntityManager.class)
public class ServerEntityManagerMixin<T extends EntityLike> {
    @Inject(method = "addEntity(Lnet/minecraft/world/entity/EntityLike;Z)Z", at = @At("HEAD"))
    private void onAddEntity(T e, boolean existing, CallbackInfoReturnable<Boolean> cir) {
        if (e instanceof Entity entity && entity.getWorld() != null) {
            var world = (ServerWorld) ((Object)entity.getWorld());
            EntityScaling.scale(entity, world);
        }
    }
}
