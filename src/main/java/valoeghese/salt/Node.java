package valoeghese.salt;

import valoeghese.salt.io.SerialisedName;

import java.util.OptionalDouble;

public class Node {
	@SerialisedName("Node")
	String name;

	@SerialisedName("Position")
	IntPosition position;

	private transient OptionalDouble displayVoltage;

	/**
	 * Set the voltage to display on top of the node.
	 * @param voltage the voltage to display on top of the node.
	 */
	public void setDisplayVoltage(double voltage) {
		this.displayVoltage = OptionalDouble.of(voltage);
	}

	/**
	 * Clear the display voltage of this node.
	 */
	public void clearDisplayVoltage() {
		this.displayVoltage = OptionalDouble.empty();
	}

	/**
	 * Get the display voltage of this node.
	 * @return the display voltage of this node.
	 */
	public OptionalDouble getDisplayVoltage() {
		return this.displayVoltage;
	}

	/**
	 * Get the position of this node in the circuit, in sketch coordinates.
	 * @return the position of this node in the circuit.
	 */
	public IntPosition getPosition() {
		return this.position;
	}

	/**
	 * Get the name of this node.
	 * @return the name of this node.
	 */
	public String getName() {
		return this.name;
	}

	@Override
	public String toString() {
		return "[Node " + this.name + " @ " + position.toString() + "]";
	}
}
