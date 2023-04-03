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
				// vertical bar down to top horizontal bar
				this.drawVerticalWire(g, position.x(), position.y(), 0.5);

				// horizontal bars
				this.drawHorizontalWire(g, position.x() - 0.5, position.y() + 0.5, 1.0);
				this.drawHorizontalWire(g, position.x() - 0.333, position.y() + 0.5 + 0.25, 0.667);
				this.drawHorizontalWire(g, position.x() - 0.167, position.y() + 0.5 + 0.25 * 2, 0.333);
			}
		}

		// Wires and Components
		for (Connection connection : Salt.getCircuit().connections()) {
			IntPosition start = connection.getNodeA().getPosition();
			IntPosition end = connection.getNodeB().getPosition();

			int componentCount = connection.getComponents().size();

			@Nullable IntPosition isct = start.intersect(end, connection.isFlipped());
			double wireLength = start.distanceManhattan(end);
			double componentSize = wireLength / (1 + componentCount);

			if (isct == null) {
				double totalSpacing = wireLength - componentCount * componentSize;
				double spacerSize = totalSpacing / (1 + componentCount); // space after each component and at beginning.

				Position lineStart = new Position(start);
				Position lineEnd = new Position(end);

				// place first spacer
				this.drawWire(g, lineStart, lineStart.move(lineEnd, spacerSize));

				// iterate over each component, placing it followed by a spacer
				for (int i = 0; i < componentCount; i++) {
					Component component = connection.getComponents().get(i);

					IntPosition componentStart = sketchToScreen(
							lineStart.move(lineEnd, spacerSize + i * (componentSize + spacerSize))
					);

					Position componentEnd = lineStart.move(lineEnd, (componentSize + spacerSize) * (i + 1));
					IntPosition componentEndI = this.sketchToScreen(componentEnd);

					Position spacerEnd = lineStart.move(lineEnd, spacerSize + (i + 1) * (componentSize + spacerSize));

					component.draw(canvas, componentStart, componentEndI, this.scale);
					this.drawWire(g, componentEnd, spacerEnd);
				}
			} else {
				this.drawWire(g, start, isct);
				this.drawWire(g, isct, end);
			}
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
