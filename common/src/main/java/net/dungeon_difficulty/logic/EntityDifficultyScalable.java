package net.dungeon_difficulty.logic;

import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public interface EntityDifficultyScalable {
    void markAlreadyScaled(int level);
    int getScalingLevel();
    default boolean isAlreadyScaled() {
        return getScalingLevel() > 0;
    }

    /**
     * Dimension id in which the entity was last evaluated for scaling, regardless of the outcome.
     * Lets entities that resolved to no difficulty (level 0) skip re-evaluation on every chunk load,
     * while still being evaluated again if they end up in another dimension.
     */
    @Nullable String getEvaluatedDimension();
    void markEvaluated(String dimensionId);
    default boolean isAlreadyEvaluated(Identifier dimensionId) {
        return isAlreadyScaled() || dimensionId.toString().equals(getEvaluatedDimension());
    }
    PatternMatching.LocationData getScalingLocationData();
    void setScalingLocationData(PatternMatching.LocationData data);
}
