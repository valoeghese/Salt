package valoeghese.salt;

import valoeghese.salt.io.SerialisedName;

public class Properties {
	@SerialisedName("GroundNode")
	private Node groundNode;

	/**
	 * Get the ground node for this circuit.
	 * @return the ground node of this circuit.
	 */
	public Node getGroundNode() {
		return this.groundNode;
	}
}
