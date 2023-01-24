package net.dungeon_difficulty.logic;

public interface EntityScalable {
    void markAlreadyScaled();
    boolean isAlreadyScaled();
    PatternMatching.LocationData getLocationData();
    void setLocationData(PatternMatching.LocationData data);
}
