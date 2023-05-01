package net.dungeon_difficulty.mixin;

import net.dungeon_difficulty.DungeonDifficulty;
import net.dungeon_difficulty.logic.MathHelper;
import net.dungeon_difficulty.logic.PatternMatching;
import net.minecraft.entity.mob.Monster;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.MobSpawnerEntry;
import net.minecraft.world.MobSpawnerLogic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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
            if(this.spawnEntry == null
                    || this.spawnEntry.getNbt() == null
                    || this.spawnEntry.getNbt().contains(modifiedKey)) {
                return;
            }

            try {
                var entityId = this.spawnEntry.getNbt().getString("id");
                var entityType = Registry.ENTITY_TYPE.get(new Identifier(entityId));
                var testEntity = entityType.create(world);
                var isMonster = testEntity instanceof Monster;
                var entityData = new PatternMatching.EntityData(entityId, isMonster);
                var locationData = PatternMatching.LocationData.create(world, pos);
                var scaling = PatternMatching.getModifiersForSpawner(locationData, entityData, world);
//                if (modifiers.size() > 0) {
//                    System.out.println("Scaling spawner of: " + entityId + " at: " + pos);
//                }
                scaleSpawner(scaling);
                initialized = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void scaleSpawner(PatternMatching.SpawnerScaleResult scaling) {
//        if (scaling.modifiers().size() > 0) {
//            System.out.println("Spawner before scaling");
//            System.out.println(" spawnRange:" + this.spawnRange
//                    + " spawnCount:" + this.spawnCount
//                    + " maxNearbyEntities:" + this.maxNearbyEntities
//                    + " minSpawnDelay:" + this.minSpawnDelay
//                    + " maxSpawnDelay:" + this.maxSpawnDelay
//                    + " requiredPlayerRange:" + this.requiredPlayerRange);
//        }
        float spawnRange = 0;
        float spawnCount = 0;
        float maxNearbyEntities = 0;
        float minSpawnDelay = 0;
        float maxSpawnDelay = 0;
        float requiredPlayerRange = 0;
        for(var modifier: scaling.modifiers()) {
            spawnRange += scaling.level() * modifier.spawn_range_multiplier;
            spawnCount += scaling.level() * modifier.spawn_count_multiplier;
            maxNearbyEntities += scaling.level() * modifier.max_nearby_entities_multiplier;
            minSpawnDelay += scaling.level() * modifier.min_spawn_delay_multiplier;
            maxSpawnDelay += scaling.level() * modifier.max_spawn_delay_multiplier;
            requiredPlayerRange += scaling.level() * modifier.required_player_range_multiplier;
        }
        this.spawnRange = MathHelper.clamp(Math.round(this.spawnRange * (1F + spawnRange)), 0, 100);
        this.spawnCount = MathHelper.clamp(Math.round(this.spawnCount * (1F + spawnCount)), 1, 20);
        this.maxNearbyEntities = MathHelper.clamp(Math.round(this.maxNearbyEntities * (1F + maxNearbyEntities)), 0, 40);
        this.minSpawnDelay = MathHelper.clamp(Math.round(this.minSpawnDelay * (1F + minSpawnDelay)), 10, 20000);
        this.maxSpawnDelay = MathHelper.clamp(Math.round(this.maxSpawnDelay * (1F + maxSpawnDelay)), 20, 20000);
        this.requiredPlayerRange = MathHelper.clamp(Math.round(this.requiredPlayerRange * (1F + requiredPlayerRange)), 1, 200);

        if (scaling.modifiers().size() > 0) {
            this.spawnEntry.getNbt().putBoolean(modifiedKey, true);
//            System.out.println("Spawner scaled");
//            System.out.println(" spawnRange:" + this.spawnRange
//                    + " spawnCount:" + this.spawnCount
//                    + " maxNearbyEntities:" + this.maxNearbyEntities
//                    + " minSpawnDelay:" + this.minSpawnDelay
//                    + " maxSpawnDelay:" + this.maxSpawnDelay
//                    + " requiredPlayerRange:" + this.requiredPlayerRange);
        }
    }
}
