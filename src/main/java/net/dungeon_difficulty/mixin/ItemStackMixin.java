package net.dungeon_difficulty.mixin;

import net.dungeon_difficulty.DungeonDifficulty;
import net.dungeon_difficulty.logic.RarityHelper;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.ItemStack;
import net.dungeon_difficulty.logic.ItemScaling;
import net.minecraft.util.Rarity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(ItemStack.class)
public class ItemStackMixin {
    private ItemStack itemStack() {
        return (ItemStack) (Object) this;
    }

    @Inject(method = "getRarity", at = @At("RETURN"), cancellable = true)
    private void injected(CallbackInfoReturnable<Rarity> cir) {
        var value = cir.getReturnValue();
        var stack = itemStack();
        if (DungeonDifficulty.clientConfig.value.enable_overriding_enchantment_rarity
                && stack.hasEnchantments()) {
            var newValue = RarityHelper.increasedRarity(this.rarity, 1);
            cir.setReturnValue(newValue);
            cir.cancel();
        }

        var nbt = itemStack().getNbt();
        if (nbt != null && nbt.contains(ItemScaling.ALREADY_SCALED_NBT_KEY)
                && DungeonDifficulty.clientConfig.value.enable_scaled_items_rarity
                && value.ordinal() <= DungeonDifficulty.clientConfig.value.scaled_item_rarity_max.ordinal()) {
            var newValue = RarityHelper.increasedRarity(value, 1);
            cir.setReturnValue(newValue);
        }

    }
}
