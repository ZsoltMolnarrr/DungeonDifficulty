package net.dungeon_difficulty.mixin;

import net.dungeon_difficulty.DungeonDifficulty;
import net.dungeon_difficulty.logic.RarityHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Rarity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/// On 1.20.1 the enchantment rarity bump lives in `Item#getRarity` (COMMON/UNCOMMON jump straight to RARE).
/// With the override enabled it becomes a single step up from the item's own rarity, as on 1.21.
/// Items overriding `getRarity` keep their own logic.
@Mixin(Item.class)
public class ItemMixin {
    @Shadow @Final private Rarity rarity;

    @Inject(method = "getRarity", at = @At("HEAD"), cancellable = true)
    private void getRarity_HEAD_DungeonDifficulty(ItemStack stack, CallbackInfoReturnable<Rarity> cir) {
        if (DungeonDifficulty.clientConfig.value.enable_overriding_enchantment_rarity
                && stack.hasEnchantments()) {
            cir.setReturnValue(RarityHelper.increasedRarity(this.rarity, 1));
        }
    }
}
