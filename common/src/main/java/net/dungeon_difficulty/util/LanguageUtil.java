package net.dungeon_difficulty.util;

import net.minecraft.util.Language;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Pattern;

public final class LanguageUtil
{
	private static final String FIRST_LETTER_REGEX = "\\b(.)(.*?)\\b";
	private static final Pattern FIRST_LETTER_PATTERN = Pattern.compile(FIRST_LETTER_REGEX);

	public static String translateId(@Nullable String prefix, String id) {
		String langKey = transformToLangKey(prefix, id);
		Language language = Language.getInstance();

		if (!language.hasTranslation(langKey)) {
			if (prefix == null) {
				langKey = id;
			} else if(id.contains(":")) {
				langKey = id.split(":")[1];
			}

			langKey = langKey.replace("_", " ").replace("/", " ");

			langKey = FIRST_LETTER_PATTERN
				.matcher(langKey)
				.replaceAll(matchResult -> matchResult.group(1).toUpperCase() + matchResult.group(2));
		}

		return langKey;
	}

	private static final int[] ROMAN_VALUES = { 1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1 };
	private static final String[] ROMAN_SYMBOLS = { "M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I" };

	/// Roman numeral of the number, or the number itself when out of the representable range
	public static String toRoman(int number) {
		if (number <= 0 || number >= 4000) {
			return String.valueOf(number);
		}
		var result = new StringBuilder();
		for (int i = 0; i < ROMAN_VALUES.length; i++) {
			while (number >= ROMAN_VALUES[i]) {
				number -= ROMAN_VALUES[i];
				result.append(ROMAN_SYMBOLS[i]);
			}
		}
		return result.toString();
	}

	private static String transformToLangKey(@Nullable String prefix, String identifier) {
		if (prefix == null) {
			return identifier.replace(":", ".");
		}

		return prefix + "." + identifier.replace(":", ".");
	}
}