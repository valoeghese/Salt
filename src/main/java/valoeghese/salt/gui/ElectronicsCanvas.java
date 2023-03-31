package valoeghese.salt.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

public class ElectronicsCanvas extends JPanel {
	public ElectronicsCanvas() {
		MouseAdapter adapter = new MouseAdapter() {
			private int lastX;
			private int lastY;

			@Override
			public void mouseClicked(MouseEvent e) {
				lastX = e.getX();
				lastY = e.getY();
			}

			@Override
			public void mouseDragged(MouseEvent e) {
				int dx = e.getX() - lastX;
				int dy = e.getY() - lastY;
				lastX = e.getX();
				lastY = e.getY();

				ElectronicsCanvas.this.xOffset -= dx * ElectronicsCanvas.this.scale;
				ElectronicsCanvas.this.yOffset -= dy * ElectronicsCanvas.this.scale;
				ElectronicsCanvas.this.repaint();
			}

			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				double scaleDiff = 0.05 * e.getPreciseWheelRotation();
				double newScale = ElectronicsCanvas.this.scale + scaleDiff;

				if (newScale < 0.5) {
					scaleDiff = 0.5 - ElectronicsCanvas.this.scale;
				} else if (newScale > 1.5) {
					scaleDiff = 1.5 - ElectronicsCanvas.this.scale;
				}

				if (scaleDiff != 0) {
					ElectronicsCanvas.this.scale += scaleDiff;
					ElectronicsCanvas.this.xOffset -= e.getX() * scaleDiff;
					ElectronicsCanvas.this.yOffset -= e.getY() * scaleDiff;

					ElectronicsCanvas.this.repaint();
				}
			}
		};

		this.addMouseListener(adapter);
		this.addMouseWheelListener(adapter);
		this.addMouseMotionListener(adapter);
	}
	private double xOffset = 0.0;
	private double yOffset = 0.0;
	private double scale = 1.0;

	@Override
	public void paintComponent(Graphics g) {
		// softer palette than white and black
		g.setColor(new Color(220, 220, 220));
		g.fillRect(this.getX(), this.getY(), this.getWidth(), this.getHeight());

		// Dots
		g.setColor(new Color(30, 30, 30));

		for (int x = this.getX(); x < this.getWidth(); x++) {
			double drawX = x * scale + this.xOffset;

			for (int y = this.getY(); y < this.getHeight(); y++) {
				double drawY = y * scale + this.yOffset;

				if (((int)drawX & 0b1110) == 0 && ((int)drawY & 0b1110) == 0) {
					g.fillRect(x,y,1,1);
				}
			}
		}
	}
}
