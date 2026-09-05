package net.dungeon_difficulty;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.network.packet.Packet;
import net.minecraft.server.network.ServerPlayerEntity;

/// Loader-neutral access to simple loader utilities, so `common` needs no loader API.
/// Each loader provides `PlatformImpl` with the same static signatures (Architectury `@ExpectPlatform`).
/// Loader-shaped subscriptions (events, loot table mutation, command registration) are NOT routed
/// through here: the loader modules subscribe natively and call into `common`.
public class Platform {
    public static final boolean Fabric;
    public static final boolean Forge;
    public static final boolean NeoForge;

    static
    {
        Fabric = getPlatformType() == Type.FABRIC;
        Forge  = getPlatformType() == Type.FORGE;
        NeoForge = getPlatformType() == Type.NEOFORGE;
    }

    public enum Type { FABRIC, FORGE, NEOFORGE }

    @ExpectPlatform
    protected static Type getPlatformType() {
        throw new AssertionError();
    }

    public interface Util {
        /// Whether a mod is present. Fabric: `FabricLoader.isModLoaded`; NeoForge: `LoadingModList`
        /// (resolved during discovery, so it is safe from static initializers such as the default config).
        boolean isModLoaded(String modid);
        /// Send a vanilla clientbound packet to a player.
        void sendVanillaPacket(ServerPlayerEntity player, Packet<?> packet);
    }

    @ExpectPlatform
    public static Util util() {
        throw new AssertionError();
    }
}
