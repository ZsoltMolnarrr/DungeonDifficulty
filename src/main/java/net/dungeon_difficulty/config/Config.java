package net.dungeon_difficulty.config;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Random;

public class Config {
    public Meta meta = new Meta();
    public class Meta { public Meta() { }
        public boolean sanitize_config = true;
        public Double rounding_unit = 0.5;
        public boolean merge_item_modifiers = true;
    }

    public Announcement announcement = new Announcement();
    public static class Announcement { public Announcement() { }
        public boolean enabled = true;
        public int check_interval_seconds = 5;
        public int history_size = 2;
    }

    public PerPlayerDifficulty per_player_difficulty;
    public static class PerPlayerDifficulty { public PerPlayerDifficulty() { }
        public boolean enabled = true;
        public enum Counting { EVERYWHERE, DIMENSION }
        public Counting counting = Counting.EVERYWHERE;
        public EntityModifier[] entities = new EntityModifier[]{};
    }

    public List<DifficultyType> difficulty_types = List.of();
    public static class DifficultyType { public DifficultyType() { }
        public String name;
        public String parent;
        @Nullable public String translation_code;
        public List<EntityModifier> entities = List.of();
        public Rewards rewards = new Rewards();
        public DifficultyType(String name) {
            this.name = name;
        }
    }
    public static class Rewards { public Rewards() { }
        public String name;
        public List<ItemModifier> armor = List.of();
        public List<ItemModifier> weapons = List.of();

        public static class SmithingUpgrade { public SmithingUpgrade() { }
            public boolean enabled = true;
            public int add_upon_upgrade = -1;
            public float multiply_upon_upgrade = 1;
        }
        public SmithingUpgrade smithing_upgrade = new SmithingUpgrade();
    }

    public static class DifficultyReference { public DifficultyReference() { }
        public String name;
        public int level = 0;
        public Integer entity_level;
        public Integer reward_level;
        public DifficultyReference(String name, int level) {
            this.name = name;
            this.level = level;
        }
    }

    public Dimension[] dimensions;

    public static class Dimension { public Dimension() { }
        public static class Filters {
            public String dimension_regex = Regex.ANY;
        }
        public Filters world_matches = new Filters();
        public DifficultyReference difficulty;

        public List<Zone> zones = List.of();
        public List<EntityMatcher> entities = List.of();
    }

    public static class Zone { public Zone() { }
        public static class Filters { public Filters() { }
            @Nullable public String biome = null;
            @Nullable public String structure = null;
        }
        public Filters zone_matches = new Filters();

        public DifficultyReference difficulty;
    }

    public static class EntityMatcher { public EntityMatcher() { }
        public String entity_type = null;
        public String loot_table = null;
        public DifficultyReference difficulty;
    }

    public enum Operation { ADDITION, MULTIPLY_BASE }

    public static class EntityModifier { public EntityModifier() { }
        public static class Filters {
            public enum Attitude {
                FRIENDLY, HOSTILE, ANY
            }
            public Attitude attitude = Attitude.ANY;
            public String entity_id_regex = Regex.ANY;
        }
        public Filters entity_matches = new Filters();
        public AttributeModifier[] attributes = new AttributeModifier[]{};
        public SpawnerModifier spawners = null;
        public float experience_multiplier = 0;
    }

    public static class ItemModifier { public ItemModifier() { }
        public static class Filters {
            public String item_id_regex = Regex.ANY;
            public String loot_table_regex = Regex.ANY;
            public String rarity_regex = Regex.ANY;
        }
        public Filters item_matches = new Filters();

        public AttributeModifier[] attributes = new AttributeModifier[]{};
    }

    public static class AttributeModifier { public AttributeModifier() { }
        public String attribute;
        public Operation operation = Operation.MULTIPLY_BASE;
        public float randomness = 0;
        public float value = 0;

        public AttributeModifier(String attribute, float value) {
            this.attribute = attribute;
            this.value = value;
        }

        private static Random rng = new Random();
        public float randomizedValue(int level) {
            var value = this.value * level;
            return (randomness > 0)
                    ?  rng.nextFloat(value - randomness, value + randomness)
                    : value;
        }
    }

    public static class SpawnerModifier { public SpawnerModifier() { }
        public float spawn_range_multiplier = 0;
        public float spawn_count_multiplier = 0;
        public float max_nearby_entities_multiplier = 0;
        public float min_spawn_delay_multiplier = 0;
        public float max_spawn_delay_multiplier = 0;
        public float required_player_range_multiplier = 0;
    }
}
