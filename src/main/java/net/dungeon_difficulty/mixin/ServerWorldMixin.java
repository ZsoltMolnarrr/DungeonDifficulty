package net.dungeon_difficulty.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.dungeon_difficulty.logic.EntityScaling;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.stream.Stream;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin {
    // Logic moved to `ServerEntityManager` mixin, to fix entities spawning with structures
//    @Inject(method = "addEntity", at = @At("HEAD"))
//    private void pre_addEntity(Entity entity, CallbackInfoReturnable<Boolean> cir) {
//        System.out.println("Adding entity: " + entity.getName());
//        if (entity.getName().toString().contains("minecraft.piglin")) {
//            System.out.println("Spawning piglin!");
//        }
//        var world = (ServerWorld) ((Object)this);
//        EntityScaling.scale(entity, world);
//    }
}
