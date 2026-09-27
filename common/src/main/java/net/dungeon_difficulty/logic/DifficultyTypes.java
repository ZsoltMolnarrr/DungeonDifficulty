package net.dungeon_difficulty.logic;

import net.dungeon_difficulty.DungeonDifficulty;
import net.dungeon_difficulty.config.Config;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class DifficultyTypes {
    public static List<Config.DifficultyType> resolved = List.of();

    public static void resolve() {
        var resolved = new ArrayList<Config.DifficultyType>();
        var types = DungeonDifficulty.config.value.difficulty_types;
        for (var type: DungeonDifficulty.config.value.difficulty_types) {
            resolved.add(resolve(type, types));
        }
        DifficultyTypes.resolved = resolved;
    }

    /// Whether the type is the named one, or inherits from it
    public static boolean inherits(Config.DifficultyType type, String name) {
        var current = type;
        for (int depth = 0; current != null && depth < 16; depth++) {
            if (name.equals(current.name)) {
                return true;
            }
            var parentName = current.parent;
            if (parentName == null || parentName.isEmpty()) {
                return false;
            }
            current = resolved.stream().filter(t -> parentName.equals(t.name)).findFirst().orElse(null);
        }
        return false;
    }

    private static Config.DifficultyType resolve(Config.DifficultyType type, List<Config.DifficultyType> types) {
        if (type.parent != null && !type.parent.isEmpty()) {
            var parent = types.stream()
                    .filter(otherType -> type.parent.equals(otherType.name))
                    .findFirst().orElse(null);
            if (parent != null) {
                parent = resolve(parent, types);
                return merge(type, parent);
            }
        }
        return type;
    }

    private static Config.DifficultyType copy(Config.DifficultyType type) {
        var copy = new Config.DifficultyType();
        copy.name = type.name;
        copy.parent = type.parent;
        copy.translation_code = type.translation_code;
        copy.allow_loot_scaling = type.allow_loot_scaling;
        copy.entities = type.entities;
        return copy;
    }

    private static Config.DifficultyType merge(Config.DifficultyType t1, Config.DifficultyType t2) {
        var merged = copy(t1);
        merged.entities = Stream.concat(t1.entities.stream(), t2.entities.stream()).toList();
        merged.allow_loot_scaling = t2.allow_loot_scaling;
        if (t1.allow_loot_scaling != null) {
            merged.allow_loot_scaling = t1.allow_loot_scaling;
        }
        return merged;
    }
}