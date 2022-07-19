package net.powerscale.config;

import java.util.Random;

public class Config {

    public Meta meta = new Meta();
    public class Meta {
        public String comment = "IMPORTANT! Make sure to set `override_with_default` to `false` to allow customization of the config";
        public boolean override_with_default = true;
        public boolean sanitize_config = true;
    }

    public Dimension[] dimensions;

    public static class Dimension {
        public static class Filters {
            public String dimension_regex = ".*";
        }
        public Filters world_matches = new Filters();

        public EntityModifier[] entities = new EntityModifier[]{};
        public Rewards rewards = new Rewards();
        public Zone[] zones = new Zone[]{};
    }

    public static class Zone {
        public static class Filters {
            public String biome_regex = ".*";
        }
        public Filters zone_matches = new Filters();

        public EntityModifier[] entities = new EntityModifier[]{};
        public Rewards rewards = new Rewards();
    }

    public enum Operation { ADD, MULTIPLY }

    public static class EntityModifier {
        public static class Filters {
            public enum Attitude {
                FRIENDLY, HOSTILE, ANY
            }
            public Attitude attitude = Attitude.ANY;
            public String entity_id_regex = ".*";
        }
        public Filters entity_matches = new Filters();
        public AttributeModifier[] attributes = new AttributeModifier[]{};
        public SpawnerModifier spawners = null;
        public float experience_multiplier = 1;
    }

    public static class Rewards {
        public ItemModifier[] armor = new ItemModifier[]{};
        public ItemModifier[] weapons = new ItemModifier[]{};
    }

    public static class ItemModifier {
        public static class Filters {
            public String item_id_regex = ".*";
            public String loot_table_regex = ".*";
            public String rarity_regex = ".*";
        }
        public Filters item_matches = new Filters();

        public AttributeModifier[] attributes = new AttributeModifier[]{};
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
