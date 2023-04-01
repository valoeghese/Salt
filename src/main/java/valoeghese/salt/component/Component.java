package valoeghese.salt.component;

import valoeghese.salt.Direction;
import valoeghese.salt.io.Database;

import java.awt.*;

/**
 * Represents an electric component.
 */
public interface Component {
	/**
	 * Draw this component onto the canvas at the specified position and orientation.
	 * @param graphics2D the graphics2D for drawing.
	 * @param x the x position of the centre of this component on the screen.
	 * @param y the y position of the centre of this component on the screen.
	 * @param scale the scale at which this component is being drawn. 1 x or y on the screen is equal to this distance in the actual sketch.
	 * @param direction the direction this component is to be drawn in.
	 */
	void draw(Graphics2D graphics2D, int x, int y, double scale, Direction direction);

	static void registerParser() {
		Database.registerParser(Component.class, raw -> switch (raw.charAt(0)) {
				case 'R' -> new Resistor(Double.parseDouble(raw.substring(1)));
				case 'V' -> new VoltageSource(Double.parseDouble(raw.substring(1)));
				case 'I' -> new CurrentSource(Double.parseDouble(raw.substring(1)));
				default -> throw new IllegalArgumentException("Unknown component type " + raw.charAt(0));
		});
	}
}
