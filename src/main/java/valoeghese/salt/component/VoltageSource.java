package valoeghese.salt.component;

import valoeghese.salt.Direction;

import java.awt.*;

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
	public void draw(Graphics2D graphics2D, int x, int y, double scale, Direction direction) {

	}

	@Override
	public String toString() {
		return "[" + this.voltage + "V DC Voltage Source]";
	}
}
