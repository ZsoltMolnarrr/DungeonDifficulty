package net.dungeon_difficulty.logic;

import com.google.common.collect.Multimap;
import com.mojang.logging.LogUtils;
import net.fabricmc.fabric.api.loot.v2.LootTableEvents;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.*;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.loot.function.LootFunctionTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.dungeon_difficulty.DungeonDifficulty;
import net.dungeon_difficulty.config.Config;
import org.slf4j.Logger;

import java.util.*;

public class ItemScaling {
    static final Logger LOGGER = LogUtils.getLogger();
    
    private static final boolean debugLogging = false;
    private static void debug(String message) {
        if (debugLogging) {
            System.out.println(message);
        }
    }

    public static void initialize() {
        LootTableEvents.MODIFY.register((resourceManager, lootManager, id, tableBuilder, source) -> {
            LootFunction function = new LootFunction() {
                @Override
                public LootFunctionType getType() {
                    return LootFunctionTypes.SET_ATTRIBUTES;
                }

                @Override
                public ItemStack apply(ItemStack itemStack, LootContext lootContext) {
                    var lootTableId = id;
                    var position = lootContext.get(LootContextParameters.ORIGIN);
                    BlockPos blockPosition = null;
                    if (position != null) {
                        blockPosition = new BlockPos(position);
                    }
                    scale(itemStack, lootContext.getWorld(), blockPosition, lootTableId.toString());
                    return itemStack;
                }
            };
            tableBuilder.apply(function);
        });
    }

    public static void scale(ItemStack itemStack, World world, BlockPos position, String lootTableId) {
        var itemId = Registries.ITEM.getId(itemStack.getItem()).toString();
        var rarity = itemStack.getRarity().toString();
        var dimensionId = world.getRegistryKey().getValue().toString(); // Just for logging
        if (itemStack.getItem() instanceof ToolItem || itemStack.getItem() instanceof RangedWeaponItem) {
            var locationData = PatternMatching.LocationData.create(world, position);
            var itemData = new PatternMatching.ItemData(PatternMatching.ItemKind.WEAPONS, lootTableId, itemId, rarity);
            debug("Item scaling start." + " dimension: " + dimensionId + " position: " + position + ", loot table: " + lootTableId + ", item: " + itemId + ", rarity: " + rarity);
            var modifiers = PatternMatching.getModifiersForItem(locationData, itemData);
            debug("Pattern matching found " + modifiers.size() + " attribute modifiers");
            applyModifiersForItemStack(new EquipmentSlot[]{ EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND }, itemId, itemStack, modifiers);
        }
        if (itemStack.getItem() instanceof ArmorItem) {
            var armor = (ArmorItem)itemStack.getItem();
            var locationData = PatternMatching.LocationData.create(world, position);
            var itemData = new PatternMatching.ItemData(PatternMatching.ItemKind.ARMOR, lootTableId, itemId, rarity);
            debug("Item scaling start." + " dimension: " + dimensionId + " position: " + position + ", loot table: " + lootTableId + ", item: " + itemId + ", rarity: " + rarity);
            var modifiers = PatternMatching.getModifiersForItem(locationData, itemData);
            debug("Pattern matching found " + modifiers.size() + " attribute modifiers");
            applyModifiersForItemStack(new EquipmentSlot[]{ armor.getSlotType() }, itemId, itemStack, modifiers);
        }
    }

    private static void applyModifiersForItemStack(EquipmentSlot[] slots, String itemId, ItemStack itemStack, List<Config.AttributeModifier> modifiers) {
        copyItemAttributesToNBT(itemStack); // We need to do this, to avoid unscaled attributes vanishing
        for (int i = 0; i < modifiers.size(); ++i) {
            var modifier = modifiers.get(i);
            try {
                var shouldRound = true;
                for (int j = i + 1; j < modifiers.size(); ++j) {
                    // Looking for an modifier for the same attribute after this
                    if (modifiers.get(j).attribute.equals(modifier.attribute)) {
                        shouldRound = false;
                        break;
                    }
                }
                if (modifier.attribute == null) {
                    continue;
                }
                var modifierValue = modifier.randomizedValue();
                debug("Starting to applying " + modifier.attribute + " to " + itemId);

                // The attribute we want to modify
                var attribute = Registries.ATTRIBUTE.get(new Identifier(modifier.attribute));

                Map<EquipmentSlot, Collection<EntityAttributeModifier>> slotSpecificAttributeCollections = new HashMap();

                for(var slot: slots) {
                    // The attribute modifiers from this item stack
                    var attributeModifiers = itemStack.getAttributeModifiers(slot);

                    // The modifiers changing the given attribute
                    var attributeSpecificCollection = attributeModifiers.get(attribute);

                    slotSpecificAttributeCollections.put(slot, attributeSpecificCollection);
                }

                for(var entry: slotSpecificAttributeCollections.entrySet()) {
                    var slot = entry.getKey();
                    var attributeSpecificCollection = entry.getValue();
                    var valueSummary = 0F;
                    var mergedModifiers = new ArrayList<EntityAttributeModifier>();
                    for (var attributeModifier : attributeSpecificCollection) {
                        if (attributeModifier.getOperation() != EntityAttributeModifier.Operation.ADDITION) {
                            continue;
                        }

                        valueSummary += attributeModifier.getValue();
                        mergedModifiers.add(attributeModifier);
                        debug("Found attribute value: " + attributeModifier.getValue()
                                + " current: " + valueSummary + " to be modified to:" + modifier.operation + " " + modifierValue);
                    }
                    switch (modifier.operation) {
                        case ADD -> {
                            valueSummary += modifierValue;
                        }
                        case MULTIPLY -> {
                            debug("Multiplying: " + valueSummary + " * " + modifierValue);
                            valueSummary *= modifierValue;
                        }
                    }
                    debug("Value summary updated to: " + valueSummary);
                    if (valueSummary != 0) {
                        for(var attributeModifier : mergedModifiers) {
                            removeAttributesFromItemStack(attributeModifier, itemStack);
                        }
                        var roundingUnit = getRoundingUnit();
                        if (shouldRound && roundingUnit != null) {
                            valueSummary = (float)MathHelper.round(valueSummary, roundingUnit);
                            debug("Rounded summary by " + roundingUnit + " to: " + valueSummary);
                        }
                        debug("Applying " + modifier.attribute + " to " + itemId + " value: " + valueSummary);
                        itemStack.addAttributeModifier(
                                attribute,
                                createEntityAttributeModifier(
                                        slot,
                                        attribute,
                                        "Scaled attribute modifier",
                                        valueSummary,
                                        EntityAttributeModifier.Operation.ADDITION
                                ),
                                slot
                        );
                    }
                }
            } catch (Exception e) {
                LOGGER.error("Failed to apply modifier to: " + itemId + " modifier:" + modifier);
                LOGGER.error("Reason: " + e.getMessage());
            }
        }
    }

    public record SlotSpecificItemAttributes(
            EquipmentSlot slot,
            Multimap<EntityAttribute, EntityAttributeModifier> attributes) { }

    private static void copyItemAttributesToNBT(ItemStack itemStack) {
        if (!itemStack.hasNbt() || !itemStack.getNbt().contains("AttributeModifiers", 9)) {
            // If no metadata yet
            List<SlotSpecificItemAttributes> slotSpecificItemAttributes = new ArrayList<>();
            for(var slot: EquipmentSlot.values()) {
                slotSpecificItemAttributes.add(new SlotSpecificItemAttributes(slot, itemStack.getAttributeModifiers(slot)));
            }
            for(var element: slotSpecificItemAttributes) {
                for(var entry: element.attributes.entries()) {
                    // debug("copyItemAttributesToNBT slot:" +  element.slot + " - adding: " + entry.getKey() + " - modifier: " + entry.getValue());
                    var attribute = entry.getKey();
                    itemStack.addAttributeModifier(
                            attribute,
                            createEntityAttributeModifier(
                                    element.slot,
                                    attribute,
                                    entry.getValue().getName(),
                                    entry.getValue().getValue(),
                                    entry.getValue().getOperation()
                            ),
                            element.slot
                    );
                }
            }
        }
    }

    private static EntityAttributeModifier createEntityAttributeModifier(EquipmentSlot slot, EntityAttribute attribute, String name, double value, EntityAttributeModifier.Operation operation) {
        UUID hardCodedUUID = null; // = hardCodedUUID(attribute);
        if (slot == EquipmentSlot.MAINHAND || slot == EquipmentSlot.OFFHAND) {
            hardCodedUUID = hardCodedUUID(attribute);
        }
        if (hardCodedUUID != null) {
            return new EntityAttributeModifier(hardCodedUUID, name, value, operation);
        } else {
            return new EntityAttributeModifier(name, value, operation);
        }
    }

    private static void removeAttributesFromItemStack(EntityAttributeModifier attributeModifier, ItemStack itemStack) {
        NbtList nbtList = itemStack.getNbt().getList("AttributeModifiers", 10);
        nbtList.removeIf(element -> {
            if (element instanceof NbtCompound compound) {
                return compound.getUuid("UUID").equals(attributeModifier.getId());
            }
            return false;
        });
    }

    private static UUID hardCodedUUID(EntityAttribute entityAttribute) {
        if (entityAttribute.equals(EntityAttributes.GENERIC_ATTACK_DAMAGE)) {
            return ItemAccessor.hardCodedAttackDamageModifier();
        }
        if (entityAttribute.equals(EntityAttributes.GENERIC_ATTACK_SPEED)) {
            return ItemAccessor.hardCodedAttackSpeedModifier();
        }
        return null;
    }

    public abstract static class ItemAccessor extends Item {
        public ItemAccessor(Settings settings) {
            super(settings);
        }

        public static UUID hardCodedAttackDamageModifier() { return ATTACK_DAMAGE_MODIFIER_ID; };
        public static UUID hardCodedAttackSpeedModifier() { return ATTACK_SPEED_MODIFIER_ID; };
    }

    private static Double getRoundingUnit() {
        var config = DungeonDifficulty.configManager.value;
        if (config.meta != null && config.meta.rounding_unit != null) {
            return config.meta.rounding_unit;
        }
        return null;
    }
}
