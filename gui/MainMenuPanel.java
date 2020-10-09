// First GUI the player sees...from here they can load games, pick maps (eventually)

package gui;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;


public class MainMenuPanel extends MyContentPanel implements ActionListener {

	private JButton newGameButton;
	private JButton loadGameButton;


	public MainMenuPanel(GUI g, int w, int h) {
		super(g, w, h);
		setLayout(new FlowLayout(FlowLayout.CENTER));
		add(new GUIRectangle(w, 64));
		JLabel title = new JLabel("Monarch");
		title.setFont(new Font("foo", Font.BOLD, 32));
		add(title);
		add(new GUIRectangle(w, 64));
		newGameButton = new JButton("New Game");
		newGameButton.addActionListener(this);
		add(newGameButton);
		add(new GUIRectangle(w, 64));
		loadGameButton = new JButton("Load Game");
		loadGameButton.addActionListener(this);
		add(loadGameButton);
	}

	// ==============================

	public void keyPressed(KeyEvent e) {
		// do nothing
	}

	public void keyReleased(KeyEvent e) {
		// do nothing
	}

	// ================================

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == newGameButton) {
			gui.getCallbackManager().newGame();
		}
		else if (e.getSource() == loadGameButton) {
			gui.getCallbackManager().loadGame();
		}
	}


}