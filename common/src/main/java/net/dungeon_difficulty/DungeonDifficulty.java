package net.dungeon_difficulty;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.dungeon_difficulty.config.ClientConfig;
import net.dungeon_difficulty.config.Config;
import net.dungeon_difficulty.compat.waystones.WaystonesCompat;
import net.dungeon_difficulty.config.Default;
import net.dungeon_difficulty.config.RegistryConfig;
import net.dungeon_difficulty.effect.DifficultyEffects;
import net.dungeon_difficulty.logic.DifficultyHandler;
import net.dungeon_difficulty.logic.DifficultyTypes;
import net.dungeon_difficulty.logic.GivenEffects;
import net.dungeon_difficulty.logic.ItemScaling;
import net.dungeon_difficulty.logic.LocalScalingLootFunction;
import net.dungeon_difficulty.logic.RarityColors;
import net.dungeon_difficulty.naming.StructureNaming;
import net.dungeon_difficulty.naming.StructureNamingCommands;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.tiny_config.ConfigManager;

public class DungeonDifficulty {
    public static final String MODID = "dungeon_difficulty";

    // Read via `safeValue()`, as registration may happen before `init()`, on another thread
    // Declared before `config`, as its defaults depend on this
    public static ConfigManager<RegistryConfig> registryConfig = new ConfigManager<>
            ("registry", new RegistryConfig())
            .builder()
            .setDirectory(MODID)
            .sanitize(true)
            .build();

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
        reloadClientConfig();
        reloadConfig();
        WaystonesCompat.init();
    }

    /// Registers the mod's commands. Called by each loader from its own command registration event.
    public static void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        StructureNamingCommands.register(dispatcher);
        dispatcher.register(CommandManager.literal(MODID + "_config_reload").executes(context -> {
                System.out.println("Reloading Dungeon Difficulty config");
                DungeonDifficulty.reloadClientConfig();
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
    }

    public static void reloadClientConfig() {
        clientConfig.refresh();
        RarityColors.initialize();
    }

    public static void reloadConfig() {
        config.load();
        var config = DungeonDifficulty.config.value;
        if (config.meta != null) {
            DungeonDifficulty.config.sanitize = config.meta.sanitize_config;
        }
        DifficultyTypes.resolve();
        StructureNaming.reloadConfig();
        GivenEffects.clearCache();
        DungeonDifficulty.config.save();

//        var gson = new GsonBuilder().setPrettyPrinting().create();
//        System.out.println("PowerScale config refreshed: " + gson.toJson(DungeonDifficulty.config.value));
    }

    public static void registerEffects() {
        if (!registryConfig.safeValue().register_status_effects) {
            return;
        }
        DifficultyEffects.register();
    }

    public static void registerLootFunctions() {
        Registry.register(Registries.LOOT_FUNCTION_TYPE, LocalScalingLootFunction.ID, LocalScalingLootFunction.TYPE);
    }
}
