package valoeghese.salt;

import valoeghese.salt.io.Database;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record Position(int x, int y) {
	private static final Pattern SERIALISED_PATTERN = Pattern.compile("^\\(\\s*(-?\\d+)\\s*,\\s*(-?\\d+)\\s*\\)");

	static void registerParser() {
		Database.registerParser(Position.class, raw -> {
			Matcher matcher = SERIALISED_PATTERN.matcher(raw);

			if (matcher.find()) {
				return new Position(
						Integer.parseInt(matcher.group(1)),
						Integer.parseInt(matcher.group(2))
				);
			} else {
				throw new IllegalArgumentException("Invalid position string: " + raw + ". Must match " + SERIALISED_PATTERN.pattern());
			}
		});
	}
}
