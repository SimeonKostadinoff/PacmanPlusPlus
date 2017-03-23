package main.java.ui;

import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import main.java.event.Event;
import main.java.event.arguments.HostStartingMultiplayerGameEventArgs;
import main.java.event.listener.CountDownStartingListener;
import main.java.event.listener.HostStartingMultiplayerGameListener;
import main.java.event.listener.UserLeavingLobbyListener;
import main.java.gamelogic.domain.GameSettings;

/**
 * Screen for the lobby of a multiplayer game
 *
 * @author Rose Kirtley
 *
 */
public class MultiPlayerLobbyScreen extends Screen implements UserLeavingLobbyListener {

	private BorderPane bPane;
	private Button play;
	private Button leaveGame;
	private Button settings;
	private Label label;

	public boolean thisClient;
	public PlayersList list;
	private GameSettings gameSettings;

	private Event<UserLeavingLobbyListener, Object> userLeavingLobbyEvent;
	private Event<HostStartingMultiplayerGameListener, Object> hostStartingGameListener;
	private Event<CountDownStartingListener, Object> countDownStartingListener;

	public MultiPlayerLobbyScreen(final GameUI game) {
		super(game);

		gameSettings = new GameSettings();

		play = new Button("Start Game");
		play.getStyleClass().add("buttonStyle");
		play.setOnAction(e -> play());
		setUpHover(play);

		leaveGame = new Button("Leave Game");
		leaveGame.getStyleClass().add("buttonStyle");
		leaveGame.setOnAction(e -> leaveGame());
		setUpHover(leaveGame);

		settings = new Button("Change Game Settings");
		settings.getStyleClass().add("buttonStyle");
		settings.setOnAction(e -> showSettings());
		setUpHover(settings);

		final FlowPane buttons = new FlowPane();
		buttons.setPadding(new Insets(5, 5, 5, 5));
		buttons.setVgap(4);
		buttons.setHgap(4);
		buttons.setOrientation(Orientation.VERTICAL);
		buttons.setAlignment(Pos.TOP_CENTER);
		buttons.getStyleClass().add("paneStyle");
		buttons.getChildren().addAll(play, leaveGame, settings);

		label = new Label("Multiplayer");
		label.getStyleClass().add("miniTitleStyle");

		final BorderPane labelPane = new BorderPane();
		labelPane.setTop(label);
		labelPane.setAlignment(label, Pos.CENTER);
		final Separator separator = new Separator();
		separator.getStyleClass().add("separator");
		labelPane.setBottom(separator);

		bPane = new BorderPane();
		bPane.setTop(labelPane);
		bPane.setLeft(buttons);
		pane.getChildren().add(bPane);

		userLeavingLobbyEvent = new Event<>((l, a) -> l.onUserLeavingLobby());
		userLeavingLobbyEvent.addListener(this);

		hostStartingGameListener = new Event<>(
				(l, a) -> l.onHostStartingGame(new HostStartingMultiplayerGameEventArgs(getMultiplayerSettings())));

		countDownStartingListener = new Event<>((l, a) -> l.onCountDownStarted());

		
		addNames();
	}

	public void showSettings() {
		game.showGameSettingsScreen();
	}

	public void setMultiplayerSettings(final GameSettings newSettings) {
		gameSettings = newSettings;
	}

	public GameSettings getMultiplayerSettings() {
		return gameSettings;
	}

	/**
	 * Start countdown
	 */
	private void play() {
		thisClient = true;
		getCountDownStartingListener().fire(null);
	}

	public void setStartGameEnabled(final boolean enabled) {
		play.setDisable(!enabled);
	}

	private void leaveGame() {
		game.switchToMenu();
		userLeavingLobbyEvent.fire(null);
	}

	public void addNames() {
		list = new PlayersList(game);
		bPane.setCenter(list.getPane());
	}

	public Event<UserLeavingLobbyListener, Object> getUserLeavingLobbyEvent() {
		return userLeavingLobbyEvent;
	}

	public Event<HostStartingMultiplayerGameListener, Object> getHostStartingGameListener() {
		return hostStartingGameListener;
	}
	
	public Event<CountDownStartingListener, Object> getCountDownStartingListener(){
		return countDownStartingListener;
	}

	@Override
	public void onUserLeavingLobby() {
		list.clear();
	}

	@Override
	public void changeSelection(final boolean up) {
		if (up) {
			if (settings.isDefaultButton()) {
				if (!play.isDisabled()) {
					unselect(play);
				}
				select(leaveGame);
				unselect(settings);
			} else if (leaveGame.isDefaultButton()) {
				if (play.isDisabled()) {
					select(settings);
				} else {
					select(play);
					unselect(settings);
				}
				unselect(leaveGame);
			} else {
				select(settings);
				unselect(play);
				unselect(leaveGame);
			}
		} else {
			if (settings.isDefaultButton()) {
				if (play.isDisabled()) {
					select(leaveGame);
				} else {
					select(play);
					unselect(leaveGame);
				}
				unselect(settings);
			} else if (leaveGame.isDefaultButton()) {
				if (!play.isDisabled()) {
					unselect(play);
				}
				select(settings);
				unselect(leaveGame);
			} else {
				select(leaveGame);
				unselect(play);
				unselect(settings);
			}
		}

	}

	@Override
	public void makeSelection() {
		if (settings.isDefaultButton()) {
			showSettings();
		} else if (leaveGame.isDefaultButton()) {
			leaveGame();
		} else {
			play();
		}
	}

	@Override
	public void unselectAll() {
		reset(play);
		reset(leaveGame);
		reset(settings);
	}
}
