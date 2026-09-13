package net.dungeon_difficulty.logic;

import net.dungeon_difficulty.DungeonDifficulty;
import net.dungeon_difficulty.mixin.AccessorAttributeContainer;
import net.dungeon_difficulty.mixin.AccessorDefaultAttributeContainer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.UUID;

public class EntityScaling {
    public static void scale(Entity entity, ServerWorld world) {
        if (entity instanceof PlayerEntity) {
            return;
        }
        if (entity instanceof LivingEntity livingEntity) {
            var scalableEntity = ((EntityDifficultyScalable)livingEntity);
            if (scalableEntity.isAlreadyEvaluated(world.getRegistryKey().getValue())) {
                return;
            }
            var locationData = PatternMatching.LocationData.create(world, livingEntity.getBlockPos());
            var entityData = PatternMatching.EntityData.create(livingEntity);
            scalableEntity.setScalingLocationData(locationData);

            var relativeHealth = livingEntity.getHealth() / livingEntity.getMaxHealth();

            EntityScaling.apply(PerPlayerDifficulty.getAttributeModifiers(entityData, world), livingEntity);
            var locationScaling = PatternMatching.getAttributeModifiersForEntity(locationData, entityData, world);
            EntityScaling.apply(locationScaling, livingEntity);

//            if (DungeonDifficulty.config.value.meta.entity_equipment_scaling) {
//                for (var itemStack : livingEntity.getItemsEquipped()) {
//                    ItemScaling.scale(itemStack, world, entityData.entityId(), locationData);
//                }
//            }

            // Store location-based level (ignore per-player scaling)
            scalableEntity.markAlreadyScaled(locationScaling.level());
            scalableEntity.markEvaluated(locationData.dimensionId().toString());
            livingEntity.setHealth(relativeHealth * livingEntity.getMaxHealth());
        }
    }

    /// 1.20.1 modifiers are identified by UUID (1.21 uses an identifier): a stable UUID derived from
    /// `dungeon_difficulty:<scaling name>`, so the "already applied" check survives save/load.
    private static UUID modifierId(String scalingName) {
        return UUID.nameUUIDFromBytes((DungeonDifficulty.MODID + ":" + scalingName).getBytes(StandardCharsets.UTF_8));
    }

    private static void apply(PatternMatching.EntityScaleResult scaling, LivingEntity entity) {
        var level = scaling.level();
        if (level <= 0) { return; }
        for (var modifier: scaling.modifiers()) {
            var pattern = modifier.attribute;
            if (pattern == null || pattern.isEmpty()) {
                continue;
            }

            ArrayList<EntityAttribute> matchingAttributes = new ArrayList<>();
            if (pattern.startsWith(PatternMatching.REGEX_PREFIX)) {
                var regex = pattern.substring(PatternMatching.REGEX_PREFIX.length());
                var instances = ((AccessorDefaultAttributeContainer)
                            ((AccessorAttributeContainer)entity.getAttributes()).getFallback())
                        .getInstances();
                for (var entry : instances.entrySet()) {
                    var key = entry.getKey();
                    if (key == null) { continue; }
                    var id = Registries.ATTRIBUTE.getId(key);
                    if (id != null && PatternMatching.regexMatches(id.toString(), regex)) {
                        matchingAttributes.add(key);
                    }
                }
            } else {
                var attribute = Registries.ATTRIBUTE.getOrEmpty(Identifier.tryParse(modifier.attribute)).orElse(null);
                if (attribute == null || !entity.getAttributes().hasAttribute(attribute)) {
                    continue;
                }
                matchingAttributes.add(attribute);
            }

            var modifierValue = modifier.randomizedValue(level);
            var roundingUnit = modifier.value * 0.25F;
            modifierValue = (float) MathHelper.round(modifierValue, roundingUnit);

            var id = modifierId(scaling.name());
            var name = DungeonDifficulty.MODID + ":" + scaling.name();

            for (var attribute: matchingAttributes) {
                var operation = switch (modifier.operation) {
                    case ADDITION -> EntityAttributeModifier.Operation.ADDITION;
                    case MULTIPLY_BASE -> EntityAttributeModifier.Operation.MULTIPLY_BASE;
                };
                var entityModifier = new EntityAttributeModifier(id, name, modifierValue, operation);
                var instance = entity.getAttributeInstance(attribute);
                if (instance != null && instance.getModifier(id) == null) {
                    instance.addPersistentModifier(entityModifier);
                }
            }
        }
    }
}
