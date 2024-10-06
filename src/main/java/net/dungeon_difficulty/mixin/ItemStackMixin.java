package net.dungeon_difficulty.mixin;

import net.dungeon_difficulty.DungeonDifficulty;
import net.dungeon_difficulty.logic.RarityHelper;
import net.minecraft.component.DataComponentTypes;
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
}
