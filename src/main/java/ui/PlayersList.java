package main.java.ui;

import java.util.HashMap;

import javafx.application.Platform;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import main.java.gamelogic.core.LobbyPlayerInfo;

/**
 * Screen to be added to the multiplayer lobby to list all current players in
 * the game
 *
 * @author Rose Kirtley
 *
 */
public class PlayersList {
	private HashMap<Integer, Label> playerLabels;
	private FlowPane pane;

	public PlayersList(final GameUI game) {

		pane = new FlowPane();
		pane.setPadding(new Insets(5, 0, 5, 0));
		pane.setVgap(4);
		pane.setHgap(4);
		pane.setColumnHalignment(HPos.CENTER);
		pane.setOrientation(Orientation.VERTICAL);
		pane.getStyleClass().add("paneStyle");
		pane.setAlignment(Pos.TOP_CENTER);

		playerLabels = new HashMap<>();
	}

	public void addPlayer(final LobbyPlayerInfo player) {
		Platform.runLater(() -> {
			final Label label = new Label(player.getName());
			label.getStyleClass().add("labelStyle");
			pane.getChildren().add(label);
			playerLabels.put(player.getID(), label);
		});
	}

	public void removePlayer(final int playerID) {
		Platform.runLater(() -> {
			if (playerLabels.containsKey(playerID)) {
				final Label label = playerLabels.remove(playerID);
				pane.getChildren().remove(label);
			}
		});
	}

	public void clear() {
		for (final int i : playerLabels.keySet()) {
			removePlayer(i);
		}
	}
	
	public boolean contains(final int playerID){
		return playerLabels.containsKey(playerID);
	}

	public Pane getPane() {
		return pane;
	}
}
