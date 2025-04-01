package net.dungeon_difficulty.logic;

import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;
import net.dungeon_difficulty.DungeonDifficulty;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributeModifier.Operation;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.registry.Registries;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;
import java.util.function.Consumer;

/**
 * Merged Attribute Modifier tooltips, inspired by NeoForge.
 */
public class AttributeTooltipHandler {
    private static final DecimalFormat FORMAT = new DecimalFormat("#.##", new DecimalFormatSymbols(Locale.ROOT));
    private static final Identifier FAKE_MERGED_ID = Identifier.of(DungeonDifficulty.MODID, "fake_merged_modifier");

    private static final Formatting BASE_COLOR = Formatting.DARK_GREEN;
    private static final Formatting MERGED_BASE_COLOR = Formatting.GOLD;
    private static final int MERGED_MODIFIER_COLOR = 7699710; // Light Blue
    private static final Formatting POSITIVE_COLOR = Formatting.BLUE;
    private static final Formatting NEGATIVE_COLOR = Formatting.RED;

    private static final Comparator<EntityAttributeModifier> ATTRIBUTE_MODIFIER_COMPARATOR = 
        Comparator.comparing(EntityAttributeModifier::operation)
            .thenComparing((EntityAttributeModifier a) -> -Math.abs(a.value()))
            .thenComparing(EntityAttributeModifier::id);

    private static final Map<String, AttributeModifierSlot> KEY_SLOT_MAP = Util.make(new HashMap<>(), map -> {
        map.put(Text.translatable("item.modifiers.mainhand").getString(), AttributeModifierSlot.MAINHAND);
        map.put(Text.translatable("item.modifiers.offhand").getString(), AttributeModifierSlot.OFFHAND);
        map.put(Text.translatable("item.modifiers.head").getString(), AttributeModifierSlot.HEAD);
        map.put(Text.translatable("item.modifiers.chest").getString(), AttributeModifierSlot.CHEST);
        map.put(Text.translatable("item.modifiers.legs").getString(), AttributeModifierSlot.LEGS);
        map.put(Text.translatable("item.modifiers.feet").getString(), AttributeModifierSlot.FEET);
        map.put(Text.translatable("item.modifiers.body").getString(), AttributeModifierSlot.BODY);
    });

    private static final Set<Identifier> BASE_ATTRIBUTE_IDS = Util.make(new HashSet<>(), set -> {
        set.add(Registries.ATTRIBUTE.getId(EntityAttributes.GENERIC_ATTACK_DAMAGE.value()));
        set.add(Registries.ATTRIBUTE.getId(EntityAttributes.GENERIC_ATTACK_SPEED.value()));
        set.add(Registries.ATTRIBUTE.getId(EntityAttributes.PLAYER_ENTITY_INTERACTION_RANGE.value()));
        set.add(Identifier.of("ranged_weapon", "damage"));
        set.add(Identifier.of("ranged_weapon", "velocity"));
        set.remove(null);
    });

    private static final Map<Identifier, Identifier> BASE_MODIFIER_IDS = Util.make(new HashMap<>(), map -> {
        map.put(Registries.ATTRIBUTE.getId(EntityAttributes.GENERIC_ATTACK_DAMAGE.value()), Item.BASE_ATTACK_DAMAGE_MODIFIER_ID);
        map.put(Registries.ATTRIBUTE.getId(EntityAttributes.GENERIC_ATTACK_SPEED.value()), Item.BASE_ATTACK_SPEED_MODIFIER_ID);
        map.put(Registries.ATTRIBUTE.getId(EntityAttributes.PLAYER_ENTITY_INTERACTION_RANGE.value()), Identifier.ofVanilla("base_entity_reach"));
        map.put(Identifier.of("ranged_weapon", "damage"), Identifier.of("ranged_weapon", "base_damage"));
        map.put(Identifier.of("ranged_weapon", "velocity"), Identifier.of("ranged_weapon", "base_velocity"));
        map.remove(null);
    });

    public static boolean isDetailedView() {
        return DungeonDifficulty.clientConfig.value.enable_enhanced_attribute_tooltips && Screen.hasShiftDown();
    }

    public static void processTooltip(ItemStack stack, List<Text> tooltip, @Nullable PlayerEntity player) {
        if (!hasEnhanceableAttributeModifiers(stack)) {
            return;
        }

        List<AttributeSection> sections = findAttributeSections(tooltip);
        if (sections.isEmpty()) {
            return;
        }

        List<Text> newTooltip = new ArrayList<>();
        int currentIndex = 0;
        for (AttributeSection section : sections) {
            int sectionStart = section.startIndex;
            while (currentIndex < sectionStart) {
                newTooltip.add(tooltip.get(currentIndex++));
            }
            newTooltip.add(tooltip.get(currentIndex++));
            processAttributeSection(stack, tooltip, newTooltip, currentIndex, section.lineCount, section.slot, player);
            currentIndex += section.lineCount;
        }

        while (currentIndex < tooltip.size()) {
            newTooltip.add(tooltip.get(currentIndex++));
        }
        tooltip.clear();
        tooltip.addAll(newTooltip);
    }

    private static void processAttributeSection(
            ItemStack stack,
            List<Text> originalTooltip, 
            List<Text> newTooltip,
            int startIndex, 
            int lineCount, 
            AttributeModifierSlot slot,
            @Nullable PlayerEntity player) {

        Multimap<RegistryEntry<EntityAttribute>, EntityAttributeModifier> modifiers = getSortedModifiers(stack, slot);
        if (modifiers.isEmpty()) {
            for (int i = 0; i < lineCount; i++) {
                newTooltip.add(originalTooltip.get(startIndex + i));
            }
            return;
        }
        applyTextFor(stack, newTooltip::add, modifiers, player);
    }

    /**
     * Creates a sorted TreeMultimap to ensure consistent ordering of modifiers
     */
    private static Multimap<RegistryEntry<EntityAttribute>, EntityAttributeModifier> sortedMap() {
        return TreeMultimap.create(
            Comparator.comparing(e -> e.getKey().toString()),
            ATTRIBUTE_MODIFIER_COMPARATOR
        );
    }

    private static Multimap<RegistryEntry<EntityAttribute>, EntityAttributeModifier> getSortedModifiers(ItemStack stack, AttributeModifierSlot slot) {
        Multimap<RegistryEntry<EntityAttribute>, EntityAttributeModifier> map = sortedMap();

        stack.applyAttributeModifier(slot, (attributeHolder, modifier) -> {
            if (attributeHolder != null && modifier != null) {
                map.put(attributeHolder, modifier);
            }
        });
        
        return map;
    }

    private static void applyTextFor(
            ItemStack stack,
            Consumer<Text> tooltip, 
            Multimap<RegistryEntry<EntityAttribute>, EntityAttributeModifier> modifierMap,
            @Nullable PlayerEntity player) {
        
        if (modifierMap.isEmpty()) {
            return;
        }

        Map<RegistryEntry<EntityAttribute>, BaseModifier> baseModifiers = new IdentityHashMap<>();

        Multimap<RegistryEntry<EntityAttribute>, EntityAttributeModifier> remainingModifiers = sortedMap();
        remainingModifiers.putAll(modifierMap);

        var it = remainingModifiers.entries().iterator();
        while (it.hasNext()) {
            var entry = it.next();
            RegistryEntry<EntityAttribute> attr = entry.getKey();
            EntityAttributeModifier modifier = entry.getValue();
            
            if (isBaseModifier(attr.value(), modifier)) {
                baseModifiers.put(attr, new BaseModifier(modifier, new ArrayList<>()));
                it.remove();
            }
        }

        it = remainingModifiers.entries().iterator();
        while (it.hasNext()) {
            var entry = it.next();
            RegistryEntry<EntityAttribute> attr = entry.getKey();
            EntityAttributeModifier modifier = entry.getValue();

            if (isBaseAttribute(attr.value())) {
                BaseModifier base = baseModifiers.get(attr);
                if (base != null) {
                    base.children.add(modifier);
                    it.remove();
                }
            }
        }

        for (var entry : baseModifiers.entrySet()) {
            RegistryEntry<EntityAttribute> attr = entry.getKey();
            BaseModifier baseModifier = entry.getValue();

            double entityBase = player == null ? 0 : player.getAttributeBaseValue(attr);
            double base = baseModifier.base.value() + entityBase;
            final double rawBase = base;
            double amount = base;

            for (EntityAttributeModifier modifier : baseModifier.children) {
                switch (modifier.operation()) {
                    case ADD_VALUE:
                        base = amount = amount + modifier.value();
                        break;
                    case ADD_MULTIPLIED_BASE:
                        amount += modifier.value() * rawBase;
                        break;
                    case ADD_MULTIPLIED_TOTAL:
                        amount *= 1.0 + modifier.value();
                        break;
                }
            }

            boolean isMerged = !baseModifier.children.isEmpty();
            
            MutableText text = createBaseComponent(attr.value(), amount, entityBase, isMerged);
            tooltip.accept(Text.literal(" ").append(text.formatted(isMerged ? MERGED_BASE_COLOR : BASE_COLOR)));
            
            if (isDetailedView() && isMerged) {
                text = createBaseComponent(attr.value(), rawBase, entityBase, false);
                tooltip.accept(listHeader().append(text.formatted(BASE_COLOR)));
                
                for (EntityAttributeModifier modifier : baseModifier.children) {
                    tooltip.accept(listHeader().append(createModifierComponent(attr.value(), modifier)));
                }
            }
        }

        for (RegistryEntry<EntityAttribute> attr : remainingModifiers.keySet()) {
            if (baseModifiers.containsKey(attr)) {
                continue;
            }
            Collection<EntityAttributeModifier> modifiers = remainingModifiers.get(attr);
            boolean isBaseAttr = isBaseAttribute(attr.value());
            if (modifiers.size() > 1) {
                Map<Operation, MergedModifierData> mergeData = new EnumMap<>(Operation.class);
                
                for (EntityAttributeModifier modifier : modifiers) {
                    if (modifier.value() == 0) {
                        continue;
                    }
                    
                    MergedModifierData data = mergeData.computeIfAbsent(modifier.operation(), op -> new MergedModifierData());
                    if (data.sum != 0) {
                        data.isMerged = true;
                    }
                    data.sum += modifier.value();
                    data.children.add(modifier);
                }
                for (Operation op : Operation.values()) {
                    MergedModifierData data = mergeData.get(op);
                    if (data == null || data.sum == 0) {
                        continue;
                    }
                    if (data.isMerged) {
                        EntityAttributeModifier fakeModifier = new EntityAttributeModifier(
                            FAKE_MERGED_ID, data.sum, op);
                        
                        MutableText modComponent = createModifierComponent(attr.value(), fakeModifier);
                        if (isBaseAttr) {
                            tooltip.accept(modComponent.formatted(MERGED_BASE_COLOR));
                        } else {
                            tooltip.accept(modComponent.styled(style -> style.withColor(MERGED_MODIFIER_COLOR)));
                        }
                        
                        if (isDetailedView()) {
                            for (EntityAttributeModifier mod : data.children) {
                                tooltip.accept(listHeader().append(createModifierComponent(attr.value(), mod)));
                            }
                        }
                    } else {
                        EntityAttributeModifier fakeModifier = new EntityAttributeModifier(
                            FAKE_MERGED_ID, data.sum, op);
                        
                        tooltip.accept(createModifierComponent(attr.value(), fakeModifier));
                    }
                }
            } else {
                for (EntityAttributeModifier modifier : modifiers) {
                    if (modifier.value() != 0) {
                        tooltip.accept(createModifierComponent(attr.value(), modifier));
                    }
                }
            }
        }
    }

    private static MutableText createBaseComponent(EntityAttribute attribute, double value, double entityBase, boolean merged) {
        return Text.translatable("attribute.modifier.equals.0",
            FORMAT.format(value),
            Text.translatable(attribute.getTranslationKey()));
    }

    private static MutableText createModifierComponent(EntityAttribute attribute, EntityAttributeModifier modifier) {
        double value = modifier.value();
        boolean isPositive = value > 0;

        String key = isPositive ? 
            "attribute.modifier.plus." + modifier.operation().getId() : 
            "attribute.modifier.take." + modifier.operation().getId();
        String formattedValue = formatValue(attribute, value, modifier.operation());
        MutableText component = Text.translatable(key,
            formattedValue,
            Text.translatable(attribute.getTranslationKey()));
        if (!isBaseAttribute(attribute) && modifier.id().equals(FAKE_MERGED_ID)) {
            return component.styled(style -> style.withColor(MERGED_MODIFIER_COLOR));
        }
        Formatting color = getModifierFormatting(attribute, modifier, isPositive);
        return component.formatted(color);
    }

    private static String formatValue(EntityAttribute attribute, double value, Operation operation) {
        double absValue = Math.abs(value);
        
        if (operation == Operation.ADD_VALUE) {
            if (attribute == EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE) {
                return FORMAT.format(absValue * 10); // Display as percentage x10
            } else {
                return FORMAT.format(absValue);
            }
        } else {
            return FORMAT.format(absValue * 100);
        }
    }

    private static Formatting getModifierFormatting(EntityAttribute attribute, EntityAttributeModifier modifier, boolean isPositive) {
        if (isBaseModifier(attribute, modifier)) {
            return BASE_COLOR;
        }
        if (isBaseAttribute(attribute) && modifier.id().equals(FAKE_MERGED_ID)) {
            return MERGED_BASE_COLOR;
        }
        return isPositive ? POSITIVE_COLOR : NEGATIVE_COLOR;
    }

    private static boolean isBaseAttribute(EntityAttribute attribute) {
        Identifier id = Registries.ATTRIBUTE.getId(attribute);
        return id != null && BASE_ATTRIBUTE_IDS.contains(id);
    }

    private static boolean isBaseModifier(EntityAttribute attribute, EntityAttributeModifier modifier) {
        Identifier baseId = getBaseModifierId(attribute);
        return modifier.id().equals(baseId);
    }

    @Nullable
    private static Identifier getBaseModifierId(EntityAttribute attribute) {
        Identifier id = Registries.ATTRIBUTE.getId(attribute);
        return id != null ? BASE_MODIFIER_IDS.get(id) : null;
    }


    private static MutableText listHeader() {
        return Text.literal(" \u2507 ").formatted(Formatting.GRAY);
    }

    private static List<AttributeSection> findAttributeSections(List<Text> tooltip) {
        List<AttributeSection> result = new ArrayList<>();
        for (int i = 0; i < tooltip.size(); i++) {
            Text line = tooltip.get(i);
            AttributeModifierSlot slot = getSlotFromText(line);
            
            if (slot != null) {
                int numLines = countAttributeLines(tooltip, i + 1);
                if (numLines > 0) {
                    result.add(new AttributeSection(i, numLines, slot));
                }
            }
        }
        return result;
    }

    @Nullable
    private static AttributeModifierSlot getSlotFromText(Text text) {
        String content = text.getString();
        return KEY_SLOT_MAP.get(content);
    }

    private static int countAttributeLines(List<Text> tooltip, int startIndex) {
        int count = 0;
        for (int i = startIndex; i < tooltip.size(); i++) {
            String line = tooltip.get(i).getString();

            // Stop if we hit an empty line or another section header
            if (line.isEmpty() || getSlotFromText(Text.literal(line)) != null) {
                break;
            }

            // This looks like an attribute line
            count++;
        }

        return count;
    }


    private static boolean hasEnhanceableAttributeModifiers(ItemStack stack) {
        if (!DungeonDifficulty.clientConfig.value.enable_enhanced_attribute_tooltips) {
            return false;
        }

        AttributeModifiersComponent component = stack.getOrDefault(
                DataComponentTypes.ATTRIBUTE_MODIFIERS, 
                AttributeModifiersComponent.DEFAULT
        );

        return component.showInTooltip() && !component.modifiers().isEmpty();
    }
    
    /**
     * Stores a single base modifier and its children
     */
    private static class BaseModifier {
        final EntityAttributeModifier base;
        final List<EntityAttributeModifier> children;
        
        BaseModifier(EntityAttributeModifier base, List<EntityAttributeModifier> children) {
            this.base = base;
            this.children = children;
        }
    }
    
    /**
     * Stores merged modifier data for a specific operation
     */
    private static class MergedModifierData {
        double sum = 0;
        boolean isMerged = false;
        List<EntityAttributeModifier> children = new ArrayList<>();
    }
    
    /**
     * Represents a section of attribute modifiers in the tooltip
     */
    private static class AttributeSection {
        final int startIndex;
        final int lineCount;
        final AttributeModifierSlot slot;
        
        AttributeSection(int startIndex, int lineCount, AttributeModifierSlot slot) {
            this.startIndex = startIndex;
            this.lineCount = lineCount;
            this.slot = slot;
        }
    }
} 