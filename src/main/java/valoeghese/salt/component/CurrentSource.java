package valoeghese.salt.component;

import valoeghese.salt.IntPosition;
import valoeghese.salt.ui.Canvas;

/**
 * Represents a DC current source.
 */
public class CurrentSource extends Component {
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
	public void draw(Canvas canvas, IntPosition from, IntPosition to, double scale) {
		this.drawBaseCircle(canvas, from, to);

		IntPosition centralFrom = from.lerp(to, 0.25);
		IntPosition centralTo = to.lerp(from, 0.25);

		// for the head
		IntPosition central = from.lerp(to, 0.5);

		// line for the body of the arrow
		canvas.drawLine(centralFrom, centralTo);

		// draw the head
		boolean horizontal = from.y() == to.y();
		int headRadius = Math.abs(horizontal ? centralFrom.x() - from.x() : centralFrom.y() - from.y()); // 1/4 the length

		// 'up' and 'down' here are only literally up and down in the "horizontal" case
		IntPosition centralUp;
		IntPosition centralDown;

		if (horizontal) {
			// base of arrow vertical as it's perpendicular
			centralUp = new IntPosition(central.x(), central.y() - headRadius);
			centralDown = new IntPosition(central.x(), central.y() + headRadius);
		} else {
			centralUp = new IntPosition(central.x() - headRadius, central.y());
			centralDown = new IntPosition(central.x() + headRadius, central.y());
		}

		canvas.drawLine(centralDown, centralUp);
		canvas.drawLine(centralDown, centralTo);
		canvas.drawLine(centralUp, centralTo);
	}

	@Override
	public String toString() {
		return "[" + this.current + "A DC Current Source]";
	}
}
