package main.java.graphics;

import java.util.ArrayList;
import java.util.HashMap;

import javafx.animation.KeyFrame;
import javafx.animation.RotateTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import main.java.constants.GameOutcome;
import main.java.constants.GameOutcomeType;
import main.java.constants.ScreenSize;
import main.java.event.arguments.GameDisplayInvalidatedEventArgs;
import main.java.event.arguments.GameEndedEventArgs;
import main.java.event.listener.GameDisplayInvalidatedListener;
import main.java.event.listener.GameEndedListener;
import main.java.gamelogic.core.GameLogic;
import main.java.gamelogic.domain.Cell;
import main.java.gamelogic.domain.ControlledPlayer;
import main.java.gamelogic.domain.Game;
import main.java.gamelogic.domain.Ghost;
import main.java.gamelogic.domain.Player;
import main.java.ui.GameUI;

/**
 * Created by Boyan Bonev on 09/02/2017.
 */
public class Render implements GameDisplayInvalidatedListener, GameEndedListener {
	private Pane root;
	private Timeline timeLine;
	private ControlledPlayer controlledPlayer;
	private GameUI gameUI;
	private Game game;
	private GameLogic gameLogic;
	private HashMap<Integer, Node> allEntities;
	private Node[][] worldNodes;
	private HashMap<Integer, RotateTransition> rotations;
	private HashMap<Integer, TranslateTransition> transitions;
	private boolean flag;

	/**
	 * Initialize new visualisation of the map
	 *
	 * @param gameUI
	 * @param game
	 */
	public Render(final GameUI gameUI, final Game game, final GameLogic gameLogic) {
		this.gameUI = gameUI;
		this.game = game;
		this.gameLogic = gameLogic;
		this.gameLogic.getOnGameDisplayInvalidated().addListener(this);
		this.gameLogic.getOnGameEnded().addListener(this);
		flag = false;

		this.transitions = new HashMap<>();
		this.rotations = new HashMap<>();
		this.allEntities = new HashMap<>();
		controlledPlayer = game.getPlayer();
	}

	/**
	 * Generate the map
	 *
	 * @return the stage that contians the scene with the map
	 */
	public Scene setupWorld() {
		final Cell[][] cells = game.getWorld().getMap().getCells();
		int rows = game.getWorld().getMap().getMapSize(), columns = rows;
		worldNodes = new Node[rows][columns];

		root = new Pane();
		root.setStyle("-fx-background-color: black");

		final Scene scene = new Scene(root, ScreenSize.Width, ScreenSize.Height);
		redrawWorld();

		return scene;
	}

	private void setupGhostAnimation(final Ghost ghost) {
		Node ghostNode = new GhostVisualisation(ghost.getPosition()).getNode();
		TranslateTransition transitionGhost = new TranslateTransition(Duration.millis(140), ghostNode);

		transitions.put(ghost.getID(), transitionGhost);

		allEntities.put(ghost.getID(), ghostNode);

		root.getChildren().add(ghostNode);
	}

	private void setupPlayerAnimation(final Player player) {
		Node playerNode = new PacmanVisualisation(player).getNode();
		TranslateTransition transitionPlayer = new TranslateTransition(Duration.millis(140), playerNode);
		RotateTransition rotatePlayer = new RotateTransition(Duration.millis(30), playerNode);

		transitions.put(player.getID(), transitionPlayer);
		rotations.put(player.getID(), rotatePlayer);

		allEntities.put(player.getID(), playerNode);
		root.getChildren().add(playerNode);
	}

	/**
	 * Redraw the map
	 */
	public void redrawWorld() {
		PositionVisualisation.initScreenDimensions();

    	redrawCells();

		for (final Player player : game.getWorld().getPlayers()) {
		    ImageView nextNode = new PacmanVisualisation(player).getNode();
		    
		    if(!allEntities.containsKey(player.getID()))
		    	setupPlayerAnimation(player);

            transitions.get(player.getID()).setToY(nextNode.getTranslateY());
            transitions.get(player.getID()).setToX(nextNode.getTranslateX());
            rotations.get(player.getID()).setToAngle(nextNode.getRotate());

            root.getChildren().get(root.getChildren().indexOf(allEntities.get(player.getID()))).toFront();
            rotations.get(player.getID()).play();
            transitions.get(player.getID()).play();
        }

		for (final Ghost ghost : game.getWorld().getGhosts()) {
		    ImageView nextNode = new GhostVisualisation(ghost.getPosition()).getNode();
		    
		    if(!allEntities.containsKey(ghost.getID())) {
		    	setupGhostAnimation(ghost);
		    }

		    transitions.get(ghost.getID()).setToY(nextNode.getTranslateY());
            transitions.get(ghost.getID()).setToX(nextNode.getTranslateX());

            root.getChildren().get(root.getChildren().indexOf(allEntities.get(ghost.getID()))).toFront();

            transitions.get(ghost.getID()).play();
		}

		root.requestFocus();
	}

	
	// only redraw cells that have changed state
	private void redrawCells() {
		final Cell[][] cells = game.getWorld().getMap().getCells();
		int rows = game.getWorld().getMap().getMapSize(), columns = rows;
		
		for(int row = 0; row < rows; row++) {
			for(int column = 0; column < columns; column++) {
				boolean firstDraw = worldNodes[row][column] == null;
				if(firstDraw || cells[row][column].needsRedraw()) {
					if(!firstDraw) root.getChildren().remove(worldNodes[row][column]);
					Node cv = new CellVisualisation(cells[row][column]).getNode();
					worldNodes[row][column] = cv;
					root.getChildren().add(cv);
					cells[row][column].clearNeedsRedrawFlag();
				}
			}
		}
	}

	/**
	 *
	 * Click listener for moving the player
	 */
	public void addClickListener() {
		root.setOnKeyPressed(event -> {
			if (event.getCode() == KeyCode.UP) {
				controlledPlayer.moveUp();
				redrawWorld();
			} else if (event.getCode() == KeyCode.DOWN) {
				controlledPlayer.moveDown();
				redrawWorld();
			} else if (event.getCode() == KeyCode.LEFT) {
				controlledPlayer.moveLeft();
				redrawWorld();
			} else if (event.getCode() == KeyCode.RIGHT) {
				controlledPlayer.moveRight();
				redrawWorld();
			}
		});
	}

	/**
	 * Start the timeline
	 */
	public void startTimeline() {
		timeLine = new Timeline(new KeyFrame(Duration.millis(200), event -> {
			gameLogic.gameStep(200);
		}));
		timeLine.setCycleCount(Timeline.INDEFINITE);
		timeLine.play();
	}

	/**
	 * Add all nodes to parent root
	 *
	 * @param cells
	 */
	private void addToRoot(final Cell[][] cells) {
		for (final Cell[] cell : cells) {
			for (final Cell c : cell) {
				final Node cv = new CellVisualisation(c).getNode();
				root.getChildren().add(cv);
			}
		}
	}
	
	private String getGameOutcomeText(final GameOutcome gameOutcome) {
		switch(gameOutcome.getOutcomeType()) {
		case GHOSTS_WON:
			return "Damn! The ghosts won this time...";
		case PLAYER_WON:
			if(gameOutcome.getWinner().getID() == game.getPlayer().getID()) {
				return "Wohoo, you won!";
			} else {
				return "Damn, " + gameOutcome.getWinner().getName() + " won this time!";
			}
		case TIE:
			return "No one won. Stop being bad at the game";
		default:
			return "A " + gameOutcome.getOutcomeType().name() + " happened";
		}
	}

	private StackPane gameEndedWindow(final GameOutcome gameOutcome) {
		final StackPane pane = new StackPane();
		pane.setStyle("-fx-background-color: rgba(0, 100, 100, 0.6)");
		pane.setPrefSize(ScreenSize.Width, ScreenSize.Height);

		final Label outcomneLabel = new Label(getGameOutcomeText(gameOutcome));
		outcomneLabel.setStyle(
				"-fx-text-fill: goldenrod; -fx-font: bold 30 \"serif\"; -fx-padding: 20 0 0 0; -fx-text-alignment: center");

		final Label escLable = new Label("* Press ESC to go back at the menu");
		escLable.setStyle(
				"-fx-text-fill: goldenrod; -fx-font: bold 20 \"serif\"; -fx-padding: 0 0 0 0; -fx-text-alignment: center");

		final Label spaceLabel = new Label("* Press SPACE to reply");
		spaceLabel.setStyle(
				"-fx-text-fill: goldenrod; -fx-font: bold 20 \"serif\"; -fx-padding: 50 103 0 0; -fx-text-alignment: center");
		StackPane.setAlignment(outcomneLabel, Pos.TOP_CENTER);
		StackPane.setAlignment(escLable, Pos.CENTER);
		StackPane.setAlignment(spaceLabel, Pos.CENTER);

		pane.getChildren().addAll(outcomneLabel, escLable, spaceLabel);
		return pane;
	}

	/**
	 * End the game
	 */
	private void gameEnded(final GameOutcome gameOutcome) {
		timeLine.stop();

		root.getChildren().add(gameEndedWindow(gameOutcome));
		root.setOnKeyPressed(e -> {
			if (e.getCode() == KeyCode.SPACE) {
				addClickListener();
				startTimeline();
				redrawWorld();
			} else if (e.getCode() == KeyCode.ESCAPE) {
				gameUI.switchToMenu();
			}
		});
	}

	@Override
	public void onGameDisplayInvalidated(final GameDisplayInvalidatedEventArgs args) {
		if (!game.hasEnded()) {
			Platform.runLater(this::redrawWorld);
		}
	}

	@Override
	public void onGameEnded(final GameEndedEventArgs args) {
		gameEnded(args.getOutcome());
	}
}