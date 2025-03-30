package net.dungeon_difficulty.logic;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface DifficultyHandler {
    @Nullable List<Difficulty.Announcement> getLastDifficultyAnnouncement();
}
