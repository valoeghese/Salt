package valoeghese.salt.ui.menu;

import valoeghese.salt.Connection;
import valoeghese.salt.LinearAlgebra;
import valoeghese.salt.Node;
import valoeghese.salt.Salt;
import valoeghese.salt.component.CurrentSource;
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

			System.out.println(system);
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
		final Node groundNode = Salt.getCircuit().properties().getGroundNode();

		for (Node node : Salt.getCircuit().nodes().values()) {
			// skip the ground node
			if (node.equals(groundNode)) {
				continue;
			}

			nodeIds.put(node.getName(), id++);
		}

		// create bimap of id to node

		return Salt.getCircuit().nodes().entrySet()
				.parallelStream()
				.filter(e -> !e.getValue().equals(groundNode))
				.collect(HashBiMap.collectToHashBiMap(e -> nodeIds.get(e.getKey()), Map.Entry::getValue));
	}

	private SystemOfEquations computeSystemOfEquations(BiMap<Integer, Node> nodes) {
		List<Connection> connections = Salt.getCircuit().connections();
		int nodeCount = nodes.size();

		double[][] coefficientMatrix = new double[nodeCount][nodeCount];
		double[] constantVector = new double[nodeCount];

		// we have list at home
		// list at home: BiMap

		// Also please recall, the ground node does NOT have an associated ID.
		// Treat its voltage as the constant 0 if you come across it
		final Node ground = Salt.getCircuit().properties().getGroundNode();

		for (int nodeId = 0; nodeId < nodes.size(); nodeId++) {
			Node node = nodes.getValue(nodeId);

			for (Connection connection : connections) {
				// ensure this connection connects to the node being analysed
				if (!connection.getNodeA().equals(node) && !connection.getNodeB().equals(node)) {
					continue;
				}

				double G = connection.getConductance();

				// Applying KCL, we know sum of currents into the branch = 0
				// Take A -> B as specified by Connection
				// any where our target node is actually A are 'backwards'.
				boolean backwards = node.equals(connection.getNodeA());

				// Sum of GiV(from) - V(node) * sum of Gi = sum of currents from current source branches
				// + sum of GiVi

				// if a current source branch, just use that
				// otherwise use the conductance and voltages
				CurrentSource currentSource = connection.getCurrentSource();

				if (currentSource == null) {
					if (backwards) {
						// node A is node
						coefficientMatrix[nodeId][nodeId] -= G;

						// add coefficient for node B, accounting for the case where it may be the ground node
						// (if it is the ground node, its voltage is zero, and thus we skip this stage, cause G*V = 0)
						Node nodeB = connection.getNodeB();

						if (!nodeB.equals(ground)) {
							coefficientMatrix[nodeId][nodes.getKey(nodeB)] += G;
						}
					} else {
						// node B is node
						coefficientMatrix[nodeId][nodeId] -= G;

						// add coefficient for node A, accounting for the case where it may be the ground node
						// (if it is the ground node, its voltage is zero, and thus we skip this stage, cause G*V = 0)
						Node nodeA = connection.getNodeA();

						if (!nodeA.equals(ground)) {
							coefficientMatrix[nodeId][nodes.getKey(nodeA)] += G;
						}
					}

					constantVector[nodeId] -= G * connection.getVoltageSourceVoltage();
				}
				else {
					constantVector[nodeId] -= backwards ? -currentSource.getCurrent() : currentSource.getCurrent();
				}
			}
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

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();

			for (int i = 0; i < this.coefficients.length; i++) {
				for (int j = 0; j < this.coefficients[i].length; j++) {
					builder.append(String.format("%.5g", this.coefficients[i][j])).append(" ");
				}

				builder.append("| ").append(String.format("%.5g", this.constants[i])).append("\n");
			}

			return builder.toString();
		}
	}
}
