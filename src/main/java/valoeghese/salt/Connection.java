package valoeghese.salt;

import valoeghese.salt.component.Component;
import valoeghese.salt.component.CurrentSource;
import valoeghese.salt.component.Resistor;
import valoeghese.salt.component.VoltageSource;
import valoeghese.salt.io.SerialisedName;

import java.util.Collection;
import java.util.List;

/**
 * Represents a connection between two nodes, containing a set of components.
 */
public class Connection {
	/**
	 * The first node of this connection.
	 */
	@SerialisedName("NodeA")
	private Node nodeA;

	/**
	 * The second node of this connection.
	 */
	@SerialisedName("NodeB")
	private Node nodeB;

	/**
	 * Whether this data should be drawn flipped. That is, with the vertical coming out of node A rather than the horizontal.
	 */
	@SerialisedName("Flipped")
	private boolean flipped;

	/**
	 * A list of components in the circuit, in order from NodeA to NodeB
	 */
	@SerialisedName("Components")
	private List<Component> components;

	public Collection<Component> getComponents() {
		return this.components;
	}

	public Node getNodeA() {
		return this.nodeA;
	}

	public Node getNodeB() {
		return this.nodeB;
	}

	public CurrentSource getCurrentSource() {
		for (Component component : this.components) {
			if (component instanceof CurrentSource cs) {
				return cs;
			}
		}

		return null;
	}

	/**
	 * Get the equivalent conductance of this connection.
	 * @return the conductance (G-parameter) of this connection. Equal to 1 / the equivalent resistance.
	 */
	public double getConductance() {
		return 1.0 / this.getResistance();
	}

	/**
	 * Get the equivalent resistance of this connection.
	 * @return the resistance R of this connection. Equal to the sum of resistances of the components.
	 */
	public double getResistance() {
		return this.components.stream()
				.filter(a -> a instanceof Resistor)
				.mapToDouble(a -> ((Resistor) a).getResistance())
				.sum();
	}

	/**
	 * Get the sum of voltage source voltages within this connection, from A to B.
	 * @return the sum of voltages across voltage sources within this connection, from node A to node B.
	 */
	public double getVoltageSourceVoltage() {
		return this.components.stream()
				.filter(a -> a instanceof VoltageSource)
				.mapToDouble(a -> ((VoltageSource) a).getVoltage())
				.sum();
	}

	public void validate() throws IllegalStateException {
		boolean hasCurrentSource = false;

		for (Component component : components) {
			if (component instanceof CurrentSource) {
				if (hasCurrentSource) {
					throw new IllegalStateException("Connection has multiple current sources. A connection may only have one current source!");
				}

				hasCurrentSource = true;
			}
		}
	}
}
