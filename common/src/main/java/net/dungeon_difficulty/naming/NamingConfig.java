package net.dungeon_difficulty.naming;

import java.util.List;

/// Stored at `config/dungeon_difficulty/structure_naming.json`.
/// Structures matched by a pool get generated names from it.
public class NamingConfig { public NamingConfig() { }
    public boolean enabled = true;

    /// The first pool matching a structure is used. Structures matching no pool are not named.
    public List<NamePool> pools = List.of(villagePool());

    private static NamePool villagePool() {
        var pool = new NamePool();
        pool.structure = "#minecraft:village";
        return pool;
    }

    /// Names are a random prefix + suffix. A prefix ending with a space starts a new word (the suffix is capitalized),
    /// for example: "Black" + "ridge" = "Blackridge", "Middle " + "gate" = "Middle Gate".
    /// After such prefixes only `word_suffixes` are used, so fragments never stand alone ("Big Us").
    public static class NamePool { public NamePool() { }
        /// Structures using this pool, universal pattern matching (id, `#tag`, `~regex`, `!` negation)
        public String structure;
        public List<String> prefixes = List.of(
                "Black", "Pine", "Wood", "Hill", "White", "Blood", "Dragon", "Grey", "Port", "Middle ", "Tund", "Vulc",
                "Sanso", "Atlant", "Bay", "Wilda", "Fountain", "Vert", "Winter", "Night", "Sand", "Lake", "Olymp",
                "Brinda", "Bur", "Xa", "Draga", "Ala", "Kalin", "Basen", "Alg", "Sorpi", "New ", "Erli", "Av", "Big ",
                "Hamp", "Chand", "South ", "Weed ", "Roc ", "Aval", "Anti", "Brown", "Wedding", "Whitting", "West",
                "Yorks", "Sher", "Rosco", "Elk's ", "Cath", "Viper's ", "Pig's ", "Blacks", "Lanker", "Lom", "Timber",
                "Fen", "Forder ", "Mera", "Quick ", "Wil", "Sheltem", "Corack", "Abad", "Ach", "Ag", "Alexan", "Alf",
                "Ana", "Arc", "Ar", "Armi", "Ash", "Athen", "Auri", "Back", "Bath", "Battle", "Blind", "Bor", "Bretto",
                "Brim", "Brown's ", "Clax", "Cand", "Castel", "Casti", "Chill", "Cinder", "Cloud", "Cold", "Cool",
                "Corak", "Corner", "Cor", "Crag", "Daemon ", "Daren", "Dark ", "Dark", "Dead", "Death's ", "Deep",
                "Dol", "Down", "Drago ", "Draken", "Dun", "Edge", "Element", "Elf", "Elles", "Emerald ", "Endur",
                "Enkin", "Equi", "Ever", "Fac", "Fallen ", "Fender", "Fire", "Fleogan ", "Forest ", "Fort ",
                "Fortune ", "Frois", "Gate", "Gehen", "Ghost", "Glade", "Grave ", "Green ", "Hals", "Hart", "Haunt's ",
                "Hell", "Hermit ", "High", "Hitch", "Jordan", "Jotunn", "Kan", "Ker", "Ket", "Kil", "Kru", "Lagu",
                "Laken", "Lant", "Lewin", "Lost ", "Lost", "Mach", "Magme", "Mari", "Mars", "Marsh", "Mass", "Megh",
                "Middle", "Morgan", "Moss", "Mount ", "Mud", "Myst", "Nith", "Nor", "Port ", "Prosp", "Rain", "Rid",
                "Rock", "Rot", "Roven", "Sal", "Sanct", "Seren", "Shadow ", "Shadow", "Silver", "Sleepy ", "Sol",
                "Sorrow ", "Still ", "Still", "Stone", "Strong", "Styg", "Styr", "Tart", "Termin", "Thul", "Tir",
                "Tor", "Trans", "Val", "Var", "Vluch", "Vol", "Walen", "Water", "Waz", "Westland ", "Whistle",
                "White ", "Wild ", "Wind", "Wise ", "Worm "
        );
        /// Suffixes that are words on their own, allowed after any prefix ("Emerald " + "moor" = "Emerald Moor")
        public List<String> word_suffixes = List.of(
                "ridge", "haven", "stone", "shield", "tooth", "wind", "gate", "watch", "head", "shadow", "moor",
                "dune", "fang", "dawn", "oak", "shire", "mill", "patch", "fork", "top", "ford", "nest", "eye", "oaks",
                "silver", "den", "water", "quarter", "root", "bay", "spire", "fire", "soul", "mire", "tor", "cloud",
                "eternal", "burrow", "hold", "fall", "wood", "breach", "wall", "mere", "night", "winter", "star",
                "brand", "mills", "glen", "keep", "raven", "falls", "grim", "cove", "castle", "grove", "hall", "dale",
                "copper", "land", "warren", "flash", "wing", "creek", "crown", "bog", "gale", "pier", "mouth", "moon",
                "willow"
        );
        /// Suffixes only joined to a prefix, never after one ending with a space ("Tund" + "ara" = "Tundara")
        public List<String> fragment_suffixes = List.of(
                "hurst", "reign", "smith", "ara", "ania", "bar", "ium", "igo", "kill", "caster", "side", "us", "lock",
                "bran", "mar", "dra", "ji", "ary", "gal", "quin", "one", "ler", "on", "och", "ston", "ton", "ham",
                "man", "mon", "cart", "burn", "bard", "mec", "low", "burg", "don", "eron", "ony", "dretta", "heim",
                "adia", "tage", "combe", "aeum", "chalcum", "iere", "ment", "ea", "nia", "ent", "latus", "gare", "ona",
                "ere", "nade", "ance", "dle", "nox", "ture", "en", "an", "way", "na", "gard", "gar", "pall", "dare",
                "ber", "ing", "ina", "tin", "shen", "hank", "choke", "ein", "ion", "os", "dolere", "enes", "al",
                "dore", "ero", "der", "unda", "er", "da", "um", "ity", "ius", "iam", "aglia", "aros", "ekh", "ith",
                "mina", "om", "tara", "and", "ta", "zar", "skalir"
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
