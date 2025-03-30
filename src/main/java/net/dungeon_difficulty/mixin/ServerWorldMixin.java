package net.dungeon_difficulty.mixin;

import net.dungeon_difficulty.logic.DifficultyHandler;
import net.dungeon_difficulty.util.LanguageUtil;
import net.dungeon_difficulty.logic.Difficulty;
import net.dungeon_difficulty.logic.PatternMatching;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin {
    // Logic moved to `ServerEntityManager` mixin, to fix entities spawning with structures
//    @Inject(method = "addEntity", at = @At("HEAD"))
//    private void pre_addEntity(Entity entity, CallbackInfoReturnable<Boolean> cir) {
//        System.out.println("Adding entity: " + entity.getName());
//        if (entity.getName().toString().contains("minecraft.piglin")) {
//            System.out.println("Spawning piglin!");
//        }
//        var world = (ServerWorld) ((Object)this);
//        EntityScaling.scale(entity, world);
//    }

    // private Map<Integer, Difficulty.Announcement> announcements = new HashMap<>();

    @Inject(method = "tick", at = @At("TAIL"))
    private void pre_tick(CallbackInfo ci) {
        var world = (ServerWorld) ((Object)this);
        boolean extra_performance_friendly_checking = true;
        int check_interval = 20;
        int throttle_interval = 20 * 10;
        for (var player: world.getPlayers()) {
            if (player.age % check_interval == 0) {
                var locationData = PatternMatching.LocationData.create(world, player.getBlockPos());
                var difficultyResult = PatternMatching.getDifficultyResult(locationData, world);

                if (difficultyResult != null && difficultyResult.difficulty().isValid()) {
                    var difficulty = difficultyResult.difficulty();

                    var previous = ((DifficultyHandler)player).getLastDifficultyAnnouncement();
                    if (previous != null && previous.difficulty().equals(difficulty)) {
                        continue;
                    }
                    var send = previous == null
                            || !previous.dimensionId().equals(locationData.dimensionId())
                            || player.age > (previous.age() + throttle_interval);
                    announce(difficultyResult, player, send);

                    if (extra_performance_friendly_checking) {
                        // Only 1 player to check per tick
                        break;
                    }
                }
            }
        }
    }

    @Unique
    private void announce(PatternMatching.DifficultySearchResult difficultyResult, ServerPlayerEntity player, boolean publish) {
        var difficulty = difficultyResult.difficulty();
        var locationData = difficultyResult.locationData();
        var announcement = new Difficulty.Announcement(difficulty, player.age, locationData.dimensionId());

        var title = "Dungeon";
        if (difficultyResult.match() != null) {
            var match = difficultyResult.match();
            if (match.matchingStructure() != null) {
                var id = match.matchingStructure().getKey().get().getValue();
                title = LanguageUtil.translateId("structure", id.toString());
            } else if (match.matchingBiome() != null && match.matchingBiome().getKey().isPresent()) {
                var id = match.matchingBiome().getKey().get().getValue();
                title = "biome." + id.getNamespace() + "." + id.getPath();
            }
        } else {
            var biome = difficultyResult.locationData().biome().biomeEntry();
            if (biome.getKey().isPresent()) {
                var id = biome.getKey().get().getValue();
                title = "biome." + id.getNamespace() + "." + id.getPath();
            }
        }

        if (publish) {
            player.networkHandler.sendPacket(new TitleS2CPacket(Text.translatable(title)));
            player.networkHandler.sendPacket(new SubtitleS2CPacket(Text.of(difficulty.type().name + " " + difficulty.level())));
        }
        ((DifficultyHandler)player).setLastDifficultyAnnouncement(announcement);
    }
}
