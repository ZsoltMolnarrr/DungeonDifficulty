package net.powerscale;

import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.powerscale.config.ConfigManager;
import net.powerscale.logic.ItemScaling;
import net.fabricmc.api.ModInitializer;

public class PowerScale implements ModInitializer {
    public static String MODID = "powerscale";

    @Override
    public void onInitialize() {
        ConfigManager.initialize();
        ItemScaling.initialize();

        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            dispatcher.register(CommandManager.literal(MODID + "_config_reload").executes(context -> {
                ConfigManager.reload();
                return 1;
            }));
        });
    }
}
