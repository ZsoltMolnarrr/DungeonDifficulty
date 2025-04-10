package net.dungeon_difficulty.logic;

import net.dungeon_difficulty.config.Config;

import java.util.Locale;

public record Difficulty(Config.DifficultyType type, int level, int rewardLevel) {
    private static final Config.DifficultyType EMPTY_TYPE = new Config.DifficultyType("empty");
    public static final Difficulty EMPTY = new Difficulty(EMPTY_TYPE, 0, 0);

    public boolean isValid() {
        return type != null && level > 0;
    }

    public boolean equals(Difficulty other) {
        return type.name.equals(other.type.name) && level == other.level;
    }

    public String typeTranslationKey() {
        var suffix = type.translation_code != null ? type.translation_code : type.name;
        return "difficulty.type." + suffix.toLowerCase(Locale.ENGLISH);
    }

    public record Announcement(Difficulty difficulty, int age, String dimensionId) {
        public static Announcement EMPTY = new Announcement(Difficulty.EMPTY, 0, "");
    }
}
