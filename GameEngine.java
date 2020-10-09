// This makes the game actually run in real time.

import core.*;
import gui.*;


public class GameEngine implements Runnable {

	private static final int dayLength = 100000; // in ms
	private int nightLength; // = 80000;
	private static final int delay = 50; //in ms
	private GameInstance instance;
	private GUI gui;
	private boolean isPaused;


	public GameEngine(GameInstance i, GUI g) {
		instance = i;
		gui = g;
		nightLength = (int)(dayLength * instance.NIGHT_TO_DAY_RATIO);
	}

	// =====================================

	public synchronized void run() {
		try {
			while (true) {
				wait(delay);

				if (!isPaused) {
					double progress;
					if (instance.isDaytime()) {
						progress = (double)delay / dayLength;
					}
					else {
						progress = (double)delay / nightLength;
					}

					// where the magic happens:
					try {
						instance.update(progress);
					}
					catch (GameOverException e) {
						isPaused = true;
						gui.onGameOver();
					}
				}

				gui.repaint();
			}
		}
		catch(InterruptedException e) {
	    	System.err.println("ERROR: game engine has been interrupted: " + e);
	    	System.exit(1);
		} 
	}

	// =====================================

	public void pause() {
		isPaused = true;
	}

	public void resume() {
		isPaused = false;
	}

	// =====================================

	public boolean isPaused() {
		return isPaused;
	}
	

}