package net.powerscale;

import net.powerscale.config.ConfigManager;
import net.powerscale.logic.ItemScaling;
import net.fabricmc.api.ModInitializer;

public class PowerScale implements ModInitializer {
    @Override
    public void onInitialize() {
        ConfigManager.initialize();
        ItemScaling.initialize();
    }
}
