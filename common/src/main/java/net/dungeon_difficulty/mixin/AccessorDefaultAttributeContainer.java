package net.dungeon_difficulty.mixin;

import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.registry.entry.RegistryEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(DefaultAttributeContainer.class)
public interface AccessorDefaultAttributeContainer {
    @Accessor("instances")
    Map<RegistryEntry<EntityAttribute>, EntityAttributeInstance> getInstances();
}
