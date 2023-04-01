package valoeghese.salt.component;

import valoeghese.salt.io.Database;

/**
 * Represents an electric component.
 */
public interface Component {
	static void registerParser() {
		Database.registerParser(Component.class, raw -> switch (raw.charAt(0)) {
				case 'R' -> new Resistor(Double.parseDouble(raw.substring(1)));
				case 'V' -> new VoltageSource(Double.parseDouble(raw.substring(1)));
				case 'I' -> new CurrentSource(Double.parseDouble(raw.substring(1)));
				default -> throw new IllegalArgumentException("Unknown component type " + raw.charAt(0));
		});
	}
}
