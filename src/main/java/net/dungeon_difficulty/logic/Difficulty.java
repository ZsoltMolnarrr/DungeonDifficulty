package net.dungeon_difficulty.logic;

import net.dungeon_difficulty.config.Config;

import java.util.Locale;

public record Difficulty(Config.DifficultyType type, int level) {
    public boolean isValid() {
        return type != null && level > 0;
    }

    public boolean equals(Difficulty other) {
        return type.name.equals(other.type.name) && level == other.level;
    }

    public String typeTranslationKey() {
        return "difficulty.type." + type.name.toLowerCase(Locale.ENGLISH);
    }

    public record Announcement(Difficulty difficulty, int age, String dimensionId) {
        public static Announcement EMPTY = new Announcement(null, 0, null);
    }
}
