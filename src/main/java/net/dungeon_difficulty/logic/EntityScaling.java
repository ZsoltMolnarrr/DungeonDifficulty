package net.dungeon_difficulty.logic;

import net.dungeon_difficulty.DungeonDifficulty;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

public class EntityScaling {
    public static void scale(Entity entity, ServerWorld world) {
        if (entity instanceof LivingEntity) {
            var livingEntity = (LivingEntity)entity;
            var scalableEntity = ((EntityScalable)livingEntity);
            if (scalableEntity.isAlreadyScaled()) {
                return;
            }
            var locationData = PatternMatching.LocationData.create(world, livingEntity.getBlockPos());
            var entityData = PatternMatching.EntityData.create(livingEntity);
            scalableEntity.setLocationData(locationData);

            var relativeHealth = livingEntity.getHealth() / livingEntity.getMaxHealth();

            EntityScaling.apply(PerPlayerDifficulty.getAttributeModifiers(entityData, world), livingEntity);
            EntityScaling.apply(PatternMatching.getAttributeModifiersForEntity(locationData, entityData, world), livingEntity);

            if (DungeonDifficulty.config.value.meta.entity_equipment_scaling) {
                for (var itemStack : livingEntity.getItemsEquipped()) {
                    ItemScaling.scale(itemStack, world, entityData.entityId(), locationData);
                }
            }

            scalableEntity.markAlreadyScaled();
            livingEntity.setHealth(relativeHealth * livingEntity.getMaxHealth());
        }
    }

    private static void apply(PatternMatching.EntityScaleResult scaling, LivingEntity entity) {
        var level = scaling.level();
        if (level <= 0) { return; }
        for (var modifier: scaling.modifiers()) {
            if (modifier.attribute == null) {
                continue;
            }
            var attribute = Registries.ATTRIBUTE.get(new Identifier(modifier.attribute));
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
    }
}
