package net.dungeon_difficulty.config;

import java.util.LinkedHashMap;
import java.util.Map;

public class ClientConfig {
    public boolean enable_overriding_enchantment_rarity = true;
    public boolean enable_scaled_items_rarity = true;
    public boolean enable_rarity_color_override = false;
    /**
     * Formatting name per `Rarity` ordinal, for example `{"4": "GOLD"}`. Rarities added by other
     * mods can be addressed too. Ordinals left out keep their original color.
     */
    public Map<String, String> rarity_color_overrides = new LinkedHashMap<>(Map.of("4", "GOLD"));
}
