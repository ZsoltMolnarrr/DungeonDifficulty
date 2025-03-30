package net.dungeon_difficulty.logic;

import java.util.List;

public interface DifficultyHandler {
    List<Difficulty.Announcement> getLastDifficultyAnnouncements();
}
