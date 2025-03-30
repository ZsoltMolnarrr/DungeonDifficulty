package net.dungeon_difficulty;

import net.dungeon_difficulty.config.ClientConfig;
import net.dungeon_difficulty.config.Config;
import net.dungeon_difficulty.config.Default;
import net.dungeon_difficulty.logic.DifficultyTypes;
import net.dungeon_difficulty.logic.ItemScaling;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.tinyconfig.ConfigManager;

public class DungeonDifficulty implements ModInitializer { // :)
    public static String MODID = "dungeon_difficulty";

    public static ConfigManager<Config> config = new ConfigManager<>
            ("difficulty", Default.config)
            .builder()
            .setDirectory(MODID)
            .sanitize(true)
            .build();

    public static ConfigManager<ClientConfig> clientConfig = new ConfigManager<>
            ("client_settings", new ClientConfig())
            .builder()
            .setDirectory(MODID)
            .sanitize(true)
            .build();

    @Override
    public void onInitialize() {
        clientConfig.refresh();
        reloadConfig();
        ItemScaling.initialize();

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal(MODID + "_config_reload").executes(context -> {
                System.out.println("Reloading Dungeon Difficulty config");
                DungeonDifficulty.reloadConfig();
//                var gson = new GsonBuilder().setPrettyPrinting().create();
//                System.out.println("Resolved difficulty types: " + gson.toJson(DifficultyTypes.resolved));
//                System.out.println("Full: " + gson.toJson(DungeonDifficulty.config.value));
                return 1;
            }));
        });


    }

    public static void reloadConfig() {
        config.load();
        var config = DungeonDifficulty.config.value;
        if (config.meta != null) {
            DungeonDifficulty.config.sanitize = config.meta.sanitize_config;
        }
        DifficultyTypes.resolve();
        DungeonDifficulty.config.save();
        // System.out.println("PowerScale config refreshed: " + (new Gson()).toJson(configManager.value));
    }
}
