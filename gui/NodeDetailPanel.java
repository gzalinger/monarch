// Part of the bottom panel, shows details about the node the monarch is standing on

package gui;
import core.*;
import java.awt.*;
import javax.swing.*;


public class NodeDetailPanel extends JComponent {

	private static final Color COLOR_BROWN = new Color(165, 42, 42);
	private GUI gui;


	public NodeDetailPanel(GUI g, int w, int h) {
		gui = g;
		setPreferredSize(new Dimension(w, h));
	}

	// =====================================

	public void paintComponent(Graphics g) {
		GameInstance instance = gui.getCallbackManager().getCurrentGame();
		MapNode node = instance.getMonarchNodeLocation();
		if (node == null) {
			return;
		}
		Settlement settlement = instance.getSettlementAt(node);

		int x = 6; // offsets everything to the right

		g.setColor(Color.BLACK);
		g.setFont(new Font("foo", Font.PLAIN, 12));
		int textY = 22;
		if (instance.isNodeGoldVein(node)) {
			int gold = instance.getGoldVeinAmount(node);
			g.drawString("Gold vein: " + gold + " gold remains to be mined", x, textY);
			textY += 18;
		}
		if (settlement != null) {
			g.drawString(settlement + ", Level " + settlement.getLevel(), x, textY);
			g.drawString("Treasury: " + settlement.getGold() + " / " + settlement.getMaxGold(), x + 130, textY);
			textY += 18;

			if (settlement instanceof Farm) {
				g.drawString("Max farmers: " + ((Farm)settlement).getMaxFarmers(), x, textY);
				textY += 18;
			}
		}

		// Print population:
		int subjectX = x - 6;
		for (SubjectType subjectType: SubjectType.values()) {
			int n = instance.getPopulationAtNode(node, subjectType);
			if (n > 0) {
				g.drawImage(NodeControlPanel.getImageForSubjectType(subjectType).getImage(), subjectX, textY - 10, 22, 16, this);
				g.drawString("x " + n, subjectX + 17, textY);
				subjectX += 40;
			}
		}
		if (instance.getPopCountAtNode(node) > 0) {
			textY += 20;
		}

		// Progress of various projects:
		if (settlement != null && settlement instanceof Peddler) {
			double progress = ((Peddler)settlement).getProgress();
			g.drawString("Trading next trinket in:", x, textY + 10);
			g.drawOval(x + 32, textY + 20, 42, 42);
			g.setColor(NodeControlPanel.COLOR_GOLDENROD);
			g.fillArc(x + 32, textY + 20, 42, 42, 90, (int)(-360*progress));
		}
		if (settlement != null && (settlement instanceof Capital || (settlement instanceof City && !settlement.isBuildingSite()))) {
			double progress = settlement instanceof Capital ? ((Capital)settlement).getNewPopProgress() : ((City)settlement).getNewPopProgress();
			g.drawString("New subject in:", x, textY);
			g.drawOval(x + 32, textY + 8, 42, 42);
			g.setColor(Color.DARK_GRAY);
			g.fillArc(x + 32, textY + 8, 42, 42, 90, (int)(-360*progress));
		}
		if (settlement != null && settlement.isBuildingSite()) {
			g.drawString("Construction:", x, textY);
			g.drawOval(x + 32, textY + 8, 42, 42);
			g.setColor(COLOR_BROWN);
			g.fillArc(x + 32, textY + 8, 42, 42, 90, (int)(-360 * settlement.getConstructionProgress()));
		}
		if (settlement != null && settlement instanceof Farm && !settlement.isBuildingSite()) {
			Farm farm = (Farm)settlement;
			for (int i = 0; i < farm.getMaxFarmers(); i++) {
				Double cropProgress = farm.getCropProgress(i);
				double progress = cropProgress == null ? 0.0 : cropProgress.doubleValue();
				g.setColor(Color.GREEN);
				g.fillArc(x + 10 + i*40, textY - 4, 30, 30, 90, (int)(-360 * progress));
				g.setColor(Color.BLACK);
				g.drawOval(x + 10 + i*40, textY - 4, 30, 30);
			}
			textY += 40;
		}
		if (settlement != null && settlement instanceof GoldMine && !settlement.isBuildingSite()) {
			g.drawString("Next gold mined in:", x, textY);
			g.setColor(NodeControlPanel.COLOR_GOLDENROD);
			g.fillArc(x + 32, textY + 8, 42, 42, 90, (int)(-360*((GoldMine)settlement).getNextGoldProgress()));
			g.setColor(Color.BLACK);
			g.drawOval(x + 32, textY + 8, 42, 42);
		}
		// Upgrade progress:
		if (settlement != null && settlement.isUpgrading()) {
			if (settlement instanceof Farm) {
				g.setColor(Color.BLACK);
				g.drawString("Upgrading:", x, textY);
				int barWidth = 50;
				g.setColor(COLOR_BROWN);
				g.fillRect(x + 70, textY - 5, (int)(barWidth * settlement.getConstructionProgress()), 8);
				g.setColor(Color.BLACK);
				g.drawRect(x + 70, textY - 5, barWidth, 8);
				textY += 18;
			}
			else {
				g.setColor(Color.BLACK);
				g.drawString("Upgrading:", x + 120, textY);
				textY += 8;
				g.setColor(COLOR_BROWN);
				g.fillArc(x + 120, textY, 42, 42, 90, (int)(-360 * settlement.getConstructionProgress()));
				g.setColor(Color.BLACK);
				g.drawOval(x + 120, textY, 42, 42);
				textY += 50;
			}
		}

		// wall construction:
		Wall wall = instance.getWallAtNode(node);
		if (wall != null && (wall.isBuildingSite() || wall.isUpgrading())) {
			int wallY = getHeight() - 8;
			g.setColor(Color.BLACK);
			g.drawString("Wall construction:", x, wallY);
			g.setColor(Color.DARK_GRAY);
			int barWidth = 60;
			g.fillRect(x + 106, wallY - 5, (int)(barWidth * wall.getConstructionProgress()), 6);
			g.setColor(Color.BLACK);
			g.drawRect(x + 106, wallY - 5, barWidth, 6);
		}
	}


}