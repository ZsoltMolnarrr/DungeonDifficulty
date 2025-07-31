package net.dungeon_difficulty.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.dungeon_difficulty.logic.Difficulty;
import net.dungeon_difficulty.logic.DifficultyTypes;
import net.dungeon_difficulty.logic.ItemScaling;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.SmithingRecipe;
import net.minecraft.recipe.input.RecipeInput;
import net.minecraft.recipe.input.SmithingRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.SmithingScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SmithingScreenHandler.class)
public class SmithingScreenHandlerMixin {

    @WrapOperation(method = "updateResult", at = @At(value = "INVOKE", target = "Lnet/minecraft/recipe/SmithingRecipe;craft(Lnet/minecraft/recipe/input/RecipeInput;Lnet/minecraft/registry/RegistryWrapper$WrapperLookup;)Lnet/minecraft/item/ItemStack;"))
    private static ItemStack updateResult_craft(
            SmithingRecipe instance, RecipeInput recipeInput, RegistryWrapper.WrapperLookup wrapperLookup, Operation<ItemStack> original) {
        var crafted = original.call(instance, recipeInput, wrapperLookup);
        var input = (SmithingRecipeInput) recipeInput;
        var baseItemStack = input.base();

        var difficultyType = DifficultyTypes.firstWithReward();
        if (difficultyType != null && difficultyType.rewards.smithing_upgrade.enabled
                && !input.template().isEmpty()
                && input.template().getRegistryEntry().getKey().get().getValue().toString().contains("upgrade") // Is upgrade?
                && ItemScaling.isScaled(baseItemStack)) {
            var upgrade = difficultyType.rewards.smithing_upgrade;
            var level = ItemScaling.getScaleFactor(baseItemStack);
            int newLevel = (int) ((level + upgrade.add_upon_upgrade) * upgrade.multiply_upon_upgrade);
            if (newLevel > 0) {
                var difficulty = new Difficulty(
                        difficultyType,
                        newLevel,
                        0,
                        newLevel
                );
                ItemScaling.scale(crafted, difficulty);
            }
        }
        return crafted;
    }
}
