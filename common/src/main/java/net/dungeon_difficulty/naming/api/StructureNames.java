package net.dungeon_difficulty.naming.api;

import net.dungeon_difficulty.naming.StructureKey;
import net.dungeon_difficulty.naming.StructureNaming;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

import java.util.Optional;

/// Public API to query the names Dungeon Difficulty assigns to structures (for example villages).
///
/// Server side only. Must be called on the server thread, calls from other threads
/// (such as world generation) return empty. Never loads chunks.
public final class StructureNames {
    private StructureNames() { }

    /// Whether structure naming is enabled (`config/dungeon_difficulty/structure_naming.json`)
    public static boolean isEnabled() {
        return StructureNaming.isEnabled();
    }

    /// The named structure at the position (the one with the closest start, when overlapping).
    /// Assigns a name if the structure has none yet, the same way visiting it would.
    /// Empty when the position is not inside a named structure, or its start chunk is not loaded.
    public static Optional<NamedStructure> find(ServerWorld world, BlockPos pos) {
        var key = StructureNaming.find(world, pos);
        if (key == null) {
            return Optional.empty();
        }
        var name = StructureNaming.getName(world, key);
        return name != null
                ? Optional.of(new NamedStructure(key.structureId(), key.startPos(), name))
                : Optional.empty();
    }

    /// Name already assigned to the structure instance (never assigns one)
    public static Optional<String> getAssignedName(ServerWorld world, Identifier structureId, ChunkPos startPos) {
        return Optional.ofNullable(StructureNaming.getAssignedName(world, new StructureKey(structureId, startPos)));
    }
}
