package rht;

import java.awt.Dimension;

import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JScrollPane;

public class ScrollTest extends JScrollPane {

	public ScrollTest() {
		super();
		DefaultListModel model = new DefaultListModel();
		for (int n = 0; n < 10000; n++) {
			model.addElement(n);
		}
		JList table = new JList(model);
		this.getViewport().setView(table);
	}

	public static void main(String[] args) throws Exception {
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		ScrollTest panel = new ScrollTest();
		panel.setPreferredSize(new Dimension(640, 480));
		frame.add(panel);
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		frame.setVisible(true);
	}

}
