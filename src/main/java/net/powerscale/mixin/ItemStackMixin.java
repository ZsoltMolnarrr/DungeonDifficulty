package net.powerscale.mixin;

import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.ItemStack;
import net.powerscale.logic.ItemScaling;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

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
}
