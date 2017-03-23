package main.java.ui;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import main.java.event.Event;
import main.java.event.arguments.UserLoggedInEventArgs;
import main.java.event.listener.UserLoggedInEventListener;

/**
 * Screen for logging in
 *
 * @author Rose Kirtley
 *
 */
public class LogInScreen extends Screen {

	private Button login;
	private Label label;
	private Label title;
	private TextField text;
	private Event<UserLoggedInEventListener, UserLoggedInEventArgs> userLogIn = new Event<>(
			(listener, arg) -> listener.onUserLoggedIn(arg));

	public LogInScreen(final GameUI game) {
		super(game);

		title = new Label("Welcome to PacMan++");
		title.getStyleClass().add("titleStyle");

		label = new Label("Enter Username:");
		label.getStyleClass().add("labelStyle");

		text = new TextField();
		text.getStyleClass().add("labelStyle");

		login = new Button("Log in");
		login.getStyleClass().add("buttonStyle");
		login.setOnAction(e -> switchScreen(text.getText()));
		select(login);

		text.setAlignment(Pos.CENTER);

		pane.getChildren().addAll(title, label, text, login);
	}

	private void switchScreen(final String username) {
		if (!username.isEmpty()) {
			game.setName(username);
			userLogIn.fire(new UserLoggedInEventArgs(username));
			game.switchToMenu();
		}
	}

	@Override
	public void changeSelection(final boolean up) {
		// Do nothing only one button
	}

	@Override
	public void makeSelection() {
		switchScreen(text.getText());
	}

	@Override
	public void unselectAll() {
		// do nothing only one button
	}
}
