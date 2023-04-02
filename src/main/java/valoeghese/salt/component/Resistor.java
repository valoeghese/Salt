package valoeghese.salt.component;

import valoeghese.salt.Direction;

import java.awt.*;

/**
 * Represents a resistor.
 */
public class Resistor implements Component {
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
	public void draw(Graphics graphics, int x, int y, double scale, Direction direction) {

	}

	@Override
	public String toString() {
		return "[" + this.resistance + "Ω Resistor]";
	}
}
