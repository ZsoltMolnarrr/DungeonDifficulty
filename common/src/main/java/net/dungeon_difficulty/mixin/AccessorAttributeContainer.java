package net.dungeon_difficulty.mixin;

import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AttributeContainer.class)
public interface AccessorAttributeContainer {
    @Accessor("fallback")
    DefaultAttributeContainer getFallback();
}
