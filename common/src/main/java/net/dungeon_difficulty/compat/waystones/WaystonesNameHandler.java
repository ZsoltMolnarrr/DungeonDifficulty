package net.dungeon_difficulty.compat.waystones;

import net.blay09.mods.balm.api.Balm;
import net.blay09.mods.waystones.api.WaystoneOrigin;
import net.blay09.mods.waystones.api.event.GenerateWaystoneNameEvent;
import net.dungeon_difficulty.naming.StructureNaming;
import net.minecraft.text.Text;

/// The only class touching Waystones and Balm types. Loaded only via `WaystonesCompat`.
class WaystonesNameHandler {
    private static volatile boolean failureLogged = false;

    static void register() {
        Balm.getEvents().onEvent(GenerateWaystoneNameEvent.class, WaystonesNameHandler::onGenerateName);
    }

    private static void onGenerateName(GenerateWaystoneNameEvent event) {
        try {
            var config = StructureNaming.config.safeValue();
            if (!StructureNaming.isEnabled() || config.waystones == null || !config.waystones.enabled) {
                return;
            }
            var waystone = event.getWaystone();
            if (waystone.getOrigin() != WaystoneOrigin.VILLAGE) {
                return;
            }
            var server = Balm.getHooks().getServer();
            // Waystones may name waystones during world generation (worker threads), naming needs the server thread
            if (server == null || !server.isOnThread()) {
                return;
            }
            var world = server.getWorld(waystone.getDimension());
            if (world == null) {
                return;
            }
            var structure = StructureNaming.find(world, waystone.getPos());
            if (structure == null) {
                return;
            }
            var name = StructureNaming.nextWaystoneName(world, structure);
            if (name != null) {
                event.setName(Text.literal(name));
            }
        } catch (Throwable throwable) {
            // Keep the name Waystones generated
            if (!failureLogged) {
                failureLogged = true;
                WaystonesCompat.LOGGER.error("Dungeon Difficulty: failed to name waystone after its village", throwable);
            }
        }
    }
}
