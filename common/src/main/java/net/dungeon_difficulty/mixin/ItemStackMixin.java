package net.dungeon_difficulty.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.dungeon_difficulty.DungeonDifficulty;
import net.dungeon_difficulty.logic.ItemScaling;
import net.dungeon_difficulty.logic.RarityHelper;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Rarity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.UUID;

@Mixin(ItemStack.class)
public class ItemStackMixin {
    private ItemStack itemStack() {
        return (ItemStack) (Object) this;
    }

    // Vanilla identifies an item's own attack damage / speed modifiers by UUID *reference*
    // (`modifier.getId() == Item.ATTACK_DAMAGE_MODIFIER_ID`), which never matches modifiers read back from
    // NBT — so scaled weapons would show "+7 Attack Damage" instead of "8 Attack Damage". Hand back the
    // canonical instance for equal ids.
    @Redirect(method = "getTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/attribute/EntityAttributeModifier;getId()Ljava/util/UUID;"))
    private UUID getTooltip_canonicalModifierId_DungeonDifficulty(EntityAttributeModifier instance) {
        var id = instance.getId();
        if (id.equals(ItemScaling.ItemAccessor.hardCodedAttackDamageModifier())) {
            return ItemScaling.ItemAccessor.hardCodedAttackDamageModifier();
        }
        if (id.equals(ItemScaling.ItemAccessor.hardCodedAttackSpeedModifier())) {
            return ItemScaling.ItemAccessor.hardCodedAttackSpeedModifier();
        }
        return id;
    }

    @Inject(method = "getRarity", at = @At("RETURN"), cancellable = true)
    private void getRarity_RETURN_DungeonDifficulty(CallbackInfoReturnable<Rarity> cir) {
        if (DungeonDifficulty.clientConfig.value.enable_scaled_items_rarity
                && ItemScaling.isScaled(itemStack())) {
            var rarity = RarityHelper.increasedRarity(cir.getReturnValue(), 1);
            if (rarity != cir.getReturnValue()) {
                cir.setReturnValue(rarity);
            }
        }
    }

    // Power level line right after the item's own tooltip lines (before enchantments and attributes)
    @Inject(method = "getTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/Item;appendTooltip(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Ljava/util/List;Lnet/minecraft/client/item/TooltipContext;)V", shift = At.Shift.AFTER))
    private void getTooltip_powerLevel_DungeonDifficulty(CallbackInfoReturnable<List<Text>> cir, @Local(ordinal = 0) List<Text> list) {
        var level = ItemScaling.getScaleFactor(itemStack());
        if (level > 0) {
            list.add(Text.translatable("item.power.level", level).formatted(Formatting.BLUE));
        }
    }
}
