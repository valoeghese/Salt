package valoeghese.salt;

import valoeghese.salt.component.Component;
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
	private String nodeA;

	/**
	 * The second node of this connection.
	 */
	@SerialisedName("NodeB")
	private String nodeB;

	/**
	 * A list of components in the circuit, in order from NodeA to NodeB
	 */
	@SerialisedName("Components")
	private List<Component> components;

	public Collection<Component> getComponents() {
		return this.components;
	}

	public void validate() throws IllegalStateException {
		boolean hasCurrentSource = false;

		for (Component component : components) {
			//component.
		}
	}
}
