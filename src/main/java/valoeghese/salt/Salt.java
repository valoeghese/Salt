package valoeghese.salt;

import valoeghese.salt.component.Component;
import valoeghese.salt.io.Database;
import valoeghese.salt.ui.menu.AnalysisMenu;
import valoeghese.salt.ui.menu.EditMenu;
import valoeghese.salt.ui.ElectronicsCanvas;
import valoeghese.salt.ui.menu.FileMenu;
import valoeghese.salt.ui.menu.HelpMenu;
import valoeghese.salt.ui.menu.ViewMenu;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Map;

public class Salt {
	private static Circuit circuit;
	private static ElectronicsCanvas canvas;

	public static Circuit getCircuit() {
		return circuit;
	}

	public static ElectronicsCanvas getCanvas() {
		return canvas;
	}

	public static void main(String[] args) throws UnsupportedLookAndFeelException, ClassNotFoundException, InstantiationException, IllegalAccessException {
		Component.registerParser();
		IntPosition.registerParser();

		loadFile(args[0]);

		System.out.println("Ground node: " + getCircuit().properties().getGroundNode());

		for (Connection connection : getCircuit().connections()) {
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
		frame.add(canvas = new ElectronicsCanvas());

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

		frame.setMinimumSize(new Dimension(300, 300));
		frame.setBounds(screenSize.width / 4, screenSize.height / 4, screenSize.width / 2, screenSize.height / 2);
		frame.setTitle("Salt");
		frame.setIconImages(List.of(
				readImageAsset("icon_256.png"),
				readImageAsset("icon_128.png"),
				readImageAsset("icon_64.png"),
				readImageAsset("icon_32.png")
		));

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}

	private static BufferedImage readImageAsset(String path) {
		try (InputStream stream = Salt.class.getClassLoader().getResourceAsStream("assets/" + path)) {
			return ImageIO.read(stream);
		} catch (IOException e) {
			throw new UncheckedIOException("Error reading image asset " + path, e);
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("Error reading image asset " + path, e);
		}
	}

	private static void loadFile(String file) {
		Map<String, Node> nodes;
		List<Connection> connections;
		Properties properties;

		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			Database database = Database.read(reader);
			nodes = database.readTable("Nodes", Node.class, "Node", String.class);

			Database.registerParser(Node.class, nodes::get);

			connections = database.readTable("Connections", Connection.class);
			properties = database.readTable("Properties", Properties.class, "Key", "Value");
		} catch (IOException e) {
			throw new UncheckedIOException("Error reading circuit file.", e);
		}

		circuit = new Circuit(nodes, connections, properties);
	}
}
