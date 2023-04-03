package valoeghese.salt.ui.menu;

import javax.swing.*;
import java.awt.event.KeyEvent;

public class FileMenu extends JMenu {
	public FileMenu() {
		super("File");

		this.add(new NewMenu());
		this.add(new JMenuItem("Open"));
		this.addSeparator();

		JMenuItem save = new JMenuItem("Save");
		save.setAccelerator(KeyStroke.getKeyStroke('S', KeyEvent.CTRL_DOWN_MASK));
		save.addActionListener(e -> {
				System.out.println("Saving...");
		});

		this.add(save);

		JMenuItem saveAs = new JMenuItem("Save As");
		saveAs.setAccelerator(KeyStroke.getKeyStroke('S', KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK));
		this.add(saveAs);

		this.add(new JMenuItem("Save Copy"));
		this.addSeparator();
		this.add(new JMenuItem("Exit"));
	}

	static class NewMenu extends JMenu {
		public NewMenu() {
			super("New");

			this.add("Sketch...");
		}
	}
}
