package valoeghese.salt.ui.menu;

import valoeghese.salt.ui.menu.operation.NodeVoltageAnalysis;

import javax.swing.*;
import java.awt.event.KeyEvent;

public class AnalysisMenu extends JMenu {
	public AnalysisMenu() {
		super("Analysis");

		JMenuItem analyseNodes = new NodeVoltageAnalysis("Node Voltages");
		analyseNodes.setAccelerator(KeyStroke.getKeyStroke('N', KeyEvent.ALT_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK));

		this.add(analyseNodes);
	}
}
