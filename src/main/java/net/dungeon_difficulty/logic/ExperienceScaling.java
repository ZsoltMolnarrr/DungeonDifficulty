package net.dungeon_difficulty.logic;

import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;

public class ExperienceScaling {
    public static int scale(World world, LivingEntity entity, int experience) {
        var locationData = PatternMatching.LocationData.create(world, entity.getBlockPos());
        var entityData = PatternMatching.EntityData.create(entity);
        float multiplier = 1.0F;
//        for (var modifier: PatternMatching.getModifiersForEntity(locationData, entityData)) {
//            multiplier *= modifier.experience_multiplier;
//        }
        var xp = Math.round((float)experience * multiplier);
        // System.out.println("Scaled XP from: " + experience + " to: " + xp);
        return xp;
    }
}
