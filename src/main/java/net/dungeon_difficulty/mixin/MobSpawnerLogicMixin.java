package net.dungeon_difficulty.mixin;

import net.minecraft.entity.mob.Monster;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.MobSpawnerEntry;
import net.minecraft.world.MobSpawnerLogic;
import net.dungeon_difficulty.DungeonDifficulty;
import net.dungeon_difficulty.config.Config;
import net.dungeon_difficulty.logic.PatternMatching;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(MobSpawnerLogic.class)
public class MobSpawnerLogicMixin {
    @Shadow private int spawnRange;
    @Shadow private int spawnCount;
    @Shadow private int maxNearbyEntities;
    @Shadow private int minSpawnDelay;
    @Shadow private int maxSpawnDelay;
    @Shadow private int requiredPlayerRange;

    @Shadow private MobSpawnerEntry spawnEntry;
    private boolean initialized = false;

    private static String modifiedKey = "modified_by_" + DungeonDifficulty.MODID;

    @Inject(method = "serverTick", at = @At("HEAD"))
    private void pre_serverTick(ServerWorld world, BlockPos pos, CallbackInfo ci) {
        if(!initialized) {
            initialized = true;

            if(this.spawnEntry.getNbt().contains(modifiedKey)) {
                return;
            }

            try {
                var entityId = this.spawnEntry.getNbt().getString("id");
                var entityType = Registries.ENTITY_TYPE.get(new Identifier(entityId));
                var testEntity = entityType.create(world);
                var isMonster = testEntity instanceof Monster;
                var entityData = new PatternMatching.EntityData(entityId, isMonster);
                var locationData = PatternMatching.LocationData.create(world, pos);
                var modifiers = PatternMatching.getModifiersForSpawner(locationData, entityData);
//                if (modifiers.size() > 0) {
//                    System.out.println("Scaling spawner of: " + entityId + " at: " + pos);
//                }
                scaleSpawner(modifiers);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void scaleSpawner(List<Config.SpawnerModifier> modifiers) {
        for(var modifier: modifiers) {
            this.spawnRange = Math.round(spawnRange * modifier.spawn_range_multiplier);
            this.spawnCount = Math.round(spawnCount * modifier.spawn_count_multiplier);
            this.maxNearbyEntities = Math.round(maxNearbyEntities * modifier.max_nearby_entities_multiplier);
            this.minSpawnDelay = Math.round(minSpawnDelay * modifier.min_spawn_delay_multiplier);
            this.maxSpawnDelay = Math.round(maxSpawnDelay * modifier.max_spawn_delay_multiplier);
            this.requiredPlayerRange = Math.round(requiredPlayerRange * modifier.required_player_range_multiplier);
        }
        if (modifiers.size() > 0) {
            this.spawnEntry.getNbt().putBoolean(modifiedKey, true);
//            System.out.println("Spawner scaled");
//            System.out.println(" spawnRange:" + spawnRange
//                    + " spawnCount:" + spawnCount
//                    + " maxNearbyEntities:" + maxNearbyEntities
//                    + " minSpawnDelay:" + minSpawnDelay
//                    + " maxSpawnDelay:" + maxSpawnDelay
//                    + " requiredPlayerRange:" + requiredPlayerRange);
        }
    }
}
