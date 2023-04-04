package valoeghese.salt;

import valoeghese.salt.io.SerialisedName;

public class Node {
	@SerialisedName("Node")
	String name;

	@SerialisedName("Position")
	IntPosition position;

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
