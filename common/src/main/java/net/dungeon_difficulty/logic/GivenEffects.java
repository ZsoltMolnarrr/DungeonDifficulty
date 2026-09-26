package net.dungeon_difficulty.logic;

import net.dungeon_difficulty.DungeonDifficulty;
import net.dungeon_difficulty.config.Config;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class GivenEffects {
    public record Resolved(RegistryEntry<StatusEffect> effect, int amplifier, Config.GivenEffect definition) { }

    // Resolved lazily, so effects registered by other mods are available
    private static final Map<String, Optional<RegistryEntry<StatusEffect>>> cache = new HashMap<>();

    public static void clearCache() {
        cache.clear();
    }

    /// Returns effects applicable at the given difficulty level, keeping the highest amplifier per effect
    public static List<Resolved> resolve(List<Config.GivenEffect> definitions, int level) {
        var results = new ArrayList<Resolved>();
        for (var definition : definitions) {
            if (definition == null || definition.id == null || level < definition.min_level) {
                continue;
            }
            var effect = lookup(definition.id);
            if (effect.isEmpty()) {
                continue;
            }
            var amplifier = amplifier(definition, level);
            var existing = results.stream().filter(r -> r.effect().equals(effect.get())).findFirst();
            if (existing.isPresent()) {
                if (existing.get().amplifier() >= amplifier) {
                    continue;
                }
                results.remove(existing.get());
            }
            results.add(new Resolved(effect.get(), amplifier, definition));
        }
        return results;
    }

    public static int amplifier(Config.GivenEffect definition, int level) {
        var bonus = (int) Math.floor(definition.amplifier_per_level * (level - definition.min_level));
        var amplifier = definition.amplifier + Math.max(bonus, 0);
        if (definition.max_amplifier != null) {
            amplifier = Math.min(amplifier, definition.max_amplifier);
        }
        return Math.max(amplifier, 0);
    }

    public static void give(LivingEntity entity, List<Resolved> effects, int durationTicks) {
        for (var resolved : effects) {
            var definition = resolved.definition();
            entity.addStatusEffect(new StatusEffectInstance(resolved.effect(), durationTicks, resolved.amplifier(),
                    definition.ambient, definition.show_particles, definition.show_icon));
        }
    }

    /// Called upon periodic presence checks, with the difficulty found at the player's location
    public static void giveToPlayer(ServerPlayerEntity player, Difficulty difficulty, int checkIntervalTicks) {
        var config = DungeonDifficulty.config.value.player_effects;
        if (config == null || !config.enabled
                || !difficulty.isValid() || !difficulty.givesPlayerEffects()
                || difficulty.type().player_effects == null || difficulty.type().player_effects.isEmpty()) {
            return;
        }
        if (config.skip_creative && player.isCreative()) {
            return;
        }
        var effects = resolve(difficulty.type().player_effects, difficulty.level());
        var duration = checkIntervalTicks + config.duration_margin_seconds * 20;
        give(player, effects, duration);
    }

    private static Optional<RegistryEntry<StatusEffect>> lookup(String id) {
        return cache.computeIfAbsent(id, key -> {
            var identifier = Identifier.tryParse(key);
            Optional<RegistryEntry<StatusEffect>> entry = identifier != null
                    ? Registries.STATUS_EFFECT.getEntry(identifier).map(reference -> reference)
                    : Optional.empty();
            if (entry.isEmpty()) {
                System.err.println("Dungeon Difficulty: unknown status effect `" + key + "` in config");
            }
            return entry;
        });
    }
}
