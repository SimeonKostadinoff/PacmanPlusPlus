package main.java.ui;

import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;

/**
 * Screen for entering an IP address to join a pre-existing game
 *
 * @author Rose Kirtley
 *
 */
public class MultiPlayerJoinScreen extends Screen {

	private Button join;
	private Button back;
	private TextField ip;
	private Label label;
	private Label title;

	public MultiPlayerJoinScreen(final GameUI game) {
		super(game);

		title = new Label("Joining a Multiplayer Game");
		title.getStyleClass().add("miniTitleStyle");

		join = new Button("Join game");
		join.getStyleClass().add("buttonStyle");
		join.setOnAction(e -> joinGame(ip.getText()));
		join.setDefaultButton(true);
		setUpHover(join);

		ip = new TextField();
		ip.getStyleClass().add("labelStyle");
		ip.setAlignment(Pos.CENTER);

		label = new Label("IP address: ");
		label.getStyleClass().add("labelStyle");

		back = new Button("Back");
		back.getStyleClass().add("backButtonStyle");
		back.setOnAction(e -> game.switchToMultiPlayerOption());
		setUpHover(back);

		final Separator separator = new Separator();
		separator.getStyleClass().add("separator");

		pane.getChildren().addAll(title, separator, label, ip, join, back);
	}
	
	/**
	 * Try to join a game with the given inputed IP adress
	 * @param text
	 */
	private void joinGame(final String text) {
		if (text.isEmpty()) {
			return;
		}
		try {
			game.joinGame(text);
			game.switchToMultiPlayerLobby();
		} catch (final RuntimeException ex) {
			final Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("IP Address Issue");
			alert.setHeaderText("The IP Address you have entered is not valid");
			alert.setContentText("Please enter an IP Address which you know has started a game");

			alert.showAndWait();
		}
	}

	@Override
	public void changeSelection(final boolean up) {
		if (join.isDefaultButton()) {
			unselect(join);
			selectBack(back);
		} else {
			select(join);
			unselectBack(back);
		}
	}

	@Override
	public void makeSelection() {
		if (join.isDefaultButton()) {
			joinGame(ip.getText());
		} else {
			game.switchToMultiPlayerOption();
		}
	}

	@Override
	public void unselectAll() {
		reset(join);
		resetBack(back);
	}
}
