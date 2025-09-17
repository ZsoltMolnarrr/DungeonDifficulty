package net.dungeon_difficulty.logic;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameter;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.function.ConditionalLootFunction;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.List;
import java.util.Set;

public class LocalScalingLootFunction extends ConditionalLootFunction {
    public static final String NAME = "local_scaling";
    public static final Identifier ID = Identifier.of("dungeon_difficulty", NAME);
    public static final MapCodec<LocalScalingLootFunction> CODEC = RecordCodecBuilder.mapCodec(
            instance -> addConditionsField(instance)
                    .<String, String>and(
                            instance.group(
                                    Codec.STRING.fieldOf("loot_table_namespace").orElse(null).forGetter(function -> function.lootTableId.getNamespace()),
                                    Codec.STRING.fieldOf("loot_table_path").orElse(null).forGetter(function -> function.lootTableId.getPath())
                            )
                    )
                    .apply(instance, LocalScalingLootFunction::new)
    );
    public static final LootFunctionType<LocalScalingLootFunction> TYPE = new LootFunctionType<LocalScalingLootFunction>(CODEC);

    private LocalScalingLootFunction(List<LootCondition> conditions, String lootTableId, String unused) {
        this(conditions, Identifier.of(lootTableId));
    }

    public Identifier lootTableId;
    public LocalScalingLootFunction(List<LootCondition> conditions, Identifier lootTableId) {
        super(conditions);
        this.lootTableId = lootTableId;
    }

    @Override
    public LootFunctionType<LocalScalingLootFunction> getType() {
        return TYPE;
    }

    @Override
    public Set<LootContextParameter<?>> getRequiredParameters() {
        return Set.of();
    }

    @Override
    public ItemStack process(ItemStack itemStack, LootContext lootContext) {
        var position = lootContext.get(LootContextParameters.ORIGIN);
        BlockPos blockPosition = null;
        if (position != null) {
            blockPosition = BlockPos.ofFloored(position);
        }
        ItemScaling.scale(itemStack, lootContext.getWorld(), blockPosition, lootTableId);
        return itemStack;
    }
}