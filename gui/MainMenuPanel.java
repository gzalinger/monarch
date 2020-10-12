// First GUI the player sees...from here they can load games, pick maps (eventually)

package gui;
import core.DifficultyLevel;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;


public class MainMenuPanel extends MyContentPanel implements ActionListener {

	private JButton newGameButton;
	private JButton loadGameButton;
	private JComboBox enemyDiffComboBox;
	private JComboBox mapDiffComboBox;
	private JComboBox mapSizeComboBox;


	public MainMenuPanel(GUI g, int w, int h) {
		super(g, w, h);
		setLayout(new FlowLayout(FlowLayout.CENTER));
		add(new GUIRectangle(w, 64));
		JLabel title = new JLabel("Monarch");
		title.setFont(new Font("foo", Font.BOLD, 32));
		add(title);
		add(new GUIRectangle(w, 64));

		JPanel panel = new JPanel();
		panel.add(new JLabel("Enemy difficulty:"));
		enemyDiffComboBox = new JComboBox(DifficultyLevel.values());
		enemyDiffComboBox.setSelectedIndex(2);
		panel.add(enemyDiffComboBox);
		add(panel);

		add(new GUIRectangle(w, 12));
		panel = new JPanel();
		panel.add(new JLabel("Map difficulty:"));
		mapDiffComboBox = new JComboBox(new String[]{"Easy", "Moderate", "Hard"});
		mapDiffComboBox.setSelectedIndex(1);
		panel.add(mapDiffComboBox);
		add(panel);

		add(new GUIRectangle(w, 12));
		panel = new JPanel();
		panel.add(new JLabel("Map size:"));
		mapSizeComboBox = new JComboBox(new String[]{"Small", "Medium", "Large"});
		mapSizeComboBox.setSelectedIndex(1);
		panel.add(mapSizeComboBox);
		add(panel);

		add(new GUIRectangle(w, 32));
		newGameButton = new JButton("New Game");
		newGameButton.addActionListener(this);
		add(newGameButton);

		add(new GUIRectangle(w, 32));
		add(new GUIRectangle(w / 2, 1, Color.BLACK));
		add(new GUIRectangle(w, 32));

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
			gui.getCallbackManager().newGame((DifficultyLevel)enemyDiffComboBox.getSelectedItem(), mapDiffComboBox.getSelectedIndex(), mapSizeComboBox.getSelectedIndex());
		}
		else if (e.getSource() == loadGameButton) {
			gui.getCallbackManager().loadGame();
		}
	}


}