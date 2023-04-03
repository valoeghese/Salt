package valoeghese.salt.component;

import valoeghese.salt.IntPosition;
import valoeghese.salt.ui.Canvas;

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
	public void draw(Canvas canvas, IntPosition from, IntPosition to, double scale) {
		int size = (int) (2 / scale);
		//graphics.drawOval(from.x(), from.y(), size, size);
	}

	@Override
	public String toString() {
		return "[" + this.current + "A DC Current Source]";
	}
}
