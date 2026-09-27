package net.dungeon_difficulty.naming;

import com.mojang.logging.LogUtils;
import net.dungeon_difficulty.DungeonDifficulty;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.gen.structure.Structure;
import net.tiny_config.ConfigManager;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.concurrent.ThreadLocalRandom;

/// Generated names for structure instances (for example villages).
/// Independent of difficulty: anything may ask for the name of a structure instance.
///
/// Threading: names are only read and assigned on the server thread (never during world generation).
/// The storage is not thread safe, so calls from other threads (for example world generation workers)
/// are refused, returning `null`. No locks are taken, and `find` never waits for chunks.
public class StructureNaming {
    static final Logger LOGGER = LogUtils.getLogger();

    public static final TagKey<Structure> NAMED = TagKey.of(RegistryKeys.STRUCTURE,
            Identifier.of(DungeonDifficulty.MODID, "named"));

    public static final ConfigManager<NamingConfig> config = new ConfigManager<>
            ("structure_naming", new NamingConfig())
            .builder()
            .setDirectory(DungeonDifficulty.MODID)
            .sanitize(true)
            .build();

    public static void reloadConfig() {
        config.refresh();
    }

    public static boolean isEnabled() {
        var value = config.safeValue();
        return value != null && value.enabled;
    }

    private static volatile boolean failureLogged = false;

    private static boolean isOnServerThread(ServerWorld world) {
        if (world.getServer().isOnThread()) {
            return true;
        }
        if (!failureLogged) {
            failureLogged = true;
            LOGGER.warn("Dungeon Difficulty: structure naming requested off the server thread, ignoring it", new IllegalStateException());
        }
        return false;
    }

    public static boolean isNamed(RegistryEntry<Structure> structure) {
        return isEnabled() && structure.isIn(NAMED);
    }

    /// Named structure instance containing the position (closest start wins, when overlapping).
    /// Never loads or waits for chunks (only uses fully loaded ones), so it is safe to call while
    /// other mods hold locks (such as Waystones naming). Unloaded structure starts are skipped.
    @Nullable public static StructureKey find(ServerWorld world, BlockPos pos) {
        if (!isEnabled() || !isOnServerThread(world)) {
            return null;
        }
        var chunkManager = world.getChunkManager();
        var chunkPos = new ChunkPos(pos);
        var chunk = chunkManager.getWorldChunk(chunkPos.x, chunkPos.z);
        if (chunk == null) {
            return null;
        }
        var registry = world.getRegistryManager().get(RegistryKeys.STRUCTURE);
        StructureKey closest = null;
        double closestDistance = Double.MAX_VALUE;
        for (var reference : chunk.getStructureReferences().entrySet()) {
            var structure = reference.getKey();
            if (!registry.getEntry(structure).isIn(NAMED)) {
                continue;
            }
            var id = registry.getId(structure);
            if (id == null) {
                continue;
            }
            for (long startLong : reference.getValue()) {
                var startPos = new ChunkPos(startLong);
                var startChunk = chunkManager.getWorldChunk(startPos.x, startPos.z);
                if (startChunk == null) {
                    continue;
                }
                var start = startChunk.getStructureStart(structure);
                if (start == null || !start.hasChildren() || !start.getBoundingBox().contains(pos)) {
                    continue;
                }
                var dx = startPos.getCenterX() - pos.getX();
                var dz = startPos.getCenterZ() - pos.getZ();
                var distance = dx * dx + dz * dz;
                if (distance < closestDistance) {
                    closestDistance = distance;
                    closest = new StructureKey(id, startPos);
                }
            }
        }
        return closest;
    }

    /// Name of the structure instance, generated upon first request
    @Nullable public static String getName(ServerWorld world, StructureKey key) {
        if (!isOnServerThread(world)) {
            return null;
        }
        return getOrCreate(world, key).name;
    }

    /// Names for waystones of the structure: "Name", then "Name 1", "Name 2", ...
    @Nullable public static String nextWaystoneName(ServerWorld world, StructureKey key) {
        if (!isOnServerThread(world)) {
            return null;
        }
        var storage = StructureNameStorage.get(world);
        var entry = getOrCreate(world, key);
        var name = entry.waystones == 0 ? entry.name : entry.name + " " + entry.waystones;
        entry.waystones += 1;
        storage.markDirty();
        return name;
    }

    private static StructureNameStorage.Entry getOrCreate(ServerWorld world, StructureKey key) {
        var storage = StructureNameStorage.get(world);
        var entry = storage.get(key);
        if (entry == null) {
            var name = NameGenerator.generate(config.safeValue().names, ThreadLocalRandom.current(), storage::isUsed);
            entry = storage.put(key, name);
        }
        return entry;
    }
}
