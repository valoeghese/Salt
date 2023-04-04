package valoeghese.salt.ui;

import org.jetbrains.annotations.Nullable;
import valoeghese.salt.Connection;
import valoeghese.salt.Node;
import valoeghese.salt.IntPosition;
import valoeghese.salt.Position;
import valoeghese.salt.Salt;
import valoeghese.salt.component.Component;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.List;

public class ElectronicsCanvas extends JPanel {
	public ElectronicsCanvas() {
		this.mouseMotion = new MouseMotion();

		this.addMouseWheelListener(this.mouseMotion);
		this.addMouseMotionListener(this.mouseMotion);
	}

	private final MouseMotion mouseMotion;

	private double xOffset = 0.0;
	private double yOffset = 0.0;
	private double scale = HOME_SCALE;
	private boolean loaded;

	/**
	 * Get the scale of this canvas.
	 * @return the scale.
	 */
	public double getScale() {
		return this.scale;
	}

	@Override
	public void setBounds(int x, int y, int width, int height) {
		// start by initially positioning (0,0) in the centre.
		if (!loaded) {
			xOffset = scale * -width/2;
			yOffset = scale * -height/2;
		}
		loaded = true;

		super.setBounds(x, y, width, height);
	}

	public void enhance() {
		this.zoom(-0.02, this.mouseMotion.posX, this.mouseMotion.posY);
	}

	public void zoomOut() {
		this.zoom(0.02, this.mouseMotion.posX, this.mouseMotion.posY);
	}

	private void zoom(double scaleDiff, int mouseX, int mouseY) {
		double newScale = this.scale + scaleDiff;

		if (newScale < MIN_SCALE) {
			scaleDiff = MIN_SCALE - this.scale;
		} else if (newScale > MAX_SCALE) {
			scaleDiff = MAX_SCALE - this.scale;
		}

		if (scaleDiff != 0) {
			this.scale += scaleDiff;
			this.xOffset -= mouseX * scaleDiff;
			this.yOffset -= mouseY * scaleDiff;

			this.repaint();
		}
	}

	private void scroll(double dx, double dy) {
		this.xOffset += dx;
		this.yOffset += dy;
		this.repaint();
	}

	/**
	 * Fill a rectangle on the screen using sketch coordinates.
	 * @param graphics the graphics object to draw on
	 * @param x the x coordinate of the top left corner of the rectangle, in the sketch
	 * @param y the y coordinate of the top left corner of the rectangle, in the sketch
	 * @param width the width of the rectangle
	 * @param height the height of the rectangle
	 */
	private void fillRect(Graphics graphics, double x, double y, double width, double height) {
		graphics.fillRect(
				sketchXToScreen(x),
				sketchYToScreen(y),
				(int) Math.ceil(width/scale),
				(int) Math.ceil(height/scale));
	}

	/**
	 * Converts an x coordinate from the sketch to an x coordinate on the screen.
	 * @param x the x coordinate in the sketch
	 * @return the x coordinate on the screen
	 */
	private int sketchXToScreen(double x) {
		return (int) ((x - this.xOffset)/this.scale);
	}

	/**
	 * Converts a y coordinate from the sketch to a y coordinate on the screen.
	 * @param y the y coordinate in the sketch
	 * @return the y coordinate on the screen
	 */
	private int sketchYToScreen(double y) {
		return (int) ((y - this.yOffset)/this.scale);
	}

	/**
	 * Converts a position from the sketch to a position on the screen.
	 * @param position the position in the sketch
	 * @return the position on the screen
	 */
	private IntPosition sketchToScreen(Position position) {
		return new IntPosition(
				this.sketchXToScreen(position.x()),
				this.sketchYToScreen(position.y())
		);
	}

	/**
	 * Draws a horizontal wire on the screen.
	 * @param graphics the graphics object to draw on
	 * @param x the x coordinate of the wire in the sketch
	 * @param y the y coordinate of the wire in the sketch
	 * @param length the length of the wire
	 */
	private void drawHorizontalWire(Graphics graphics, double x, double y, double length) {
		this.fillRect(
				graphics,
				length < 0 ? x + length : x,
				y - WIRE_OFFSET,
				length < 0 ? -length : length,
				WIRE_THICKNESS);
	}

	/**
	 * Draws a vertical wire on the screen.
	 * @param graphics the graphics object to draw on
	 * @param x the x coordinate of the wire in the sketch
	 * @param y the y coordinate of the wire in the sketch
	 * @param length the length of the wire
	 */
	private void drawVerticalWire(Graphics graphics, double x, double y, double length) {
		this.fillRect(
				graphics,
				x - WIRE_OFFSET,
				length < 0 ? y + length : y,
				WIRE_THICKNESS,
				length < 0 ? -length : length);
	}

	/**
	 * Draws a wire on the screen.
	 * @param graphics the graphics object to draw on
	 * @param start the start position of the wire, in the sketch
	 * @param end the end position of the wire, in the sketch
	 * @throws IllegalArgumentException if the wire is not directly along a cardinal direction.
	 */
	private void drawWire(Graphics graphics, IntPosition start, IntPosition end) throws IllegalArgumentException {
		this.drawWire(graphics, new Position(start), new Position(end));
	}

	/**
	 * Draws a wire on the screen.
	 * @param graphics the graphics object to draw on
	 * @param start the start position of the wire, in the sketch
	 * @param end the end position of the wire, in the sketch
	 * @throws IllegalArgumentException if the wire is not directly along a cardinal direction.
	 */
	private void drawWire(Graphics graphics, Position start, Position end) throws IllegalArgumentException {
		if (start.x() == end.x()) {
			this.drawVerticalWire(graphics, start.x(), start.y(), end.y() - start.y());
		} else if (start.y() == end.y()) {
			this.drawHorizontalWire(graphics, start.x(), start.y(), end.x() - start.x());
		} else {
			throw new IllegalArgumentException("Cannot draw wire directly along a cardinal direction between " + start + " and " + end);
		}
	}

	@Override
	public void paintComponent(Graphics g) {
		Canvas canvas = new Canvas(this, (Graphics2D) g);

		// softer palette than white and black
		g.setColor(new Color(220, 220, 220));
		g.fillRect(this.getX(), this.getY(), this.getWidth(), this.getHeight());

		// Dots
		g.setColor(new Color(30, 30, 30));

		for (int x = this.getX(); x < this.getWidth(); x++) {
			double sketchX = x * scale + this.xOffset;

			for (int y = this.getY(); y < this.getHeight(); y++) {
				double sketchY = y * scale + this.yOffset;

				g.setColor(new Color(30, 30, 30));

				// add 0.5 to move the test from whether it's within (0,0,1/8,1/8) to being (-1/16,-1/16,1/16,1/16)
				boolean inUnitX = ((int) Math.floor(sketchX * 8 + 0.5) & 0b111) == 0;
				boolean inUnitY = ((int) Math.floor(sketchY * 8 + 0.5) & 0b111) == 0;

				if (inUnitX && inUnitY) {
					g.fillRect(x, y, 1, 1);
				}
			}
		}

		// Draw circuit onto the board

		// Node
		for (Node node : Salt.getCircuit().nodes().values()) {
			final double nodeSize = 0.125;
			IntPosition position = node.getPosition();

			this.fillRect(
					g,
					position.x() - nodeSize,
					position.y() - nodeSize,
					2.0 * nodeSize,
					2.0 * nodeSize);

			if (node == Salt.getCircuit().properties().getGroundNode()) {
				// compute occupied directions of ground node (using bits 0x4 for up, 0x2 for right, 0x1 for down)
				final int UP = 0x4;
				final int RIGHT = 0x2;
				final int DOWN = 0x1;
				int occupiedDirections = 0;

				for (Connection connection : Salt.getCircuit().connections()) {
					Node nodeA = connection.getNodeA();
					Node nodeB = connection.getNodeB();

					// check if connection is from the node
					boolean fromNode = nodeA == node;

					if (fromNode || nodeB == node) {
						// if fromNode, comes out horizontally first. Otherwise (where it will be into the node), comes out vertically first
						// 'flipped' indicates this should be reversed
						// Though if they are only vertical from one another the connection will only be vertical
						boolean horizontal = (fromNode ^ connection.isFlipped()) && !(nodeA.getPosition().x() == nodeB.getPosition().x());

						if (horizontal) {
							// find dx from this node.
							// first position of B relative to A
							int dx = nodeB.getPosition().x() - nodeA.getPosition().x();

							// if our node is actually node B, flip the direction
							if (!fromNode) {
								dx = -dx;
							}

							// if dx is positive, it's to the right
							if (dx > 0) {
								occupiedDirections |= RIGHT;
							}

							// otherwise it's to the left
							// we don't care about the case that it's to the left, because that's our default direction
						}
						else {
							// find dy from this node.
							// first position of B relative to A
							int dy = nodeB.getPosition().y() - nodeA.getPosition().y();

							// if our node is actually node B, flip the direction
							if (!fromNode) {
								dy = -dy;
							}

							// if dy is positive, it's down (remember positive y is down)
							if (dy > 0) {
								occupiedDirections |= DOWN;
							}
							// otherwise it's up
							else {
								occupiedDirections |= UP;
							}
						}
					}
				}

				// bars for ground node
				// pick a non-occupied node.
				// Prefer down, then right, then up, then left (default if all occupied)
				if ((occupiedDirections & DOWN) == 0) {
					// vertical bar down to top horizontal bar
					this.drawVerticalWire(g, position.x(), position.y(), 0.5);

					// three bars
					this.drawHorizontalWire(g, position.x() - 0.5, position.y() + 0.5, 1.0);
					this.drawHorizontalWire(g, position.x() - 0.333, position.y() + 0.5 + 0.25, 0.667);
					this.drawHorizontalWire(g, position.x() - 0.167, position.y() + 0.5 + 0.25 * 2, 0.333);
				}
				else if ((occupiedDirections & RIGHT) == 0) {
					// horizontal bar to right vertical bar
					this.drawHorizontalWire(g, position.x(), position.y(), 0.5);

					// three bars
					this.drawVerticalWire(g, position.x() + 0.5, position.y() - 0.5, 1.0);
					this.drawVerticalWire(g, position.x() + 0.5 + 0.25, position.y() - 0.333, 0.667);
					this.drawVerticalWire(g, position.x() + 0.5 + 0.25 * 2, position.y() - 0.167, 0.333);
				}
				else if ((occupiedDirections & UP) == 0) {
					// vertical bar up to bottom horizontal bar
					this.drawVerticalWire(g, position.x(), position.y(), -0.5);

					// three bars
					this.drawHorizontalWire(g, position.x() - 0.5, position.y() - 0.5, 1.0);
					this.drawHorizontalWire(g, position.x() - 0.333, position.y() - 0.5 - 0.25, 0.667);
					this.drawHorizontalWire(g, position.x() - 0.167, position.y() - 0.5 - 0.25 * 2, 0.333);
				}
				else {
					// horizontal bar to left vertical bar
					this.drawHorizontalWire(g, position.x(), position.y(), -0.5);

					// three bars
					this.drawVerticalWire(g, position.x() - 0.5, position.y() - 0.5, 1.0);
					this.drawVerticalWire(g, position.x() - 0.5 - 0.25, position.y() - 0.333, 0.667);
					this.drawVerticalWire(g, position.x() - 0.5 - 0.25 * 2, position.y() - 0.167, 0.333);
				}
			}
		}

		// Wires and Components
		for (Connection connection : Salt.getCircuit().connections()) {
			IntPosition start = connection.getNodeA().getPosition();
			IntPosition end = connection.getNodeB().getPosition();

			int componentCount = connection.getComponents().size();

			@Nullable IntPosition isct = start.intersect(end, connection.isFlipped());
			double wireLength = start.distanceManhattan(end);
			double componentSize = Math.min(wireLength / (1 + componentCount), 1.25);

			// draw components and wires
			if (isct == null) {
				this.drawComponents(canvas, start, end, connection.getComponents(), 0, componentCount, componentSize);
			} else {
				double firstStretchLength = start.distanceManhattan(isct);
				double secondStretchLength = end.distanceManhattan(isct);

				int componentsOnFirstStretch = (int) (componentCount * firstStretchLength / (firstStretchLength + secondStretchLength));
				int componentsOnSecondStretch = componentCount - componentsOnFirstStretch;

				this.drawComponents(canvas, start, isct, connection.getComponents(), 0, componentsOnFirstStretch, componentSize);
				this.drawComponents(canvas, isct, end, connection.getComponents(), componentsOnFirstStretch, componentsOnSecondStretch, componentSize);
			}
		}
	}

	private void drawComponents(Canvas canvas, IntPosition start, IntPosition end, List<Component> components, int from, int count, double componentSize) {
		double wireLength = start.distanceManhattan(end);
		double totalSpacing = wireLength - count * componentSize;
		double spacerSize = totalSpacing / (1 + count); // space after each component and at beginning.

		Position lineStart = new Position(start);
		Position lineEnd = new Position(end);

		// place first spacer
		this.drawWire(canvas.graphics(), lineStart, lineStart.move(lineEnd, spacerSize));

		// iterate over each component, placing it followed by a spacer
		for (int i = 0; i < count; i++) {
			Component component = components.get(i + from);

			IntPosition componentStart = sketchToScreen(
					lineStart.move(lineEnd, spacerSize + i * (componentSize + spacerSize))
			);

			Position componentEnd = lineStart.move(lineEnd, (componentSize + spacerSize) * (i + 1));
			IntPosition componentEndI = this.sketchToScreen(componentEnd);

			Position spacerEnd = lineStart.move(lineEnd, spacerSize + (i + 1) * (componentSize + spacerSize));

			component.draw(canvas, componentStart, componentEndI, this.scale);
			this.drawWire(canvas.graphics(), componentEnd, spacerEnd);
		}
	}

	private static final double MIN_SCALE = 0.02;
	private static final double HOME_SCALE = 0.04;
	private static final double MAX_SCALE = 0.08;

	static final double WIRE_THICKNESS = 0.0625;
	private static final double WIRE_OFFSET = WIRE_THICKNESS / 2;

	class MouseMotion extends MouseAdapter {
		private int lastDragX;
		private int lastDragY;

		private int posX;
		private int posY;

		@Override
		public void mouseMoved(MouseEvent e) {
			posX = e.getX();
			posY = e.getY();

			lastDragX = e.getX();
			lastDragY = e.getY();
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			int dx = e.getX() - lastDragX;
			int dy = e.getY() - lastDragY;
			lastDragX = e.getX();
			lastDragY = e.getY();

			ElectronicsCanvas.this.scroll(-dx * ElectronicsCanvas.this.scale, -dy * ElectronicsCanvas.this.scale);
		}

		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			if (e.isControlDown()) {
				ElectronicsCanvas.this.zoom(0.01 * e.getPreciseWheelRotation(), e.getX(), e.getY());
			} else if (e.isShiftDown()) {
				ElectronicsCanvas.this.scroll(6 * e.getPreciseWheelRotation() * ElectronicsCanvas.this.scale, 0);
			} else {
				ElectronicsCanvas.this.scroll(0, 6 * e.getPreciseWheelRotation() * ElectronicsCanvas.this.scale);
			}
		}
	}
}
