package valoeghese.salt;

import org.jetbrains.annotations.Nullable;
import valoeghese.salt.io.Database;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a position in the circuit.
 */
public record IntPosition(int x, int y) {
	/**
	 * Computes the manhattan distance between two positions.
	 * @param other the other position to compute the distance to.
	 * @return the manhattan distance between the two positions.
	 */
	public double distanceManhattan(IntPosition other) {
		return Math.abs(other.x - this.x) + Math.abs(other.y - this.y);
	}

	/**
	 * Get the 'intersection' position along cardinal axes of this position and the other.
	 * @param other the other position to intersect with.
	 * @param keepX whether x should be kept from this position. False means y should be kept from this position.'
	 *                 This allows you to select which intersection point to use of the two possible ones.
	 * @return the intersection position, which will contain x from one, and y from the other, where 'which is which' is determined by the keepX parameter.
	 * Will return {@code null} if the points both lie along a line in a cardinal direction.
	 */
	public @Nullable IntPosition intersect(IntPosition other, boolean keepX) {
		if (other.x == this.x || other.y == this.y) {
			return null;
		}

		if (keepX) {
			return new IntPosition(this.x, other.y);
		} else {
			return new IntPosition(other.x, this.y);
		}
	}

	@Override
	public String toString() {
		return "(" + x + ", " + y + ")";
	}

	private static final Pattern SERIALISED_PATTERN = Pattern.compile("^\\(\\s*(-?\\d+)\\s*,\\s*(-?\\d+)\\s*\\)");

	static void registerParser() {
		Database.registerParser(IntPosition.class, raw -> {
			Matcher matcher = SERIALISED_PATTERN.matcher(raw);

			if (matcher.find()) {
				return new IntPosition(
						Integer.parseInt(matcher.group(1)),
						Integer.parseInt(matcher.group(2))
				);
			} else {
				throw new IllegalArgumentException("Invalid position string: " + raw + ". Must match " + SERIALISED_PATTERN.pattern());
			}
		});
	}
}
