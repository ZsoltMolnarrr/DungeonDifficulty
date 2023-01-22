package net.dungeon_difficulty.logic;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

public class EntityScaling {
    public static void scale(Entity entity, World world) {
        if (entity instanceof LivingEntity) {
            var livingEntity = (LivingEntity)entity;
            var locationData = PatternMatching.LocationData.create(world, livingEntity.getBlockPos());
            var entityData = PatternMatching.EntityData.create(livingEntity);

            // EntityScaling.apply(PerPlayerDifficulty.getAttributeModifiers(entityData, world), livingEntity);
            EntityScaling.apply(PatternMatching.getAttributeModifiersForEntity(locationData, entityData), livingEntity);

            for (var itemStack: livingEntity.getItemsEquipped()) {
                ItemScaling.scale(itemStack, world, entityData.entityId(), locationData);
            }
        }
    }

    private static void apply(PatternMatching.EntityScaleResult scaling, LivingEntity entity) {
        var relativeHealth = entity.getHealth() / entity.getMaxHealth();
        var level = scaling.level();
        if (level <= 0) { return; }
        for (var modifier: scaling.modifiers()) {
            if (modifier.attribute == null) {
                continue;
            }
            var attribute = Registry.ATTRIBUTE.get(new Identifier(modifier.attribute));
            if (!entity.getAttributes().hasAttribute(attribute)) {
                continue;
            }

            var modifierValue = modifier.randomizedValue(level);

            switch (modifier.operation) {
                case ADDITION -> {
                    var entityAttribute = entity.getAttributeInstance(attribute);
                    if (entityAttribute != null) {
                        entityAttribute.setBaseValue(entityAttribute.getBaseValue() + modifierValue);
                    }
                }
                case MULTIPLY_BASE -> {
                    var defaultValue = entity.getAttributeValue(attribute);
                    if (defaultValue > 0) {
                        entity.getAttributeInstance(attribute).setBaseValue(defaultValue * (1F + modifierValue));
                    }
                }
            }
        }
        entity.setHealth(relativeHealth * entity.getMaxHealth());
    }
}
