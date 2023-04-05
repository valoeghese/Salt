package valoeghese.salt.utils;

import java.util.Map;

public class Units {
	/**
	 * Is there a higher power? An ancient philosophical question. And quite elegantly answered here.
	 * Coefficients for powers above 10^0.
	 */
	private static final Map<String, Double> HIGHER_POWERS = Map.of(
			"k", 1_000.0,
			"M", 1_000_000.0,
			"G", 1_000_000_000.0,
			"T", 1_000_000_000_000.0
	);

	/**
	 * Coefficients for powers below 10^0.
	 */
	private static final Map<String, Double> LOWER_POWERS = Map.of(
			"m", 0.001,
			"μ", 0.000_001,
			"n", 0.000_000_001,
			"p", 0.000_000_000_001
	);

	private static final Map.Entry<String, Double> UNIT = Map.entry("", 1.0);

	public static String getUnitString(double value, String unit) {
		Map.Entry<String, Double> power = UNIT;

		if (value < 1) {
			for (Map.Entry<String, Double> _power : LOWER_POWERS.entrySet()) {
				power = _power;

				if (value >= _power.getValue()) {
					break; // use this power
				}
			}
		} else if (value >= 1000) {
			for (Map.Entry<String, Double> _power : HIGHER_POWERS.entrySet()) {
				power = _power;

				if (value < _power.getValue()) {
					break; // use this power
				}
			}
		}

		return String.format("%.5g %s%s", value / power.getValue(), power.getKey(), unit);
	}
}
