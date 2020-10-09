// Superclass for any panel that sits directly beneath the GUI JFrame

package gui;
import java.awt.*;
import java.awt.event.KeyEvent;
import javax.swing.*;
import javax.swing.border.*;


public abstract class MyContentPanel extends JPanel {

	protected GUI gui;


	public MyContentPanel(GUI g, int w, int h) {
		setPreferredSize(new Dimension(w, h));
		setBorder(new LineBorder(Color.BLACK, 2));
		gui = g;
	}

	// ==================================

	public abstract void keyPressed(KeyEvent e);


	public abstract void keyReleased(KeyEvent e);



}