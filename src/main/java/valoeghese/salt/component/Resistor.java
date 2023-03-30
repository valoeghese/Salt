package valoeghese.salt.component;

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

	@Override
	public String toString() {
		return "[" + this.resistance + "Ω Resistor]";
	}
}
