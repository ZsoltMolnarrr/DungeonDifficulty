package net.powerscale;

import net.powerscale.config.ConfigManager;
import net.powerscale.logic.ItemScaling;
import net.fabricmc.api.ModInitializer;

public class PowerScale implements ModInitializer {
    public static String MODID = "powerscale";

    @Override
    public void onInitialize() {
        ConfigManager.initialize();
        ItemScaling.initialize();
    }
}
