package main.java.event.arguments;

import java.util.Map;

import main.java.constants.GameSetting;

public class GameSettingsChangedEventArgs {

	private Map<GameSetting, Boolean> newGameSettings;

	// TODO: replace Boolean with something more abstract
	public GameSettingsChangedEventArgs(final Map<GameSetting, Boolean> newGameSettings) {
		this.newGameSettings = newGameSettings;
	}

	public Map<GameSetting, Boolean> getNewGameSettings() {
		return newGameSettings;
	}
}