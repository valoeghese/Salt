package valoeghese.salt.ui;

import java.awt.*;

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
		this.parent.drawOval(x, y, width, height);
	}

	public void drawRect(int x, int y, int width, int height) {
		int screenThickness = this.getScreenThickness();
		this.parent.setStroke(new BasicStroke(screenThickness));
		this.parent.drawRect(x, y, width, height);

//		// top and bottom bars
//		this.fillRect(x, y, width, screenThickness);
//		this.fillRect(x, y + height, width, -screenThickness);
//
//		// left and right bars
//		this.fillRect(x, y, screenThickness, height);
//		this.fillRect(x + width, y, -screenThickness, height);
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

	private int getScreenThickness() {
		return (int) Math.ceil(ElectronicsCanvas.WIRE_THICKNESS/this.trueCanvas.getScale());
	}
}
