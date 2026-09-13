package net.dungeon_difficulty.logic;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.dungeon_difficulty.DungeonDifficulty;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameter;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.function.ConditionalLootFunction;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.math.BlockPos;

import java.util.Set;

/// Per-table loot function (Fabric side): scales every generated stack by the table's id and the drop location.
/// 1.20.1 loot functions are GSON-serialized (no codecs yet); the serializer round-trips the table id so mods
/// that re-serialize loot tables (MineColonies) keep the function intact.
public class LocalScalingLootFunction extends ConditionalLootFunction {
    public static final String NAME = "local_scaling";
    public static final Identifier ID = new Identifier(DungeonDifficulty.MODID, NAME);
    public static final LootFunctionType TYPE = new LootFunctionType(new Serializer());

    public final Identifier lootTableId;
    public LocalScalingLootFunction(LootCondition[] conditions, Identifier lootTableId) {
        super(conditions);
        this.lootTableId = lootTableId;
    }

    @Override
    public LootFunctionType getType() {
        return TYPE;
    }

    @Override
    public Set<LootContextParameter<?>> getRequiredParameters() {
        return Set.of();
    }

    @Override
    protected ItemStack process(ItemStack itemStack, LootContext lootContext) {
        var position = lootContext.get(LootContextParameters.ORIGIN);
        BlockPos blockPosition = null;
        if (position != null) {
            blockPosition = BlockPos.ofFloored(position);
        }
        ItemScaling.scale(itemStack, lootContext.getWorld(), blockPosition, lootTableId);
        return itemStack;
    }

    public static class Serializer extends ConditionalLootFunction.Serializer<LocalScalingLootFunction> {
        @Override
        public void toJson(JsonObject json, LocalScalingLootFunction function, JsonSerializationContext context) {
            super.toJson(json, function, context);
            json.addProperty("loot_table_namespace", function.lootTableId.getNamespace());
            json.addProperty("loot_table_path", function.lootTableId.getPath());
        }

        @Override
        public LocalScalingLootFunction fromJson(JsonObject json, JsonDeserializationContext context, LootCondition[] conditions) {
            var namespace = JsonHelper.getString(json, "loot_table_namespace", Identifier.DEFAULT_NAMESPACE);
            var path = JsonHelper.getString(json, "loot_table_path", "none");
            return new LocalScalingLootFunction(conditions, new Identifier(namespace, path));
        }
    }
}
