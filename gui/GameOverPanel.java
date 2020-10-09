// A simple panel that shows game results.

package gui;
import java.awt.*;
import java.awt.event.KeyEvent;
import javax.swing.*;


public class GameOverPanel extends MyContentPanel {


	public GameOverPanel(GUI g, int w, int h) {
		super(g, w, h);
	}

	// =======================================

	public void keyPressed(KeyEvent e) {
		// do nothing
	}


	public void keyReleased(KeyEvent e) {
		// do nothing
	}

	// =======================================

	public void paintComponent(Graphics g) {
		g.setColor(Color.BLACK);
		g.setFont(new Font("title", Font.PLAIN, 36));
		if (gui.getCallbackManager().didPlayerWin()) {
			g.drawString("You are victorious!", 40, 90);
		}
		else {
			g.drawString("You have been defeated", 40, 90);
		}
	}

}