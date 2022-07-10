package net.powerscale;

import net.powerscale.logic.ItemScaling;
import net.fabricmc.api.ModInitializer;

public class PowerScale implements ModInitializer {
    @Override
    public void onInitialize() {
        ItemScaling.initialize();
    }
}
