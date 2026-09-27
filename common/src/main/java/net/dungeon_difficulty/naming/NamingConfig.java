package net.dungeon_difficulty.naming;

import java.util.List;

/// Stored at `config/dungeon_difficulty/structure_naming.json`.
/// Structures of the `#dungeon_difficulty:named` structure tag get generated names.
public class NamingConfig { public NamingConfig() { }
    public boolean enabled = true;

    public NamePool names = new NamePool();
    /// Names are a random prefix + suffix. A prefix ending with a space starts a new word (the suffix is capitalized),
    /// for example: "Black" + "ridge" = "Blackridge", "Middle " + "gate" = "Middle Gate"
    public static class NamePool { public NamePool() { }
        public List<String> prefixes = List.of(
                "Black", "Pine", "Wood", "Hill", "White", "Blood", "Dragon", "Grey", "Port", "Middle ", "Tund", "Vulc",
                "Sanso", "Atlant", "Bay", "Wilda", "Fountain", "Vert", "Winter", "Night", "Sand", "Lake", "Olymp",
                "Brinda", "Bur", "Xa", "Draga", "Ala", "Kalin", "Basen", "Alg", "Sorpi", "New ", "Erli", "Av", "Big ",
                "Hamp", "Chand", "South ", "Weed ", "Roc ", "Aval", "Anti", "Brown", "Wedding", "Whitting", "West",
                "Yorks", "Sher", "Rosco", "Elk's ", "Cath", "Viper's ", "Pig's ", "Blacks", "Lanker", "Lom", "Timber",
                "Fen", "Forder ", "Mera", "Quick ", "Wil", "Sheltem", "Corack"
        );
        public List<String> suffixes = List.of(
                "ridge", "hurst", "haven", "stone", "shield", "reign", "tooth", "wind", "smith", "gate", "ara", "ania",
                "bar", "ium", "watch", "head", "igo", "kill", "shadow", "caster", "side", "us", "moor", "lock", "bran",
                "dune", "mar", "dra", "fang", "ji", "ary", "gal", "dawn", "quin", "one", "oak", "shire", "ler", "mill",
                "patch", "on", "och", "ston", "ton", "ham", "fork", "top", "ford", "man", "mon", "cart", "nest", "eye",
                "burn", "bard", "oaks", "silver", "mec", "low", "burg"
        );
        /// Fallback once two segment names are used up: prefix + middle(s) + suffix, for example
        /// "Black" + "en" + "ridge" = "Blackenridge". Vowels and consonants alternate at the joins.
        public List<String> middles = List.of(
                "a", "e", "i", "o", "al", "am", "an", "ar", "as", "ath", "el", "em", "en", "er", "es", "eth", "il",
                "im", "in", "ir", "is", "ith", "ol", "om", "on", "or", "os", "ul", "un", "ur", "ald", "ard", "eld",
                "end", "ern", "ing", "ock", "orn", "ow", "ba", "bel", "ber", "bri", "ca", "cor", "da", "dal", "dan",
                "dor", "dra", "fal", "fen", "gar", "gor", "gra", "hal", "har", "ka", "kel", "kor", "la", "len", "lin",
                "ma", "mar", "mer", "mon", "na", "nor", "ra", "rin", "ro", "sa", "sel", "sil", "ta", "tar", "thal",
                "thor", "tor", "va", "ven", "vor", "wyn", "zar"
        );
        public float four_segment_chance = 0.25F;
        // Longer fallback names are rerolled
        public int max_length = 16;
    }

    public Waystones waystones = new Waystones();
    public static class Waystones { public Waystones() { }
        // Village waystones take the name of their village (when the Waystones mod is present)
        public boolean enabled = true;
    }
}
