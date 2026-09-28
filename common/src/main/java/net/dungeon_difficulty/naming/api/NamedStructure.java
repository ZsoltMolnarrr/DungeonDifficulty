package net.dungeon_difficulty.naming.api;

import net.minecraft.util.Identifier;
import net.minecraft.util.math.ChunkPos;

/// A named structure instance.
/// A structure instance is identified by its structure id and start chunk, within a dimension.
///
/// @param structureId structure id, for example `minecraft:village_plains`
/// @param startPos chunk of the structure's start
/// @param name name of the structure instance, for example `Blackridge`
public record NamedStructure(Identifier structureId, ChunkPos startPos, String name) { }
