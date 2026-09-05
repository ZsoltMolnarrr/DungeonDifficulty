package net.dungeon_difficulty.neoforge.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.dungeon_difficulty.DungeonDifficulty;
import net.dungeon_difficulty.logic.ItemScaling;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;

/// NeoForge counterpart of the Fabric side's per-table `LocalScalingLootFunction`:
/// a global loot modifier that scales every generated stack by the queried table's id and the drop location.
/// Declared in `data/dungeon_difficulty/loot_modifiers/local_scaling.json`.
public class LocalScalingLootModifier extends LootModifier {
    public static final Identifier ID = Identifier.of(DungeonDifficulty.MODID, "local_scaling");
    public static final MapCodec<LocalScalingLootModifier> CODEC = RecordCodecBuilder.mapCodec(instance ->
            codecStart(instance).apply(instance, LocalScalingLootModifier::new));

    public LocalScalingLootModifier(LootCondition[] conditions) {
        super(conditions);
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        var lootTableId = context.getQueriedLootTableId();
        if (lootTableId == null) {
            // Tables built in code have no id; those are not reached by the Fabric per-table function either
            return generatedLoot;
        }
        var origin = context.get(LootContextParameters.ORIGIN);
        BlockPos position = origin != null ? BlockPos.ofFloored(origin) : null;
        for (var itemStack : generatedLoot) {
            ItemScaling.scale(itemStack, context.getWorld(), position, lootTableId);
        }
        return generatedLoot;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}
