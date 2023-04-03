package valoeghese.salt.component;

import valoeghese.salt.IntPosition;
import valoeghese.salt.io.Database;
import valoeghese.salt.ui.Canvas;

/**
 * Represents an electric component.
 */
public interface Component {
	/**
	 * Draws the base circle for many components.
	 * @param canvas the canvas upon which to draw this circle.
	 * @param from the position to draw this circle from.
	 * @param to the position to draw this circle to.
	 */
	default void drawBaseCircle(Canvas canvas, IntPosition from, IntPosition to) {
		if (to.x() == from.x()) {
			int size = to.y() - from.y();
			canvas.drawEllipse(from.x() - size/2, from.y(), size, size);
		} else {
			int size = to.x() - from.x();
			canvas.drawEllipse(from.x(), from.y() - size/2, size, size);
		}
	}

	/**
	 * Draw this component onto the canvas at the specified position and orientation.
	 * @param canvas the canvas upon which to draw.
	 * @param from the position on the wire from which to draw this component, in screen coordinates.
	 * @param to the position on the wire to which to draw this component, in screen coordinates.
	 * @param scale the scale at which this component is being drawn. 1 x or y on the screen is equal to this distance in the actual sketch.
	 */
	void draw(Canvas canvas, IntPosition from, IntPosition to, double scale);

	static void registerParser() {
		Database.registerParser(Component.class, raw -> switch (raw.charAt(0)) {
				case 'R' -> new Resistor(Double.parseDouble(raw.substring(1)));
				case 'V' -> new VoltageSource(Double.parseDouble(raw.substring(1)));
				case 'I' -> new CurrentSource(Double.parseDouble(raw.substring(1)));
				default -> throw new IllegalArgumentException("Unknown component type " + raw.charAt(0));
		});
	}
}
