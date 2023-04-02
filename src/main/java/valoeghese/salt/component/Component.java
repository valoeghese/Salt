package valoeghese.salt.component;

import valoeghese.salt.Position;
import valoeghese.salt.io.Database;

import java.awt.*;

/**
 * Represents an electric component.
 */
public interface Component {
	/**
	 * Draw this component onto the canvas at the specified position and orientation.
	 * @param graphics the graphics2D for drawing.
	 * @param from the position on the wire from which to draw this component.
	 * @param to the position on the wire to which to draw this component.
	 * @param scale the scale at which this component is being drawn. 1 x or y on the screen is equal to this distance in the actual sketch.
	 */
	void draw(Graphics graphics, Position from, Position to, double scale);

	static void registerParser() {
		Database.registerParser(Component.class, raw -> switch (raw.charAt(0)) {
				case 'R' -> new Resistor(Double.parseDouble(raw.substring(1)));
				case 'V' -> new VoltageSource(Double.parseDouble(raw.substring(1)));
				case 'I' -> new CurrentSource(Double.parseDouble(raw.substring(1)));
				default -> throw new IllegalArgumentException("Unknown component type " + raw.charAt(0));
		});
	}
}
