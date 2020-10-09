// A way for the GUI to make calls back to the Main object

package gui;
import core.*;


public interface GUICallbackManager {


	public GameInstance getCurrentGame();
	

	public void loadGame();


	public void newGame();


	public void saveGame();


	public boolean isPaused();


	public void pause();


	public void resume();


	public boolean didPlayerWin();


}