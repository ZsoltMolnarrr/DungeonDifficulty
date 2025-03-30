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

	private static String transformToLangKey(@Nullable String prefix, String identifier) {
		if (prefix == null) {
			return identifier.replace(":", ".");
		}

		return prefix + "." + identifier.replace(":", ".");
	}
}