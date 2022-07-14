package net.powerscale.logic;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.powerscale.config.Config;

import java.util.List;

public class EntityScaling {
    public static void scale(Entity entity, World world) {
        if (entity instanceof LivingEntity) {
            var livingEntity = (LivingEntity)entity;
            var locationData = PatternMatching.LocationData.create(world, livingEntity.getBlockPos());
            var entityData = PatternMatching.EntityData.create(livingEntity);
            var attributeModifiers = PatternMatching.getModifiersForEntity(locationData, entityData);

            EntityScaling.apply(attributeModifiers, livingEntity);

            for (var itemStack: livingEntity.getItemsEquipped()) {
                ItemScaling.scale(itemStack, world, livingEntity.getBlockPos(), entityData.entityId());
            }
        }
    }

    private static void apply(List<Config.AttributeModifier> attributeModifiers, LivingEntity entity) {
        var relativeHealth = entity.getHealth() / entity.getMaxHealth();
        for (var modifier: attributeModifiers) {
            if (modifier.attribute == null) {
                continue;
            }
            var attribute = Registry.ATTRIBUTE.get(new Identifier(modifier.attribute));

            switch (modifier.operation) {
                case ADD -> {
                    var entityAttribute = entity.getAttributeInstance(attribute);
                    if (entityAttribute != null) {
                        entityAttribute.setBaseValue(entityAttribute.getBaseValue() + modifier.value);
                    }
                }
                case MULTIPLY -> {
                    var defaultValue = entity.getAttributeValue(attribute);
                    if (defaultValue > 0) {
                        entity.getAttributeInstance(attribute).setBaseValue(defaultValue * modifier.value);
                    }
                }
            }
        }
        entity.setHealth(relativeHealth * entity.getMaxHealth());
    }
}
