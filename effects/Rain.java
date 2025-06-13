package effects;

import java.awt.Graphics;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.Random;

import audio.AudioPlayer;
import gamestates.Gamestate;
import gamestates.Playing;
import main.Game;
import utilz.LoadSave;

public class Rain {

	private Point2D.Float[] drops;
	private Random rand;
	private float rainSpeed = 1.25f;
	private BufferedImage rainParticle;
	private boolean rainSoundPlaying = false;
	private Playing playing;
	private long lastStateChangeTime = 0;
	private static final long STATE_CHANGE_DELAY = 300;

	public Rain(Playing playing) {
		rand = new Random();
		drops = new Point2D.Float[1000];
		rainParticle = LoadSave.GetSpriteAtlas(LoadSave.RAIN_PARTICLE);
		this.playing = playing;
		initDrops();
	}

	public void startRainSound() {
		if (!rainSoundPlaying) {
			playing.getGame().getAudioPlayer().playEffect(AudioPlayer.RAIN_EFFECT, true);
			rainSoundPlaying = true;
			lastStateChangeTime = System.currentTimeMillis();
		}
	}

	public void stopRainSound() {
		if (rainSoundPlaying) {
			playing.getGame().getAudioPlayer().stopEffect(AudioPlayer.RAIN_EFFECT);
			rainSoundPlaying = false;
			lastStateChangeTime = System.currentTimeMillis();
		}
	}

	private void initDrops() {
		for (int i = 0; i < drops.length; i++)
			drops[i] = getRndPos();
	}

	private Point2D.Float getRndPos() {
		return new Point2D.Float((int) getNewX(0), rand.nextInt(Game.GAME_HEIGHT));
	}

	public void update(int xLvlOffset) {
		if (System.currentTimeMillis() - lastStateChangeTime < STATE_CHANGE_DELAY) {
			updateDropPositions(xLvlOffset);
			return;
		}

		boolean shouldBePlaying = shouldPlayRainSound();

		if (shouldBePlaying && !rainSoundPlaying) {
			startRainSound();
		}
		else if (!shouldBePlaying && rainSoundPlaying) {
			stopRainSound();
		}

		updateDropPositions(xLvlOffset);
	}

	private boolean shouldPlayRainSound() {
		return Gamestate.state == Gamestate.PLAYING
				&& !playing.isPaused()
				&& !playing.isGameOver()
				&& !playing.isLevelCompleted()
				&& !playing.isGameCompleted()
				&& !playing.isPlayerDying();
	}

	private void updateDropPositions(int xLvlOffset) {
		for (Point2D.Float p : drops) {
			p.y += rainSpeed;
			if (p.y >= Game.GAME_HEIGHT) {
				p.y = -20;
				p.x = getNewX(xLvlOffset);
			}
		}
	}

	private float getNewX(int xLvlOffset) {
		float value = (-Game.GAME_WIDTH) + rand.nextInt((int) (Game.GAME_WIDTH * 3f)) + xLvlOffset;
		return value;
	}

	public void draw(Graphics g, int xLvlOffset) {
		for (Point2D.Float p : drops)
			g.drawImage(rainParticle, (int) p.getX() - xLvlOffset, (int) p.getY(), 3, 12, null);
	}
}