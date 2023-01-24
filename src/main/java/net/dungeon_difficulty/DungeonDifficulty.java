package net.dungeon_difficulty;

import net.dungeon_difficulty.config.Config;
import net.dungeon_difficulty.config.Default;
import net.dungeon_difficulty.logic.DifficultyTypes;
import net.dungeon_difficulty.logic.ItemScaling;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.tinyconfig.ConfigManager;

public class DungeonDifficulty implements ModInitializer {
    public static String MODID = "dungeon_difficulty";

    public static ConfigManager<Config> config = new ConfigManager<Config>
            (MODID + "_v2", Default.config)
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
                // var gson = new GsonBuilder().setPrettyPrinting().create();
                // System.out.println("Resolved difficulty types: " + gson.toJson(DifficultyTypes.resolved));
                return 1;
            }));
        });
    }

    public static void reloadConfig() {
        config.load();
        var config = DungeonDifficulty.config.value;
        if (config.meta != null) {
            DungeonDifficulty.config.sanitize = config.meta.sanitize_config;
            if (!config.meta.allow_customization) {
                DungeonDifficulty.config.value = Default.config;
            }
        }
        DifficultyTypes.resolve();
        DungeonDifficulty.config.save();
        // System.out.println("PowerScale config refreshed: " + (new Gson()).toJson(configManager.value));
    }
}
