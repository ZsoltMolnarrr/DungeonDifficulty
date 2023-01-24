package net.dungeon_difficulty.logic;

import com.google.common.collect.Multimap;
import com.mojang.logging.LogUtils;
import net.dungeon_difficulty.DungeonDifficulty;
import net.dungeon_difficulty.config.Config;
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
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
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

    public static void scale(ItemStack itemStack, ServerWorld world, BlockPos position, String lootTableId) {
        var locationData = PatternMatching.LocationData.create(world, position);
        scale(itemStack, world, lootTableId, locationData);
    }

    public static void scale(ItemStack itemStack, ServerWorld world, String lootTableId, PatternMatching.LocationData locationData) {
        var itemId = Registry.ITEM.getId(itemStack.getItem()).toString();
        var rarity = itemStack.getRarity().toString();
        var dimensionId = world.getRegistryKey().getValue().toString(); // Just for logging
        var position = locationData.position();
        if (itemStack.getItem() instanceof ToolItem || itemStack.getItem() instanceof RangedWeaponItem) {
            var itemData = new PatternMatching.ItemData(PatternMatching.ItemKind.WEAPONS, lootTableId, itemId, rarity);
            debug("Item scaling start." + " dimension: " + dimensionId + " position: " + position + ", loot table: " + lootTableId + ", item: " + itemId + ", rarity: " + rarity);
            var result = PatternMatching.getModifiersForItem(locationData, itemData, world);
            debug("Pattern matching found " + result.modifiers().size() + " attribute modifiers");
            applyModifiersForItemStack(new EquipmentSlot[]{ EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND }, itemId, itemStack, result.modifiers(), result.level());
        }
        if (itemStack.getItem() instanceof ArmorItem) {
            var armor = (ArmorItem)itemStack.getItem();
            var itemData = new PatternMatching.ItemData(PatternMatching.ItemKind.ARMOR, lootTableId, itemId, rarity);
            debug("Item scaling start." + " dimension: " + dimensionId + " position: " + position + ", loot table: " + lootTableId + ", item: " + itemId + ", rarity: " + rarity);
            var result = PatternMatching.getModifiersForItem(locationData, itemData, world);
            debug("Pattern matching found " + result.modifiers().size() + " attribute modifiers");
            applyModifiersForItemStack(new EquipmentSlot[]{ armor.getSlotType() }, itemId, itemStack, result.modifiers(), result.level());
        }
    }

    private record ModifierSummary(float add, float multiplyBase) {
        public ModifierSummary add(float value) {
            return new ModifierSummary(add + value, multiplyBase);
        }
        public ModifierSummary multiplyBase(float value) {
            return new ModifierSummary(add, multiplyBase  + value);
        }
    }

    private static void applyModifiersForItemStack(EquipmentSlot[] slots, String itemId, ItemStack itemStack, List<Config.AttributeModifier> modifiers, int level) {
        if (modifiers.isEmpty() || level == 0) {
            return;
        }

        ArrayList<EntityAttribute> originalAttributeOrder = new ArrayList<>();
        for (var slot: slots) {
            var multimap = itemStack.getAttributeModifiers(slot);
            if (multimap.isEmpty()) { continue; }
            for (var entry: multimap.entries()) {
                originalAttributeOrder.add(entry.getKey());
            }
            break;
        }

        copyItemAttributesToNBT(itemStack); // We need to do this, to avoid unscaled attributes vanishing

        var summary = new HashMap<String, ModifierSummary>();
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
            summary.put(modifier.attribute, element);
        }

        for(var slot: slots) {
            // The attribute modifiers from this item stack
            var attributeModifiers = itemStack.getAttributeModifiers(slot);
            if (attributeModifiers.isEmpty()) { continue; }
            // System.out.println("ItemStack attributes after copying: " + itemStack.getNbt());

            for (var entry: summary.entrySet()) {
                // Apply additions
                try {
                    var scaling = entry.getValue();
                    if (scaling.add() == 0) {
                        continue;
                    }
                    var attributeId = new Identifier(entry.getKey());
                    var attribute = Registry.ATTRIBUTE.get(attributeId);
                    var currentModifiers = attributeModifiers.get(attribute);
                    var newValue = combineAdditionModifiers(currentModifiers) + scaling.add();
                    var roundingUnit = getRoundingUnit();
                    if (roundingUnit != null) {
                        newValue = MathHelper.round(newValue, roundingUnit);
                    }
                    removeAttributesFromItemStack(currentModifiers, attributeId.toString(), itemStack);
                    itemStack.addAttributeModifier(
                            attribute,
                            createEntityAttributeModifier(
                                    slot,
                                    attribute,
                                    "DD Bonus",
                                    newValue,
                                    EntityAttributeModifier.Operation.ADDITION
                            ),
                            slot
                    );
                } catch (Exception e) {
                    System.err.println("Failed to apply addition of " + entry.getKey() + " " + entry.getValue().add() + ", to: " + itemId);
                    LOGGER.error("Reason: " + e.getMessage());
                }
            }
            for (var entry: summary.entrySet()) {
                // Apply multiply base
                try {
                    var scaling = entry.getValue();
                    if (scaling.multiplyBase() == 0) {
                        continue;
                    }
                    var regex = entry.getKey();
                    ArrayList<Identifier> attributeIds = new ArrayList<>();
                    for(var attribute: attributeModifiers.keySet()) {
                        var id = Registry.ATTRIBUTE.getId(attribute);
                        var idString = id.toString();
                        if (PatternMatching.matches(idString, regex)) {
                            attributeIds.add(id);
                        }
                    }
                    for (var attributeId: attributeIds) {
                        var attribute = Registry.ATTRIBUTE.get(attributeId);
                        var currentModifiers = attributeModifiers.get(attribute);
                        var newValue = combineAdditionModifiers(currentModifiers) * (1F + scaling.multiplyBase());
                        var roundingUnit = getRoundingUnit();
                        if (roundingUnit != null) {
                            newValue = MathHelper.round(newValue, roundingUnit);
                        }
                        removeAttributesFromItemStack(currentModifiers, attributeId.toString(), itemStack);
                        itemStack.addAttributeModifier(
                                attribute,
                                createEntityAttributeModifier(
                                        slot,
                                        attribute,
                                        "DD Multiply",
                                        newValue,
                                        EntityAttributeModifier.Operation.ADDITION
                                ),
                                slot
                        );
                    }
                } catch (Exception e) {
                    System.err.println("Failed to apply multiply_base of " + entry.getKey() + " " + entry.getValue().add() + ", to: " + itemId);
                    LOGGER.error("Reason: " + e.getMessage());
                }
            }

            // System.out.println("Restoring original order of attributes");
            var unsortedAttributes = itemStack.getAttributeModifiers(slot);
            itemStack.getNbt().put("AttributeModifiers", new NbtList()); // Resetting the list of attribute modifiers
            // System.out.println("ItemStack NBT: " + itemStack.getNbt().toString());
            for (var attribute: originalAttributeOrder) {
                // System.out.println(" - " + Registry.ATTRIBUTE.getId(attribute).toString());
                var modifiersToRestore = unsortedAttributes.get(attribute);
                for (var modifierToRestore: modifiersToRestore) {
                    itemStack.addAttributeModifier(
                            attribute,
                            modifierToRestore,
                            slot
                    );
                }
                unsortedAttributes.removeAll(attribute);
            }
            for (var entry: unsortedAttributes.entries()) {
                var attribute = entry.getKey();
                var modifierToRestore = entry.getValue();
                itemStack.addAttributeModifier(
                        attribute,
                        modifierToRestore,
                        slot
                );

            }
            //System.out.println("ItemStack NBT: " + itemStack.getNbt().toString());
        }
    }

    private static double combineAdditionModifiers(Collection<EntityAttributeModifier> modifiers) {
        float summary = 0;
        for (var modifier : modifiers) {
            if (modifier.getOperation() != EntityAttributeModifier.Operation.ADDITION) { continue; }
            summary += modifier.getValue();
        }
        return summary;
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
                    // System.out.println("copyItemAttributesToNBT slot:" +  element.slot + " - adding: " + entry.getKey() + " - modifier: " + entry.getValue());
                    var attribute = entry.getKey();
                    itemStack.addAttributeModifier(
                            attribute,
                            entry.getValue(),
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

    private static void removeAttributesFromItemStack(Collection<EntityAttributeModifier> modifiers, String attributeId, ItemStack itemStack) {
        for (var modifier: modifiers) {
            removeAttributesFromItemStack(modifier, attributeId, itemStack);
        }
    }

    private static void removeAttributesFromItemStack(EntityAttributeModifier attributeModifier, String attributeId, ItemStack itemStack) {
        NbtList nbtList = itemStack.getNbt().getList("AttributeModifiers", 10);
        nbtList.removeIf(element -> {
            if (element instanceof NbtCompound compound) {
                return compound.getUuid("UUID").equals(attributeModifier.getId())
                        && compound.getString("AttributeName").equals(attributeId);
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