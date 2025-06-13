package gamestates;

import audio.AudioPlayer;
import main.Game;

public class State {

	protected Game game;

	public State(Game game) {
		this.game = game;
	}


	public Game getGame() {
		return game;
	}

	@SuppressWarnings("incomplete-switch")
	public void setGamestate(Gamestate state) {
		switch (state) {
		case MENU -> game.getAudioPlayer().playSong(AudioPlayer.MENU_1);
		case PLAYING -> game.getAudioPlayer().setLevelSong(game.getPlaying().getLevelManager().getLevelIndex());
		}

		Gamestate.state = state;
	}

}