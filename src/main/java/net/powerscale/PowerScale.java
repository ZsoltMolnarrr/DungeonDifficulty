package net.powerscale;

import com.google.gson.Gson;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.powerscale.config.Config;
import net.powerscale.config.Default;
import net.powerscale.logic.ItemScaling;
import net.fabricmc.api.ModInitializer;
import net.tinyconfig.ConfigManager;

public class PowerScale implements ModInitializer {
    public static String MODID = "powerscale";

    public static ConfigManager<Config> configManager = new ConfigManager<Config>
            (MODID, Default.config)
            .builder()
            .sanitize(true)
            .build();

    @Override
    public void onInitialize() {
        reloadConfig();
        ItemScaling.initialize();

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal(MODID + "_config_reload").executes(context -> {
                PowerScale.reloadConfig();
                return 1;
            }));
        });
    }

    public static void reloadConfig() {
        configManager.load();
        var config = configManager.currentConfig;
        if (config.meta != null) {
            configManager.sanitize = config.meta.sanitize_config;
            if (config.meta.override_with_default) {
                configManager.currentConfig = Default.config;
            }
        }
        configManager.save();
        System.out.println("PowerScale config refreshed: " + (new Gson()).toJson(configManager.currentConfig));
    }
}
