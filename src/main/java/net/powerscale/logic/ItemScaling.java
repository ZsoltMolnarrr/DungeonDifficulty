package net.powerscale.logic;

import com.mojang.logging.LogUtils;
import net.fabricmc.fabric.api.loot.v2.LootTableEvents;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.RangedWeaponItem;
import net.minecraft.item.ToolItem;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.loot.function.LootFunctionTypes;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.powerscale.config.Config;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemScaling {
    static final Logger LOGGER = LogUtils.getLogger();

    private static void applyModifiersForItemStack(EquipmentSlot[] slots, Identifier itemId, ItemStack itemStack, List<Config.ItemModifier> modifiers) {
        for (Config.ItemModifier modifier: modifiers) {
            try {
                System.out.println("Applying A extra attack damage to " + itemId);
                // The attribute we want to modify
                var attribute = Registry.ATTRIBUTE.get(new Identifier(modifier.attribute));

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

                    switch (modifier.operation) {
                        case ADD -> {
                            itemStack.addAttributeModifier(
                                    attribute,
                                    new EntityAttributeModifier(
                                            "powerscale:item_scaling_addition",
                                            modifier.randomizedValue(),
                                            EntityAttributeModifier.Operation.ADDITION
                                    ),
                                    slot
                            );
                        }
                        case MULTIPLY -> {
                            for (EntityAttributeModifier attributeModifier: attributeSpecificCollection) {
                                if(attributeModifier.getOperation() != EntityAttributeModifier.Operation.ADDITION) {
                                    continue;
                                }
                                System.out.println("Applying extra B attack damage to " + itemId);
                                itemStack.addAttributeModifier(
                                        attribute,
                                        new EntityAttributeModifier(
                                                attributeModifier.getName(),
                                                attributeModifier.getValue() * modifier.randomizedValue(),
                                                attributeModifier.getOperation()
                                        ),
                                        slot
                                );
                            }
                        }
                    }
                }
            } catch (Exception e) {
                LOGGER.error("Failed to apply modifier to: " + itemId + " modifier:" + modifier);
                LOGGER.error("Reason: " + e.getMessage());
            }
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
                    var itemId = Registry.ITEM.getId(itemStack.getItem());
                    var dimensionId = lootContext.getWorld().getRegistryKey().getValue();
                    System.out.println("Checking apply for: " + itemId + " in dimension: " + dimensionId);
                    if (itemStack.getItem() instanceof ToolItem || itemStack.getItem() instanceof RangedWeaponItem) { // TODO: Ranged weapons
                        var modifiers = PatternMatching.getModifiersForWeapon(itemId, dimensionId);
                        applyModifiersForItemStack(new EquipmentSlot[]{ EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND }, itemId, itemStack, modifiers);
                    }
                    if (itemStack.getItem() instanceof ArmorItem) {
                        var armor = (ArmorItem)itemStack.getItem();
                        var modifiers = PatternMatching.getModifiersForArmor(itemId, dimensionId);
                        applyModifiersForItemStack(new EquipmentSlot[]{ armor.getSlotType() }, itemId, itemStack, modifiers);
                    }
                    return itemStack;
                }
            };
            tableBuilder.apply(function);
        });
    }
}
