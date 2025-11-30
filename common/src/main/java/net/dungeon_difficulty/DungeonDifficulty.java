package net.dungeon_difficulty;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.dungeon_difficulty.config.ClientConfig;
import net.dungeon_difficulty.config.Config;
import net.dungeon_difficulty.config.Default;
import net.dungeon_difficulty.logic.DifficultyHandler;
import net.dungeon_difficulty.logic.DifficultyTypes;
import net.dungeon_difficulty.logic.ItemScaling;
import net.dungeon_difficulty.logic.LocalScalingLootFunction;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.command.CommandManager;
import net.tiny_config.ConfigManager;

public class DungeonDifficulty {
    public static final String MODID = "dungeon_difficulty";

    public static ConfigManager<Config> config = new ConfigManager<>
            ("difficulty_v2", Default.config)
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

    public static void init() {
        clientConfig.refresh();
        reloadConfig();
        ItemScaling.initialize();

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal(MODID + "_config_reload").executes(context -> {
                System.out.println("Reloading Dungeon Difficulty config");
                DungeonDifficulty.reloadConfig();
                try {
                    for (var player: context.getSource().getServer().getPlayerManager().getPlayerList()) {
                        ((DifficultyHandler)player).getLastDifficultyAnnouncements().clear();
                    }
                } catch (Exception e) {
                    // ignore
                }
//                var gson = new GsonBuilder().setPrettyPrinting().create();
//                System.out.println("Resolved difficulty types: " + gson.toJson(DifficultyTypes.resolved));
//                System.out.println("Full: " + gson.toJson(DungeonDifficulty.config.value));
                return 1;
            }));
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal("power_level")
                    .requires(source -> source.hasPermissionLevel(2))
                    .then(CommandManager.argument("players", EntityArgumentType.player())
                        .then(CommandManager.argument("level", IntegerArgumentType.integer(0))
                            .executes(context -> {
                                var players = EntityArgumentType.getPlayers(context, "players");
                                var level = IntegerArgumentType.getInteger(context, "level");
                                if (level < 0) {
                                    level = 0;
                                }
                                for (var player : players) {
                                    var heldItemStack = player.getMainHandStack();
                                    ItemScaling.rescale(heldItemStack, level);
                                }
                                return 1;
                            })
                        )
                    )
            );
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

//        var gson = new GsonBuilder().setPrettyPrinting().create();
//        System.out.println("PowerScale config refreshed: " + gson.toJson(DungeonDifficulty.config.value));
    }

    public static void registerLootFunctions() {
        Registry.register(Registries.LOOT_FUNCTION_TYPE, LocalScalingLootFunction.ID, LocalScalingLootFunction.TYPE);
    }
}
