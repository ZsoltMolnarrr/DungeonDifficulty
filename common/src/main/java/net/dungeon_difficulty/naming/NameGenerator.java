package net.dungeon_difficulty.naming;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.function.Predicate;
import java.util.random.RandomGenerator;

/// Builds names from segments: prefix + suffix, falling back to prefix + middle(s) + suffix
/// once two segment names are used up. Plain Java, no game dependencies.
public class NameGenerator {
    private static final int RANDOM_ATTEMPTS = 10;
    private static final int FALLBACK_ATTEMPTS = 50;
    private static final String DEFAULT_NAME = "Settlement";

    /// Generates a name not yet in use:
    /// 1. random two segment names
    /// 2. any unused two segment name (full scan)
    /// 3. random three or four segment names
    /// 4. numbered name (only for tiny, misconfigured pools)
    public static String generate(NamingConfig.NamePool pool, RandomGenerator random, Predicate<String> isUsed) {
        var prefixes = clean(pool.prefixes);
        var suffixes = clean(pool.suffixes);
        if (prefixes.isEmpty() || suffixes.isEmpty()) {
            return numbered(DEFAULT_NAME, isUsed);
        }

        String name = null;
        for (int i = 0; i < RANDOM_ATTEMPTS; i++) {
            name = join(pick(prefixes, random), List.of(), pick(suffixes, random));
            if (!isUsed.test(name)) {
                return name;
            }
        }

        // Few hundred microseconds, and only runs when random picks keep hitting used names
        var unused = new ArrayList<String>();
        var seen = new LinkedHashSet<String>();
        for (var prefix : prefixes) {
            for (var suffix : suffixes) {
                var candidate = join(prefix, List.of(), suffix);
                if (seen.add(candidate) && !isUsed.test(candidate)) {
                    unused.add(candidate);
                }
            }
        }
        if (!unused.isEmpty()) {
            return pick(unused, random);
        }

        var middles = clean(pool.middles);
        if (!middles.isEmpty()) {
            for (int i = 0; i < FALLBACK_ATTEMPTS; i++) {
                var count = random.nextFloat() < pool.four_segment_chance ? 2 : 1;
                var candidate = joinWithMiddles(pick(prefixes, random), middles, count, pick(suffixes, random), random);
                if (candidate.length() <= pool.max_length && !isUsed.test(candidate)) {
                    return candidate;
                }
            }
        }

        return numbered(name, isUsed);
    }

    private static String joinWithMiddles(String prefix, List<String> middles, int count, String suffix, RandomGenerator random) {
        // Middles after a word ending prefix ("New ") start the next word, without constraint
        var chosen = new ArrayList<String>();
        var previous = endsWithSpace(prefix) ? "" : prefix;
        for (int i = 0; i < count; i++) {
            var middle = pickMiddle(middles, previous, random);
            chosen.add(middle);
            previous = middle;
        }
        return join(prefix, chosen, suffix);
    }

    /// Alternates vowels and consonants at the joins: "Black" + "en", "Wilda" + "mar"
    private static String pickMiddle(List<String> middles, String previous, RandomGenerator random) {
        if (previous.isEmpty()) {
            return pick(middles, random);
        }
        var wantVowel = !isVowel(previous.charAt(previous.length() - 1));
        var matching = new ArrayList<String>();
        for (var middle : middles) {
            if (isVowel(middle.charAt(0)) == wantVowel) {
                matching.add(middle);
            }
        }
        return pick(matching.isEmpty() ? middles : matching, random);
    }

    /// "Black" + "ridge" = "Blackridge", "Middle " + "gate" = "Middle Gate", "Hill" + "low" = "Hillow"
    static String join(String prefix, List<String> middles, String suffix) {
        var word = new StringBuilder();
        for (var middle : middles) {
            word.append(middle);
        }
        word.append(suffix);
        var rest = word.toString().toLowerCase();
        if (endsWithSpace(prefix)) {
            rest = Character.toUpperCase(rest.charAt(0)) + rest.substring(1);
        }
        return collapseTripleLetters(prefix + rest);
    }

    private static String collapseTripleLetters(String name) {
        var result = new StringBuilder();
        for (int i = 0; i < name.length(); i++) {
            var c = name.charAt(i);
            var length = result.length();
            if (length >= 2
                    && Character.toLowerCase(result.charAt(length - 1)) == Character.toLowerCase(c)
                    && Character.toLowerCase(result.charAt(length - 2)) == Character.toLowerCase(c)) {
                continue;
            }
            result.append(c);
        }
        return result.toString();
    }

    private static String numbered(String base, Predicate<String> isUsed) {
        var name = base;
        for (int n = 2; isUsed.test(name); n++) {
            name = base + " " + n;
        }
        return name;
    }

    private static boolean endsWithSpace(String text) {
        return !text.isEmpty() && Character.isWhitespace(text.charAt(text.length() - 1));
    }

    private static boolean isVowel(char c) {
        return "aeiouy".indexOf(Character.toLowerCase(c)) >= 0;
    }

    /// Drops null and blank entries, trims suffixes and middles (prefixes keep their trailing space)
    private static List<String> clean(List<String> list) {
        var result = new ArrayList<String>();
        if (list == null) {
            return result;
        }
        for (var entry : list) {
            if (entry != null && !entry.isBlank()) {
                result.add(entry.stripLeading());
            }
        }
        return result;
    }

    private static <T> T pick(List<T> list, RandomGenerator random) {
        return list.get(random.nextInt(list.size()));
    }
}
