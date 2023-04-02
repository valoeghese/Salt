package valoeghese.salt.component;

import valoeghese.salt.Direction;
import valoeghese.salt.Position;

import java.awt.*;

/**
 * Represents a DC current source.
 */
public class CurrentSource implements Component {
	/**
	 * Construct a DC voltage source with the given current in its branch.
	 * @param current the current of this current source.
	 */
	public CurrentSource(double current) {
		this.current = current;
	}

	private final double current;

	public double getCurrent() {
		return this.current;
	}

	@Override
	public void draw(Graphics graphics, Position from, Position to, double scale) {
		int size = (int) (2 / scale);
		graphics.drawOval(x, y, size, size);
	}

	@Override
	public String toString() {
		return "[" + this.current + "A DC Current Source]";
	}
}
