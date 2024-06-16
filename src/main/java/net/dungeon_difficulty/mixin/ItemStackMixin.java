package net.dungeon_difficulty.mixin;

import net.dungeon_difficulty.DungeonDifficulty;
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
    // This is a bugfix for Mojang :)
    // `entityAttributeModifier.getId() == Item.ATTACK_DAMAGE_MODIFIER_ID` never matches for
    // UUIDs those were deserialized from NBT
    @Redirect(method = "getTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/attribute/EntityAttributeModifier;getId()Ljava/util/UUID;"))
    public UUID fixId(EntityAttributeModifier instance) {
        if (instance.getId().equals(ItemScaling.ItemAccessor.hardCodedAttackDamageModifier())) {
            return ItemScaling.ItemAccessor.hardCodedAttackDamageModifier();
        }
        if (instance.getId().equals(ItemScaling.ItemAccessor.hardCodedAttackSpeedModifier())) {
            return ItemScaling.ItemAccessor.hardCodedAttackSpeedModifier();
        }
        return instance.getId();
    }

    private ItemStack itemStack() {
        return (ItemStack) (Object) this;
    }

    @Inject(method = "getRarity", at = @At("RETURN"), cancellable = true)
    private void injected(CallbackInfoReturnable<Rarity> cir) {
        var value = cir.getReturnValue();
        var nbt = itemStack().getNbt();
        if (nbt != null && nbt.contains(ItemScaling.ALREADY_SCALED_NBT_KEY)
                && DungeonDifficulty.clientConfig.value.enable_scaled_items_rarity
                && value.ordinal() <= DungeonDifficulty.clientConfig.value.scaled_item_rarity_max.ordinal()
                && value.ordinal() < Rarity.values().length) {
            var newValue = Rarity.values()[value.ordinal() + 1];
            cir.setReturnValue(newValue);
        }
    }
}
