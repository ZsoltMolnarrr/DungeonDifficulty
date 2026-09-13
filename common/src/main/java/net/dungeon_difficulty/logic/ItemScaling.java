package net.dungeon_difficulty.logic;

import com.mojang.logging.LogUtils;
import net.dungeon_difficulty.DungeonDifficulty;
import net.dungeon_difficulty.config.Config;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.nio.charset.StandardCharsets;
import java.util.*;

/// Loot scaling on 1.20.1: item attribute modifiers live in the stack's `AttributeModifiers` NBT list
/// (no data components on this line). Once that list exists vanilla reads ONLY the list, for every slot,
/// so scaling always copies the item's defaults for all slots into NBT before touching the scaled ones.
/// The scale factor is kept in the root NBT under `dd.rsf`, which is exactly where 1.20.5+ migrates it
/// to (`custom_data`), so worlds upgraded later keep their power levels.
public class ItemScaling {
    static final Logger LOGGER = LogUtils.getLogger();
    public static final String REWARD_SCALE_FACTOR = "dd.rsf";
    /// Marker written by the legacy 2.x (1.20.1, Fabric only) line: a boolean. Read as level 1, so loot
    /// scaled by that version is neither scaled twice nor loses its rarity bump.
    private static final String LEGACY_SCALED_KEY = "DDS";
    private static final String ATTRIBUTE_MODIFIERS_KEY = "AttributeModifiers";
    private static final boolean debugLogging = false;
    private static void debug(String message) {
        if (debugLogging) {
            System.out.println(message);
        }
    }

    /// Entry point for loot scaling. Each loader hooks this into its own loot pipeline:
    /// Fabric appends {@link LocalScalingLootFunction} to every loot table (the function must be a
    /// serializable type, as some mods such as MineColonies re-serialize loot tables);
    /// Forge applies a global loot modifier.
    public static void scale(ItemStack itemStack, ServerWorld world, BlockPos position, Identifier lootTableId) {
        if (!isScalableItem(itemStack)) {
            return; // Most loot (block drops, materials) is never scaled, skip location resolution for it
        }
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
            applyModifiersForItemStack(handSlots(itemStack, false), itemId, itemStack, result.modifiers(), result.level());
        }
        if (itemStack.getItem() instanceof ArmorItem armor) {
            var itemData = new PatternMatching.ItemData(PatternMatching.ItemKind.ARMOR, lootTableId, itemEntry, rarity);
            debug("Item scaling start." + " dimension: " + dimensionId + " position: " + position + ", loot table: " + lootTableId + ", item: " + itemId + ", rarity: " + rarity);
            var result = PatternMatching.getModifiersForItem(locationData, itemData, world, scaling);
            debug("Pattern matching found " + result.modifiers().size() + " attribute modifiers");
            applyModifiersForItemStack(List.of(armor.getSlotType()), itemId, itemStack, result.modifiers(), result.level());
        }
        if (itemStack.getItem() instanceof ShieldItem) {
            var itemData = new PatternMatching.ItemData(PatternMatching.ItemKind.ARMOR, lootTableId, itemEntry, rarity);
            debug("Item scaling start." + " dimension: " + dimensionId + " position: " + position + ", loot table: " + lootTableId + ", item: " + itemId + ", rarity: " + rarity);
            var result = PatternMatching.getModifiersForItem(locationData, itemData, world, scaling);
            debug("Pattern matching found " + result.modifiers().size() + " attribute modifiers");
            applyModifiersForItemStack(handSlots(itemStack, true), itemId, itemStack, result.modifiers(), result.level());
        }
    }

    private static boolean isScalableItem(ItemStack itemStack) {
        var item = itemStack.getItem();
        return item instanceof ToolItem
                || item instanceof RangedWeaponItem
                || item instanceof ArmorItem
                || item instanceof ShieldItem;
    }

    public static void scale(ItemStack itemStack, int level) {
        var itemEntry = itemStack.getRegistryEntry();
        var itemId = Registries.ITEM.getId(itemStack.getItem()).toString();
        var rarity = itemStack.getRarity().toString();
        var lootTableId = new Identifier("minecraft", "none");
        var scaling = DungeonDifficulty.config.value.loot_scaling;

        if (itemStack.getItem() instanceof ToolItem || itemStack.getItem() instanceof RangedWeaponItem) {
            var itemData = new PatternMatching.ItemData(PatternMatching.ItemKind.WEAPONS, lootTableId, itemEntry, rarity);
            var result = PatternMatching.getItemScaleResult(itemData, scaling, level);
            applyModifiersForItemStack(handSlots(itemStack, false), itemId, itemStack, result.modifiers(), result.level());
        }
        if (itemStack.getItem() instanceof ArmorItem armor) {
            var itemData = new PatternMatching.ItemData(PatternMatching.ItemKind.ARMOR, lootTableId, itemEntry, rarity);
            var result = PatternMatching.getItemScaleResult(itemData, scaling, level);
            applyModifiersForItemStack(List.of(armor.getSlotType()), itemId, itemStack, result.modifiers(), result.level());
        }
        if (itemStack.getItem() instanceof ShieldItem) {
            var itemData = new PatternMatching.ItemData(PatternMatching.ItemKind.ARMOR, lootTableId, itemEntry, rarity);
            var result = PatternMatching.getItemScaleResult(itemData, scaling, level);
            applyModifiersForItemStack(handSlots(itemStack, true), itemId, itemStack, result.modifiers(), result.level());
        }
    }

    /// 1.20.1 has no `HAND` attribute slot. Weapons are scaled in the main hand (plus the off hand when the
    /// item already carries off-hand modifiers); shields, which 1.21 scales for `HAND`, get both hands.
    private static List<EquipmentSlot> handSlots(ItemStack itemStack, boolean bothHands) {
        if (bothHands || !itemStack.getAttributeModifiers(EquipmentSlot.OFFHAND).isEmpty()) {
            return List.of(EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND);
        }
        return List.of(EquipmentSlot.MAINHAND);
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

    private static double addValuesOf(List<Map.Entry<EntityAttribute, EntityAttributeModifier>> slotModifiers, EntityAttribute givenAttribute) {
        double value = 0;
        for (var entry : slotModifiers) {
            var modifier = entry.getValue();
            if (entry.getKey().equals(givenAttribute) && modifier.getOperation() == EntityAttributeModifier.Operation.ADDITION) {
                value += modifier.getValue();
            }
        }
        return value;
    }

    private record ScaledAttributeResult(double value) { }

    private static void applyModifiersForItemStack(List<EquipmentSlot> slots, String itemId, ItemStack itemStack, List<Config.AttributeModifier> modifiers, int level) {
        if (modifiers.isEmpty() || level == 0) {
            return;
        }
        var roundingUnit = getRoundingUnit();
        boolean useAdditiveModifiers = !DungeonDifficulty.config.value.meta.merge_item_modifiers;

        // Snapshot of every slot's modifiers (item defaults, or the stack's own NBT list) before the NBT is touched.
        // Untouched slots must be written back too, see the class comment.
        var snapshot = new LinkedHashMap<EquipmentSlot, List<Map.Entry<EntityAttribute, EntityAttributeModifier>>>();
        for (var slot : EquipmentSlot.values()) {
            snapshot.put(slot, new ArrayList<>(itemStack.getAttributeModifiers(slot).entries()));
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

        LinkedHashMap<EquipmentSlot, LinkedHashMap<EntityAttribute, ScaledAttributeResult>> results = new LinkedHashMap<>();
        for (var slot : slots) {
            var slotModifiers = snapshot.get(slot);
            var slotResults = new LinkedHashMap<EntityAttribute, ScaledAttributeResult>();
            results.put(slot, slotResults);
            for (var attributeBoost : summary.entrySet()) {
                List<EntityAttribute> affectedAttributes;
                var attributePattern = attributeBoost.getKey();
                var exactMatch = Registries.ATTRIBUTE.getOrEmpty(Identifier.tryParse(attributePattern)).orElse(null);
                if (exactMatch != null) {
                    affectedAttributes = List.of(exactMatch);
                } else {
                    var arrayList = new ArrayList<EntityAttribute>();
                    for (var entry : slotModifiers) {
                        var attribute = entry.getKey();
                        if (arrayList.contains(attribute)) {
                            continue;
                        }
                        if (PatternMatching.regexMatches(Registries.ATTRIBUTE.getId(attribute).toString(), attributePattern)) {
                            arrayList.add(attribute);
                        }
                    }
                    affectedAttributes = arrayList;
                }
                for (var attribute : affectedAttributes) {
                    var baseValue = addValuesOf(slotModifiers, attribute);

                    if (useAdditiveModifiers) {
                        // Additive behavior - calculate and apply only the difference
                        var boostedValue = attributeBoost.getValue().apply((float) baseValue);
                        var boostAmount = boostedValue - baseValue;
                        if (roundingUnit != null) {
                            boostAmount = MathHelper.round(boostAmount, roundingUnit);
                        }
                        if (boostAmount != 0) {
                            slotResults.put(attribute, new ScaledAttributeResult(boostAmount));
                        }
                    } else {
                        // Merge behavior - replace with scaled value
                        var value = baseValue;
                        value = attributeBoost.getValue().apply((float) value);
                        if (roundingUnit != null) {
                            value = MathHelper.round(value, roundingUnit);
                        }
                        slotResults.put(attribute, new ScaledAttributeResult(value));
                    }
                }
            }
        }

        // Rebuild the NBT list: every slot's modifiers in their original order, scaled ones replaced in place,
        // then the boosts for attributes the item did not have yet.
        itemStack.getOrCreateNbt().put(ATTRIBUTE_MODIFIERS_KEY, new NbtList());
        for (var slot : EquipmentSlot.values()) {
            var slotResults = results.get(slot);
            for (var entry : snapshot.get(slot)) {
                var attribute = entry.getKey();
                var modifier = entry.getValue();
                var result = slotResults != null ? slotResults.get(attribute) : null;
                boolean replacing = !useAdditiveModifiers && result != null && modifier.getOperation() == EntityAttributeModifier.Operation.ADDITION;

                if (replacing) {
                    // Same UUID and name (vanilla identifies the weapon damage/speed modifiers by UUID), new value
                    itemStack.addAttributeModifier(
                            attribute,
                            new EntityAttributeModifier(modifier.getId(), modifier.getName(), result.value(), EntityAttributeModifier.Operation.ADDITION),
                            slot);
                    slotResults.remove(attribute);
                } else {
                    // Still copy the original modifier into the new list
                    itemStack.addAttributeModifier(attribute, modifier, slot);
                }
            }
            if (slotResults == null) {
                continue;
            }
            for (var entry : slotResults.entrySet()) {
                var attribute = entry.getKey();
                var result = entry.getValue();
                itemStack.addAttributeModifier(
                        attribute,
                        new EntityAttributeModifier(boostModifierId(slot), boostModifierName(slot), result.value(), EntityAttributeModifier.Operation.ADDITION),
                        slot);
            }
        }
        markAsScaled(itemStack, level);
    }

    /// Stable per-slot id, the 1.20.1 counterpart of the `dungeon_difficulty:power_boost_<slot>` identifier used on 1.21.
    private static String boostModifierName(EquipmentSlot slot) {
        return DungeonDifficulty.MODID + ":power_boost_" + slot.getName();
    }

    private static UUID boostModifierId(EquipmentSlot slot) {
        return UUID.nameUUIDFromBytes(boostModifierName(slot).getBytes(StandardCharsets.UTF_8));
    }

    private static Double getRoundingUnit() {
        var config = DungeonDifficulty.config.value;
        if (config.meta != null && config.meta.rounding_unit != null) {
            return config.meta.rounding_unit;
        }
        return null;
    }

    public static void markAsScaled(ItemStack itemStack, int level) {
        itemStack.getOrCreateNbt().putInt(REWARD_SCALE_FACTOR, level);
    }

    public static boolean isScaled(ItemStack itemStack) {
        var nbt = itemStack.getNbt();
        if (nbt == null) {
            return false;
        }
        return nbt.contains(REWARD_SCALE_FACTOR, NbtElement.NUMBER_TYPE) || nbt.contains(LEGACY_SCALED_KEY);
    }

    public static int getScaleFactor(ItemStack itemStack) {
        var nbt = itemStack.getNbt();
        if (nbt == null) {
            return 0;
        }
        if (nbt.contains(REWARD_SCALE_FACTOR, NbtElement.NUMBER_TYPE)) {
            return nbt.getInt(REWARD_SCALE_FACTOR);
        }
        if (nbt.getBoolean(LEGACY_SCALED_KEY)) {
            return 1;
        }
        return 0;
    }

    public static void removeScaling(ItemStack itemStack) {
        NbtCompound nbt = itemStack.getNbt();
        if (nbt == null) {
            return;
        }
        nbt.remove(REWARD_SCALE_FACTOR);
        nbt.remove(LEGACY_SCALED_KEY);
        // Removing all attribute modifiers, as we made a full copy during scaling; the item's defaults apply again
        nbt.remove(ATTRIBUTE_MODIFIERS_KEY);
        if (nbt.isEmpty()) {
            itemStack.setNbt(null);
        }
    }

    public static void rescale(ItemStack itemStack, int newLevel) {
        if (isScaled(itemStack)) {
            ItemScaling.removeScaling(itemStack);
        }
        if (newLevel > 0) {
            ItemScaling.scale(itemStack, newLevel);
        }
    }

    /// Reaches the protected vanilla UUIDs that mark an item's own attack damage / speed modifiers.
    public abstract static class ItemAccessor extends Item {
        public ItemAccessor(Settings settings) {
            super(settings);
        }

        public static UUID hardCodedAttackDamageModifier() { return ATTACK_DAMAGE_MODIFIER_ID; }
        public static UUID hardCodedAttackSpeedModifier() { return ATTACK_SPEED_MODIFIER_ID; }
    }
}
