package valoeghese.salt.ui.menu;

import org.jetbrains.annotations.Nullable;
import valoeghese.salt.Connection;
import valoeghese.salt.LinearAlgebra;
import valoeghese.salt.Node;
import valoeghese.salt.Salt;
import valoeghese.salt.component.CurrentSource;
import valoeghese.salt.utils.BiMap;
import valoeghese.salt.utils.HashBiMap;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class AnalysisMenu extends JMenu {
	public AnalysisMenu() {
		super("Analysis");

		JMenuItem analyseNodes = new JMenuItem("Node Voltages");
		analyseNodes.setAccelerator(KeyStroke.getKeyStroke('N', KeyEvent.ALT_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK));

		analyseNodes.addActionListener(e -> {
			System.out.println("Simplifying Circuit... ");

			System.out.println("> Equivalent Nodes and Supernodes");

			// map that contains nodes that should be analysed as an equivalent to another node with only a voltage difference.
			// these will be handled like a supernode.
			Map<Node, SuperNode> superNodes = new HashMap<>();

			// iterate through connections
			for (Connection connection : Salt.getCircuit().connections()) {
				if (connection.getCurrentSource() == null && connection.getResistance() == 0) {
					// The nodes are equivalent with only a voltage difference

					// As there is no other components, this is the voltage of B as seen from A. That is, Vb - Va
					double voltageDiff = connection.getVoltageSourceVoltage();

					Node nodeA = connection.getNodeA();
					Node nodeB = connection.getNodeB();

					// check if there's an existing supernode(s) for these nodes
					@Nullable SuperNode superNodeA = superNodes.get(nodeA);
					@Nullable SuperNode superNodeB = superNodes.get(nodeB);

					if (superNodeA == null && superNodeB == null) {
						// create new supernode
						SuperNode superNode = new SuperNode(nodeA);
						superNode.addNode(nodeB, voltageDiff);

						// add into the map
						superNodes.put(nodeA, superNode);
						superNodes.put(nodeB, superNode);
					} else if (superNodeA == null) {
						// add nodeA to supernodeB
						double nodeBVoltageRelativeToHead = superNodeB.getVoltageRelativeToHead(nodeB);
						superNodeB.addNode(nodeA, nodeBVoltageRelativeToHead - voltageDiff);

						// add into the map
						superNodes.put(nodeA, superNodeB);
					} else if (superNodeB == null) {
						// add nodeB to supernodeA
						double nodeAVoltageRelativeToHead = superNodeA.getVoltageRelativeToHead(nodeA);
						superNodeA.addNode(nodeB, nodeAVoltageRelativeToHead + voltageDiff);
					} else {
						// merge supernodes
						double nodeAVoltageRelativeToHead = superNodeA.getVoltageRelativeToHead(nodeA);
						double nodeBVoltageRelativeToHead = superNodeB.getVoltageRelativeToHead(nodeB);

						double headDifference = nodeAVoltageRelativeToHead - nodeBVoltageRelativeToHead + voltageDiff;

						superNodeA.addAll(superNodeB, headDifference);

						// move nodes from superNodeB to superNodeA
						superNodeB.forEach((node, relVoltage) -> superNodes.put(node, superNodeA));
					}
				} else {
					// otherwise, put any independent nodes in a supernode

					Node nodeA = connection.getNodeA();
					Node nodeB = connection.getNodeB();

					@Nullable SuperNode superNodeA = superNodes.get(nodeA);
					@Nullable SuperNode superNodeB = superNodes.get(nodeB);

					if (superNodeA == null) {
						superNodeA = new SuperNode(nodeA);
						superNodes.put(nodeA, superNodeA);
					}

					if (superNodeB == null) {
						superNodeB = new SuperNode(nodeB);
						superNodes.put(nodeB, superNodeB);
					}
				}
			}

			System.out.println("> Non-Important Nodes (NOT IMPLEMENTED)");

			System.out.println("Analysing Node Voltages");
			System.out.println("> Computing Coefficient Matrix and Vector of Constants");

			BiMap<Integer, Node> nodes = AnalysisMenu.this.createNodeIdMap(superNodes.keySet());
			SystemOfEquations system = AnalysisMenu.this.computeSystemOfEquations(nodes, superNodes);

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

	private BiMap<Integer, Node> createNodeIdMap(Collection<Node> nodes) {
		// create temporary ids for each node
		Map<String, Integer> nodeIds = new HashMap<>();
		int id = 0;
		final Node groundNode = Salt.getCircuit().properties().getGroundNode();

		for (Node node : nodes) {
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

	private SystemOfEquations computeSystemOfEquations(BiMap<Integer, Node> nodes, Map<Node, SuperNode> superNodes) {
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
				// convert dependent nodes to their head nodes

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

	/**
	 * Represents a collection of nodes which are related all only by a voltage source(s) at most.
	 * One of these nodes is arbitrarily picked as the 'head' to be solved for; other voltages can then be determined
	 * later from that node.
	 */
	private static class SuperNode {
		public SuperNode(Node head) {
			this.head = head;
			this.dependents = new HashMap<>();
		}

		private final Node head;

		/**
		 * A map of other nodes against the difference between their voltage and the head voltage.
		 */
		private final Map<Node, Double> dependents;

		public Node getHead() {
			return this.head;
		}

		public boolean hasNode(Node node) {
			return this.head.equals(node) || this.dependents.containsKey(node);
		}

		public double getVoltageRelativeToHead(Node node) {
			if (this.head.equals(node)) {
				return 0.0;
			}

			return this.dependents.get(node);
		}

		/**
		 * Adds the given node to thhis supernode with the given voltage difference.
		 * @param node the node to add.
		 * @param voltageDifference the difference between the voltages of the node and the head. That is, the voltage of this node relative to the head.
		 */
		public void addNode(Node node, double voltageDifference) {
			if (this.head.equals(node)) {
				return;
			}

			this.dependents.put(node, voltageDifference);
		}

		/**
		 * Iterate over each node in this supernode, along with the voltage difference between that node and the head.
		 * @param callback the callback to run for each node.
		 */
		public void forEach(BiConsumer<Node, Double> callback) {
			callback.accept(this.head, 0.0);
			this.dependents.forEach(callback);
		}

		/**
		 * Adds all the nodes from another supernode into this supernode, with the heads having the given voltage
		 * difference.
		 * @param other the other supernode from which to add nodes.
		 * @param headDifference the difference in the voltage between the heads of the two supernodes.
		 */
		public void addAll(SuperNode other, double headDifference) {
			if (this == other) {
				return;
			}

			// add all the other nodes from the other supernode
			other.forEach((node, voltage) -> this.dependents.put(node, voltage + headDifference));

			// add the other supernode's head to this supernode
			this.dependents.put(other.head, headDifference);
		}
	}
}
