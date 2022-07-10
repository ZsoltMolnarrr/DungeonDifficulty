package net.powerscale.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Config {

    public Map<String, Dimension> dimensions = new HashMap<>();

    public static class Dimension {

        // Key is regex for item id
        public Map<String, ItemModifier[]> items = new HashMap<>();

    }

    public static class ItemModifier {
        public String attribute;
        public Operation operation = Operation.MULTIPLY;
        public float randomness = 0;
        public float value;

        public ItemModifier(String attribute, float value) {
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
