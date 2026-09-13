package net.dungeon_difficulty.forge;

import net.dungeon_difficulty.Platform;
import net.minecraft.network.packet.Packet;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraftforge.fml.loading.LoadingModList;

public class PlatformImpl {
    public static Platform.Type getPlatformType() {
        return Platform.Type.FORGE;
    }

    public static class ForgeUtil implements Platform.Util {
        @Override
        public boolean isModLoaded(String modid) {
            // LoadingModList (not ModList) is populated during discovery, before any mod constructor runs,
            // so the check is safe from static initializers such as the default config.
            return LoadingModList.get().getModFileById(modid) != null;
        }

        @Override
        public void sendVanillaPacket(ServerPlayerEntity player, Packet<?> packet) {
            player.networkHandler.sendPacket(packet);
        }

    }
    private static final Platform.Util UTIL = new ForgeUtil();
    public static Platform.Util util() {
        return UTIL;
    }
}
