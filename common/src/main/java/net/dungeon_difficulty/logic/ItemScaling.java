package net.dungeon_difficulty.logic;

import com.mojang.logging.LogUtils;
import net.dungeon_difficulty.DungeonDifficulty;
import net.dungeon_difficulty.config.Config;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.*;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.loot.function.LootFunctionTypes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.server.world.ServerWorld;
import org.apache.commons.lang3.mutable.MutableDouble;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.*;

public class ItemScaling {
    static final Logger LOGGER = LogUtils.getLogger();
    public static final String REWARD_SCALE_FACTOR = "dd.rsf";
    private static final boolean debugLogging = false;
    private static void debug(String message) {
        if (debugLogging) {
            System.out.println(message);
        }
    }

    public static void initialize() {
        // Some other mods (MineColonies) attempt to reserialize the loot table
        // this crashes anonym LootFunction implementations

//        LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {
//            LootFunction function = new LootFunction() {
//                @Override
//                public LootFunctionType getType() {
//                    return LootFunctionTypes.SET_ATTRIBUTES;
//                }
//
//                @Override
//                public ItemStack apply(ItemStack itemStack, LootContext lootContext) {
//                    var lootTableId = key;
//                    var position = lootContext.get(LootContextParameters.ORIGIN);
//                    BlockPos blockPosition = null;
//                    if (position != null) {
//                        blockPosition = BlockPos.ofFloored(position);
//                    }
//                    scale(itemStack, lootContext.getWorld(), blockPosition, lootTableId.getValue());
//                    return itemStack;
//                }
//            };
//            tableBuilder.apply(function);
//        });
        LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {
            var function = new LocalScalingLootFunction(List.of(), key.getValue());
            tableBuilder.apply(function);
        });
    }

    public static void scale(ItemStack itemStack, ServerWorld world, BlockPos position, Identifier lootTableId) {
        if (isScaled(itemStack)) {
            return; // Avoid scaling items multiple times
        }
        var locationData = PatternMatching.LocationData.create(world, position);
        scale(itemStack, world, lootTableId, locationData);
    }

    public static void scale(ItemStack itemStack, ServerWorld world, Identifier lootTableId, PatternMatching.LocationData locationData) {
        var itemEntry = itemStack.getRegistryEntry();
        var itemId = Registries.ITEM.getId(itemStack.getItem()).toString();
        var rarity = itemStack.getRarity().toString();
        var dimensionId = world.getRegistryKey().getValue().toString(); // Just for logging
        var position = locationData.position();
        var scaling = DungeonDifficulty.config.value.loot_scaling;

        if (itemStack.getItem() instanceof ToolItem || itemStack.getItem() instanceof RangedWeaponItem) {
            var itemData = new PatternMatching.ItemData(PatternMatching.ItemKind.WEAPONS, lootTableId, itemEntry, rarity);
            debug("Item scaling start." + " dimension: " + dimensionId + " position: " + position + ", loot table: " + lootTableId + ", item: " + itemId + ", rarity: " + rarity);
            var result = PatternMatching.getModifiersForItem(locationData, itemData, world, scaling);
            debug("Pattern matching found " + result.modifiers().size() + " attribute modifiers");

            var hasHandModifiers = false;
            var attributes = itemStack.get(DataComponentTypes.ATTRIBUTE_MODIFIERS);
            if (attributes != null) {
                // Find modifiers with the slot type of `HAND
                var hand = attributes.modifiers().stream()
                        .filter(modifier -> modifier.slot() == AttributeModifierSlot.HAND)
                        .findFirst();
                hasHandModifiers = hand.isPresent();
            }

            applyModifiersForItemStack(List.of(hasHandModifiers ? AttributeModifierSlot.HAND : AttributeModifierSlot.MAINHAND),
                    itemId, itemStack, result.modifiers(), result.level());
        }
        if (itemStack.getItem() instanceof ArmorItem armor) {
            var itemData = new PatternMatching.ItemData(PatternMatching.ItemKind.ARMOR, lootTableId, itemEntry, rarity);
            debug("Item scaling start." + " dimension: " + dimensionId + " position: " + position + ", loot table: " + lootTableId + ", item: " + itemId + ", rarity: " + rarity);
            var result = PatternMatching.getModifiersForItem(locationData, itemData, world, scaling);
            debug("Pattern matching found " + result.modifiers().size() + " attribute modifiers");
            applyModifiersForItemStack(List.of( AttributeModifierSlot.forEquipmentSlot(armor.getSlotType()) ), itemId, itemStack, result.modifiers(), result.level());
        }
        if (itemStack.getItem() instanceof ShieldItem shield) {
            var itemData = new PatternMatching.ItemData(PatternMatching.ItemKind.ARMOR, lootTableId, itemEntry, rarity);
            debug("Item scaling start." + " dimension: " + dimensionId + " position: " + position + ", loot table: " + lootTableId + ", item: " + itemId + ", rarity: " + rarity);
            var result = PatternMatching.getModifiersForItem(locationData, itemData, world, scaling);
            debug("Pattern matching found " + result.modifiers().size() + " attribute modifiers");
            applyModifiersForItemStack(List.of(AttributeModifierSlot.HAND), itemId, itemStack, result.modifiers(), result.level());
        }
    }

    public static void scale(ItemStack itemStack, int level) {
        var itemEntry = itemStack.getRegistryEntry();
        var itemId = Registries.ITEM.getId(itemStack.getItem()).toString();
        var rarity = itemStack.getRarity().toString();
        var lootTableId = Identifier.ofVanilla("none");
        var scaling = DungeonDifficulty.config.value.loot_scaling;

        if (itemStack.getItem() instanceof ToolItem || itemStack.getItem() instanceof RangedWeaponItem) {
            var itemData = new PatternMatching.ItemData(PatternMatching.ItemKind.WEAPONS, lootTableId, itemEntry, rarity);
            var result = PatternMatching.getItemScaleResult(itemData, scaling, level);

            var hasHandModifiers = false;
            var attributes = itemStack.get(DataComponentTypes.ATTRIBUTE_MODIFIERS);
            if (attributes != null) {
                // Find modifiers with the slot type of `HAND
                var hand = attributes.modifiers().stream()
                        .filter(modifier -> modifier.slot() == AttributeModifierSlot.HAND)
                        .findFirst();
                hasHandModifiers = hand.isPresent();
            }

            applyModifiersForItemStack(List.of(hasHandModifiers ? AttributeModifierSlot.HAND : AttributeModifierSlot.MAINHAND),
                    itemId, itemStack, result.modifiers(), result.level());
        }
        if (itemStack.getItem() instanceof ArmorItem armor) {
            var itemData = new PatternMatching.ItemData(PatternMatching.ItemKind.ARMOR, lootTableId, itemEntry, rarity);
            var result = PatternMatching.getItemScaleResult(itemData, scaling, level);
            applyModifiersForItemStack(List.of( AttributeModifierSlot.forEquipmentSlot(armor.getSlotType()) ), itemId, itemStack, result.modifiers(), result.level());
        }
        if (itemStack.getItem() instanceof ShieldItem shield) {
            var itemData = new PatternMatching.ItemData(PatternMatching.ItemKind.ARMOR, lootTableId, itemEntry, rarity);
            var result = PatternMatching.getItemScaleResult(itemData, scaling, level);
            applyModifiersForItemStack(List.of(AttributeModifierSlot.HAND), itemId, itemStack, result.modifiers(), result.level());
        }
    }

    private record ModifierSummary(float add, float multiplyBase) {
        public ModifierSummary add(float value) {
            return new ModifierSummary(add + value, multiplyBase);
        }
        public ModifierSummary multiplyBase(float value) {
            return new ModifierSummary(add, multiplyBase  + value);
        }
        public boolean isEmpty() {
            return add == 0 && multiplyBase == 0;
        }
        public float apply(float value) {
            return (value + add) * (1F + multiplyBase);
        }
    }

    private record AddResult(double value, @Nullable Identifier id) { }
    private static AddResult addValuesOf(AttributeModifiersComponent component, AttributeModifierSlot slot, RegistryEntry<EntityAttribute> givenAttribute) {
        var mutableValue = new MutableDouble(0);
        final @Nullable Identifier[] modifierId = {null};
        component.applyModifiers(slot, (attribute,modifier) -> {
                if (attribute.equals(givenAttribute) && modifier.operation() == EntityAttributeModifier.Operation.ADD_VALUE) {
                    if (modifierId[0] == null) {
                        modifierId[0] = modifier.id();
                    }
                    mutableValue.add(modifier.value());
                }
            }
        );
        return new AddResult(mutableValue.doubleValue(), modifierId[0]);
    }

    private record ScaledAttributeResult(double value) { }

    private static void applyModifiersForItemStack(List<AttributeModifierSlot> slots, String itemId, ItemStack itemStack, List<Config.AttributeModifier> modifiers, int level) {
        if (modifiers.isEmpty() || level == 0) {
            return;
        }
        var roundingUnit = getRoundingUnit();
        boolean useAdditiveModifiers = !DungeonDifficulty.config.value.meta.merge_item_modifiers;

        var attributesComponents = itemStack.get(DataComponentTypes.ATTRIBUTE_MODIFIERS);
        if (attributesComponents == null || attributesComponents.modifiers().isEmpty()) {
            attributesComponents = itemStack.getItem().getAttributeModifiers();
            if (attributesComponents == null) {
                attributesComponents = itemStack.getOrDefault(DataComponentTypes.ATTRIBUTE_MODIFIERS, AttributeModifiersComponent.DEFAULT);
            }
        }

        var summary = new LinkedHashMap<String, ModifierSummary>();
        for (var modifier : modifiers) {
            var element = summary.get(modifier.attribute);
            if (element == null) {
                element = new ModifierSummary(0, 0);
            }
            switch (modifier.operation) {
                case ADDITION -> {
                    element = element.add(modifier.randomizedValue(level));
                }
                case MULTIPLY_BASE -> {
                    element = element.multiplyBase(modifier.randomizedValue(level));
                }
            }
            if (!element.isEmpty()) {
                summary.put(modifier.attribute, element);
            }
        }

        // System.out.println("Scaling item: " + itemId + " with " + summary.size() + " modifiers");

        LinkedHashMap<AttributeModifierSlot, LinkedHashMap<RegistryEntry<EntityAttribute>,  ScaledAttributeResult>> results = new LinkedHashMap<>();
        for(var slot: slots) {
            results.put(slot, new LinkedHashMap<>());
            for (var attributeBoost : summary.entrySet()) {
                List<RegistryEntry<EntityAttribute>> affectedAttributes = List.of();
                var attributePattern = attributeBoost.getKey();
                var exactMatch = Registries.ATTRIBUTE.getEntry(Identifier.of(attributePattern)).orElse(null);
                if (exactMatch != null) {
                    affectedAttributes = List.of(exactMatch);
                } else {
                    var arrayList = new ArrayList<RegistryEntry<EntityAttribute>>();
                    attributesComponents.applyModifiers(slot, (attribute, modifier) -> {
                        if (PatternMatching.regexMatches(attribute.getKey().get().getValue().toString(), attributePattern)) {
                            arrayList.add(attribute);
                        }
                    });
                    affectedAttributes = arrayList;
                }
                for (var attribute: affectedAttributes) {
                    var baseline = addValuesOf(attributesComponents, slot, attribute);
                    var baseValue = baseline.value;
                    
                    if (useAdditiveModifiers) {
                        // Additive behavior - calculate and apply only the difference
                        var boostedValue = attributeBoost.getValue().apply((float) baseValue);
                        var boostAmount = boostedValue - baseValue;
                        if (roundingUnit != null) {
                            boostAmount = MathHelper.round(boostAmount, roundingUnit);
                        }
                        if (boostAmount != 0) {
                            results.get(slot).put(attribute, new ScaledAttributeResult(boostAmount));
                        }
                    } else {
                        // Merge behavior - replace with scaled value
                        var value = baseValue;
                        value = attributeBoost.getValue().apply((float) value);
                        if (roundingUnit != null) {
                            value = MathHelper.round(value, roundingUnit);
                        }
                        results.get(slot).put(attribute, new ScaledAttributeResult(value));
                    }
                }
            }
        }

        var newAttributeComponent = AttributeModifiersComponent.builder();
        
        for (var slot : slots) {
            Map<RegistryEntry<EntityAttribute>, ScaledAttributeResult> slotResults = results.computeIfAbsent(slot, k -> new LinkedHashMap<>());
            
            attributesComponents.applyModifiers(slot, (attribute, modifier) -> {
                var result = slotResults.get(attribute);
                boolean replacing = !useAdditiveModifiers && result != null && modifier.operation() == EntityAttributeModifier.Operation.ADD_VALUE;
                
                if (replacing) {
                    newAttributeComponent.add(
                            attribute,
                            new EntityAttributeModifier(modifier.id(), result.value, EntityAttributeModifier.Operation.ADD_VALUE),
                            slot);
                    slotResults.remove(attribute); 
                } else {
                    // Still copy the original modifier into the new component
                    newAttributeComponent.add(
                            attribute,
                            modifier,
                            slot);
                }
            });

            for (var entry : slotResults.entrySet()) {
                var attribute = entry.getKey();
                var result = entry.getValue();
                var id = Identifier.of(DungeonDifficulty.MODID, "dd.boost." + slot.asString());
                newAttributeComponent.add(
                        attribute,
                        new EntityAttributeModifier(id, result.value, EntityAttributeModifier.Operation.ADD_VALUE),
                        slot);
            }
        }

        itemStack.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, newAttributeComponent.build());
        markAsScaled(itemStack, level);
    }

    private static Double getRoundingUnit() {
        var config = DungeonDifficulty.config.value;
        if (config.meta != null && config.meta.rounding_unit != null) {
            return config.meta.rounding_unit;
        }
        return null;
    }

    public static void markAsScaled(ItemStack itemStack, int level) {
        itemStack.apply(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT, comp -> comp.apply(currentNbt -> {
            currentNbt.putInt(REWARD_SCALE_FACTOR, level);
        }));
    }

    public static boolean isScaled(ItemStack itemStack) {
        var nbt = itemStack.get(DataComponentTypes.CUSTOM_DATA);
        if (nbt == null) {
            return false;
        }
        return nbt.contains(REWARD_SCALE_FACTOR);
    }

    public static int getScaleFactor(ItemStack itemStack) {
        var nbt = itemStack.get(DataComponentTypes.CUSTOM_DATA);
        if (nbt == null) {
            return 0;
        }
        if (nbt.contains(REWARD_SCALE_FACTOR)) {
            return nbt.getNbt().getInt(REWARD_SCALE_FACTOR);
        }
        return 0;
    }

    public static void removeScaling(ItemStack itemStack) {
        itemStack.apply(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT, comp -> comp.apply(currentNbt -> {
            currentNbt.remove(REWARD_SCALE_FACTOR);
        }));
        var attributesComponents = itemStack.get(DataComponentTypes.ATTRIBUTE_MODIFIERS);
        if (attributesComponents != null) {
            // Filter all attribute modifiers where the modifier ID namespace is `DungeonDifficulty.MODID`
            var newAttributes = attributesComponents.modifiers().stream()
                    .filter(entry -> !entry.modifier().id().getNamespace().equals(DungeonDifficulty.MODID))
                    .toList();
            var component = AttributeModifiersComponent.builder();
            for (var entry: newAttributes) {
                component.add(entry.attribute(), entry.modifier(), entry.slot());
            }
            itemStack.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, component.build());
        }
    }
}