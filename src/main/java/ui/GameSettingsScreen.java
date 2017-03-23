package main.java.ui;

import javafx.collections.FXCollections;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import main.java.event.Event;
import main.java.event.arguments.GameSettingsChangedEventArgs;
import main.java.event.listener.GameSettingsChangedEventListener;
/**
 * Screen for changing the settings of a multiplayer game
 * 
 * @author Rose Kirtley
 *
 */
public class GameSettingsScreen extends Screen {

	public Button returnButton;
	private Label label;
	private CheckBox ai;
	private ChoiceBox<Integer> lives;
	
	private Event<GameSettingsChangedEventListener, GameSettingsChangedEventArgs> event = new Event<>((l, a) -> l.onGameSettingsChanged(a));

	public GameSettingsScreen(final GameUI game) {
		super(game);

		returnButton = new Button("Return");
		returnButton.getStyleClass().add("backButtonStyle");
		returnButton.setOnAction(e -> closeSettings());
		
		ai = new CheckBox("Play against AI");
		ai.getStyleClass().add("check-box");
		ai.setSelected(true);

		lives = new ChoiceBox<Integer>();
		lives.getStyleClass().add("check-box");
	    lives.getItems().addAll(1,2,3,4,5);
	    lives.getSelectionModel().select(3);
		
		label = new Label("Game Settings");
		label.getStyleClass().add("miniTitleStyle");

		final Separator separator = new Separator();
		separator.getStyleClass().add("separator");

		pane.getChildren().addAll(label, separator, lives, ai, returnButton);
	}
	
	public Event<GameSettingsChangedEventListener, GameSettingsChangedEventArgs> getGameSettingsChangedEvent(){
		return event;
	}
	
	/**
	 *Sets the new settings on returning to the lobby. 
	 */
	private void closeSettings(){
		game.multiPlayerLobbyScreen.getMultiplayerSettings().setInitialPlayerLives(lives.getValue()-1);
		game.multiPlayerLobbyScreen.getMultiplayerSettings().setAIPlayer(ai.isSelected());
		event.fire(new GameSettingsChangedEventArgs(game.multiPlayerLobbyScreen.getMultiplayerSettings()));
		game.removeGameSettingsScreen();
	}

	@Override
	public void changeSelection(final boolean up) {
		// Only one button
	}

	@Override
	public void unselectAll() {
		// only one button
	}

	@Override
	public void makeSelection() {
		closeSettings();
		
	}
	
	/**
	 * updates the values from the settings object
	 */
	public void update() {
		ai.setSelected(game.multiPlayerLobbyScreen.getMultiplayerSettings().getAIPlayer());
	    lives.getSelectionModel().select(game.multiPlayerLobbyScreen.getMultiplayerSettings().getInitialPlayerLives());
	}
}
