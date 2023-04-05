package valoeghese.salt;

/**
 * Represents a position in the circuit.
 */
public record Position(double x, double y) {
	public Position(IntPosition position) {
		this(position.x(), position.y());
	}

	public Position add(double x, double y) {
		return new Position(this.x + x, this.y + y);
	}

	/**
	 * Linearly interpolates (lerps) between two positions.
	 * @param other the other position to lerp to.
	 * @param progress the progress to lerp by. 0.0 is this position, 1.0 is the other position.
	 * @return the lerped position.
	 */
	public Position lerp(Position other, double progress) {
		double lerpedX = this.x + (other.x - this.x) * progress;
		double lerpedY = this.y + (other.y - this.y) * progress;

		return new Position(lerpedX, lerpedY);
	}

	/**
	 * Moves towards the other position (from this position) by the given distance.
	 * @param towards the position to move towards.
	 * @param distance the distance to move by.
	 * @return the moved position.
	 */
	public Position move(Position towards, double distance) {
		double dx = towards.x - this.x;
		double dy = towards.y - this.y;

		// Normalise dx and dy
		double length = Math.sqrt(dx * dx + dy * dy);
		dx /= length;
		dy /= length;

		double resultX = this.x + dx * distance;
		double resultY = this.y + dy * distance;

		return new Position(resultX, resultY);
	}

	@Override
	public String toString() {
		return String.format("(%.6f, %.6f)", this.x, this.y);
	}
}
