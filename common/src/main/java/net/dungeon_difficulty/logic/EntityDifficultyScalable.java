package net.dungeon_difficulty.logic;

public interface EntityDifficultyScalable {
    void markAlreadyScaled();
    boolean isAlreadyScaled();
    PatternMatching.LocationData getScalingLocationData();
    void setScalingLocationData(PatternMatching.LocationData data);
}
