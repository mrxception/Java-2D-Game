package audio;

import java.io.IOException;
import java.net.URL;
import java.util.Random;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.BooleanControl;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public class AudioPlayer {

	public static int MENU_1 = 0;
	public static int LEVEL_1 = 1;
	public static int LEVEL_2 = 2;
	public static int LEVEL_3 = 3;

	public static int STORY = 4;

	public static int DIE = 0;
	public static int JUMP = 1;
	public static int GAMEOVER = 2;
	public static int LVL_COMPLETED = 3;
	public static int ATTACK_ONE = 4;
	public static int ATTACK_TWO = 5;
	public static int ATTACK_THREE = 6;
	public static int HIT = 7;

	public static int SKULL_FIRE = 8;

	public static int MESSAGE_EFFECT = 9;
	public static int AURA_EFFECT = 10;

	public static final int RAIN_EFFECT = 11;
	public static final int STEALTH_EFFECT = 12;

	public static final int THUNDER_EFFECT1 = 13;
	public static final int THUNDER_EFFECT2 = 14;
	public static final int THUNDER_EFFECT3 = 15;

	public static final int LEVEL_UP = 16;

	public static final int SLORACK_DIE = 17;
	public static final int SLORACK_HIT = 18;
	public static final int SLORACK_ATTACK = 19;

	public static final int VELGORN_DIE = 20;
	public static final int VELGORN_HIT = 21;
	public static final int VELGORN_ATTACK = 22;

	public static final int RADOBAAN_DIE = 20;
	public static final int RADOBAAN_HIT = 21;
	public static final int RADOBAAN_ATTACK = 19;

	private Clip[] songs, effects;
	private int currentSongId;
	private float volume = 0.5f;
	private boolean songMute, effectMute;
	private Random rand = new Random();

	public AudioPlayer() {
		loadSongs();
		loadEffects();
		playSong(MENU_1);
	}

	private void loadSongs() {
		String[] names = { "menu", "level1", "level2", "level3", "story" };
		songs = new Clip[names.length];
		for (int i = 0; i < songs.length; i++)
			songs[i] = getClip(names[i]);
	}

	private void loadEffects() {
		String[] effectNames = { "die", "jump", "gameover", "lvlcompleted", "attack1", "attack2", "attack3", "hit",
				"skull_fire", "message_effect", "aura_effect" , "rain", "stealth", "thunder1", "thunder2", "thunder3", "level_up",
				"slorack_die", "slorack_hit", "slorack_attack", "velgorn_die", "velgorn_hit", "velgorn_attack"};
		effects = new Clip[effectNames.length];
		for (int i = 0; i < effects.length; i++)
			effects[i] = getClip(effectNames[i]);

		updateEffectsVolume();

	}

	public void playThunder() {
		int start = 13;
		start += rand.nextInt(3);
		playEffect(start);
	}


	private Clip getClip(String name) {
		URL url = getClass().getResource("/audio/" + name + ".wav");
		AudioInputStream audio;

		try {
			audio = AudioSystem.getAudioInputStream(url);
			Clip c = AudioSystem.getClip();
			c.open(audio);
			return c;

		} catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {

			e.printStackTrace();
		}

		return null;

	}

	public void setVolume(float volume) {
		this.volume = volume;
		updateSongVolume();
		updateEffectsVolume();
	}

	public void stopSong() {
		if (songs[currentSongId].isActive())
			songs[currentSongId].stop();
	}

	public void setLevelSong(int lvlIndex) {
		if (lvlIndex == 0)
			playSong(LEVEL_1);
		else if (lvlIndex == 1)
			playSong(LEVEL_2);
		else if (lvlIndex == 2)
			playSong(LEVEL_3);
	}

	public void lvlCompleted() {
		stopSong();
		playEffect(LVL_COMPLETED);
	}

	public void playAttackSound() {
		int start = 4;
		start += rand.nextInt(3);
		playEffect(start);
	}

	public void playEffect(int effect) {
		effects[effect].stop();
		effects[effect].setMicrosecondPosition(0);

		effects[effect].start();
	}

	public void playEffect(int effect, boolean loop) {
		effects[effect].stop();
		effects[effect].setMicrosecondPosition(0);
		effects[effect].loop(Clip.LOOP_CONTINUOUSLY);
		effects[effect].start();
	}

	public void playSong(int song) {
		stopSong();

		currentSongId = song;
		updateSongVolume();
		songs[currentSongId].setMicrosecondPosition(0);
		songs[currentSongId].loop(Clip.LOOP_CONTINUOUSLY);
	}

	public void toggleSongMute() {
		this.songMute = !songMute;
		for (Clip c : songs) {
			BooleanControl booleanControl = (BooleanControl) c.getControl(BooleanControl.Type.MUTE);
			booleanControl.setValue(songMute);
		}
	}

	public void toggleEffectMute() {
		this.effectMute = !effectMute;
		for (Clip c : effects) {
			BooleanControl booleanControl = (BooleanControl) c.getControl(BooleanControl.Type.MUTE);
			booleanControl.setValue(effectMute);
		}
		if (!effectMute)
			playEffect(JUMP);
	}

	private void updateSongVolume() {

		FloatControl gainControl = (FloatControl) songs[currentSongId].getControl(FloatControl.Type.MASTER_GAIN);
		float range = gainControl.getMaximum() - gainControl.getMinimum();
		float gain = (range * volume) + gainControl.getMinimum();
		gainControl.setValue(gain);

	}

	private void updateEffectsVolume() {
		for (Clip c : effects) {
			FloatControl gainControl = (FloatControl) c.getControl(FloatControl.Type.MASTER_GAIN);
			float range = gainControl.getMaximum() - gainControl.getMinimum();
			float gain = (range * volume) + gainControl.getMinimum();
			gainControl.setValue(gain);
		}
	}

	public void stopEffect(int effect) {
		if (effects[effect].isRunning()) {
			effects[effect].stop();
		}
	}

	public void continueEffect(int effect) {
		if (!effects[effect].isRunning() && !effectMute) {
			effects[effect].start();
		}
	}

}
