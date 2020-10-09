// Top part of game GUI, shows details of monarch and retinue and has some buttons.

package gui;
import core.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;


public class GameInfoPanel extends JPanel implements ActionListener {

	private static ImageIcon goldImage = new ImageIcon("images/gold.png");
	private static final ImageIcon idleSubjectImage = new ImageIcon("images/subject_idle.png");
	private static final ImageIcon soldierImage = new ImageIcon("images/subject_soldier.png");
	private static final ImageIcon workerImage = new ImageIcon("images/subject_worker.png");
	private static final ImageIcon farmerImage = new ImageIcon("images/subject_farmer.png");
	private static final ImageIcon captainImage = new ImageIcon("images/subject_captain.png");
	private GUI gui;
	private JButton saveButton;
	private JButton pauseButton;


	public GameInfoPanel(GUI g, int w, int h) {
		super();
		gui = g;
		setPreferredSize(new Dimension(w, h));
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

		add(new GUIRectangle(w, h));
		add(new GUIRectangle(1, h, Color.BLACK));
		saveButton = new JButton("Save");
		saveButton.addActionListener(this);
		add(saveButton);
		pauseButton = new JButton("Pause");
		pauseButton.addActionListener(this);
		add(pauseButton);
	}

	// ================================================

	public void paintComponent(Graphics g) {
		g.setColor(Color.BLACK);
		g.setFont(new Font("foo", Font.PLAIN, 14));
		int textY = 17;

		GameInstance instance = gui.getCallbackManager().getCurrentGame();
		g.drawString("Day " + instance.getDayCount(), 8, textY);

		g.drawLine(58, 4, 58, getHeight() - 8);

		int x = 70;
		// show gold amount:
		g.drawImage(goldImage.getImage(), x, -2, 36, 32, this);
		x += 33;
		g.drawString("x " + instance.getMonarchGold() + " / " + instance.getMonarchMaxGold(), x, textY);
		x += 76;

		// Show retinue:
		g.drawString("Retinue (" + instance.getRetinueCount() + " / " + instance.getRetinueMax() + "):", x, textY);
		x += 100;
		for (SubjectType s: SubjectType.values()) {
			for (int i = 0; i < instance.getRetinueCount(s); i++) {
				g.drawImage(getImageForSubjectType(s).getImage(), x, textY - 12, 28, 21, this);
				x += 22;
			}
		}
		x += 40;

		// move speed bonus:
		double moveBonus = instance.getMonarchMoveBonus();
		if (moveBonus > 0.0) {
			g.drawString("Movement bonus: " + (int)(moveBonus * 100) + "%", x, textY);
			x += 100;
		}

		// Danger level:
		g.drawString("Danger level: " + (int)instance.getDangerLevel(), getWidth() - 290, textY);
	}

	// ================================================

	private ImageIcon getImageForSubjectType(SubjectType s) {
		switch(s) {
			case IDLE: return idleSubjectImage;
			case SOLDIER: return soldierImage;
			case WORKER: return workerImage;
			case FARMER: return farmerImage;
			case CAPTAIN: return captainImage;
			default: return null;
		}
	}

	// ================================================

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == saveButton) {
			gui.getCallbackManager().saveGame();
		}
		if (e.getSource() == pauseButton) {
			if (gui.getCallbackManager().isPaused()) {
				gui.getCallbackManager().resume();
				pauseButton.setText("Pause");
			}
			else {
				gui.getCallbackManager().pause();
				pauseButton.setText("Resume");
			}
		}
		
		gui.resetFocus();
	}


}