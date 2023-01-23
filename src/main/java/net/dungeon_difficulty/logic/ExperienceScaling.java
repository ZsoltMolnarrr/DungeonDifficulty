package net.dungeon_difficulty.logic;

import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;

public class ExperienceScaling {
    public static int scale(World world, LivingEntity entity, int experience) {
        var locationData = ((EntityScalable)entity).getLocationData();
        if (locationData == null) {
            locationData = PatternMatching.LocationData.create(world, entity.getBlockPos());
        }
        var entityData = PatternMatching.EntityData.create(entity);
        var scaling = PatternMatching.getAttributeModifiersForEntity(locationData, entityData);
        var xp = experience;
        if (scaling != null) {
            // System.out.println("scaling.experienceMultiplier(): " + scaling.experienceMultiplier() + " scaling.level(): " + scaling.level());
            xp = Math.round((float) experience * (1F + scaling.experienceMultiplier() * scaling.level()));
            // System.out.println("Scaled XP from: " + experience + " to: " + xp);
        }
        return xp;
    }
}
