package valoeghese.salt.ui.menu;

import valoeghese.salt.Connection;
import valoeghese.salt.LinearAlgebra;
import valoeghese.salt.Node;
import valoeghese.salt.Salt;
import valoeghese.salt.utils.BiMap;
import valoeghese.salt.utils.HashBiMap;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AnalysisMenu extends JMenu {
	public AnalysisMenu() {
		super("Analysis");

		JMenuItem analyseNodes = new JMenuItem("Node Voltages");
		analyseNodes.setAccelerator(KeyStroke.getKeyStroke('N', KeyEvent.ALT_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK));

		analyseNodes.addActionListener(e -> {
			System.out.println("Simplifying Circuit... ");
			System.out.println("> Equivalent Nodes (NOT IMPLEMENTED)");
			System.out.println("> Non-Important Nodes (NOT IMPLEMENTED)");

			System.out.println("Analysing Node Voltages");
			System.out.println("> Computing Coefficient Matrix and Vector of Constants");

			BiMap<Integer, Node> nodes = AnalysisMenu.this.createNodeIdMap();
			SystemOfEquations system = AnalysisMenu.this.computeSystemOfEquations(nodes);

			System.out.println("> Solving Matrix");

			double[] solution = system.solve();

			// print solution array
			for (int i = 0; i < solution.length; i++) {
				System.out.println("Node " + nodes.getValue(i) + ": " + solution[i]);
			}
		});

		this.add(analyseNodes);
	}

	private BiMap<Integer, Node> createNodeIdMap() {
		// create temporary ids for each node
		Map<String, Integer> nodeIds = new HashMap<>();
		int id = 0;

		for (Node node : Salt.getCircuit().nodes().values()) {
			nodeIds.put(node.getName(), id++);
		}

		// create bimap of id to node
		BiMap<Integer, Node> nodes = Salt.getCircuit().nodes().entrySet()
				.parallelStream()
				.collect(HashBiMap.collectToHashBiMap(e -> nodeIds.get(e.getKey()), Map.Entry::getValue));

		return nodes;
	}

	private SystemOfEquations computeSystemOfEquations(BiMap<Integer, Node> nodes) {
		List<Connection> connections = Salt.getCircuit().connections();
		int nodeCount = nodes.size();

		double[][] coefficientMatrix = new double[connections.size()][nodeCount];
		double[] constantVector = new double[connections.size()];

		for (int i = 0; i < connections.size(); i++) {
			Connection connection = connections.get(i);
			double G = connection.getConductance();

			// GVb - GVa || GV
			coefficientMatrix[i][nodes.getKey(connection.getNodeA())] = -G;
			coefficientMatrix[i][nodes.getKey(connection.getNodeB())] = G;
			constantVector[i] = G * connection.getVoltageSourceVoltage();
		}

		return new SystemOfEquations(coefficientMatrix, constantVector);
	}

	private record SystemOfEquations(double[][] coefficients, double[] constants) {
		public SystemOfEquations {
			if (coefficients.length != constants.length) {
				throw new IllegalArgumentException("Coefficient matrix and constant vector must be the same length");
			}
		}

		public double[] solve() {
			return LinearAlgebra.solveSystem(this.coefficients, this.constants);
		}
	}
}
