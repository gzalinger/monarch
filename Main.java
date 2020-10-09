// Monarch MVP
// Started 15 September 2020

import core.*;
import gui.*;
import java.io.*;


public class Main implements GUICallbackManager {

	private static final String SAVE_GAME_FILENAME = "saved_game.dat";
	private GUI gui;
	private GameInstance currentGame;
	private GameEngine engine;


	public static void main(String[] args) {
		new Main();
	}

	private Main() {
		gui = new GUI(this);
	}

	// ===============================================

	public void newGame() {
		currentGame = new GameInstance();
		gui.onNewGameStart();
		
		// Start the game engine:
		engine = new GameEngine(currentGame, gui);
		(new Thread(engine)).start();
	}

	// ===============================================

	public void loadGame() {
		try {
    		ObjectInputStream in = new ObjectInputStream(new DataInputStream(new FileInputStream(new File(SAVE_GAME_FILENAME))));
    		currentGame = (GameInstance)in.readObject();
    		in.close();

    		gui.onNewGameStart();
    		// Start the game engine:
			engine = new GameEngine(currentGame, gui);
			(new Thread(engine)).start();
    	}
    	catch(Exception e) {
    		e.printStackTrace();
    		System.err.println("There was an error while loading the game.");
    	}
	}

	// ===============================================

	public GameInstance getCurrentGame() {
		return currentGame;
	}

	// ===============================================

	public void saveGame() {
		try {
    		ObjectOutputStream out = new ObjectOutputStream(new DataOutputStream(new FileOutputStream(SAVE_GAME_FILENAME)));
    		out.writeObject(currentGame);
    		out.close();
    		System.out.println("Save successful.");
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    	}
	}

	// ===============================================

	public boolean isPaused() {
		return engine.isPaused();
	}

	// ===============================================

	public void pause() {
		engine.pause();
	}

	// ===============================================

	public void resume() {
		engine.resume();
	}

	// ===============================================

	public boolean didPlayerWin() {
		return currentGame.getGameResult();
	}


}