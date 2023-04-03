package valoeghese.salt.ui.menu;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class HelpMenu extends JMenu {
	public HelpMenu() {
		super("Help");

		JMenuItem help = new JMenuItem("Get Help");
		help.setAccelerator(KeyStroke.getKeyStroke("F1"));
		help.addActionListener(e -> {
			try {
				Desktop.getDesktop().browse(new URI("https://www.youtube.com/watch?v=xvFZjo5PgG0"));
			} catch (URISyntaxException | IOException ex) {
				ex.printStackTrace();
			}
		});

		this.add(help);
	}
}
