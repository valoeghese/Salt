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

	}

	@Override
	public String toString() {
		return "[" + this.voltage + "V DC Voltage Source]";
	}
}
