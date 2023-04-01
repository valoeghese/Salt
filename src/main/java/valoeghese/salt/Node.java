package valoeghese.salt;

import valoeghese.salt.io.SerialisedName;

public class Node {
	@SerialisedName("Node")
	String name;

	@SerialisedName("Position")
	Position position;

	public Position getPosition() {
		return this.position;
	}

	@Override
	public String toString() {
		return "[Node " + this.name + " @ " + position.toString() + "]";
	}
}
