package main.java.ui;

import java.util.List;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import main.java.audio.DefaultMusic;
import main.java.audio.DefaultSoundEffects;
import main.java.audio.DisabledMusic;
import main.java.audio.DisabledSoundEffects;
import main.java.audio.Music;
import main.java.audio.SoundEffects;
import main.java.constants.GameType;
import main.java.event.Event;
import main.java.event.arguments.GameCreatedEventArgs;
import main.java.event.arguments.LobbyChangedEventArgs;
import main.java.event.listener.CountDownStartingListener;
import main.java.event.listener.GameClosingListener;
import main.java.event.listener.GameCreatedListener;
import main.java.event.listener.GameEndedListener;
import main.java.event.listener.LobbyStateChangedListener;
import main.java.event.listener.PlayerLeavingGameListener;
import main.java.gamelogic.core.GameCommandService;
import main.java.gamelogic.core.Lobby;
import main.java.gamelogic.domain.Game;
import main.java.graphics.PositionVisualisation;
import main.java.graphics.Render;
import main.java.networking.data.Packet;
import main.java.networking.integration.ClientInstance;
import main.java.networking.integration.ServerInstance;

/**
 * UI to be run, contains all screens
 *
 * @author Rose Kirtley
 *
 */
public class GameUI extends Application implements GameInterface, LobbyStateChangedListener, GameCreatedListener, PlayerLeavingGameListener {
	private Lobby lobby;
	private Game game;
	private Music music;
	private SoundEffects sounds;

	private Stage thisStage;
	private Scene uiScene;
	private Scene settingsSceneGame;
	private Scene settingsSceneMenu;
	private Scene gameScene;
	private BorderPane pane;
	private BorderPane banner;
	public Button settings;
	private StackPane centerPane;
	private Scene helpScene;
	private BorderPane helpPane;
	
	private static final Integer STARTTIME = 3;
	private Timeline timeline;
	private Label timerLabel = new Label();
	private IntegerProperty timeSeconds = new SimpleIntegerProperty(STARTTIME);

	private String name;

	public Screen currentScreen;
	public LogInScreen logInScreen;
	public MenuScreen menuScreen;
	private GameSettingsScreen gameSettingsScreen;
	private SinglePlayerLobbyScreen singlePlayerLobbyScreen;
	public MultiPlayerLobbyScreen multiPlayerLobbyScreen;
	private MultiPlayerOptionScreen multiPlayerOptionScreen;
	private MultiPlayerJoinScreen multiPlayerJoinScreen;
	private HelpScreen helpScreen;
	private SettingsScreenGame settingsScreenGame;
	private SettingsScreenMenu settingsScreenMenu;

	private Event<GameClosingListener, Object> onGameClosing = new Event<>((l, a) -> l.onGameClosing());
	private Event<PlayerLeavingGameListener, Object> onPlayerLeavingGame = new Event<>((l, a) -> l.onPlayerLeavingGame());
	
	private GameCommandService gameCommandService;

	@Override
	public void start(final Stage primaryStage) throws Exception {
		boolean audioDisabled = false;
		List<String> params = getParameters().getUnnamed();
		if(params.contains("--noaudio")) audioDisabled = true;
		
		gameCommandService = new GameCommandService();
		gameCommandService.getLocalGameCreatedEvent().addListener(this);
		gameCommandService.getRemoteGameCreatedEvent().addListener(this);
		setup(audioDisabled);

		thisStage = primaryStage;
		primaryStage.setTitle("PacMan++");

		pane = new BorderPane();

		centerPane = new StackPane();
		pane.setCenter(centerPane);
		pane.getStyleClass().add("paneStyle");
		uiScene = new Scene(pane, 500, 500);
		uiScene.setOnKeyPressed(e -> sendMoveEvent(e.getCode()));
		
		settingsSceneGame = new Scene(settingsScreenGame.getPane(), 500, 500);
		settingsSceneMenu = new Scene(settingsScreenMenu.getPane(), 500, 500);
		
		helpPane = new BorderPane();
		helpPane.getStyleClass().add("paneStyle");
		helpPane.setCenter(helpScreen.getPane());

		helpScene = new Scene(helpPane, 1150, 600);
		helpScene.setOnKeyPressed(e -> sendMoveEvent(e.getCode()));

		final String css = this.getClass().getResource("style.css").toExternalForm();
		uiScene.getStylesheets().add(css);
		helpScene.getStylesheets().add(css);
		settingsSceneGame.getStylesheets().add(css);
		settingsSceneMenu.getStylesheets().add(css);
		pane.getStyleClass().add("paneStyle");

		setUpSettingsButton();

		timerLabel.setText(timeSeconds.toString());
		timerLabel.getStyleClass().add("countdown");
		timerLabel.setAlignment(Pos.CENTER);
	    timerLabel.textProperty().bind(timeSeconds.asString());

	    primaryStage.setResizable(false);
	    thisStage.setResizable(false);

		primaryStage.setScene(uiScene);
		switchToLogIn();
		primaryStage.show();
	}

	@Override
	public void stop() throws Exception {
		onGameClosing.fire(null);
	}

	public Event<GameClosingListener, Object> getOnGameClosingEvent() {
		return onGameClosing;
	}
	
	public Event<PlayerLeavingGameListener, Object> getOnPlayerLeavingGame() {
		return onPlayerLeavingGame;
	}

	/**
	 * setups up the screens
	 * @param audioDisabled setups up disabled sounds if true
	 */
	private void setup(boolean audioDisabled) {
		music = audioDisabled ? new DisabledMusic() : new DefaultMusic();
		gameCommandService.getLocalGameCreatedEvent().addListener(music);
		gameCommandService.getRemoteGameCreatedEvent().addListener(music);
		sounds = audioDisabled ? new DisabledSoundEffects() : new DefaultSoundEffects(this);
		gameCommandService.getLocalGameCreatedEvent().addListener(sounds);
		gameCommandService.getRemoteGameCreatedEvent().addListener(sounds);
		logInScreen = new LogInScreen(this);
		menuScreen = new MenuScreen(this);
		gameSettingsScreen = new GameSettingsScreen(this);
		settingsScreenGame = new SettingsScreenGame(this);
		settingsScreenMenu = new SettingsScreenMenu(this);
		singlePlayerLobbyScreen = new SinglePlayerLobbyScreen(this);
		singlePlayerLobbyScreen.getOnStartingSingleplayerGame().addListener(gameCommandService);
		multiPlayerLobbyScreen = new MultiPlayerLobbyScreen(this);
		multiPlayerOptionScreen = new MultiPlayerOptionScreen(this);
		multiPlayerJoinScreen = new MultiPlayerJoinScreen(this);
		helpScreen = new HelpScreen(this);
		
	}

	/**
	 * Key press events to button selection
	 * @param key
	 */
	private void sendMoveEvent(final KeyCode key) {
		if (key == KeyCode.UP) {
			currentScreen.changeSelection(true);
		} else if (key == KeyCode.DOWN) {
			currentScreen.changeSelection(false);
		}
	}

	/**
	 * Sets up the settings button for the top right of the screen
	 */
	private void setUpSettingsButton() {

		settings = new Button("Settings");
		settings.setOnAction(e -> switchToSettingsMenu());
		settings.getStyleClass().add("buttonStyle");

		banner = new BorderPane();

		banner.setRight(settings);

		pane.setTop(banner);
	}

	public static void main(final String[] args) {
		launch(args);
	}

	/**
	 * Sets the screen as that given
	 * @param screen
	 */
	private void setScreen(final Screen screen) {
		currentScreen = screen;
		centerPane.getChildren().remove(0, centerPane.getChildren().size());
		centerPane.getChildren().add(screen.getPane());
	}
	
	/**
	 * Centres screen
	 */
	private void adjustScreenPosition(){
		Rectangle2D primScreenBounds = javafx.stage.Screen.getPrimary().getVisualBounds();
		thisStage.setX((primScreenBounds.getWidth() - thisStage.getWidth()) / 2);
		thisStage.setY((primScreenBounds.getHeight() - thisStage.getHeight()) / 2);
	}
	
	/**
	 * Shows the game setting screen for multiplayer games 
	 */
	public void showGameSettingsScreen(){
		gameSettingsScreen.update();
		centerPane.getChildren().add(gameSettingsScreen.getPane());
	}
	
	/**
	 * Removes the game setting screen for multiplayer games
	 */
	public void removeGameSettingsScreen(){
		centerPane.getChildren().remove(gameSettingsScreen.getPane());
	}
	
	/**
	 * Sets the screen to the main menu (resets the scene as ui)
	 */
	public void switchToMenu() {
		thisStage.setScene(uiScene);
		adjustScreenPosition();
		setScreen(menuScreen);
		final Label label = new Label("PacMan++ " + getName());
		label.getStyleClass().add("labelStyle");
		banner.setLeft(label);
		settings.setDisable(false);
	}

	/**
	 * Sets the screen to the login screen
	 */
	public void switchToLogIn() {
		final Label label = new Label("PacMan++");
		label.getStyleClass().add("labelStyle");
		banner.setLeft(label);
		setScreen(logInScreen);
	}

	/**
	 * Sets the screen to the client settings (audio)
	 */
	public void switchToSettingsMenu() {
		thisStage.setScene(settingsSceneMenu);
	}

	/**
	 * Sets the scene back to the game
	 */
	public void returnBackFromGame() {
		thisStage.setScene(gameScene);
	}
	
	/**
	 * Sets the screen back to the menu (uiScene)
	 */
	public void returnBackFromMenu(){
		thisStage.setScene(uiScene);
	}

	/**
	 * Sets the screen to the single player lobby
	 */
	public void switchToSinglePlayerLobby() {
		setScreen(singlePlayerLobbyScreen);
	}

	/**
	 * Sets the screen to the multiplayer lobby
	 */
	public void switchToMultiPlayerLobby() {
		setScreen(multiPlayerLobbyScreen);

	}

	/**
	 * Sets the screen to the multiplayer option screen
	 */
	public void switchToMultiPlayerOption() {
		setScreen(multiPlayerOptionScreen);
	}

	/**
	 * Sets the screen to the multiplayer join screen
	 */
	public void switchToMultiPlayerJoin() {
		setScreen(multiPlayerJoinScreen);
	}
	
	/**
	 * Sets the scene to the help scene (not ui scene any more)
	 */
	public void switchToHelp(){
		final Label label = new Label("PacMan++ " + getName());
		label.getStyleClass().add("labelStyle");
		helpPane.setTop(label);
		thisStage.setScene(helpScene);
		adjustScreenPosition();
	}

	/**
	 * Sets the scene to the client settings screen from the Game
	 */
	public void switchToSettingsGame(){
		thisStage.setScene(settingsSceneGame);
	}
	
	/**
	 * Closes the game
	 */
	public void close() {
		thisStage.close();
	}

	/**
	 * returns the Game object
	 * @return
	 */
	public Game getGame() {
		return game;
	}

	/**
	 * Sets the name of the client
	 * @param name
	 */
	public void setName(final String name) {
		this.name = name;
	}

	/**
	 * Returns the name of the client
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * Starts a new pending multiplayer game (when in lobby screen, not an actual game)
	 */
	public void createNewPendingMultiPlayerGame() {
		multiPlayerLobbyScreen.addNames();

		lobby = new Lobby();
		final ServerInstance server = new ServerInstance(lobby);
		final ClientInstance client = new ClientInstance(this, name, "localhost");

		onGameClosing.addOneTimeListener(() -> {
			server.stop();
			client.stop();
		});
		
		gameSettingsScreen.getGameSettingsChangedEvent().addListener(server);

		multiPlayerLobbyScreen.getUserLeavingLobbyEvent().addOneTimeListener(() -> {
			client.stop();
			server.stop();
			multiPlayerLobbyScreen.getHostStartingGameListener().removeListener(server);
			multiPlayerLobbyScreen.getCountDownStartingListener().removeListener(server);
			gameCommandService.getRemoteGameCreatedEvent().removeListener(client);
			gameCommandService.getLocalGameCreatedEvent().removeListener(server);

			client.getMultiplayerGameStartingEvent().removeListener(gameCommandService);
			server.getMultiplayerGameStartingEvent().removeListener(gameCommandService);
		});
		
		multiPlayerLobbyScreen.getHostStartingGameListener().addOneTimeListener(server);
		multiPlayerLobbyScreen.getCountDownStartingListener().addOneTimeListener(server);
		multiPlayerLobbyScreen.setStartGameEnabled(true);
		gameCommandService.getRemoteGameCreatedEvent().addOneTimeListener(client);
		gameCommandService.getLocalGameCreatedEvent().addOneTimeListener(server);

		client.getMultiplayerGameStartingEvent().addOneTimeListener(gameCommandService);
		server.getMultiplayerGameStartingEvent().addOneTimeListener(gameCommandService);

		server.run();
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		client.run();
	}

	/**
	 *  Sets up client joining a game
	 * @param gameIp
	 */
	public void joinGame(final String gameIp) {
		final ClientInstance client = new ClientInstance(this, name, gameIp);

		onGameClosing.addOneTimeListener(() -> {
			client.stop();
		});
		
		multiPlayerLobbyScreen.getUserLeavingLobbyEvent().addOneTimeListener(() -> client.stop());
		multiPlayerLobbyScreen.setStartGameEnabled(false);
		gameCommandService.getRemoteGameCreatedEvent().addOneTimeListener(client);
		client.getMultiplayerGameStartingEvent().addOneTimeListener(gameCommandService);

		client.run();
	}

	/**
	 * Mutes music
	 * @param bool
	 */
	public void muteMusic(final boolean bool) {
		settingsScreenGame.selectMusic(bool);
		settingsScreenMenu.selectMusic(bool);
		music.setOn(bool);
	}

	/**
	 * Mutes sound effects
	 * @param bool
	 */
	public void muteSounds(final boolean bool) {
		settingsScreenGame.selectSounds(bool);
		settingsScreenMenu.selectSounds(bool);
		sounds.setOn(bool);
	}

	/**
	 * Deals with players joing and leaving the lobby
	 */
	@Override
	public void onLobbyStateChanged(final LobbyChangedEventArgs args) {
		if (args instanceof LobbyChangedEventArgs.LobbyPlayerLeftEventArgs) {
			multiPlayerLobbyScreen.list
					.removePlayer(((LobbyChangedEventArgs.LobbyPlayerLeftEventArgs) args).getPlayerID());
		} else if (args instanceof LobbyChangedEventArgs.LobbyPlayerJoinedEventArgs) {
			multiPlayerLobbyScreen.list
					.addPlayer(((LobbyChangedEventArgs.LobbyPlayerJoinedEventArgs) args).getPlayerInfo());
		} else {
			// TODO: update rules display
		}
	}

	/**
	 * Sets the lobby for multiplayer game
	 */
	public void setLobby(final Lobby lobby) {
		this.lobby = lobby;
		lobby.getLobbyStateChangedEvent().addListener(this);
	}

	/**
	 * Deals with new game created
	 */
	@Override
	public void onGameCreated(final GameCreatedEventArgs args) {
		if (args.getGame().getGameType() != GameType.MULTIPLAYER_SERVER) {
			Platform.runLater(() -> {
				final Render render = new Render(this, args.getGame(), args.getGameLogic());

				args.getGameLogic().getOnGameEnded().addOneTimeListener((GameEndedListener) music);
				args.getGameLogic().getOnGameEnded().addOneTimeListener((GameEndedListener) sounds);
				render.getOnStartingSingleplayerGame().addOneTimeListener(gameCommandService);
				
				// Initialize Screen dimensions
				PositionVisualisation.initScreenDimensions();
		        
				// Draw Map
				gameScene = render.setupWorld();
				thisStage.setScene(gameScene);
				thisStage.show();
				adjustScreenPosition();

				// Add CLick Listener
				render.addClickListener();

				// Start Timeline
				render.startTimeline();
				
				render.getOnPlayerLeavingGame().addOneTimeListener(this);
			});
		}
	}
	
	public void fireLaser(){
		sounds.fireLasers();
	}
	
	public void playShield(){
		sounds.playShield();
	}
	
	public void pausePlay(){
		music.pausePlay();
	}
	
	public void stopMusic(){
		music.stopMusic();
	}
	
	public void timerEnded(){
		if(multiPlayerLobbyScreen.thisClient){
			multiPlayerLobbyScreen.getHostStartingGameListener().fire(null);
			multiPlayerLobbyScreen.thisClient = false;
		}
	}

	/**
	 * Count down for starting a multiplayer game
	 */
	public void timer(){
		Platform.runLater(() -> {
		centerPane.getChildren().add(timerLabel);
		 if(timeline != null)
             timeline.stop();

         timeSeconds.set(STARTTIME);
         timeline = new Timeline();

         KeyValue keyValue = new KeyValue(timeSeconds, 1);
         KeyFrame keyFrame = new KeyFrame(Duration.seconds(STARTTIME + 1), keyValue);

         timeline.getKeyFrames().add(keyFrame);
         timeline.playFromStart();
         
         timeline.setOnFinished(e -> timerEnded());
		});
	}

	/**
	 * fires event for player leaving a game
	 */
	@Override
	public void onPlayerLeavingGame() {
		this.getOnPlayerLeavingGame().fire(null);
	}

	/**
	 * Set game settings for whether playing against AI or not
	 */
	@Override
	public void setAIPlayer(boolean ai) {
		multiPlayerLobbyScreen.getMultiplayerSettings().setAIPlayer(ai);
	}

	/**
	 * Set game settings for number of lives
	 */
	@Override
	public void setLives(int lives) {
		multiPlayerLobbyScreen.getMultiplayerSettings().setInitialPlayerLives(lives);		
	}
}