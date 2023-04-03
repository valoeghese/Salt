package valoeghese.salt.ui;

import java.awt.*;

/**
 * A canvas for drawing. This provides a wrapper around the Graphics2D class that allows for negative width and height.
 */
public class Canvas {
	public Canvas(ElectronicsCanvas canvas, Graphics2D parent) {
		this.trueCanvas = canvas;
		this.parent = parent;
	}

	private final ElectronicsCanvas trueCanvas;
	private final Graphics2D parent;

	public void drawEllipse(int x, int y, int width, int height) {
		int screenThickness = this.getScreenThickness();
		this.parent.setStroke(new BasicStroke(screenThickness));

		int topY = height < 0 ? y + height : y;
		height = height < 0 ? -height : height;

		if (width < 0) {
			this.parent.drawOval(
					x + width,
					topY,
					-width,
					height
			);
		} else {
			this.parent.drawOval(
					x,
					topY,
					width,
					height
			);
		}
	}

	public void drawRect(int x, int y, int width, int height) {
		int screenThickness = this.getScreenThickness();
		this.parent.setStroke(new BasicStroke(screenThickness));

		int topY = height < 0 ? y + height : y;
		height = height < 0 ? -height : height;

		if (width < 0) {
			this.parent.drawRect(
					x + width,
					topY,
					-width,
					height
			);
		} else {
			this.parent.drawRect(
					x,
					topY,
					width,
					height
			);
		}
	}

	public void fillRect(int x, int y, int width, int height) {
		int topY = height < 0 ? y + height : y;
		height = height < 0 ? -height : height;

		if (width < 0) {
			this.parent.fillRect(
					x + width,
					topY,
					-width,
					height
			);
		} else {
			this.parent.fillRect(
					x,
					topY,
					width,
					height
			);
		}
	}

	public void drawLine(int x, int y, int dx, int dy) {
		int screenThickness = this.getScreenThickness();
		this.parent.setStroke(new BasicStroke(screenThickness));

		// handle cases that dx and dy are negative
		if (dx < 0) {
			x += dx;
			dx = -dx;
		}

		if (dy < 0) {
			y += dy;
			dy = -dy;
		}

		// draw the line
		this.parent.drawLine(x, y, x + dx, y + dy);
	}

	private int getScreenThickness() {
		return (int) Math.ceil(ElectronicsCanvas.WIRE_THICKNESS/this.trueCanvas.getScale());
	}
}
