package valoeghese.salt.ui.menu;

import valoeghese.salt.Salt;

import javax.swing.*;

public class ViewMenu extends JMenu {
	public ViewMenu() {
		super("View");

		JMenuItem enhance = new JMenuItem("Zoom In");
		enhance.setAccelerator(KeyStroke.getKeyStroke('.'));
		enhance.addActionListener(e -> Salt.getCanvas().enhance());

		JMenuItem zoomOut = new JMenuItem("Zoom Out");
		zoomOut.setAccelerator(KeyStroke.getKeyStroke(','));
		zoomOut.addActionListener(e -> Salt.getCanvas().zoomOut());

		this.add(enhance);
		this.add(zoomOut);
	}
}
