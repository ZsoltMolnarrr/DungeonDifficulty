package net.dungeon_difficulty.logic;

import net.dungeon_difficulty.DungeonDifficulty;
import net.dungeon_difficulty.config.Config;
import net.minecraft.server.world.ServerWorld;

import java.util.ArrayList;
import java.util.List;

public class PerPlayerDifficulty {
    public static PatternMatching.EntityScaleResult getAttributeModifiers(PatternMatching.EntityData entityData, ServerWorld world) {
        var empty = new PatternMatching.EntityScaleResult(List.of(), 0);
        var perPlayer = DungeonDifficulty.configManager.value.perPlayerDifficulty;
        if (perPlayer == null || !perPlayer.enabled || perPlayer.entities == null || perPlayer.entities.length == 0 || perPlayer.counting == null) {
            return empty;
        }

        var playerCount = 0;
        switch (perPlayer.counting) {
            case EVERYWHERE -> {
                playerCount = world.getServer().getPlayerManager().getPlayerList().size();
            }
            case DIMENSION -> {
                playerCount = world.getPlayers().size();
            }
        }
        if (playerCount < 2) {
            return empty;
        }

        int applyCount = playerCount - 1;
        var attributeModifiers = new ArrayList<Config.AttributeModifier>();
        for(var entityBaseModifier: perPlayer.entities) {
            if (entityData.matches(entityBaseModifier.entity_matches)) {
                attributeModifiers.addAll(List.of(entityBaseModifier.attributes));
            }
        }
        return new PatternMatching.EntityScaleResult(attributeModifiers, applyCount);
    }
}
