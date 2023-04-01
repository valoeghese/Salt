package valoeghese.salt;

import valoeghese.salt.component.Component;
import valoeghese.salt.component.CurrentSource;
import valoeghese.salt.component.Resistor;
import valoeghese.salt.component.VoltageSource;
import valoeghese.salt.ui.AnalysisMenu;
import valoeghese.salt.ui.EditMenu;
import valoeghese.salt.ui.ElectronicsCanvas;
import valoeghese.salt.io.Database;
import valoeghese.salt.ui.FileMenu;
import valoeghese.salt.ui.HelpMenu;
import valoeghese.salt.ui.ViewMenu;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

public class Salt {
	public static void main(String[] args) throws UnsupportedLookAndFeelException, ClassNotFoundException, InstantiationException, IllegalAccessException {
		Database.registerParser(Component.class, raw -> {
			switch (raw.charAt(0)) {
			case 'R':
				return new Resistor(Double.parseDouble(raw.substring(1)));
			case 'V':
				return new VoltageSource(Double.parseDouble(raw.substring(1)));
			case 'I':
				return new CurrentSource(Double.parseDouble(raw.substring(1)));
			default:
				throw new IllegalArgumentException("Unknown component type " + raw.charAt(0));
			}
		});

		List<Connection> connections;
		Properties properties;

		try (BufferedReader reader = new BufferedReader(new FileReader("run/voltage_divider.crc"))) {
			Database database = Database.read(reader);
			connections = database.readTable("Connections", Connection.class);
			properties = database.readTable("Properties", Properties.class, "Key", "Value");
		} catch (IOException e) {
			throw new UncheckedIOException("Error reading circuit file.", e);
		}

		System.out.println("Ground node: " + properties);

		for (Connection connection : connections) {
			connection.getComponents().forEach(System.out::println);
		}

		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

		JMenuBar bar = new JMenuBar();
		bar.add(new FileMenu());
		bar.add(new EditMenu());
		bar.add(new ViewMenu());
		bar.add(new AnalysisMenu());
		bar.add(new HelpMenu());

		JFrame frame = new JFrame();
		frame.setJMenuBar(bar);
		frame.add(new ElectronicsCanvas());

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

		frame.setMinimumSize(new Dimension(300, 300));
		frame.setBounds(screenSize.width / 4, screenSize.height / 4, screenSize.width / 2, screenSize.height / 2);
		frame.setTitle("Salt");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}
}
