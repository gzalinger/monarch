// Top level GUI

package gui;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;


public class GUI extends JFrame implements KeyListener {

	private GUICallbackManager callbackManager;
	private MyContentPanel currentPanel;
    private static final int guiWidth = 1040;
    private static final int guiHeight = 680;


	public GUI(GUICallbackManager cbm) {
		super("Monarch");
		callbackManager = cbm;
		JPanel temp = new JPanel(new FlowLayout());
		currentPanel = new MainMenuPanel(this, guiWidth, guiHeight);
		temp.add(currentPanel);
		setContentPane(temp);
		setVisible(true);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		pack();
	}

	// ====================================

	public void onNewGameStart() {
		setNewContentPane(new GamePanel(this, guiWidth, guiHeight));
	}

	// ====================================

	private void setNewContentPane(MyContentPanel newPanel) {
		newPanel.addKeyListener(this);
		getContentPane().removeAll();
		getContentPane().add(newPanel);
		currentPanel = newPanel;
		validate();
		currentPanel.requestFocusInWindow();
	}

	// ====================================

	public void onGameOver() {
		setNewContentPane(new GameOverPanel(this, guiWidth, guiHeight));
	}

	// ====================================

	public GUICallbackManager getCallbackManager() {
		return callbackManager;
	}

	// ====================================

	public void resetFocus() {
		currentPanel.requestFocusInWindow();
	}

	// ====================================

	public void keyPressed(KeyEvent e) {
		if (!callbackManager.isPaused()) {
			currentPanel.keyPressed(e);
		}
	}

	public void keyReleased(KeyEvent e) {
		currentPanel.keyReleased(e);
	}

	public void keyTyped(KeyEvent e) {
		// do nothing
	}


}