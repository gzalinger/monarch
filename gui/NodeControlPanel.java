// Bottom portion of game GUI, shows controls for the current node and details about it.

package gui;
import core.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import javax.swing.*;


public class NodeControlPanel extends JPanel {

	private static final Color COLOR_GOLDENROD = new Color(218, 165, 32);
	private static final Color COLOR_BROWN = new Color(165, 42, 42);
	private static final ImageIcon idleSubjectImage = new ImageIcon("images/subject_idle.png");
	private static final ImageIcon soldierImage = new ImageIcon("images/subject_soldier.png");
	private static final ImageIcon workerImage = new ImageIcon("images/subject_worker.png");
	private static final ImageIcon farmerImage = new ImageIcon("images/subject_farmer.png");
	private static final ImageIcon captainImage = new ImageIcon("images/subject_captain.png");
	private static final ImageIcon buttonRetinueMgmtImage = new ImageIcon("images/button_retinue_mgmt.png");
	private static final ImageIcon buttonConstruction = new ImageIcon("images/hammer.png");
	private static final ImageIcon buttonTreasury = new ImageIcon("images/treasury.png");
	private static final ImageIcon buttonBack = new ImageIcon("images/back.png");
	private static final ImageIcon buttonRetinueRecruit = new ImageIcon("images/button_retinue_recruit.png");
	private static final ImageIcon buttonRetinueLeave = new ImageIcon("images/button_retinue_leave.png");
	private static final ImageIcon buttonRecruitIdleSubject = new ImageIcon("images/button_recruit_idle.png");
	private static final ImageIcon buttonLeaveIdleSubject = new ImageIcon("images/button_leave_idle.png");
	private static final ImageIcon buttonLeaveSoldier = new ImageIcon("images/button_leave_soldier.png");
	private static final ImageIcon buttonRecruitSoldier = new ImageIcon("images/button_recruit_soldier.png");
	private static final ImageIcon buttonRecruitWorker = new ImageIcon("images/button_recruit_worker.png");
	private static final ImageIcon buttonLeaveWorker = new ImageIcon("images/button_leave_worker.png");
	private static final ImageIcon buttonRecruitFarmer = new ImageIcon("images/button_recruit_farmer.png");
	private static final ImageIcon buttonLeaveFarmer = new ImageIcon("images/button_leave_farmer.png");
	private static final ImageIcon buttonRecruitCaptain = new ImageIcon("images/button_recruit_captain.png");
	private static final ImageIcon buttonLeaveCaptain = new ImageIcon("images/button_leave_captain.png");
	private static final ImageIcon buttonEquipMenu = new ImageIcon("images/button_equipment.png");
	private static final ImageIcon buttonEquipWorker = new ImageIcon("images/button_equip_worker.png");
	private static final ImageIcon buttonEquipSoldier = new ImageIcon("images/button_equip_soldier.png");
	private static final ImageIcon buttonEquipFarmer = new ImageIcon("images/button_equip_farmer.png");
	private static final ImageIcon buttonEquipCaptain = new ImageIcon("images/button_equip_captain.png");
	private static final ImageIcon buttonWithdrawOneGold = new ImageIcon("images/button_withdraw_one_gold.png");
	private static final ImageIcon buttonDepositOneGold = new ImageIcon("images/button_deposit_one_gold.png");
	private static final ImageIcon buttonWithdrawTenGold = new ImageIcon("images/button_withdraw_ten_gold.png");
	private static final ImageIcon buttonDepositTenGold = new ImageIcon("images/button_deposit_ten_gold.png");
	private static final ImageIcon buttonCapture = new ImageIcon("images/capture.png");
	private static final ImageIcon buttonFarm = new ImageIcon("images/farm.png");
	private static final ImageIcon buttonCity = new ImageIcon("images/city.png");
	private static final ImageIcon buttonConstructionUpgrade = new ImageIcon("images/upgrade_building.png");
	private static final ImageIcon buttonSortie = new ImageIcon("images/sortie.png");
	private static final ImageIcon buttonCancel = new ImageIcon("images/cancel.png");
	private static final ImageIcon buttonAddWall = new ImageIcon("images/add_wall.png");
	private static final ImageIcon buttonUpgradeWall = new ImageIcon("images/upgrade_wall.png");
	private static final ImageIcon buttonGoldMine = new ImageIcon("images/gold_mine.png");
	private GUI gui;
	private String menu; // e.g. "main" or "treasury"


	public NodeControlPanel(GUI g, int w, int h) {
		gui = g;
		setPreferredSize(new Dimension(w, h));
		menu = "main"; // by default
	}

	// ========================================

	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_Q) {
			processButtonPress("Q");
		}
		if (e.getKeyCode() == KeyEvent.VK_W) {
			processButtonPress("W");
		}
		if (e.getKeyCode() == KeyEvent.VK_E) {
			processButtonPress("E");
		}
		if (e.getKeyCode() == KeyEvent.VK_R) {
			processButtonPress("R");
		}
		if (e.getKeyCode() == KeyEvent.VK_T) {
			processButtonPress("T");
		}
		if (e.getKeyCode() == KeyEvent.VK_Y) {
			processButtonPress("Y");
		}
	}

	// ========================================

	private void processButtonPress(String buttonLetter) {
		GameInstance instance = gui.getCallbackManager().getCurrentGame();
		MapNode node = instance.getMonarchNodeLocation();
		Settlement settlement = instance.getSettlementAt(node);
		ButtonInfo info = getButtonInfo(buttonLetter);
		if (!info.isActive) {
			return;
		}
		if (menu.equals("main")) {
			if (buttonLetter.equals("W")) {
				menu = "construction";
				repaint();
				return;
			}
			if (buttonLetter.equals("E")) {
				menu = "treasury";
				repaint();
				return;
			}
			if (buttonLetter.equals("R")) {
				menu = "subject_main";
				repaint();
				return;
			}
			if (buttonLetter.equals("T")) {
				menu = "equip";
				repaint();
				return;
			}
			if (buttonLetter.equals("Y")) {
				if (!instance.isNodeCaptured(node)) {
					instance.attemptCaptureNode(node);
				}
				else if (instance.getSortieAt(node) != null) {
					instance.cancelSortie(instance.getSortieAt(node));
				}
				else {
					instance.attemptLaunchSortie();
				}
			}
		}

		// treasury sub-menu:
		else if(menu.equals("treasury")) {
			if (buttonLetter.equals("Q")) {
				menu = "main";
				repaint();
				return;
			}
			if (buttonLetter.equals("W")) {
				instance.attemptGoldWithdrawal(1);
				return;
			}
			if (buttonLetter.equals("E")) {
				instance.attemptGoldWithdrawal(10);
			}
			if (buttonLetter.equals("R")) {
				instance.attemptGoldDeposit(1);
			}
			if (buttonLetter.equals("T")) {
				instance.attemptGoldDeposit(10);
			}
		}

		// this is the sub-menu for selecting either recruiting or leaving subjects:
		else if(menu.equals("subject_main")) {
			if (buttonLetter.equals("Q")) {
				menu = "main";
				repaint();
				return;
			}
			if (buttonLetter.equals("W")) {
				menu = "subject_recruit";
				repaint();
				return;
			}
			if (buttonLetter.equals("E")) {
				menu = "subject_leave";
				repaint();
				return;
			}
			if (buttonLetter.equals("Y")) {
				instance.attemptLeaveSpecialRetinue();
			}
		}

		else if(menu.equals("subject_recruit") || menu.equals("subject_leave")) {
			if (buttonLetter.equals("Q")) {
				menu = "subject_main";
				repaint();
				return;
			}
			SubjectType s = null;
			if (buttonLetter.equals("W")) {
				s = SubjectType.IDLE;
			}
			if (buttonLetter.equals("E")) {
				s = SubjectType.WORKER;
			}
			if (buttonLetter.equals("R")) {
				s = SubjectType.SOLDIER;
			}
			if (buttonLetter.equals("T")) {
				s = SubjectType.FARMER;
			}
			if (buttonLetter.equals("Y")) {
				s = SubjectType.CAPTAIN;
			}

			if (s != null) {
				if (menu.equals("subject_recruit")) {
					instance.attemptRecruitSubject(s);
				}
				else {
					instance.attemptLeaveSubject(s);
				}
			}
		}

		else if (menu.equals("equip")) {
			if (buttonLetter.equals("Q")) {
				menu = "main";
				repaint();
				return;
			}
			SubjectType s = null;
			if (buttonLetter.equals("E")) {
				s = SubjectType.WORKER;
			}
			if (buttonLetter.equals("R")) {
				s = SubjectType.SOLDIER;
			}
			if (buttonLetter.equals("T")) {
				s = SubjectType.FARMER;
			}
			if (buttonLetter.equals("Y")) {
				s = SubjectType.CAPTAIN;
			}

			if (s != null) {
				instance.attemptEquipSubject(s);
			}
		}

		else if (menu.equals("construction")) {
			if (buttonLetter.equals("Q")) {
				menu = "main";
				repaint();
				return;
			}
			if (buttonLetter.equals("W")) {
				instance.attemptUpgradeSettlement();
			}
			if (buttonLetter.equals("E")) {
				if (settlement == null) {
					instance.attemptBuildNewCity(node);
				}
			}
			if (buttonLetter.equals("R")) {
				if (settlement == null) {
					instance.attemptBuildNewFarm(node);
				}
			}
			if (buttonLetter.equals("T")) {
				instance.attemptBuildOrUpgradeWall();
			}
			if (buttonLetter.equals("Y")) {
				instance.attemptBuildGoldMine();
			}
		}
	}

	// ========================================

	public void paintComponent(Graphics g) {
		int leftWidth = getWidth() - 290;
		GameInstance instance = gui.getCallbackManager().getCurrentGame();
		Settlement settlement = instance.getMonarchNodeLocation() == null ? null : instance.getSettlementAt(instance.getMonarchNodeLocation());

		// Left side buttons:
		String[] buttonLetters = new String[]{"Q", "W", "E", "R", "T", "Y"};
		for (int i = 0; i < buttonLetters.length; i++) {
			ButtonInfo info = getButtonInfo(buttonLetters[i]);

			// Draw button outlines and key shortcut labels:
			if (info.isActive) {
				g.setColor(Color.BLACK);
			}
			else {
				g.setColor(Color.GRAY);
			}
			g.drawString(buttonLetters[i], 64 + i * 116, 18);
			g.drawRoundRect(38 + i * 116, 30, 56, 56, 4, 4);
			if (info.image != null) {
				g.drawImage(info.image.getImage(), 38 + i * 116, 30, 56, 56, this);
			}

			// images and labels which are context specific
			if (info.label != null) {
				g.setColor(Color.BLACK);
				g.setFont(new Font("foo", Font.PLAIN, 10));
				int labelWidth = g.getFontMetrics().stringWidth(info.label);
				g.drawString(info.label, 66 + i * 116 - labelWidth / 2, 101);
			}
		}

		// divider line:
		g.setColor(Color.BLACK);
		g.drawLine(leftWidth, 0, leftWidth, getHeight());

		// Right side info:
		if (instance.getMonarchNodeLocation() != null) {
			paintNodeDetails(g, instance.getMonarchNodeLocation(), leftWidth + 8, 22);
		}
		

		// hacky: use this frequently called method to check when monarch has left a node:
		if (gui.getCallbackManager().getCurrentGame().getMonarchNodeLocation() == null) {
			// reset menu state:
			menu = "main";
		}
	}

	// ========================================

	private void paintNodeDetails(Graphics g, MapNode node, int x, int y) {
		// Show info (not controls) about this node, with top-left at (x, y)
		GameInstance instance = gui.getCallbackManager().getCurrentGame();
		Settlement settlement = instance.getSettlementAt(node);
		g.setColor(Color.BLACK);
		g.setFont(new Font("foo", Font.PLAIN, 12));
		int textY = y;
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
				g.drawImage(getImageForSubjectType(subjectType).getImage(), subjectX, textY - 10, 22, 16, this);
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
			g.setColor(COLOR_GOLDENROD);
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
			g.setColor(COLOR_GOLDENROD);
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

	// ========================================

	private ButtonInfo getButtonInfo(String buttonLetter) {
		// Determines image and label that go with a particular button in the current situation
		GameInstance instance = gui.getCallbackManager().getCurrentGame();
		MapNode node = instance.getMonarchNodeLocation();
		if (node == null) {
			return new ButtonInfo(); // with everything blank
		}
		Settlement settlement = instance.getSettlementAt(node);
		Wall wall = instance.getWallAtNode(node);

		// Main menu:
		if (menu.equals("main")) {
			if (buttonLetter.equals("Q")) {
				// cannot 'go back' from main menu
				return new ButtonInfo();
			}
			if (buttonLetter.equals("W")) {
				// construction menu is active whenever you are in friendly territory
				if (instance.isNodeCaptured(node)) {
					return new ButtonInfo("Construction", true, buttonConstruction);
				}
				else {
					return new ButtonInfo();
				}
			}
			if (buttonLetter.equals("E")) {
				// treasury is available for at least withdrawal at all settlements
				if (settlement != null && !settlement.isBuildingSite()) {
					return new ButtonInfo("Treasury", true, buttonTreasury);
				}
				else {
					return new ButtonInfo();
				}
			}
			if (buttonLetter.equals("R")) {
				// citizen management is available anywhere tin human territory
				if (instance.isNodeCaptured(node)) {
					return new ButtonInfo("Retinue Mgmt", true, buttonRetinueMgmtImage);
				}
				else {
					return new ButtonInfo();
				}
			}
			if (buttonLetter.equals("T")) {
				if (settlement != null && !settlement.isBuildingSite() && (settlement instanceof Capital || settlement instanceof City)) {
					return new ButtonInfo("Equip subjects", true, buttonEquipMenu);
				}
				else {
					return new ButtonInfo();
				}
			}
			if (buttonLetter.equals("Y")) {
				if (instance.canNodeBeCaptured(node)) {
					return new ButtonInfo("Capture (" + instance.NODE_CAPTURE_COST + "gp)", true, buttonCapture);
				}
				else if (instance.canSortieBeLaunched(node)) {
					return new ButtonInfo("Launch sortie", true, buttonSortie);
				}
				else if (instance.getSortieAt(node) != null) {
					return new ButtonInfo("Cancel sortie", true, buttonCancel);
				}
			}
		}

		// Treasury sub-menu
		else if (menu.equals("treasury")) {
			if (buttonLetter.equals("Q")) {
				return new ButtonInfo("Go back", true, buttonBack);
			}
			if (buttonLetter.equals("W")) {
				return new ButtonInfo("Withdraw 1gp", true, buttonWithdrawOneGold);
			}
			if (buttonLetter.equals("E")) {
				return new ButtonInfo("Withdraw 10gp", true, buttonWithdrawTenGold);
			}
			if (settlement != null && settlement.allowsGoldDeposits()) {
				if (buttonLetter.equals("R")) {
					return new ButtonInfo("Deposit 1gp", true, buttonDepositOneGold);
				}
				if (buttonLetter.equals("T")) {
					return new ButtonInfo("Deposit 10gp", true, buttonDepositTenGold);
				}
			}
		}

		// Subject home menu:
		else if (menu.equals("subject_main")) {
			if (buttonLetter.equals("Q")) {
				return new ButtonInfo("Go back", true, buttonBack);
			}
			if (buttonLetter.equals("W")) {
				return new ButtonInfo("Recruit subjects", true, buttonRetinueRecruit);
			}
			if (buttonLetter.equals("E")) {
				return new ButtonInfo("Leave subjects", true, buttonRetinueLeave);
			}
			if (buttonLetter.equals("Y")) {
				if (instance.getSpecialRetinue() != null) {
					return new ButtonInfo("Drop special retinue", true, buttonRetinueLeave);
				}
			}
		}
		else if(menu.equals("subject_recruit") || menu.equals("subject_leave")) {
			if (buttonLetter.equals("Q")) {
				return new ButtonInfo("Go back", true, buttonBack);
			}
			if (buttonLetter.equals("W")) {
				return new ButtonInfo("Idle", true, menu.equals("subject_recruit") ? buttonRecruitIdleSubject : buttonLeaveIdleSubject);
			}
			if (buttonLetter.equals("E")) {
				return new ButtonInfo("Worker", true, menu.equals("subject_recruit") ? buttonRecruitWorker : buttonLeaveWorker);
			}
			if (buttonLetter.equals("R")) {
				return new ButtonInfo("Soldier", true, menu.equals("subject_recruit") ? buttonRecruitSoldier : buttonLeaveSoldier);
			}
			if (buttonLetter.equals("T")) {
				return new ButtonInfo("Farmer", true, menu.equals("subject_recruit") ? buttonRecruitFarmer : buttonLeaveFarmer);
			}
			if (buttonLetter.equals("Y")) {
				return new ButtonInfo("Captain", true, menu.equals("subject_recruit") ? buttonRecruitCaptain : buttonLeaveCaptain);
			}
		}

		// equipment sub-menu
		else if (menu.equals("equip")) {
			if (buttonLetter.equals("Q")) {
				return new ButtonInfo("Go back", true, buttonBack);
			}
			if (buttonLetter.equals("E") && settlement.doesSellEquipment(SubjectType.WORKER)) {
				return new ButtonInfo("Equip worker (" + instance.calculateEquipmentCost(SubjectType.WORKER) + "gp)", true, buttonEquipWorker);
			}
			if (buttonLetter.equals("R") && settlement.doesSellEquipment(SubjectType.SOLDIER)) {
				return new ButtonInfo("Equip soldier (" + instance.calculateEquipmentCost(SubjectType.SOLDIER) + "gp)", true, buttonEquipSoldier);
			}
			if (buttonLetter.equals("T") && settlement.doesSellEquipment(SubjectType.FARMER)) {
				return new ButtonInfo("Equip farmer (" + instance.calculateEquipmentCost(SubjectType.FARMER) + "gp)", true, buttonEquipFarmer);
			}
			if (buttonLetter.equals("Y") && settlement.doesSellEquipment(SubjectType.CAPTAIN)) {
				return new ButtonInfo("Equip captain (" + instance.calculateEquipmentCost(SubjectType.CAPTAIN) + "gp)", true, buttonEquipCaptain);
			}
		}

		// construction sub-menu:
		else if(menu.equals("construction")) {
			if (buttonLetter.equals("Q")) {
				return new ButtonInfo("Go back", true, buttonBack);
			}
			if (buttonLetter.equals("W")) {
				if (settlement != null && !settlement.isUpgrading() && !settlement.isBuildingSite() && settlement.getLevel() < settlement.getMaxLevel() && instance.isSettlementUpgradeUnlocked(settlement)) {
					return new ButtonInfo("Upgrade (" + settlement.getUpgradeCost() + "gp)", true, buttonConstructionUpgrade);
				}
			}
			if (buttonLetter.equals("E")) {
				if (instance.isNodeCaptured(node) && settlement == null && !instance.isTooCloseToCity(node)) {
					return new ButtonInfo("Found new city (" + City.getCost(1) + "gp)", true, buttonCity);
				}
			}
			if ( buttonLetter.equals("R")) {
				if (instance.isNodeCaptured(node) 
					&& settlement == null && instance.getCapital().getLevel() >= instance.FARM_UNLOCK_LEVEL
					&& instance.isNodeArable(node))
				{
					return new ButtonInfo("Build new farm (" + Farm.getCost(1) + "gp)", true, buttonFarm);
				}
			}
			if (buttonLetter.equals("T")) {
				if (instance.isNodeCaptured(node) && (settlement == null || (!settlement.isBuildingSite() && !settlement.isUpgrading()))) {
					if (wall == null) {
						return new ButtonInfo("Build wall (" + Wall.getCost(1) + "gp)", true, buttonAddWall);
					}
					else if (wall.getLevel() < Wall.MAX_LEVEL && !wall.isUpgrading() && !wall.isBuildingSite()) {
						return new ButtonInfo("Upgrade wall (" + wall.getUpgradeCost() + "gp)", true, buttonUpgradeWall);
					}
				}
			}
			if (buttonLetter.equals("Y")) {
				if (instance.isNodeCaptured(node) && settlement == null && instance.isNodeGoldVein(node) && (wall == null || !wall.isUpgrading())) {
					return new ButtonInfo("Build gold mine", true, buttonGoldMine);
				}
			}
		}

		// default
		return new ButtonInfo();
	}

	// ========================================

	public static ImageIcon getImageForSubjectType(SubjectType s) {
		switch(s) {
			case IDLE: return idleSubjectImage;
			case SOLDIER: return soldierImage;
			case WORKER: return workerImage;
			case FARMER: return farmerImage;
			case CAPTAIN: return captainImage;
			default: return null;
		}
	}

	// ================================================================
	// =====================  INNER CLASS =============================
	// ================================================================

	private class ButtonInfo {
		// Simple wrapper

		protected String label;
		protected boolean isActive;
		protected ImageIcon image;

		public ButtonInfo() {
			// leave everything false, null, etc
		}

		public ButtonInfo(String s, boolean b) {
			label = s;
			isActive = b;
		}

		public ButtonInfo(String s, boolean b, ImageIcon i) {
			this(s, b);
			image = i;
		}
	}

}