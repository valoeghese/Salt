package valoeghese.salt.ui;

import valoeghese.salt.Connection;
import valoeghese.salt.Node;
import valoeghese.salt.Position;
import valoeghese.salt.Salt;

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

	private void fillRect(Graphics graphics, double x, double y, double width, double height) {
		graphics.fillRect(
				(int) ((x - this.xOffset)/scale),
				(int) ((y - this.yOffset)/scale),
				(int) Math.ceil(width/scale),
				(int) Math.ceil(height/scale));
	}

	private void drawHorizontalWire(Graphics graphics, double x, double y, double length) {
		this.fillRect(
				graphics,
				length < 0 ? x - length : x,
				y - WIRE_OFFSET,
				length,
				WIRE_WIDTH);
	}

	private void drawVerticalWire(Graphics graphics, double x, double y, double length) {
		this.fillRect(
				graphics,
				x - WIRE_OFFSET,
				length < 0 ? y - length : y,
				WIRE_WIDTH,
				length);
	}

	@Override
	public void paintComponent(Graphics g) {
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
			Position position = node.getPosition();

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

		}
	}

	private static final double MIN_SCALE = 0.02;
	private static final double HOME_SCALE = 0.04;
	private static final double MAX_SCALE = 0.08;

	private static final double WIRE_WIDTH = 0.0625;
	private static final double WIRE_OFFSET = WIRE_WIDTH / 2;

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
				ElectronicsCanvas.this.zoom(0.05 * e.getPreciseWheelRotation(), e.getX(), e.getY());
			} else if (e.isShiftDown()) {
				ElectronicsCanvas.this.scroll(e.getPreciseWheelRotation() * ElectronicsCanvas.this.scale, 0);
			} else {
				ElectronicsCanvas.this.scroll(0, e.getPreciseWheelRotation() * ElectronicsCanvas.this.scale);
			}
		}
	}
}
