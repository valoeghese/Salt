package valoeghese.salt.ui.menu;

import valoeghese.salt.Node;
import valoeghese.salt.Salt;
import valoeghese.salt.ui.menu.operation.NodeVoltageAnalysis;

import javax.swing.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

public class AnalysisMenu extends JMenu {
	public AnalysisMenu() {
		super("Analysis");

		// Analysis Actions
		JMenuItem analyseNodes = new NodeVoltageAnalysis("Node Voltages");
		analyseNodes.setAccelerator(KeyStroke.getKeyStroke('N', KeyEvent.ALT_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK));

		JMenuItem saveAnalysis = new JMenuItem("Save Analysis");
		saveAnalysis.setAccelerator(KeyStroke.getKeyStroke('S', InputEvent.ALT_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));

		JMenuItem clearAnalysis = new JMenuItem("Clear Analysis");
		clearAnalysis.setAccelerator(KeyStroke.getKeyStroke('C', KeyEvent.ALT_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK));

		// Extra Action Implementations
		analyseNodes.addActionListener(e -> {
			clearAnalysis.setEnabled(true);
			saveAnalysis.setEnabled(true);
			analyseNodes.setEnabled(false);
		});

		clearAnalysis.addActionListener(e -> {
			Salt.getCircuit().nodes().values().forEach(Node::clearDisplayVoltage);
			Salt.refresh();
			clearAnalysis.setEnabled(false);
			saveAnalysis.setEnabled(false);
			analyseNodes.setEnabled(true);
		});

		// Default activation level
		saveAnalysis.setEnabled(false);
		clearAnalysis.setEnabled(false);

		// Add to menu
		this.add(analyseNodes);

		this.addSeparator();

		this.add(saveAnalysis);
		this.add(clearAnalysis);
	}
}
