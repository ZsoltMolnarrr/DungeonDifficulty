package net.dungeon_difficulty.naming;

import net.minecraft.util.Identifier;
import net.minecraft.util.math.ChunkPos;

/// Identifies a single structure instance within a dimension.
/// Only one start of a given structure can exist per chunk, so structure id + start chunk is unique and stable.
public record StructureKey(Identifier structureId, ChunkPos startPos) {
    public String asString() {
        return structureId + "@" + startPos.x + "," + startPos.z;
    }
}
