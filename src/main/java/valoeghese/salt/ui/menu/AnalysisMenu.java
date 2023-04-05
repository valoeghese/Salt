package valoeghese.salt.ui.menu;

import valoeghese.salt.Node;
import valoeghese.salt.Salt;
import valoeghese.salt.ui.menu.operation.NodeVoltageAnalysis;

import javax.swing.*;
import java.awt.event.KeyEvent;

public class AnalysisMenu extends JMenu {
	public AnalysisMenu() {
		super("Analysis");

		JMenuItem analyseNodes = new NodeVoltageAnalysis("Node Voltages");
		analyseNodes.setAccelerator(KeyStroke.getKeyStroke('N', KeyEvent.ALT_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK));

		JMenuItem clearAnalyses = new JMenuItem("Clear Analyses");
		clearAnalyses.setAccelerator(KeyStroke.getKeyStroke('C', KeyEvent.ALT_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK));

		analyseNodes.addActionListener(e -> {
			clearAnalyses.setEnabled(true);
			analyseNodes.setEnabled(false);
		});

		clearAnalyses.addActionListener(e -> {
			Salt.getCircuit().nodes().values().forEach(Node::clearDisplayVoltage);
			Salt.refresh();
			clearAnalyses.setEnabled(false);
			analyseNodes.setEnabled(true);
		});

		this.add(analyseNodes);
		this.add(clearAnalyses);
	}
}
