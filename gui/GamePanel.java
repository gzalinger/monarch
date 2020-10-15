// Top-level GUI for everything that occurs within a game

package gui;
import java.awt.*;
import java.awt.geom.Point2D.Double;
import java.awt.event.KeyEvent;
import javax.swing.*;


public class GamePanel extends MyContentPanel {

	private NodeControlPanel controlPanel;
	private MapPanel mapPanel;


	public GamePanel(GUI g, int w, int h) {
		super(g, w, h);
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		int topHeight = 32;
		int bottomHeight = 130;
		add(new GameInfoPanel(g, w, topHeight));
		add(new GUIRectangle(w, 1, Color.BLACK));
		mapPanel = new MapPanel(g, w, h - topHeight - bottomHeight - 2);
		add(mapPanel);
		add(new GUIRectangle(w, 1, Color.BLACK));
		JPanel bottom = new JPanel();
		bottom.setPreferredSize(new Dimension(w, bottomHeight));
		bottom.setLayout(new BoxLayout(bottom, BoxLayout.X_AXIS));
		controlPanel = new NodeControlPanel(g, w - 291, bottomHeight);
		bottom.add(controlPanel);
		bottom.add(new GUIRectangle(1, bottomHeight, Color.BLACK));
		bottom.add(new NodeDetailPanel(g, 290, bottomHeight));
		add(bottom);
	}

	// ==================================

	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_K) {
			mapPanel.toggleKingdomView();
			return;
		}

		Double direction = null;
		if (e.getKeyCode() == KeyEvent.VK_UP) {
			direction = new Double(0, -1);
		}
		if (e.getKeyCode() == KeyEvent.VK_DOWN) {
			direction = new Double(0, 1);
		}
		if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
			direction = new Double(1, 0);
		}
		if (e.getKeyCode() == KeyEvent.VK_LEFT) {
			direction = new Double(-1, 0);
		}
		
		if (direction != null) {
			gui.getCallbackManager().getCurrentGame().startMonarchMovementInDirection(direction);
		}
		else {
			controlPanel.keyPressed(e);
		}
	}

	// ==================================

	public void keyReleased(KeyEvent e) {
		Double direction = null;
		if (e.getKeyCode() == KeyEvent.VK_UP) {
			direction = new Double(0, -1);
		}
		if (e.getKeyCode() == KeyEvent.VK_DOWN) {
			direction = new Double(0, 1);
		}
		if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
			direction = new Double(1, 0);
		}
		if (e.getKeyCode() == KeyEvent.VK_LEFT) {
			direction = new Double(-1, 0);
		}
		
		if (direction != null) {
			gui.getCallbackManager().getCurrentGame().stopMonarchMovementInDirection(direction);
		}
	}
}