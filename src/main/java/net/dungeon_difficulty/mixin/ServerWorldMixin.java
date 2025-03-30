package net.dungeon_difficulty.mixin;

import net.dungeon_difficulty.DungeonDifficulty;
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
        var config = DungeonDifficulty.config.value.announcement;
        if (!config.enabled) {
            return;
        }

        int check_interval = config.check_interval_seconds * 20;
        for (var player: world.getPlayers()) {
            if (player.age % check_interval == 0) {
                var locationData = PatternMatching.LocationData.create(world, player.getBlockPos());
                var difficultyResult = PatternMatching.getDifficultyResult(locationData, world);

                if (difficultyResult != null && difficultyResult.difficulty().isValid()) {
                    var difficulty = difficultyResult.difficulty();

                    Difficulty.Announcement previous = null;
                    if (!((DifficultyHandler)player).getLastDifficultyAnnouncements().isEmpty()) {
                        previous = ((DifficultyHandler)player).getLastDifficultyAnnouncements().getLast();
                    }
                    if (previous != null && previous.difficulty().equals(difficulty)) {
                        continue;
                    }

                    announce(difficultyResult, player);
                }
            }
        }
    }

    @Unique
    private void announce(PatternMatching.DifficultySearchResult difficultyResult, ServerPlayerEntity player) {
        var difficulty = difficultyResult.difficulty();
        var locationData = difficultyResult.locationData();

        ((DifficultyHandler)player).getLastDifficultyAnnouncements();
        var announcement = new Difficulty.Announcement(difficulty, player.age, locationData.dimensionId());
        var announcements = ((DifficultyHandler)player).getLastDifficultyAnnouncements();
        for (var previous: announcements) {
            if (previous.difficulty().equals(difficulty)) {
                return;
            }
        }
        announcements.add(announcement);
        if (announcements.size() > 2) {
            announcements.removeFirst();
        }

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

        player.networkHandler.sendPacket(new TitleS2CPacket(Text.translatable(title)));
        player.networkHandler.sendPacket(new SubtitleS2CPacket(Text.translatable(difficulty.typeTranslationKey())
                .append(" " + difficulty.level()))
        );
    }
}
