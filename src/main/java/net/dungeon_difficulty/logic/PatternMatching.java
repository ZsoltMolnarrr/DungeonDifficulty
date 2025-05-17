package net.dungeon_difficulty.logic;

import net.dungeon_difficulty.DungeonDifficulty;
import net.dungeon_difficulty.config.Config;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureStart;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.structure.Structure;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PatternMatching {

    public record BiomeData(RegistryEntry<Biome> biomeEntry) { }

    public record LocationData(String dimensionId, BlockPos position, BiomeData biome) {
        public static LocationData create(ServerWorld world, BlockPos position) {
            var dimensionId = world.getRegistryKey().getValue().toString();
            BiomeData biome = null;
            if (position != null) {
                biome = new BiomeData(world.getBiome(position));
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

        public enum Scope { DIMENSION, BIOME, STRUCTURE }
        public record Match(boolean matches, Scope scope,
                            @Nullable RegistryEntry<Biome> matchingBiome,
                            @Nullable RegistryEntry<Structure> matchingStructure) {
            public static Match trueMatch() {
                return new Match(true, Scope.DIMENSION, null, null);
            }
            public static Match falseMatch() {
                return new Match(false, Scope.DIMENSION, null, null);
            }
            @Nullable public Identifier id() {
                if (matchingBiome != null) {
                    return matchingBiome.getKey().get().getValue();
                }
                if (matchingStructure != null) {
                    return matchingStructure.getKey().get().getValue();
                }
                return null;
            }
        }

        public Match matches(Config.Zone.Filters filters, @Nullable ServerWorld world) {
            if (filters == null || biome == null) {
                return Match.trueMatch();
            }
            var result = false;
            if (world == null) {
                return Match.falseMatch();
            }
            var registries = world.getServer().getRegistryManager();

            // Biome pattern matching

            Scope matchScope = Scope.DIMENSION;
            RegistryEntry<Biome> matchingBiome = null;


            if (filters.biome == null || filters.biome.isEmpty()) {
                result = true;
            } else if (universalMatch(biome.biomeEntry, RegistryKeys.BIOME, filters.biome)) {
                result = true;
                matchingBiome = biome.biomeEntry;
                matchScope = Scope.BIOME;
            }

            // Structure pattern matching

            RegistryEntry<Structure> matchingStructure = null;

            if (result && filters.structure != null && !filters.structure.isEmpty()) {
                result = false;
                var registry = registries.get(RegistryKeys.STRUCTURE);
                var structureStartsUnfiltered = world.getStructureAccessor().getStructureStarts(new ChunkPos(position), s -> true);
                for (var structureStart : structureStartsUnfiltered) {
                    var entry = registry.getEntry(registry.getRawId(structureStart.getStructure())).orElse(null);
                    if (entry != null
                            && PatternMatching.universalMatch(entry, RegistryKeys.STRUCTURE, filters.structure)
                            && isInsideStructure(world, position, structureStart)) {
                        matchingStructure = entry;
                        matchScope = Scope.STRUCTURE;
                        result = true;
                        break;
                    }
                }
            }

            // System.out.println("PatternMatching - biome:" + biome + " matches: " + filters.biome_regex + " - " + result);
            return new Match(result, matchScope, matchingBiome, matchingStructure);
        }
    }

    private static boolean isInsideStructure(ServerWorld world, BlockPos pos, StructureStart structureStart) {
        if (structureStart.hasChildren()) {
            return structureStart.getBoundingBox().contains(pos);
        }
        return false;
    }

    public record ItemData(
            ItemKind kind,
            Identifier lootTableId,
            String itemId,
            String rarity) {

        public boolean matches(Config.ItemModifier.Filters filters) {
            if (filters == null) {
                return true;
            }
            var result = PatternMatching.matches(itemId, filters.item_id_regex)
                    && PatternMatching.matches(lootTableId.toString(), filters.loot_table_regex)
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

        var result = getDifficultyResult(locationData, itemData.lootTableId(), ScalingGoal.LOOT, world);
        var level = 0;
        if (result != null && result.difficulty() != null) {
            var difficulty = result.difficulty();
            level = difficulty.rewardLevel();
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
            level = difficulty.entityLevel();
            if (level != 0) {
                for (var modifier : getModifiersForEntity(difficulty.type().entities, entityData)) {
                    attributeModifiers.addAll(Arrays.asList(modifier.attributes));
                    experienceMultiplier += modifier.experience_multiplier;
                }
            }
            // System.out.println("Difficulty for entity: " + entityData.entityId() + " | difficulty: " + difficulty.type().name + " level " + level);
        }
        return new EntityScaleResult(attributeModifiers, level, experienceMultiplier);
    }

    public record SpawnerScaleResult(List<Config.SpawnerModifier> modifiers, int level) { }

    public static SpawnerScaleResult getModifiersForSpawner(LocationData locationData, EntityData entityData, ServerWorld world) {
        var spawnerModifiers = new ArrayList<Config.SpawnerModifier>();
        var difficulty = getDifficulty(locationData, world);
        int level = 0;
        if (difficulty != null) {
            level = difficulty.entityLevel();
            if (level != 0) {
                for (var modifier: getModifiersForEntity(difficulty.type().entities, entityData)) {
                    if (modifier.spawners != null) {
                        spawnerModifiers.add(modifier.spawners);
                    }
                }
            }
            // System.out.println("Difficulty for entity: " + entityData.entityId() + " | difficulty: " + difficulty.type().name + " level " + level);
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


    public record DifficultySearchResult(Difficulty difficulty, LocationData locationData, LocationData.Match match) {
        @Nullable public Identifier matchId() {
            return match != null ? match.id() : null;
        }
    }


    @Nullable
    public static Difficulty getDifficulty(LocationData locationData, ServerWorld world) {
        return getDifficulty(locationData, null, world);
    }

    @Nullable
    public static Difficulty getDifficulty(LocationData locationData, @Nullable Identifier sourceId, ServerWorld world) {
        var result = getDifficultyResult(locationData, sourceId, ScalingGoal.ENTITY, world);
        if (result != null) {
            return result.difficulty();
        }
        return null;
    }

    public enum ScalingGoal { ENTITY, LOOT }

    @Nullable
    public static DifficultySearchResult getDifficultyResult(LocationData locationData, @Nullable Identifier sourceId, ScalingGoal scalingGoal, ServerWorld world) {
        for (var dimension : DungeonDifficulty.config.value.dimensions) {
            if (locationData.matches(dimension.world_matches)) {
                var dimensionDifficulty = findDifficulty(dimension.difficulty);
                if (dimension.zones != null) {
                    DifficultySearchResult zoneResult = null;
                    for(var zone: dimension.zones) {
                        var match = locationData.matches(zone.zone_matches, world);
                        if (match.matches()) {
                            var zoneDifficulty = findDifficulty(zone.difficulty);
                            if (zoneDifficulty != null && zoneDifficulty.isValid()) {
                                zoneResult = new DifficultySearchResult(zoneDifficulty, locationData, match);
                                break;
                            }
                        }
                    }
                    var entityDifficulty = matchEntityDifficulty(locationData, sourceId, scalingGoal, dimension.entities);
                    var result = chooseHigherDifficulty(zoneResult, entityDifficulty);
                    if (result != null) {
                        return result;
                    }
                }
                if (dimensionDifficulty != null && dimensionDifficulty.isValid()) {
                    return new DifficultySearchResult(dimensionDifficulty, locationData, null);
                }
            }
        }
        return null;
    }

    private static DifficultySearchResult chooseHigherDifficulty(@Nullable DifficultySearchResult a, @Nullable DifficultySearchResult b) {
        if (a == null && b == null) {
            return null;
        }
        int aLevel = a != null ? a.difficulty().level() : -100;
        int bLevel = b != null ? b.difficulty().level() : -100;
        if (aLevel >= bLevel) {
            return a;
        } else {
            return b;
        }
    }

    private static @Nullable DifficultySearchResult matchEntityDifficulty(LocationData locationData, @Nullable Identifier sourceId, ScalingGoal scalingGoal, List<Config.EntityMatcher> matchers) {
        if (sourceId != null) {
            for (var entityMatcher : matchers) {
                switch (scalingGoal) {
                    case ENTITY -> {
                        if (entityMatcher.entity_type != null) {
                            var entityTypeEntry = Registries.ENTITY_TYPE.getEntry(sourceId);
                            if (entityTypeEntry.isEmpty()) {
                                continue;
                            }
                            if (PatternMatching.universalMatch(entityTypeEntry.get(), RegistryKeys.ENTITY_TYPE, entityMatcher.entity_type)) {
                                var difficulty = findDifficulty(entityMatcher.difficulty);
                                return new DifficultySearchResult(difficulty, locationData, null);
                            }
                        }
                    }
                    case LOOT -> {
                        if (entityMatcher.loot_table != null) {
                            if (PatternMatching.regexMatches(sourceId.toString(), entityMatcher.loot_table)) {
                                var difficulty = findDifficulty(entityMatcher.difficulty);
                                return new DifficultySearchResult(difficulty, locationData, null);
                            }
                        }
                    }
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
                var rewardLevel = reference.reward_level != null ? reference.reward_level : reference.level;
                var entityLevel = reference.entity_level != null ? reference.entity_level : reference.level;
                return new Difficulty(entry, reference.level, entityLevel, rewardLevel);
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

    public static final String TAG_PREFIX = "#";
    public static final String REGEX_PREFIX = "~";

    public static <T> boolean universalMatch(RegistryEntry<T> entry, RegistryKey<Registry<T>> registryKey, @Nullable String pattern) {
        if (pattern == null) {
            return true;
        }
        if (pattern.startsWith(TAG_PREFIX)) {
            var tag = TagKey.of(registryKey, Identifier.of(pattern.substring(1)));
            return entry.isIn(tag);
        }
        var id = entry.getKey().get().getValue().toString();
        if (pattern.startsWith(REGEX_PREFIX)) {
            return regexMatches(id, pattern.substring(1));
        } else {
            return id.equals(pattern);
        }
    }

    public static <T> boolean universalMatchNoTag(RegistryEntry<T> entry, RegistryKey<Registry<T>> registryKey, @Nullable String pattern) {
        if (pattern == null) {
            return true;
        }
        if (pattern.startsWith(TAG_PREFIX)) {
            var tag = TagKey.of(registryKey, Identifier.of(pattern.substring(1)));
            return entry.isIn(tag);
        }
        var id = entry.getKey().get().getValue().toString();
        return regexMatches(id, pattern);
    }

    public static boolean regexMatches(String subject, String regex) {
        if (subject == null) {
            return false;
        }
        if (regex == null || regex.isEmpty() || regex.equals("*") || subject.equals(regex)) {
            return true;
        }
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(subject);
        return matcher.find();
    }
}
