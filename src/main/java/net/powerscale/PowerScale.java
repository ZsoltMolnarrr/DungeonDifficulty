package net.powerscale;

import logic.ItemScale;
import net.fabricmc.api.ModInitializer;

public class PowerScale implements ModInitializer {
    @Override
    public void onInitialize() {
        ItemScale.initialize();
    }
}
