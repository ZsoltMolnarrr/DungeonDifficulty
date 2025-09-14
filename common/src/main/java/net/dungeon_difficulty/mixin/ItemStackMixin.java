package net.dungeon_difficulty.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.dungeon_difficulty.DungeonDifficulty;
import net.dungeon_difficulty.logic.RarityHelper;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.dungeon_difficulty.logic.ItemScaling;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Rarity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;
import java.util.function.Consumer;

@Mixin(ItemStack.class)
public class ItemStackMixin {
    private ItemStack itemStack() {
        return (ItemStack) (Object) this;
    }

    @Inject(method = "getRarity", at = @At("RETURN"), cancellable = true)
    private void injected(CallbackInfoReturnable<Rarity> cir) {
        var itemStack = itemStack();
        Rarity rarity = itemStack.getOrDefault(DataComponentTypes.RARITY, Rarity.COMMON);
        if (DungeonDifficulty.clientConfig.value.enable_overriding_enchantment_rarity
                && itemStack.hasEnchantments()) {
            rarity = RarityHelper.increasedRarity(rarity, 1);
        }
        if (DungeonDifficulty.clientConfig.value.enable_scaled_items_rarity
                && ItemScaling.isScaled(itemStack)) {
            rarity = RarityHelper.increasedRarity(rarity, 1);
        }

        if (rarity != cir.getReturnValue()) {
            cir.setReturnValue(rarity);
            cir.cancel();
        }
    }

    @WrapOperation(
            method = "getTooltip",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;appendTooltip(Lnet/minecraft/component/ComponentType;Lnet/minecraft/item/Item$TooltipContext;Ljava/util/function/Consumer;Lnet/minecraft/item/tooltip/TooltipType;)V"))
    private void injected(ItemStack instance, ComponentType<?> componentType, Item.TooltipContext context, Consumer<Text> textConsumer, TooltipType type, Operation<Void> original) {
        if (componentType == DataComponentTypes.JUKEBOX_PLAYABLE) {
            var level = ItemScaling.getScaleFactor(instance);
            if (level > 0) {
                textConsumer.accept(Text.translatable("item.power.level", level)
                        .formatted(EntityAttribute.Category.POSITIVE.getFormatting(true))
                );
            }
        }
        original.call(instance, componentType, context, textConsumer, type);
    }
}
