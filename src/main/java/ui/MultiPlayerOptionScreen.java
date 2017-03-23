package main.java.ui;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Tooltip;

/**
 * Screen for choosing whether to join a multiplayer game or start a new
 * multiplayer game
 *
 * @author Rose Kirtley
 *
 */
public class MultiPlayerOptionScreen extends Screen {

	private Button joinGame;
	private Button startGame;
	private Button back;
	private Label title;

	public MultiPlayerOptionScreen(final GameUI game) {
		super(game);

		title = new Label("Mutliplayer Game Options");
		title.getStyleClass().add("miniTitleStyle");

		joinGame = new Button("Join game");
		joinGame.getStyleClass().add("buttonStyle");
		joinGame.setTooltip(new Tooltip("Join a game"));
		joinGame.setOnAction(e -> game.switchToMultiPlayerJoin());
		setUpHover(joinGame);

		startGame = new Button("Start a new game");
		startGame.getStyleClass().add("buttonStyle");
		startGame.setTooltip(new Tooltip("Initiate a new game"));
		startGame.setOnAction(e -> startNewGame());
		setUpHover(startGame);

		back = new Button("Back");
		back.getStyleClass().add("backButtonStyle");
		back.setOnAction(e -> game.switchToMenu());

		final Separator separator = new Separator();
		separator.getStyleClass().add("separator");

		pane.getChildren().addAll(title, separator, joinGame, startGame, back);
	}

	private void startNewGame() {
		game.createNewPendingMultiPlayerGame();
		game.switchToMultiPlayerLobby();
	}

	@Override
	public void changeSelection(final boolean up) {
		if (up) {
			if (joinGame.isDefaultButton()) {
				selectBack(back);
				unselect(joinGame);
				unselect(startGame);
			} else if (startGame.isDefaultButton()) {
				select(joinGame);
				unselect(startGame);
				unselectBack(back);
			} else {
				select(startGame);
				unselectBack(back);
				unselect(joinGame);
			}
		} else {
			if (joinGame.isDefaultButton()) {
				select(startGame);
				unselect(joinGame);
				unselectBack(back);
			} else if (startGame.isDefaultButton()) {
				selectBack(back);
				unselect(startGame);
				unselect(joinGame);
			} else {
				select(joinGame);
				unselectBack(back);
				unselect(startGame);
			}
		}

	}

	@Override
	public void makeSelection() {
		if (joinGame.isDefaultButton()) {
			game.switchToMultiPlayerJoin();
		} else if (startGame.isDefaultButton()) {
			startNewGame();
		} else {
			game.switchToMenu();
		}
	}

	@Override
	public void unselectAll() {
		reset(startGame);
		reset(joinGame);
		resetBack(back);
	}
}
