package net.dungeon_difficulty.naming;

import net.dungeon_difficulty.DungeonDifficulty;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/// Names of structure instances, saved per dimension (`<world>/<dimension>/data/dungeon_difficulty_structure_names.dat`)
public class StructureNameStorage extends PersistentState {
    private static final String ID = DungeonDifficulty.MODID + "_structure_names";
    private static final Type<StructureNameStorage> TYPE = new Type<>(StructureNameStorage::new, StructureNameStorage::fromNbt, null);

    public static class Entry {
        public String name;
        public int waystones = 0;
        public Entry(String name) {
            this.name = name;
        }
    }

    private final Map<String, Entry> entries = new HashMap<>();
    private final Set<String> usedNames = new HashSet<>();

    public static StructureNameStorage get(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(TYPE, ID);
    }

    @Nullable public Entry get(StructureKey key) {
        return entries.get(key.asString());
    }

    public boolean isUsed(String name) {
        return usedNames.contains(name);
    }

    public Entry put(StructureKey key, String name) {
        var entry = new Entry(name);
        entries.put(key.asString(), entry);
        usedNames.add(name);
        markDirty();
        return entry;
    }

    /// Renames (or names) the structure, restarting its waystone numbering
    public Entry rename(StructureKey key, String name) {
        var entry = entries.get(key.asString());
        if (entry == null) {
            return put(key, name);
        }
        usedNames.remove(entry.name);
        entry.name = name;
        entry.waystones = 0;
        usedNames.add(name);
        markDirty();
        return entry;
    }

    /// Forgets the name, a new one is generated upon next request
    public boolean remove(StructureKey key) {
        var entry = entries.remove(key.asString());
        if (entry == null) {
            return false;
        }
        usedNames.remove(entry.name);
        markDirty();
        return true;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        var list = new NbtList();
        for (var mapEntry : entries.entrySet()) {
            var compound = new NbtCompound();
            compound.putString("key", mapEntry.getKey());
            compound.putString("name", mapEntry.getValue().name);
            compound.putInt("waystones", mapEntry.getValue().waystones);
            list.add(compound);
        }
        nbt.put("structures", list);
        return nbt;
    }

    private static StructureNameStorage fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        var storage = new StructureNameStorage();
        var list = nbt.getList("structures", NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < list.size(); i++) {
            var compound = list.getCompound(i);
            var key = compound.getString("key");
            var name = compound.getString("name");
            if (key.isEmpty() || name.isEmpty()) {
                continue;
            }
            var entry = new Entry(name);
            entry.waystones = compound.getInt("waystones");
            storage.entries.put(key, entry);
            storage.usedNames.add(name);
        }
        return storage;
    }
}
