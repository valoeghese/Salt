package valoeghese.salt;

import valoeghese.salt.component.Component;
import valoeghese.salt.component.Resistor;
import valoeghese.salt.component.VoltageSource;
import valoeghese.salt.io.Database;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

public class Salt extends JFrame {
	public static void main(String[] args) {
		Database.registerParser(Component.class, raw -> {
			switch (raw.charAt(0)) {
			case 'R':
				return new Resistor(Double.parseDouble(raw.substring(1)));
			case 'V':
				return new VoltageSource(Double.parseDouble(raw.substring(1)));
			default:
				throw new IllegalArgumentException("Unknown component type " + raw.charAt(0));
			}
		});

		List<Connection> connections;

		try (BufferedReader reader = new BufferedReader(new FileReader("run/voltage_divider.crc"))) {
			Database database = Database.read(reader);
			connections = database.readTable("Connections", Connection.class);
		} catch (IOException e) {
			throw new UncheckedIOException("Error reading circuit file.", e);
		}

		for (Connection connection : connections) {
			connection.Components.forEach(System.out::println);
		}

		Salt salt = new Salt();

		salt.setMinimumSize(new Dimension(300, 300));
		salt.setTitle("Salt");
		salt.setDefaultCloseOperation(EXIT_ON_CLOSE);
		salt.setVisible(true);
	}
}
