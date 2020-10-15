// Portion of the Game GUI that displays what's on the map.

package gui;
import core.*;
import java.awt.*;
import javax.swing.*;


public class MapPanel extends JComponent {

	private static final int PX_PER_MAPCOORD = 190; // controls zoom level
	private static final int NODE_RADIUS = 34; // in pixels
	private static final Color BG_COLOR_DAY = new Color(190, 190, 190);
	private static final Color BG_COLOR_NIGHT = new Color(125, 125, 125);
	private static ImageIcon penantImage = new ImageIcon("images/penant.png");
	private static ImageIcon doublePenantImage = new ImageIcon("images/double_penant.png");
	private static ImageIcon goldImage = new ImageIcon("images/gold.png");
	private static ImageIcon peddlerImage = new ImageIcon("images/peddler.png");
	private static ImageIcon[] capitalImages = new ImageIcon[]{
		new ImageIcon("images/capital1.png"), new ImageIcon("images/capital2.png"), new ImageIcon("images/capital3.png"), new ImageIcon("images/capital4.png"), new ImageIcon("images/capital5.png")
	};
	private static ImageIcon[] cityImages = new ImageIcon[]{
		new ImageIcon("images/city1.png"), new ImageIcon("images/city2.png"), new ImageIcon("images/city3.png"), new ImageIcon("images/city4.png"), new ImageIcon("images/city5.png")
	};
	private static ImageIcon[] farmImages = new ImageIcon[]{
		new ImageIcon("images/farm1.png"), new ImageIcon("images/farm2.png"), new ImageIcon("images/farm3.png") 
	};
	private static final ImageIcon goldMineImage = new ImageIcon("images/gold_mine.png");
	private static final ImageIcon goldMineExhaustedImage = new ImageIcon("images/gold_mine_exhausted.png");
	private static ImageIcon genericPopImage = new ImageIcon("images/subject_generic.png");
	private static ImageIcon hammerImage = new ImageIcon("images/hammer.png");
	private static ImageIcon defenseIcon = new ImageIcon("images/defense.png");
	private static ImageIcon powerIcon = new ImageIcon("images/enemy_strength.png");
	private static ImageIcon arableLandIcon = new ImageIcon("images/arable_land.png");
	private static ImageIcon goldVeinIcon = new ImageIcon("images/gold_vein.png");
	private static ImageIcon strongholdImage = new ImageIcon("images/stronghold.png");
	private static ImageIcon sortieImage = new ImageIcon("images/sortie.png");
	private static ImageIcon[] wallIcons;
	private GUI gui;
	private boolean isKingdomViewOpen;

	static {
		wallIcons = new ImageIcon[8];
		wallIcons[0] = new ImageIcon("images/wall1.png");
		wallIcons[1] = new ImageIcon("images/wall2.png");
		wallIcons[2] = new ImageIcon("images/wall3.png");
		wallIcons[3] = new ImageIcon("images/wall4.png");
		wallIcons[4] = new ImageIcon("images/wall5.png");
		wallIcons[5] = new ImageIcon("images/wall6.png");
		wallIcons[6] = new ImageIcon("images/wall7.png");
		wallIcons[7] = new ImageIcon("images/wall8.png");
	}


	public MapPanel(GUI g, int w, int h) {
		gui = g;
		setPreferredSize(new Dimension(w, h));
	}

	// =======================================

	public void paintComponent(Graphics g) {
		GameInstance instance = gui.getCallbackManager().getCurrentGame();
		// background:
		g.setColor(instance.isDaytime() ? BG_COLOR_DAY : BG_COLOR_NIGHT);
		g.fillRect(0, 0, getWidth(), getHeight());

		// draw edges:
		for (MapNode node: instance.getMap().getNodeList()) {
			if (instance.isNodeExplored(node)) {
				paintEdges(g, node);
			}
		}

		// paint nodes:
		paintMapNode(g, instance.getMap().getRootNode());

		// paint monarch if they are not at a node:
		if (instance.getMonarchNodeLocation() == null) {
			paintMonarch(g);
		}

		// paint the clock that shows progress through the day or night:
		paintClock(g);

		// show details of a sortie (if applicable):
		Sortie sortie = instance.getMonarchNodeLocation() != null ? instance.getSortieAt(instance.getMonarchNodeLocation()) : null;
		if (sortie != null) {
			paintSortieDetails(g, instance, sortie);
		}

		// show special retinue (if applicable)
		if (instance.getSpecialRetinue() != null) {
			paintSpecialRetinue(g, instance);
		}

		// kingdom view:
		paintKingdomView(g);

		// Indicate if game is paused:
		if (gui.getCallbackManager().isPaused()) {
			g.setColor(new Color(180, 180, 180, 130));
			g.fillRect(0, 0, getWidth(), getHeight());
			g.setColor(Color.BLACK);
			g.setFont(new Font("big", Font.PLAIN, 56));
			g.drawString("Paused", getWidth() / 2 - 90, 70);
		}
	}

	// =======================================

	private void paintMapNode(Graphics g, MapNode node) {
		// recursive way to draw all explored nodes
		GameInstance instance = gui.getCallbackManager().getCurrentGame();
		Settlement settlement = instance.getSettlementAt(node);

		java.awt.geom.Point2D.Double center = instance.getMonarchLocation();
		int screenX = getWidth() / 2 + (int)Math.round((node.getX() - center.getX()) * PX_PER_MAPCOORD); // coords of node
		int screenY = getHeight() / 2 + (int)Math.round((node.getY() - center.getY()) * PX_PER_MAPCOORD);
		
		// make sure some part of node is on screen
		if (!(screenX + NODE_RADIUS < 0 || screenX - NODE_RADIUS > getWidth() || screenY + NODE_RADIUS < 0 || screenY - NODE_RADIUS > getHeight())) {

			// If it is under attack, show that:
			FrontierAttack attack = instance.getAttackAtNode(node);
			if (attack != null) {
				g.setColor(Color.RED);
				double danger = attack.getCurrentStrength() / instance.getDefenseRating(node);
				g.fillArc(screenX - NODE_RADIUS - 4, screenY - NODE_RADIUS - 4, NODE_RADIUS * 2 + 8, NODE_RADIUS * 2 + 8, 270, (int)(-360 * danger));
			}

			// draw circle and stuff inside it
			if (instance.isNodeCaptured(node)) {
				g.setColor(Color.BLUE);
			}
			else {
				g.setColor(Color.BLACK);
			}
			g.fillOval(screenX - NODE_RADIUS - 2, screenY - NODE_RADIUS - 2, NODE_RADIUS * 2 + 4, NODE_RADIUS * 2 + 4);
			g.setColor(Color.WHITE);
			g.fillOval(screenX - NODE_RADIUS, screenY - NODE_RADIUS, NODE_RADIUS * 2, NODE_RADIUS * 2);
			
			// settlement icon:
			if (instance.isNodeExplored(node) && settlement != null) {
				ImageIcon icon = null;
				if (settlement instanceof Peddler) {
					icon = peddlerImage;
				}
				else if(settlement instanceof Capital) {
					icon = capitalImages[settlement.getLevel() - 1];
				}
				else if (settlement instanceof City) {
					icon = cityImages[settlement.getLevel() - 1];
				}
				else if (settlement instanceof Farm) {
					icon = farmImages[settlement.getLevel() - 1];
				}
				else if (settlement instanceof GoldMine) {
					if (instance.getGoldVeinAmount(node) == 0) {
						icon = goldMineExhaustedImage;
					}
					else {
						icon = goldMineImage;
					}
				}
				if (icon != null) {
					g.drawImage(icon.getImage(), screenX - NODE_RADIUS, screenY - NODE_RADIUS, NODE_RADIUS * 2, NODE_RADIUS * 2, this);
				}

				// Indicating when it is a building site and not yet useable:
				if (settlement.isBuildingSite()) {
					g.drawImage(hammerImage.getImage(), screenX - NODE_RADIUS, screenY - NODE_RADIUS, NODE_RADIUS * 2, NODE_RADIUS * 2, this);
				}
			}
			Wall wall = instance.getWallAtNode(node);
			if (wall != null && !wall.isBuildingSite()) {
				g.drawImage(wallIcons[wall.getLevel() - 1].getImage(), screenX - NODE_RADIUS, screenY - NODE_RADIUS, NODE_RADIUS * 2, NODE_RADIUS * 2, this);
			}

			// unexplored text:
			if (!instance.isNodeExplored(node)) {
				g.setColor(Color.BLACK);
				g.setFont(new Font("foo", Font.PLAIN, 16));
				g.drawString("?", screenX - 2, screenY + 4);
			}

			if (instance.isNodeExplored(node)) {
				paintNodeResourcesAndDefense(g, node, screenX, screenY);
			}

			// arable land:
			if (settlement == null && instance.isNodeArable(node) && instance.isNodeExplored(node)) {
				g.drawImage(arableLandIcon.getImage(), screenX - NODE_RADIUS, screenY - NODE_RADIUS, NODE_RADIUS * 2, NODE_RADIUS * 2, this);
			}
			// gold veins:
			if (settlement == null && instance.isNodeGoldVein(node) && instance.isNodeExplored(node)) {
				g.drawImage(goldVeinIcon.getImage(), screenX - NODE_RADIUS, screenY - NODE_RADIUS, NODE_RADIUS * 2, NODE_RADIUS * 2, this);
			}

			// Enemy strongholds:
			if (instance.isNodeExplored(node) && instance.getStrongholdAt(node) != null) {
				g.drawImage(strongholdImage.getImage(), screenX - NODE_RADIUS, screenY - NODE_RADIUS, NODE_RADIUS * 2, NODE_RADIUS * 2, this);
			}

			// indicate monarch's location:
			if (instance.getMonarchNodeLocation() == node) {
				ImageIcon penant = instance.getSpecialRetinue() == null ? penantImage : doublePenantImage;
				g.drawImage(penant.getImage(), screenX + NODE_RADIUS - 6, screenY - 34, 24, 44, this);
			}
		}

		// if explored, draw children (i.e. the recursion part)
		if (instance.isNodeExplored(node)) {
			for (MapNode child: node.getChildren()) {
				paintMapNode(g, child);
			}
		}
	}

	// =======================================

	private void paintNodeResourcesAndDefense(Graphics g, MapNode node, int x, int y) {
		// shows gold, citizens, defense level, etc
		// node is centered at (x, y) on the component
		GameInstance instance = gui.getCallbackManager().getCurrentGame();
		Settlement settlement = instance.getSettlementAt(node);

		g.setFont(new Font("foo", Font.PLAIN, 10));
		// showing gold:
		if (settlement != null && settlement.getGold() > 0) {
			int infoX = x; // coordinates of the center of the piece of info
			int infoY = y - NODE_RADIUS - 12;
			if (instance.getPopCountAtNode(node) > 0) {
				infoX = x - NODE_RADIUS + 2;
				infoY = y - NODE_RADIUS - 8;
			}
			g.drawImage(goldImage.getImage(), infoX - 14, infoY - 12, 24, 22, this);
			g.setColor(Color.BLACK);
			g.drawString("x " + settlement.getGold(), infoX + 2, infoY + 5);
		}

		// showing population:
		if (instance.getPopCountAtNode(node) > 0) {
			int infoX = x;
			int infoY = y - NODE_RADIUS - 12;
			if (settlement != null && settlement.getGold() > 0) {
				infoX = x + NODE_RADIUS - 6;
				infoY = y - NODE_RADIUS - 8;
			}
			g.drawImage(genericPopImage.getImage(), infoX - 16, infoY - 12, 22, 18, this);
			g.setColor(Color.BLACK);
			
			g.drawString("x " + instance.getPopCountAtNode(node), infoX + 2, infoY + 5);
		}

		// defense rating:
		if (instance.isNodeCaptured(node)) {
			int defenseRating = instance.getDefenseRating(node);
			g.drawImage(defenseIcon.getImage(), x - 16, y + NODE_RADIUS, 18, 18, this);
			g.setColor(Color.BLACK);
			g.drawString("x " + defenseRating, x, y + NODE_RADIUS + 10);
		}
		// enemy strength rating:
		Stronghold stronghold = instance.getStrongholdAt(node);
		if (stronghold != null && instance.isNodeExplored(node)) {
			g.drawImage(powerIcon.getImage(), x - 16, y + NODE_RADIUS, 18, 18, this);
			g.setColor(Color.BLACK);
			g.drawString("x " + stronghold.getStrength(instance), x, y + NODE_RADIUS + 10);
		}
		// sortie icon:
		if (instance.getSortieAt(node) != null || (stronghold != null && instance.getSortieTargeting(stronghold) != null)) {
			g.drawImage(sortieImage.getImage(), x - 9, y + NODE_RADIUS + 20, 18, 18, this);
		}
	}

	// =======================================

	private void paintMonarch(Graphics g) {
		// Only used when monarch is not at a node.
		java.awt.geom.Point2D.Double center = gui.getCallbackManager().getCurrentGame().getMonarchLocation();
		java.awt.geom.Point2D.Double monarchLoc = gui.getCallbackManager().getCurrentGame().getMonarchLocation(); 
		int screenX = getWidth() / 2 + (int)Math.round((monarchLoc.getX() - center.getX()) * PX_PER_MAPCOORD);
		int screenY = getHeight() / 2 + (int)Math.round((monarchLoc.getY() - center.getY()) * PX_PER_MAPCOORD);

		g.setColor(Color.BLUE);
		g.fillOval(screenX - 3, screenY - 3, 6, 6);
	}

	// =======================================

	private void paintEdges(Graphics g, MapNode node) {
		// draw edges to this node's children.
		java.awt.geom.Point2D.Double center = gui.getCallbackManager().getCurrentGame().getMonarchLocation();
		int screenX = getWidth() / 2 + (int)Math.round((node.getX() - center.getX()) * PX_PER_MAPCOORD); // coords of node
		int screenY = getHeight() / 2 + (int)Math.round((node.getY() - center.getY()) * PX_PER_MAPCOORD);

		g.setColor(Color.BLACK);
		for (MapNode child: node.getChildren()) {
			int childX = getWidth() / 2 + (int)Math.round((child.getX() - center.getX()) * PX_PER_MAPCOORD);
			int childY = getHeight() / 2 + (int)Math.round((child.getY() - center.getY()) * PX_PER_MAPCOORD);

			g.drawLine(screenX, screenY, childX, childY);
		}
	}

	// =======================================

	private void paintClock(Graphics g) {
		int clockWidth = 224;
		int clockHeight = 28;

		//background:
		g.setColor(Color.WHITE);
		g.fillRect((getWidth() - clockWidth) / 2, 0, clockWidth, clockHeight);
		g.setColor(Color.BLACK);
		g.drawRect((getWidth() - clockWidth) / 2, 0, clockWidth, clockHeight);

		// text indicating day or night:
		g.setFont(new Font("foo", Font.PLAIN, 14));
		boolean isDaytime = gui.getCallbackManager().getCurrentGame().isDaytime();
		g.drawString(isDaytime ? "Day" : "Night", (getWidth() - clockWidth) / 2 + 4, 18);

		int timeLineX = (getWidth() - clockWidth) / 2 + 44;
		int timeLineLength = clockWidth - 54;
		int timeLineY = clockHeight - 8;
		g.drawLine(timeLineX, timeLineY, timeLineX + timeLineLength, timeLineY);
		// tick marks:
		int tickmarkHeight = 8;
		g.drawLine(timeLineX, timeLineY, timeLineX, timeLineY - tickmarkHeight);
		g.drawLine(timeLineX + timeLineLength, timeLineY, timeLineX + timeLineLength, timeLineY - tickmarkHeight);
		g.drawLine(timeLineX + timeLineLength/2, timeLineY, timeLineX + timeLineLength/2, timeLineY - tickmarkHeight);
		// progress marker:
		if (isDaytime) {
			g.setColor(Color.ORANGE);
		}
		else {
			g.setColor(Color.BLUE);
		}
		double progress = gui.getCallbackManager().getCurrentGame().getDayNightTime();
		g.fillRect(timeLineX + (int)(progress * timeLineLength) - 2, timeLineY - tickmarkHeight + 1, 4, tickmarkHeight);
	}

	// =======================================

	private void paintSortieDetails(Graphics g, GameInstance instance, Sortie sortie) {
		int w = 290;
		int h = 200;
		// background:
		g.setColor(new Color(225, 225, 225));
		g.fillRect(getWidth() - w, getHeight() - h, w, h);
		g.setColor(Color.BLACK);
		g.drawRect(getWidth() - w, getHeight() - h, w, h);

		g.setFont(new Font("foo", Font.PLAIN, 10));
		g.drawString("You have launched a sortie!", getWidth() - w + 8, getHeight() - h + 12);
		g.drawString("Casaulties so far: " + sortie.getCasaultyCount(), getWidth() - w + 8, getHeight() - 15);
		g.drawString("x " + sortie.calculateHumanStrength(instance) + "  vs", getWidth() - w + 28, getHeight() - h + 28);
		g.drawString("x " + sortie.getTarget().getStrength(instance), getWidth() - w + 86, getHeight() - h + 28);
		g.drawString("(Distance penalty: " + (int)(100 * sortie.getDistancePenalty()) + "%)", getWidth() - w + 114, getHeight() - h + 28);
		// sword icons:
		g.drawImage(sortieImage.getImage(), getWidth() - w + 8, getHeight() - h + 15, 20, 20, this);
		g.drawImage(powerIcon.getImage(), getWidth() - w + 66, getHeight() - h + 15, 20, 20, this);

		// victory progress:
		g.drawString("Victory in:", getWidth() - w + 8, getHeight() - h + 61);
		g.setColor(Color.BLUE);
		g.fillArc(getWidth() - w + 16, getHeight() - h + 68, 72, 72, 90, (int)(-360 * sortie.getVictoryProgress()));
		g.setColor(Color.BLACK);
		g.drawOval(getWidth() - w + 16, getHeight() - h + 68, 72, 72);

		// casaulty progress;
		g.drawString("Next casaulty in:", getWidth() - w + 68, getHeight() - h + 61);
		g.setColor(Color.RED);
		g.fillArc(getWidth() - w + 100, getHeight() - h + 68, 72, 72, 90, (int)(-360 * sortie.getCasaultyProgress()));
		g.setColor(Color.BLACK);
		g.drawOval(getWidth() - w + 100, getHeight() - h + 68, 72, 72);
	}

	// =======================================

	private void paintSpecialRetinue(Graphics g, GameInstance instance) {
		int w = 220;
		int h = 60;
		// background:
		g.setColor(new Color(225, 225, 225));
		g.fillRect(0, 0, w, h);
		g.setColor(Color.BLACK);
		g.drawRect(0, 0, w, h);

		g.setFont(new Font("foo", Font.PLAIN, 12));
		g.drawString(instance.getSpecialRetinueName(), 36, 13);

		// double-penant icon:
		g.drawImage(doublePenantImage.getImage(), 6, 6, 28, 38, this);

		// Print population:
		int subjectX = 36;
		int subjectY = 30;
		for (SubjectType subjectType: SubjectType.values()) {
			int n = instance.getSpecialRetinue().getCount(subjectType);
			if (n > 0) {
				g.drawImage(NodeControlPanel.getImageForSubjectType(subjectType).getImage(), subjectX, subjectY - 10, 22, 16, this);
				g.drawString("x " + n, subjectX + 19, subjectY + 3);
				subjectX += 44;
			}
		}
	}

	// =======================================

	private void paintKingdomView(Graphics g) {
		int w = 220;
		int h;
		if (isKingdomViewOpen) {
			h = 160;
		}
		else {
			h = 20;
		}
		// background:
		g.setColor(new Color(225, 225, 225));
		g.fillRect(0, getHeight() - h, w, h);
		g.setColor(Color.BLACK);
		g.drawRect(0, getHeight() - h, w, h);

		g.setFont(new Font("foo", Font.PLAIN, 11));

		if (!isKingdomViewOpen) {
			g.drawString("Press 'k' to open Kingdom Overview", 4, getHeight() - h + 12);
		}
		else {
			GameInstance instance = gui.getCallbackManager().getCurrentGame();
			int y = getHeight() - h + 12;

			g.drawString("Kingdom Overview", 20, y);
			y += 24;
			g.drawString("Population:", 4, y);
			y += 8;
			paintPopulation(g, instance.getTotalKingdomPopulation(), 6, y);
			y += 38;
		
			HousingTracker tracker = instance.getHousingTracker();
			g.drawString("Housing: " + instance.getTotalKingdomPopulation().getTotal() + " / " + tracker.getTotal(), 6, y);
			y += 14;
			PopGrowthModifier mod = instance.getPopGrowthModifier();
			g.drawString("Growth rate: " + (int)(100 * mod.getGrowthMod()) + "%", 27, y);
			g.setColor(mod.getColor());
			g.fillRect(12, y - 6, 8, 8);
			y += 14;

			g.setColor(Color.BLACK);
			g.drawString("Housing at capital: " + tracker.getCapitalHousing(), 16, y);
			y += 14;
			g.drawString("Housing at cities: " + tracker.getCitiesHousing(), 16, y);
			y += 14;
			g.drawString("Housing from undeveloped land: " + tracker.getOpenNodeHousing(), 16, y);
		}
	}

	// =======================================

	public void toggleKingdomView() {
		isKingdomViewOpen = !isKingdomViewOpen;
	}

	// =======================================

	private void paintPopulation(Graphics g, Population population, int x, int y) {
		GameInstance instance = gui.getCallbackManager().getCurrentGame();
		for (SubjectType subjectType: SubjectType.values()) {
			int n = population.getCount(subjectType);
			if (n > 0) {
				g.drawImage(NodeControlPanel.getImageForSubjectType(subjectType).getImage(), x, y, 22, 16, this);
				g.drawString("x " + n, x + 17, y + 8);
				x += 40;
			}
		}
	}

}