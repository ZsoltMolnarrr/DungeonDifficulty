package net.dungeon_difficulty.naming;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

/// `/structure_name get|set <name>|reset`, acting on the named structure at the command source's position
public class StructureNamingCommands {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("structure_name")
                .requires(source -> source.hasPermissionLevel(2))
                .then(CommandManager.literal("get")
                        .executes(context -> get(context.getSource())))
                .then(CommandManager.literal("set")
                        .then(CommandManager.argument("name", StringArgumentType.greedyString())
                                .executes(context -> set(context.getSource(), StringArgumentType.getString(context, "name")))))
                .then(CommandManager.literal("reset")
                        .executes(context -> reset(context.getSource())))
        );
    }

    private static int get(ServerCommandSource source) {
        var key = locate(source);
        if (key == null) {
            return 0;
        }
        var name = StructureNaming.getName(source.getWorld(), key);
        if (name == null) {
            source.sendError(Text.literal("Structure naming is not available right now"));
            return 0;
        }
        source.sendFeedback(() -> Text.literal("This is " + name + " (" + key.structureId() + ")"), false);
        return 1;
    }

    private static int set(ServerCommandSource source, String name) {
        var key = locate(source);
        if (key == null) {
            return 0;
        }
        var previous = StructureNaming.getName(source.getWorld(), key);
        switch (StructureNaming.rename(source.getWorld(), key, name)) {
            case SUCCESS -> {
                var newName = name.trim();
                source.sendFeedback(() -> Text.literal("Renamed " + previous + " to " + newName + " (" + key.structureId() + ")"), true);
                return 1;
            }
            case INVALID_NAME -> source.sendError(Text.literal("Names must be 1 to " + StructureNaming.MAX_NAME_LENGTH + " characters long"));
            case NAME_TAKEN -> source.sendError(Text.literal("Name already used: " + name.trim()));
            case UNAVAILABLE -> source.sendError(Text.literal("Structure naming is not available right now"));
        }
        return 0;
    }

    private static int reset(ServerCommandSource source) {
        var key = locate(source);
        if (key == null) {
            return 0;
        }
        var previous = StructureNaming.getName(source.getWorld(), key);
        if (!StructureNaming.reset(source.getWorld(), key)) {
            source.sendError(Text.literal("Structure naming is not available right now"));
            return 0;
        }
        source.sendFeedback(() -> Text.literal("Reset the name of " + previous + " (" + key.structureId() + "), a new one is generated upon next visit"), true);
        return 1;
    }

    @Nullable private static StructureKey locate(ServerCommandSource source) {
        if (!StructureNaming.isEnabled()) {
            source.sendError(Text.literal("Structure naming is disabled (config/dungeon_difficulty/structure_naming.json)"));
            return null;
        }
        var key = StructureNaming.find(source.getWorld(), BlockPos.ofFloored(source.getPosition()));
        if (key == null) {
            source.sendError(Text.literal("Not inside a named structure"));
        }
        return key;
    }
}
