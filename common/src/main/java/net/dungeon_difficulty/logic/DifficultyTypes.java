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