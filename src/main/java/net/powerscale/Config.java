package net.powerscale;

import java.util.HashMap;
import java.util.Map;

public class Config {

    public Map<String, Dimension> dimensions = new HashMap<>();

    public static class Dimension {

        // Key is regex for item id
        public Map<String, ItemModifier> items = new HashMap<>();


    }

    public static class ItemModifier {
        public String attribute;
        // public String operation;
        public float value;

        public ItemModifier(String attribute, float value) {
            this.attribute = attribute;
            this.value = value;
        }
    }
}
