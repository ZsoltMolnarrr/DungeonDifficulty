package net.powerscale.config;

import java.util.Random;

public class Config {
    public Location[] locations;

    public static class Location {
        public static class Rewards {
            public ItemModifier[] armor;
            public ItemModifier[] weapons;
        }
        public Rewards rewards;

        public static class Filters {
            public String dimension_regex = ".*";
        }
        public Filters filters;
    }

    public static class ItemModifier {
        public static class Filters {
            public String item_id_regex = ".*";
            public String loot_table_regex = ".*";
            public String rarity_regex = ".*";
        }
        public Filters filters;

        public AttributeModifier[] modifiers;
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

    public enum Operation {
        ADD,
        MULTIPLY
    }
}
