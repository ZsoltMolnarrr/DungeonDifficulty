package net.dungeon_difficulty.neoforge;

import net.dungeon_difficulty.Platform;
import net.minecraft.entity.attribute.ClampedEntityAttribute;
import net.minecraft.network.packet.Packet;
import net.minecraft.server.network.ServerPlayerEntity;
import net.neoforged.fml.ModList;

public class PlatformImpl {
    public static Platform.Type getPlatformType() {
        return Platform.Type.NEOFORGE;
    }

    public static class NeoForgeUtil implements Platform.Util {
        @Override
        public boolean isModLoaded(String modid) {
            return ModList.get().isLoaded(modid);
        }

        @Override
        public void sendVanillaPacket(ServerPlayerEntity player, Packet<?> packet) {
            player.networkHandler.send(packet);
        }

    }
    private static final Platform.Util UTIL = new NeoForgeUtil();
    public static Platform.Util util() {
        return UTIL;
    }
}
