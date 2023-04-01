package valoeghese.salt.component;

/**
 * Represents a DC current source.
 */
public class CurrentSource implements Component {
	/**
	 * Construct a DC voltage source with the given current in its branch.
	 * @param current the current of this current source.
	 */
	public CurrentSource(double current) {
		this.current = current;
	}

	private final double current;

	public double getCurrent() {
		return this.current;
	}

	@Override
	public String toString() {
		return "[" + this.current + "A DC Current Source]";
	}
}
