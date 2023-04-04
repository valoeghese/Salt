package valoeghese.salt.component;

import valoeghese.salt.IntPosition;
import valoeghese.salt.ui.Canvas;

/**
 * Represents a resistor.
 */
public class Resistor extends Component {
	/**
	 * Constructs a resistor with the given resistance.
	 * @param resistance the resistance, in ohms.
	 */
	public Resistor(double resistance) {
		this.resistance = resistance;
	}

	private final double resistance;

	public double getResistance() {
		return this.resistance;
	}

	@Override
	public void draw(Canvas canvas, IntPosition from, IntPosition to, double scale) {
		final int resistorWidth = (int) (0.33 / scale);

		if (from.x() == to.x()) {
			canvas.drawRect(from.x() - resistorWidth/2, from.y(), resistorWidth, to.y() - from.y());
		} else {
			canvas.drawRect(from.x(), from.y() - resistorWidth/2, to.x() - from.x(), resistorWidth);
		}
	}

	@Override
	public String toString() {
		return "[" + this.resistance + "Ω Resistor]";
	}
}
