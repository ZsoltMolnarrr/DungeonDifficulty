package net.dungeon_difficulty.logic;

public interface EntityDifficultyScalable {
    void markAlreadyScaled(int level);
    int getScalingLevel();
    default boolean isAlreadyScaled() {
        return getScalingLevel() > 0;
    }
    PatternMatching.LocationData getScalingLocationData();
    void setScalingLocationData(PatternMatching.LocationData data);
}
