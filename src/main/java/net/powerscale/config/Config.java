package net.powerscale.config;

import java.util.Random;

public class Config {

    // STRUCTURE

    public Location[] locations;

    public static class Location {
        public static class Filters {
            public String dimension_regex = ".*";
        }
        public Filters world_matches;

        public EntityModifier[] entities;

        public static class Rewards {
            public ItemModifier[] armor;
            public ItemModifier[] weapons;
        }
        public Rewards rewards;
    }

    // TYPES

    public enum Operation { ADD, MULTIPLY }

    public static class EntityModifier {
        public static class Filters {
            public enum Attitude {
                FRIENDLY, HOSTILE, ANY
            }
            public Attitude attitude = Attitude.ANY;
            public String entity_id_regex = ".*";
        }
        public Filters entity_matches;
        public AttributeModifier[] attributes;
        public SpawnerModifier spawners = null;
    }

    public static class ItemModifier {
        public static class Filters {
            public String item_id_regex = ".*";
            public String loot_table_regex = ".*";
            public String rarity_regex = ".*";
        }
        public Filters item_matches;

        public AttributeModifier[] attributes;
    }

    public static class AttributeModifier {
        public String attribute;
        public Operation operation = Operation.MULTIPLY;
        public float randomness = 0;
        public float value = 1;

        public AttributeModifier() {}

        public AttributeModifier(String attribute, float value) {
            this.attribute = attribute;
            this.value = value;
        }

        private static Random rng = new Random();
        public float randomizedValue() {
            return (randomness > 0)
                    ?  rng.nextFloat(value - randomness, value + randomness)
                    : value;
        }
    }

    public static class SpawnerModifier {
        public float spawn_range_multiplier = 1;
        public float spawn_count_multiplier = 1;
        public float max_nearby_entities_multiplier = 1;
        public float min_spawn_delay_multiplier = 1;
        public float max_spawn_delay_multiplier = 1;
        public float required_player_range_multiplier = 1;
    }
}
