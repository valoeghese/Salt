package valoeghese.salt.component;

import valoeghese.salt.IntPosition;
import valoeghese.salt.ui.Canvas;

/**
 * Represents a DC voltage source.
 */
public class VoltageSource implements Component {
	/**
	 * Construct a DC voltage source with the given voltage across its terminals.
	 * @param voltage the voltage of this voltage source.
	 */
	public VoltageSource(double voltage) {
		this.voltage = voltage;
	}

	private final double voltage;

	public double getVoltage() {
		return this.voltage;
	}

	@Override
	public void draw(Canvas canvas, IntPosition from, IntPosition to, double scale) {
		// draw the circle of the voltage source symbol
		this.drawBaseCircle(canvas, from, to);

		// draw the positive and negative terminals
		final int length = (int) (0.2 / scale);

		// negative terminal: a small horizontal line near the 'from' position
		if (from.x() == to.x()) {
			// vertical case
			// if moving down, draw below the top (+1)
			final int direction = from.y() < to.y() ? 1 : -1;
			canvas.drawLine(from.x() - length/2, from.y() + length * direction, length, 0);
		} else {
			// if moving right, draw right of the left (+1)
			final int direction = from.x() < to.x() ? 1 : -1;
			canvas.drawLine(from.x() + length * direction, from.y() - length/2, 0, length);
		}

		// positive terminal: a plus sign at the opposite end
		if (from.x() == to.x()) {
			// vertical case
			// if moving down, draw above the bottom (-1)
			final int direction = from.y() < to.y() ? -1 : 1;
			canvas.drawLine(to.x() - length/2, to.y() + length * direction, length, 0);
			canvas.drawLine(to.x(), to.y() + length * direction - length/2, 0, length);
		} else {
			// if moving right, draw left of the right (-1)
			final int direction = from.x() < to.x() ? -1 : 1;
			canvas.drawLine(to.x() + length * direction, to.y() - length/2, 0, length);
			canvas.drawLine(to.x() + length * direction - length/2, to.y(), length, 0);
		}
	}

	@Override
	public String toString() {
		return "[" + this.voltage + "V DC Voltage Source]";
	}
}
