package net.dungeon_difficulty.logic;

import net.dungeon_difficulty.mixin.AccessorAttributeContainer;
import net.dungeon_difficulty.mixin.AccessorDefaultAttributeContainer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

import java.util.ArrayList;

public class EntityScaling {
    public static void scale(Entity entity, ServerWorld world) {
        if (entity instanceof PlayerEntity) {
            return;
        }
        if (entity instanceof LivingEntity livingEntity) {
            var scalableEntity = ((EntityDifficultyScalable)livingEntity);
            if (scalableEntity.isAlreadyScaled()) {
                return;
            }
            var locationData = PatternMatching.LocationData.create(world, livingEntity.getBlockPos());
            var entityData = PatternMatching.EntityData.create(livingEntity);
            scalableEntity.setScalingLocationData(locationData);

            var relativeHealth = livingEntity.getHealth() / livingEntity.getMaxHealth();

            EntityScaling.apply(PerPlayerDifficulty.getAttributeModifiers(entityData, world), livingEntity);
            EntityScaling.apply(PatternMatching.getAttributeModifiersForEntity(locationData, entityData, world), livingEntity);

//            if (DungeonDifficulty.config.value.meta.entity_equipment_scaling) {
//                for (var itemStack : livingEntity.getItemsEquipped()) {
//                    ItemScaling.scale(itemStack, world, entityData.entityId(), locationData);
//                }
//            }

            scalableEntity.markAlreadyScaled();
            livingEntity.setHealth(relativeHealth * livingEntity.getMaxHealth());
        }
    }

    private static void apply(PatternMatching.EntityScaleResult scaling, LivingEntity entity) {
        var level = scaling.level();
        if (level <= 0) { return; }
        for (var modifier: scaling.modifiers()) {
            var pattern = modifier.attribute;
            if (pattern == null || pattern.isEmpty()) {
                continue;
            }

            ArrayList< RegistryEntry< EntityAttribute>> matchingAttributes = new ArrayList<>();
            if (pattern.startsWith(PatternMatching.REGEX_PREFIX)) {
                var regex = pattern.substring(PatternMatching.REGEX_PREFIX.length());
                var instances = ((AccessorDefaultAttributeContainer)
                            (AccessorAttributeContainer)entity.getAttributes())
                        .getInstances();
                for (var entry : instances.entrySet()) {
                    var key = entry.getKey();
                    if (key == null || key.value() == null) { continue; }
                    var id = key.value().toString();
                    if (PatternMatching.regexMatches(id, regex)) {
                        matchingAttributes.add(entry.getKey());
                    }
                }
            } else {
                var attribute = Registries.ATTRIBUTE.getEntry(Identifier.of(modifier.attribute)).orElse(null);
                if (attribute == null || !entity.getAttributes().hasAttribute(attribute)) {
                    continue;
                }
            }

            var modifierValue = modifier.randomizedValue(level);

            for (var attribute: matchingAttributes) {
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
}
