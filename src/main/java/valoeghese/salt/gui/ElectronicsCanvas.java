package valoeghese.salt.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

public class ElectronicsCanvas extends JPanel {
	public ElectronicsCanvas() {
		MouseAdapter adapter = new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				System.out.println("eeeee");
				super.mouseClicked(e);
			}

			@Override
			public void mouseDragged(MouseEvent e) {
				System.out.println("sfgsf");
				super.mouseDragged(e);
			}

			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				ElectronicsCanvas.this.scale += 0.05 * e.getPreciseWheelRotation();

				if (ElectronicsCanvas.this.scale < 0.5) {
					ElectronicsCanvas.this.scale = 0.5;
				} else if (ElectronicsCanvas.this.scale > 2) {
					ElectronicsCanvas.this.scale = 2;
				}

				ElectronicsCanvas.this.repaint();
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
			double drawX = (x + this.xOffset) * scale;

			for (int y = this.getY(); y < this.getHeight(); y++) {
				double drawY = (y + this.yOffset) * scale;

				if (drawX % 20 < 1 && drawY % 20 < 1) {
					g.fillRect(x,y,1,1);
				}
			}
		}
	}
}
