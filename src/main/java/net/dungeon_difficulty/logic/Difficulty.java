package net.dungeon_difficulty.logic;

import net.dungeon_difficulty.config.Config;

public record Difficulty(Config.DifficultyType type, int level) {
    public boolean isValid() {
        return type != null && level > 0;
    }
}
