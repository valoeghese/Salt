package valoeghese.salt.component;

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

	@Override
	public String toString() {
		return "[" + this.voltage + "V DC Voltage Source]";
	}
}
