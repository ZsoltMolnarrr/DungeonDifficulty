package net.dungeon_difficulty.logic;

import net.dungeon_difficulty.DungeonDifficulty;
import net.dungeon_difficulty.config.Config;
import net.dungeon_difficulty.config.Regex;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.BiomeKeys;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PatternMatching {

    public record BiomeData(String key, List<String> tags) { }

    public record LocationData(String dimensionId, BlockPos position, BiomeData biome) {
        public static LocationData create(ServerWorld world, BlockPos position) {
            var dimensionId = world.getRegistryKey().getValue().toString();
            BiomeData biome = null;
            if (position != null) {
                var biomeKey = world.getBiome(position).getKey().orElse(BiomeKeys.PLAINS);
                var entry = world.getRegistryManager().get(RegistryKeys.BIOME).entryOf(biomeKey);
                var tags = entry.streamTags().map(biomeTagKey -> {
                    return biomeTagKey.id().toString();
                }).toList();
                biome = new BiomeData(biomeKey.getValue().toString(), tags);
                // System.out.println("Biome info! Key: " + biome + " tags: " + tags);
            }
            return new LocationData(dimensionId, position, biome);
        }

        public boolean matches(Config.Dimension.Filters filters) {
            if (filters == null) {
                return true;
            }
            var result = PatternMatching.matches(dimensionId, filters.dimension_regex);
            // System.out.println("PatternMatching - dimension:" + dimensionId + " matches: " + filters.dimension_regex + " - " + result);
            return result;
        }

        public boolean matches(Config.Zone.Filters filters, @Nullable ServerWorld world) {
            if (filters == null || biome == null) {
                return true;
            }
            var result = PatternMatching.matches(biome.key, filters.biome_regex);
            if (filters.biome_tag_regex != null
                    && !filters.biome_tag_regex.isEmpty()
                    && !filters.biome_tag_regex.equals(Regex.ANY)) {
                var foundMatchingTag = false;
                for(var tag: biome.tags) {
                    if (PatternMatching.matches(tag, filters.biome_tag_regex)) {
                        foundMatchingTag = true;
                        break;
                    }
                }
                result = result && foundMatchingTag;
            }
            if (result && filters.structure_id != null) {
                if (world != null) {
                    var key = RegistryKey.of(RegistryKeys.STRUCTURE, new Identifier(filters.structure_id));
                    if (key != null) {
                        result = result && world.getStructureAccessor().getStructureContaining(position, key).hasChildren();
                    }
                } else {
                    result = false;
                }
            }
            // System.out.println("PatternMatching - biome:" + biome + " matches: " + filters.biome_regex + " - " + result);
            return result;
        }
    }

    public record ItemData(
            ItemKind kind,
            String lootTableId,
            String itemId,
            String rarity) {

        public boolean matches(Config.ItemModifier.Filters filters) {
            if (filters == null) {
                return true;
            }
            var result = PatternMatching.matches(itemId, filters.item_id_regex)
                    && PatternMatching.matches(lootTableId, filters.loot_table_regex)
                    && PatternMatching.matches(rarity, filters.rarity_regex);
            // System.out.println("PatternMatching - item:" + itemId + " matches all" + " - " + result);
            return result;
        }
    }

    public enum ItemKind {
        ARMOR, WEAPONS
    }

    public record ItemScaleResult(List<Config.AttributeModifier> modifiers, int level) { }
    public static ItemScaleResult getModifiersForItem(LocationData locationData, ItemData itemData, ServerWorld world) {
        var attributeModifiers = new ArrayList<Config.AttributeModifier>();
        var difficulty = getDifficulty(locationData, world);
        var level = 0;
        if (difficulty != null) {
            level = difficulty.level();
            var rewards = difficulty.type().rewards;
            if (rewards != null) {
                List<Config.ItemModifier> itemModifiers = null;
                switch (itemData.kind) {
                    case ARMOR -> {
                        itemModifiers = rewards.armor;
                    }
                    case WEAPONS -> {
                        itemModifiers = rewards.weapons;
                    }
                }
                if (itemModifiers != null) {
                    for(var entry: itemModifiers) {
                        if (itemData.matches(entry.item_matches)) {
                            attributeModifiers.addAll(Arrays.asList(entry.attributes));
                        }
                    }
                }
            }
        }
        return new ItemScaleResult(attributeModifiers, level);
    }


    public record EntityData(String entityId, boolean isHostile) {
        public static EntityData create(LivingEntity entity) {
            var entityId = Registries.ENTITY_TYPE.getId(entity.getType()).toString();
            var isHostile = entity instanceof Monster;
            return new EntityData(entityId, isHostile);
        }
        public boolean matches(Config.EntityModifier.Filters filters) {
            if (filters == null) {
                return true;
            }
            var matchesAttitude = true;
            if (filters.attitude != null) {
                switch (filters.attitude) {
                    case FRIENDLY -> {
                        matchesAttitude = !isHostile;
                    }
                    case HOSTILE -> {
                        matchesAttitude = isHostile;
                    }
                    case ANY -> {
                        matchesAttitude = true;
                    }
                }
            }
            var result = matchesAttitude && PatternMatching.matches(entityId, filters.entity_id_regex);

            // System.out.println("PatternMatching - dimension:" + entityId + " matches: " + filters.entity_id_regex + " - " + result);
            return result;
        }
    }

    public record EntityScaleResult(List<Config.AttributeModifier> modifiers, int level, float experienceMultiplier) { }

    public static EntityScaleResult getAttributeModifiersForEntity(LocationData locationData, EntityData entityData, ServerWorld world) {
        var attributeModifiers = new ArrayList<Config.AttributeModifier>();
        var difficulty = getDifficulty(locationData, world);
        var level = 0;
        float experienceMultiplier = 0;
        if (difficulty != null) {
            level = difficulty.level();
            for (var modifier: getModifiersForEntity(difficulty.type().entities, entityData)) {
                attributeModifiers.addAll(Arrays.asList(modifier.attributes));
                experienceMultiplier += modifier.experience_multiplier;
            }
        }
        return new EntityScaleResult(attributeModifiers, level, experienceMultiplier);
    }

    public record SpawnerScaleResult(List<Config.SpawnerModifier> modifiers, int level) { }

    public static SpawnerScaleResult getModifiersForSpawner(LocationData locationData, EntityData entityData, ServerWorld world) {
        var spawnerModifiers = new ArrayList<Config.SpawnerModifier>();
        var difficulty = getDifficulty(locationData, world);
        int level = 0;
        if (difficulty != null) {
            level = difficulty.level();
            // System.out.println("Found difficulty for spawner: " + difficulty.type().name + " level " + level);
            for (var modifier: getModifiersForEntity(difficulty.type().entities, entityData)) {
                if (modifier.spawners != null) {
                    spawnerModifiers.add(modifier.spawners);
                }
            }
        }
        return new SpawnerScaleResult(spawnerModifiers, level);
    }

    public static List<Config.EntityModifier> getModifiersForEntity(List<Config.EntityModifier> definitions, EntityData entityData) {
        var entityModifiers = new ArrayList<Config.EntityModifier>();
        for(var entityModifier: definitions) {
            if (entityData.matches(entityModifier.entity_matches)) {
                entityModifiers.add(entityModifier);
            }
        }
        return entityModifiers;
    }

    public record Location(Config.EntityModifier[] entities,
                           Config.Rewards rewards) { }

    @Nullable
    public static Difficulty getDifficulty(LocationData locationData, ServerWorld world) {
        for (var dimension : DungeonDifficulty.config.value.dimensions) {
            if (locationData.matches(dimension.world_matches)) {
                var dimensionDifficulty = findDifficulty(dimension.difficulty);
                if (dimension.zones != null) {
                    for(var zone: dimension.zones) {
                        if(locationData.matches(zone.zone_matches, world)) {
                            var zoneDifficulty = findDifficulty(zone.difficulty);
                            if (zoneDifficulty != null && zoneDifficulty.isValid()) {
                                return zoneDifficulty;
                            }
                        }
                    }
                }
                if (dimensionDifficulty != null && dimensionDifficulty.isValid()) {
                    return dimensionDifficulty;
                }
            }
        }
        return null;
    }

    @Nullable
    private static Difficulty findDifficulty(Config.DifficultyReference reference) {
        if (reference == null) {
            return null;
        }
        var name = reference.name;
        if (name == null || name.isEmpty()) {
            return null;
        }
        for(var entry: DifficultyTypes.resolved) {
            if (name.equals(entry.name)) {
                return new Difficulty(entry, reference.level);
            }
        }
        return null;
    }

    public static boolean matches(String subject, @Nullable String nullableRegex) {
        if (subject == null) {
            subject = "";
        }
        if (nullableRegex == null || nullableRegex.isEmpty()) {
            return true;
        }
        Pattern pattern = Pattern.compile(nullableRegex, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(subject);
        return matcher.find();
    }
}
