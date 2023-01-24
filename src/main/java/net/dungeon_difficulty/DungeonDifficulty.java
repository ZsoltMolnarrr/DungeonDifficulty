package net.dungeon_difficulty;

import com.google.gson.Gson;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.dungeon_difficulty.config.Config;
import net.dungeon_difficulty.config.Default;
import net.dungeon_difficulty.logic.ItemScaling;
import net.fabricmc.api.ModInitializer;
import net.tinyconfig.ConfigManager;

public class DungeonDifficulty implements ModInitializer { // :)
    public static String MODID = "dungeon_difficulty";

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
                DungeonDifficulty.reloadConfig();
                return 1;
            }));
        });
    }

    public static void reloadConfig() {
        configManager.load();
        var config = configManager.value;
        if (config.meta != null) {
            configManager.sanitize = config.meta.sanitize_config;
            if (config.meta.override_with_default) {
                configManager.value = Default.config;
            }
        }
        configManager.save();
        // System.out.println("PowerScale config refreshed: " + (new Gson()).toJson(configManager.value));
    }
}
