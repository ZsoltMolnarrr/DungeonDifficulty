package net.dungeon_difficulty.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.dungeon_difficulty.DungeonDifficulty;
import net.dungeon_difficulty.logic.ItemScaling;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.SmithingRecipe;
import net.minecraft.inventory.Inventory;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.screen.SmithingScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SmithingScreenHandler.class)
public class SmithingScreenHandlerMixin {

    @WrapOperation(method = "updateResult", at = @At(value = "INVOKE", target = "Lnet/minecraft/recipe/SmithingRecipe;craft(Lnet/minecraft/inventory/Inventory;Lnet/minecraft/registry/DynamicRegistryManager;)Lnet/minecraft/item/ItemStack;"))
    private static ItemStack updateResult_craft(
            SmithingRecipe instance, Inventory inventory, DynamicRegistryManager registryManager, Operation<ItemStack> original) {
        var crafted = original.call(instance, inventory, registryManager);
        // Smithing input slots: 0 = template, 1 = base, 2 = addition
        var template = inventory.getStack(0);
        var baseItemStack = inventory.getStack(1);

        var config = DungeonDifficulty.config.value;
        var lootScaling = config.loot_scaling;
        if (lootScaling != null && lootScaling.smithing_upgrade.enabled
                && !template.isEmpty()
                && Registries.ITEM.getId(template.getItem()).toString().contains("upgrade") // Is upgrade?
                && ItemScaling.isScaled(baseItemStack)) {
            var upgrade = lootScaling.smithing_upgrade;
            var level = ItemScaling.getScaleFactor(baseItemStack);
            int newLevel = (int) ((level + upgrade.add_upon_upgrade) * upgrade.multiply_upon_upgrade);
            ItemScaling.rescale(crafted, newLevel);
        }
        return crafted;
    }
}
