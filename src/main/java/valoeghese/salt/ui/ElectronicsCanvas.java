package valoeghese.salt.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

public class ElectronicsCanvas extends JPanel {
	public ElectronicsCanvas() {
		MouseMotion mouseMotion = new MouseMotion();

		this.addMouseListener(mouseMotion);
		this.addMouseWheelListener(mouseMotion);
		this.addMouseMotionListener(mouseMotion);

		this.getActionMap().put("enhance", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ElectronicsCanvas.this.zoom(-0.25, mouseMotion.posX, mouseMotion.posY);
			}
		});

		this.getActionMap().put("zoomOut", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ElectronicsCanvas.this.zoom(0.25, mouseMotion.posX, mouseMotion.posY);
			}
		});

		this.getInputMap().put(KeyStroke.getKeyStroke('.'), "enhance");
		this.getInputMap().put(KeyStroke.getKeyStroke(','), "zoomOut");
	}

	private double xOffset = 0.0;
	private double yOffset = 0.0;
	private double scale = 1.0;

	private void zoom(double scaleDiff, int mouseX, int mouseY) {
		double newScale = this.scale + scaleDiff;

		if (newScale < 0.25) {
			scaleDiff = 0.25 - this.scale;
		} else if (newScale > 1.5) {
			scaleDiff = 1.5 - this.scale;
		}

		if (scaleDiff != 0) {
			this.scale += scaleDiff;
			this.xOffset -= mouseX * scaleDiff;
			this.yOffset -= mouseY * scaleDiff;

			this.repaint();
		}
	}

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

	class MouseMotion extends MouseAdapter {
		private int lastDragX;
		private int lastDragY;

		private int posX;
		private int posY;

		@Override
		public void mouseClicked(MouseEvent e) {
			lastDragX = e.getX();
			lastDragY = e.getY();
		}

		@Override
		public void mouseMoved(MouseEvent e) {
			posX = e.getX();
			posY = e.getY();
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			int dx = e.getX() - lastDragX;
			int dy = e.getY() - lastDragY;
			lastDragX = e.getX();
			lastDragY = e.getY();

			ElectronicsCanvas.this.xOffset -= dx * ElectronicsCanvas.this.scale;
			ElectronicsCanvas.this.yOffset -= dy * ElectronicsCanvas.this.scale;
			ElectronicsCanvas.this.repaint();
		}

		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			ElectronicsCanvas.this.zoom(0.05 * e.getPreciseWheelRotation(), e.getX(), e.getY());
		}
	}
}
