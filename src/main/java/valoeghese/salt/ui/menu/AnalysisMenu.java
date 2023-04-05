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
import java.util.AbstractMap;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

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
			Map<Node, SuperNode> superNodes = this.createSuperNodes();

			System.out.println("Super nodes:");
			superNodes.values().stream().distinct().forEach(sn -> System.out.println("-\t" + sn));
			System.out.println();

			System.out.println("> Non-Important Nodes (NOT IMPLEMENTED)");

			System.out.println("Analysing Node Voltages");
			System.out.println("> Computing Coefficient Matrix and Vector of Constants");

			Collection<Node> heads = superNodes.values().stream().distinct().map(SuperNode::getHead).collect(Collectors.toList());
			BiMap<Integer, Node> nodes = AnalysisMenu.this.createNodeIdMap(heads);
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

	/**
	 * Create supernodes for the circuit.
	 * @return a map from nodes to the supernodes they are part of.
	 */
	private Map<Node, SuperNode> createSuperNodes() {
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

					// add into the map
					superNodes.put(nodeB, superNodeA);
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

		// rearrange ground supernode
		SuperNode groundSupernode = superNodes.get(Salt.getCircuit().properties().getGroundNode());

		if (!groundSupernode.isGround()) {
			SuperNode trueGroundSupernode = groundSupernode.rearrange();

			groundSupernode.forEach((node, relVoltage) -> superNodes.put(node, trueGroundSupernode));
		}

		return superNodes;
	}

	private BiMap<Integer, Node> createNodeIdMap(Collection<Node> heads) {
		// create temporary ids for each node
		Map<Node, Integer> nodeIds = new HashMap<>();
		int id = 0;
		final Node groundNode = Salt.getCircuit().properties().getGroundNode();

		for (Node node : heads) {
			// skip the ground node
			if (node.equals(groundNode)) {
				continue;
			}

			nodeIds.put(node, id++);
		}

		// create bimap of id to node
		return new HashBiMap<>(nodeIds, true);
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
				// (sum of V(node) Gi = V(node) sum of Gi because Vnode always @ 1 specific node)

				// Sum of GiV(from) - sum of V(node) Gi = sum of currents from current source branches
				// + sum of GiVi
				// where V(from) is relative as V(fromrel) = V(from) - VheadFrom, we can also say V(from) = V(fromrel) + V(headFrom)
				// and thus constant gets -GiV(fromrel) and left gets GiV(headFrom)
				// where V(node) is relative as V(noderel) = V(node) - VheadNode, we can also say -V(node) = -V(noderel) + -V(headNode)
				// and thus constant gets GiV(noderel) and left gets -GiV(headNode)
				// thus the total constant on the RHS is Gi (V(noderel) - V(fromrel))

				// convert dependent nodes to their head nodes
				SuperNode superNodeA = superNodes.get(connection.getNodeA());
				Node nodeAHead = superNodeA.getHead();
				double nodeAVoltageRelativeToHead = superNodeA.getVoltageRelativeToHead(nodeAHead);

				SuperNode superNodeB = superNodes.get(connection.getNodeB());
				Node nodeBHead = superNodeB.getHead();
				double nodeBVoltageRelativeToHead = superNodeB.getVoltageRelativeToHead(nodeBHead);

				// ensure this connection connects to the node being analysed
				if (!nodeAHead.equals(node) && !nodeBHead.equals(node)) {
					continue;
				}

				double G = connection.getConductance();

				// Applying KCL, we know sum of currents into the branch = 0
				// Take A -> B as specified by Connection
				// any where our target node is actually A are 'backwards'.
				boolean backwards = node.equals(connection.getNodeA());

				// Actually implement the equation (see: earlier)

				// if a current source branch, just use that
				// otherwise use the conductance and voltages
				CurrentSource currentSource = connection.getCurrentSource();

				if (currentSource == null) {
					double noderel;
					double fromrel;

					if (backwards) {
						// node A is node, node B is from
						noderel = nodeAVoltageRelativeToHead;
						fromrel = nodeBVoltageRelativeToHead;

						coefficientMatrix[nodeId][nodeId] -= G;

						// add coefficient for node B, accounting for the case where it may be the ground node
						// (if it is the ground node, its voltage is zero, and thus we skip this stage, cause G*V = 0)

						if (!superNodeB.isGround()) {
							coefficientMatrix[nodeId][nodes.getKey(nodeBHead)] += G;
						}
					} else {
						// node B is node, node A is from
						noderel = nodeBVoltageRelativeToHead;
						fromrel = nodeAVoltageRelativeToHead;

						coefficientMatrix[nodeId][nodeId] -= G;

						// add coefficient for node A, accounting for the case where it may be the ground node
						// (if it is the ground node, its voltage is zero, and thus we skip this stage, cause G*V = 0)

						if (!superNodeA.isGround()) {
							coefficientMatrix[nodeId][nodes.getKey(nodeAHead)] += G;
						}
					}

					// add constant offset based on voltage source voltage
					constantVector[nodeId] -= G * connection.getVoltageSourceVoltage();

					// add coefficient for offset from head
					constantVector[nodeId] += G * (noderel - fromrel);
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
		 * If the ground node is a dependent, returns a new, duplicate supernode with everything relative to the ground node.
		 * Otherwise, returns itself.
		 */
		public SuperNode rearrange() {
			final Node ground = Salt.getCircuit().properties().getGroundNode();

			if (this.dependents.containsKey(ground)) {
				// we need to rearrange this supernode so that the ground node is the head
				SuperNode newSuperNode = new SuperNode(ground);
				double groundVoltage = this.dependents.get(ground);

				this.forEach((node, voltageDifference) -> {
					if (node.equals(ground)) {
						return;
					}

					newSuperNode.addNode(node, voltageDifference - groundVoltage);
				});

				return newSuperNode;
			}

			return this;
		}

		/**
		 * Get whether this supernode's head is the ground.
		 * @return whether this supernode has the ground node as the head.
		 */
		public boolean isGround() {
			final Node ground = Salt.getCircuit().properties().getGroundNode();
			return this.head.equals(ground);
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

		@Override
		public String toString() {
			return "SuperNode{" +
					"head=" + this.head +
					", dependents=" + this.dependents +
					'}';
		}
	}
}
