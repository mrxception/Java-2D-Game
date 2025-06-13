package gamestates;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;
import java.util.ArrayList;

import audio.AudioPlayer;
import effects.*;
import entities.Enemy;
import entities.EnemyManager;
import entities.Player;
import levels.LevelManager;
import main.Game;
import objects.ObjectManager;
import ui.GameCompletedOverlay;
import ui.GameOverOverlay;
import ui.LevelCompletedOverlay;
import ui.PauseOverlay;
import utilz.LoadSave;

import static utilz.Constants.Environment.*;
import static utilz.Constants.Dialogue.*;

public class Playing extends State implements Statemethods {
	private ShootingStarManager shootingStarManager;

	private Player player;
	private LevelManager levelManager;
	private EnemyManager enemyManager;
	private ObjectManager objectManager;
	private PauseOverlay pauseOverlay;
	private GameOverOverlay gameOverOverlay;
	private GameCompletedOverlay gameCompletedOverlay;
	private LevelCompletedOverlay levelCompletedOverlay;
	private Rain rain;

	private boolean paused = false;

	private int xLvlOffset;
	private int leftBorder = (int) (0.25 * Game.GAME_WIDTH);
	private int rightBorder = (int) (0.75 * Game.GAME_WIDTH);
	private int maxLvlOffsetX;

	private BufferedImage backgroundImg, bigCloud, smallCloud;
	private BufferedImage[] questionImgs, exclamationImgs;
	private ArrayList<DialogueEffect> dialogEffects = new ArrayList<>();

	private int[] smallCloudsPos;
	private Random rnd = new Random();

	private boolean gameOver;
	private boolean lvlCompleted;
	private boolean gameCompleted;
	private boolean playerDying;
	private boolean drawRain;


	private BufferedImage skillBoxImg;
	private BufferedImage[] skillIcons;
	private boolean[] skillCooldowns = new boolean[4];
	private int[] skillCooldownTimers = new int[4];
	private final int SKILL_BOX_WIDTH = (int) (50 * Game.SCALE);
	private final int SKILL_BOX_HEIGHT = (int) (50 * Game.SCALE);
	private final int SKILL_BOX_Y = (int) (Game.GAME_HEIGHT - SKILL_BOX_HEIGHT - 10);

	private boolean showBorderEffect = false;
	private int borderEffectAlpha = 0;
	private int borderEffectMaxAlpha = 180;
	private int borderEffectTarget = 0;
	private Color borderEffectColor = new Color(255, 0, 0);
	private int borderWidth = 150;
	private boolean pulsatingEffect = false;
	private int pulsateTimer = 0;


	private long levelStartTime;
	private long currentTime;
	private boolean timerRunning = false;
	private final Font LEVEL_FONT = new Font("Arial", Font.BOLD, 20);
	private final Color LEVEL_TEXT_COLOR = Color.WHITE;
	private final Color LEVEL_TEXT_SHADOW = new Color(0, 0, 0, 150);

	private boolean fadeInTransition = false;
	private int fadeInAlpha = 255;
	private final int FADE_IN_SPEED = 1;

	private Lightning lightning;

	private long lastFrameTime;
	private float deltaTime;

	private Stars starBackground;

	private boolean showMonsterRank = false;
	private int monsterRankAlpha = 255;
	private final int MONSTER_RANK_FADE_SPEED = 1;
	private final int MONSTER_RANK_DISPLAY_TIME = 3000; 
	private long monsterRankDisplayStartTime;
	private String monsterName = "";
	private final Font MONSTER_RANK_FONT = new Font("Arial", Font.BOLD, 36);
	private final Color MONSTER_RANK_COLOR = new Color(255, 215, 0); 
	private final Color MONSTER_RANK_SHADOW = new Color(0, 0, 0, 200);

	private boolean showLevelUpEffect = false;
	private int levelUpEffectTime = 0;
	private final int LEVEL_UP_EFFECT_DURATION = 5000;
	private int playerLevel = 1;
	private float levelUpEffectScale = 0.1f;
	private final float LEVEL_UP_TARGET_SCALE = 1.0f;
	private final float LEVEL_UP_SCALE_SPEED = 0.05f;
	private int levelUpEffectAlpha = 0;
	private final int LEVEL_UP_FADE_SPEED = 5;
	private int levelUpBoxX = 0;
	private int levelUpEffectSpawnX;
	private int levelUpEffectSpawnY;
	private Random levelUpRandom = new Random();

	public Playing(Game game) {
		super(game);
		lastFrameTime = System.currentTimeMillis();

		initClasses();

		backgroundImg = LoadSave.GetSpriteAtlas(LoadSave.PLAYING_BG_IMG);
		bigCloud = LoadSave.GetSpriteAtlas(LoadSave.BIG_CLOUDS);
		smallCloud = LoadSave.GetSpriteAtlas(LoadSave.SMALL_CLOUDS);
		smallCloudsPos = new int[8];
		for (int i = 0; i < smallCloudsPos.length; i++)
			smallCloudsPos[i] = (int) (90 * Game.SCALE) + rnd.nextInt((int) (100 * Game.SCALE));

		loadDialogue();
		loadSkillUI();
		calcLvlOffset();
		loadStartLevel();
		setDrawRainBoolean();

		
	}

	
	private void showMonsterRankForCurrentLevel() {
		int currentLevel = levelManager.getLevelIndex();

		
		switch(currentLevel) {
			case 0:
				monsterName = "Rank B - Slorack";
				break;
			case 1:
				monsterName = "Rank A - Velgorn";
				break;
			case 2:
				monsterName = "Rank S - Hell Flame";
				break;
			default:
				monsterName = "Unknown Beast";
				break;
		}

		
		showMonsterRank = true;
		monsterRankAlpha = 255;
		monsterRankDisplayStartTime = System.currentTimeMillis();
	}

	
	private void updateMonsterRankDisplay() {
		if (showMonsterRank) {
			long currentTime = System.currentTimeMillis();
			long elapsedTime = currentTime - monsterRankDisplayStartTime;

			
			if (elapsedTime > MONSTER_RANK_DISPLAY_TIME) {
				monsterRankAlpha -= MONSTER_RANK_FADE_SPEED;
				if (monsterRankAlpha <= 0) {
					monsterRankAlpha = 0;
					showMonsterRank = false;
				}
			}
		}
	}

	
	private void drawMonsterRankDisplay(Graphics g) {
		if (showMonsterRank && monsterRankAlpha > 0) {
			Font originalFont = g.getFont();

			
			Font rankFont;
			try {
				rankFont = Font.createFont(Font.TRUETYPE_FONT,
						new File("res/fonts/pixel_font.ttf")).deriveFont(48f);
			} catch (Exception e) {
				rankFont = MONSTER_RANK_FONT;
			}

			g.setFont(rankFont);
			FontMetrics fm = g.getFontMetrics();

			String fullText = monsterName;

			int textWidth = fm.stringWidth(fullText);
			int x = (Game.GAME_WIDTH / 2) - (textWidth / 2);
			int y = (Game.GAME_HEIGHT / 2);

			
			g.setColor(new Color(
					MONSTER_RANK_SHADOW.getRed(),
					MONSTER_RANK_SHADOW.getGreen(),
					MONSTER_RANK_SHADOW.getBlue(),
					(MONSTER_RANK_SHADOW.getAlpha() * monsterRankAlpha) / 255
			));
			g.drawString(fullText, x + 3, y + 3);

			
			g.setColor(new Color(
					MONSTER_RANK_COLOR.getRed(),
					MONSTER_RANK_COLOR.getGreen(),
					MONSTER_RANK_COLOR.getBlue(),
					(monsterRankAlpha)
			));
			g.drawString(fullText, x, y);

			
			g.setFont(originalFont);
		}
	}

	public void startFadeInTransition() {
		fadeInTransition = true;
		fadeInAlpha = 255;
	}

	public void startGame() {
		player.retryCurrentLevel();
		showMonsterRankForCurrentLevel();
		resetAll();
	}

	private void startLevelTimer() {
		levelStartTime = System.currentTimeMillis();
		timerRunning = true;
	}

	private void updateTimer() {
		if (timerRunning && !paused && !gameOver && !lvlCompleted && !gameCompleted) {
			currentTime = System.currentTimeMillis() - levelStartTime;
		}
	}

	public long getTime() {
		return currentTime;
	}

	private void resetLevelTimer() {
		levelStartTime = System.currentTimeMillis();
		currentTime = 0;
		timerRunning = true;
	}

	private String formatTime (long timeInMillis) {

		long minutes = (timeInMillis / 60000) % 60;
		long seconds = (timeInMillis / 1000) % 60;
		long millis = (timeInMillis % 1000) / 10;

		return String.format("%02d:%02d:%02d", minutes, seconds, millis);
	}

	private void drawGameInfo(Graphics g) {

		Font originalFont = g.getFont();
		Color originalColor = g.getColor();

		g.setFont(LEVEL_FONT);
		FontMetrics fm = g.getFontMetrics();

		String levelText = "ROUND " + (levelManager.getLevelIndex() + 1);
		int levelTextWidth = fm.stringWidth(levelText);
		int levelX = (Game.GAME_WIDTH / 2) - (levelTextWidth / 2);

		g.setColor(LEVEL_TEXT_SHADOW);
		g.drawString(levelText, levelX + 2, 40 + 2);

		g.setColor(LEVEL_TEXT_COLOR);
		g.drawString(levelText, levelX, 40);

		String monsterText = monsterName;
		int monsterTextWidth = fm.stringWidth(monsterText);
		int monsterX = (Game.GAME_WIDTH / 2) - (monsterTextWidth / 2);

		g.setColor(LEVEL_TEXT_SHADOW);
		g.drawString(monsterText, monsterX + 2, 70 + 2);

		g.setColor(new Color(255, 215, 0));
		g.drawString(monsterText, monsterX, 70);

		String timerText = "TIME: " + formatTime(currentTime);
		int timerTextWidth = fm.stringWidth(timerText);
		int timerX = (Game.GAME_WIDTH / 2) - (timerTextWidth / 2);

		g.setColor(LEVEL_TEXT_SHADOW);
		g.drawString(timerText, timerX + 2, 100 + 2);

		g.setColor(LEVEL_TEXT_COLOR);
		g.drawString(timerText, timerX, 100);

		g.setFont(originalFont);
		g.setColor(originalColor);
	}

	public void showBorderEffect(boolean show, Color color) {
		this.showBorderEffect = show;
		if (color != null) {
			this.borderEffectColor = color;
		}

		if (show) {
			borderEffectTarget = borderEffectMaxAlpha;
		} else {
			borderEffectTarget = 0;
		}
	}

	public void setPulsatingEffect(boolean pulsate) {
		this.pulsatingEffect = pulsate;
	}

	public void setBorderWidth(int width) {
		this.borderWidth = width;
	}


	private void updateBorderEffect() {
		if (borderEffectAlpha < borderEffectTarget) {
			borderEffectAlpha += 5;
			if (borderEffectAlpha > borderEffectTarget)
				borderEffectAlpha = borderEffectTarget;
		} else if (borderEffectAlpha > borderEffectTarget) {
			borderEffectAlpha -= 5;
			if (borderEffectAlpha < borderEffectTarget)
				borderEffectAlpha = borderEffectTarget;
		}


		if (pulsatingEffect && borderEffectAlpha > 0) {
			pulsateTimer++;
			if (pulsateTimer > 60) pulsateTimer = 0;
		}
	}

	private void loadSkillUI() {
		skillBoxImg = LoadSave.GetSpriteAtlas(LoadSave.SKILL_BOX);
		skillIcons = new BufferedImage[3];
		BufferedImage temp = LoadSave.GetSpriteAtlas(LoadSave.SKILL_ICONS);

		for (int i = 0; i < 3; i++) {
			skillIcons[i] = temp.getSubimage(i * 32, 0, 32, 32);
		}
	}

	private void loadDialogue() {
		loadDialogueImgs();

		for (int i = 0; i < 10; i++)
			dialogEffects.add(new DialogueEffect(0, 0, EXCLAMATION));
		for (int i = 0; i < 10; i++)
			dialogEffects.add(new DialogueEffect(0, 0, QUESTION));

		for (DialogueEffect de : dialogEffects)
			de.deactive();
	}

	private void loadDialogueImgs() {
		BufferedImage qtemp = LoadSave.GetSpriteAtlas(LoadSave.QUESTION_ATLAS);
		questionImgs = new BufferedImage[5];
		for (int i = 0; i < questionImgs.length; i++)
			questionImgs[i] = qtemp.getSubimage(i * 14, 0, 14, 12);

		BufferedImage etemp = LoadSave.GetSpriteAtlas(LoadSave.EXCLAMATION_ATLAS);
		exclamationImgs = new BufferedImage[5];
		for (int i = 0; i < exclamationImgs.length; i++)
			exclamationImgs[i] = etemp.getSubimage(i * 14, 0, 14, 12);
	}

	public void loadNextLevel() {
		levelManager.setLevelIndex(levelManager.getLevelIndex() + 1);
		levelManager.loadNextLevel();
		player.setSpawn(levelManager.getCurrentLevel().getPlayerSpawn());
		resetAll();
		player.startNextLevel();

		startLevelTimer();

		showMonsterRankForCurrentLevel();
	}

	private void loadStartLevel() {
		enemyManager.loadEnemies(levelManager.getCurrentLevel());
		objectManager.loadObjects(levelManager.getCurrentLevel());
	}

	private void calcLvlOffset() {
		maxLvlOffsetX = levelManager.getCurrentLevel().getLvlOffset();
	}

	private void initClasses() {
		levelManager = new LevelManager(game);
		enemyManager = new EnemyManager(this);
		objectManager = new ObjectManager(this);

		player = new Player(200, 200, (int) (64 * Game.SCALE), (int) (40 * Game.SCALE), this);
		player.loadLvlData(levelManager.getCurrentLevel().getLevelData());
		player.setSpawn(levelManager.getCurrentLevel().getPlayerSpawn());

		pauseOverlay = new PauseOverlay(this);
		gameOverOverlay = new GameOverOverlay(this);
		levelCompletedOverlay = new LevelCompletedOverlay(this);
		gameCompletedOverlay = new GameCompletedOverlay(this);

		lightning = new Lightning(this);
		rain = new Rain(this);
		shootingStarManager = new ShootingStarManager(100);
		starBackground = new Stars();


		for (int i = 0; i < skillCooldowns.length; i++) {
			skillCooldowns[i] = false;
			skillCooldownTimers[i] = 0;
		}
	}

	@Override
	public void update() {
		long currentTime = System.currentTimeMillis();
		deltaTime = (currentTime - lastFrameTime) / 1000f;
		lastFrameTime = currentTime;

		if (fadeInTransition) {
			fadeInAlpha -= FADE_IN_SPEED;
			if (fadeInAlpha <= 0) {
				fadeInAlpha = 0;
				fadeInTransition = false;
			}
		}

		
		updateMonsterRankDisplay();
		updateLevelUpEffect();

		if (paused || lvlCompleted || gameCompleted || gameOver || playerDying) {
			game.getAudioPlayer().stopEffect(AudioPlayer.AURA_EFFECT);
			rain.stopRainSound();
		}

		if (paused)
			pauseOverlay.update();
		else if (lvlCompleted)
			levelCompletedOverlay.update();
		else if (gameCompleted)
			gameCompletedOverlay.update();
		else if (gameOver)
			gameOverOverlay.update();
		else if (playerDying)
			player.update();
		else {
			updateTimer();
			updateBorderEffect();
			updateDialogue();
			updateSkillCooldowns();
			shootingStarManager.update();
			if (drawRain) {
				rain.update(xLvlOffset);
				lightning.update();
			}
			levelManager.update();
			if (!drawRain) {
				starBackground.update();
			}
			objectManager.update(levelManager.getCurrentLevel().getLevelData(), player);
			player.update();
			enemyManager.update(levelManager.getCurrentLevel().getLevelData());
			checkCloseToBorder();
		}
	}

	public void showLevelUpEffect(int newLevel) {
		showLevelUpEffect = true;
		levelUpEffectTime = 0;
		levelUpEffectScale = 0.1f;
		levelUpEffectAlpha = 0;
		playerLevel = newLevel;

		// Calculate box dimensions
		int boxWidth = (int)(100 * Game.SCALE);
		int boxHeight = (int)(40 * Game.SCALE);

		// Calculate the INITIAL position - this will be fixed
		// Get the player's position on screen (not in the world)
		levelUpEffectSpawnX = (int)player.getHitbox().x - xLvlOffset;
		levelUpEffectSpawnY = (int)player.getHitbox().y - boxHeight - (int)(20 * Game.SCALE);

		// Center it over the player
		levelUpEffectSpawnX += (player.getHitbox().width / 2) - (boxWidth / 2);

	}

	private void updateLevelUpEffect() {
		if (showLevelUpEffect) {
			levelUpEffectTime += deltaTime * 1000;

			
			if (levelUpEffectTime < 300) {
				levelUpEffectAlpha += LEVEL_UP_FADE_SPEED * 2;
				if (levelUpEffectAlpha > 255) levelUpEffectAlpha = 255;
			}
			
			else if (levelUpEffectTime > LEVEL_UP_EFFECT_DURATION - 300) {
				levelUpEffectAlpha -= LEVEL_UP_FADE_SPEED * 2;
				if (levelUpEffectAlpha < 0) {
					levelUpEffectAlpha = 0;
					showLevelUpEffect = false;
				}
			}

			
			if (levelUpEffectScale < LEVEL_UP_TARGET_SCALE) {
				levelUpEffectScale += LEVEL_UP_SCALE_SPEED;
				if (levelUpEffectScale > LEVEL_UP_TARGET_SCALE)
					levelUpEffectScale = LEVEL_UP_TARGET_SCALE;
			}
		}
	}

	private void drawLevelUpEffect(Graphics g) {
		if (!showLevelUpEffect) return;

		Graphics2D g2d = (Graphics2D) g;
		Composite originalComposite = g2d.getComposite();
		AffineTransform originalTransform = g2d.getTransform();

		g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, levelUpEffectAlpha / 255f));

		
		int boxWidth = (int)(100 * Game.SCALE);
		int boxHeight = (int)(40 * Game.SCALE);

		
		int centerX = levelUpEffectSpawnX + boxWidth/2;
		int centerY = levelUpEffectSpawnY + boxHeight/2;

		g2d.translate(centerX, centerY);
		g2d.scale(levelUpEffectScale, levelUpEffectScale);
		g2d.translate(-centerX, -centerY);

		GradientPaint blueGradient = new GradientPaint(
				levelUpEffectSpawnX, levelUpEffectSpawnY,
				new Color(0, 20, 80, 230),
				levelUpEffectSpawnX + boxWidth, levelUpEffectSpawnY + boxHeight,
				new Color(0, 50, 150, 230));

		g2d.setPaint(blueGradient);
		g2d.fillRect(levelUpEffectSpawnX, levelUpEffectSpawnY, boxWidth, boxHeight);

		g2d.setColor(new Color(100, 200, 255, 200));
		g2d.setStroke(new BasicStroke(1));
		g2d.drawRect(levelUpEffectSpawnX, levelUpEffectSpawnY, boxWidth, boxHeight);

		g2d.setColor(new Color(180, 230, 255, 100));
		g2d.drawRect(levelUpEffectSpawnX + 2, levelUpEffectSpawnY + 2, boxWidth - 4, boxHeight - 4);

		Font originalFont = g2d.getFont();

		
		try {
			Font levelUpTextFont = Font.createFont(Font.TRUETYPE_FONT,
					new File("res/fonts/pixel_font.ttf")).deriveFont(Font.BOLD, 14f);
			g2d.setFont(levelUpTextFont);
		} catch (Exception e) {
			g2d.setFont(new Font("Arial", Font.BOLD, 14));
		}

		String levelUpText = "LEVEL UP!";
		FontMetrics levelUpFm = g2d.getFontMetrics();
		int levelUpTextWidth = levelUpFm.stringWidth(levelUpText);
		int levelUpTextX = levelUpEffectSpawnX + (boxWidth - levelUpTextWidth) / 2;
		int levelUpTextY = levelUpEffectSpawnY + (boxHeight / 3) + 3;

		
		g2d.setColor(new Color(0, 0, 0, 150));
		g2d.drawString(levelUpText, levelUpTextX + 1, levelUpTextY + 1);

		
		g2d.setColor(new Color(255, 215, 0)); 
		g2d.drawString(levelUpText, levelUpTextX, levelUpTextY);

		
		try {
			Font levelUpFont = Font.createFont(Font.TRUETYPE_FONT,
					new File("res/fonts/pixel_font.ttf")).deriveFont(Font.BOLD, 12f);
			g2d.setFont(levelUpFont);
		} catch (Exception e) {
			g2d.setFont(new Font("Arial", Font.BOLD, 12));
		}

		
		String levelText = "Lv." + playerLevel;
		String bonusText = "+" + (int)(Math.pow(1.5, playerLevel)) + "% damage ";
		String combinedText = levelText + " " + bonusText;

		FontMetrics fm = g2d.getFontMetrics();
		int textWidth = fm.stringWidth(combinedText);
		int textX = levelUpEffectSpawnX + (boxWidth - textWidth) / 2;
		int textY = levelUpEffectSpawnY + (boxHeight * 2 / 3) + 3;

		
		g2d.setColor(new Color(0, 0, 0, 150));
		g2d.drawString(combinedText, textX + 1, textY + 1);

		
		g2d.setColor(new Color(170, 255, 255));
		g2d.drawString(combinedText, textX, textY);

		
		for (int i = 0; i < 3; i++) {
			int particleSize = (int)(1 * Game.SCALE) + levelUpRandom.nextInt((int)(2 * Game.SCALE));
			int particleX = levelUpEffectSpawnX + levelUpRandom.nextInt(boxWidth);
			int particleY = levelUpEffectSpawnY + levelUpRandom.nextInt(boxHeight);
			int particleAlpha = 50 + levelUpRandom.nextInt(150);

			g2d.setColor(new Color(255, 215, 0, particleAlpha)); 
			g2d.fillOval(particleX, particleY, particleSize, particleSize);
		}

		g2d.setFont(originalFont);
		g2d.setComposite(originalComposite);
		g2d.setTransform(originalTransform);
	}

	private void drawBorderEffect(Graphics g) {
		if (borderEffectAlpha <= 0)
			return;

		Graphics2D g2d = (Graphics2D) g;


		Composite originalComposite = g2d.getComposite();
		Paint originalPaint = g2d.getPaint();


		int currentAlpha = borderEffectAlpha;
		if (pulsatingEffect) {

			double pulseFactor = Math.sin(pulsateTimer * 0.1) * 0.3 + 0.7;
			currentAlpha = (int)(borderEffectAlpha * pulseFactor);
		}



		GradientPaint topGradient = new GradientPaint(
				0, 0, new Color(borderEffectColor.getRed(), borderEffectColor.getGreen(),
				borderEffectColor.getBlue(), currentAlpha),
				0, borderWidth, new Color(borderEffectColor.getRed(), borderEffectColor.getGreen(),
				borderEffectColor.getBlue(), 0)
		);


		GradientPaint bottomGradient = new GradientPaint(
				0, Game.GAME_HEIGHT, new Color(borderEffectColor.getRed(), borderEffectColor.getGreen(),
				borderEffectColor.getBlue(), currentAlpha),
				0, Game.GAME_HEIGHT - borderWidth, new Color(borderEffectColor.getRed(), borderEffectColor.getGreen(),
				borderEffectColor.getBlue(), 0)
		);


		GradientPaint leftGradient = new GradientPaint(
				0, 0, new Color(borderEffectColor.getRed(), borderEffectColor.getGreen(),
				borderEffectColor.getBlue(), currentAlpha),
				borderWidth, 0, new Color(borderEffectColor.getRed(), borderEffectColor.getGreen(),
				borderEffectColor.getBlue(), 0)
		);


		GradientPaint rightGradient = new GradientPaint(
				Game.GAME_WIDTH, 0, new Color(borderEffectColor.getRed(), borderEffectColor.getGreen(),
				borderEffectColor.getBlue(), currentAlpha),
				Game.GAME_WIDTH - borderWidth, 0, new Color(borderEffectColor.getRed(), borderEffectColor.getGreen(),
				borderEffectColor.getBlue(), 0)
		);


		g2d.setPaint(topGradient);
		g2d.fillRect(0, 0, Game.GAME_WIDTH, borderWidth);


		g2d.setPaint(bottomGradient);
		g2d.fillRect(0, Game.GAME_HEIGHT - borderWidth, Game.GAME_WIDTH, borderWidth);


		g2d.setPaint(leftGradient);
		g2d.fillRect(0, 0, borderWidth, Game.GAME_HEIGHT);


		g2d.setPaint(rightGradient);
		g2d.fillRect(Game.GAME_WIDTH - borderWidth, 0, borderWidth, Game.GAME_HEIGHT);



		RadialGradientPaint topLeftGradient = new RadialGradientPaint(
				new Point2D.Float(0, 0),
				borderWidth,
				new float[] {0.0f, 1.0f},
				new Color[] {
						new Color(borderEffectColor.getRed(), borderEffectColor.getGreen(),
								borderEffectColor.getBlue(), currentAlpha),
						new Color(borderEffectColor.getRed(), borderEffectColor.getGreen(),
								borderEffectColor.getBlue(), 0)
				}
		);
		g2d.setPaint(topLeftGradient);
		g2d.fillRect(0, 0, borderWidth, borderWidth);


		RadialGradientPaint topRightGradient = new RadialGradientPaint(
				new Point2D.Float(Game.GAME_WIDTH, 0),
				borderWidth,
				new float[] {0.0f, 1.0f},
				new Color[] {
						new Color(borderEffectColor.getRed(), borderEffectColor.getGreen(),
								borderEffectColor.getBlue(), currentAlpha),
						new Color(borderEffectColor.getRed(), borderEffectColor.getGreen(),
								borderEffectColor.getBlue(), 0)
				}
		);
		g2d.setPaint(topRightGradient);
		g2d.fillRect(Game.GAME_WIDTH - borderWidth, 0, borderWidth, borderWidth);


		RadialGradientPaint bottomLeftGradient = new RadialGradientPaint(
				new Point2D.Float(0, Game.GAME_HEIGHT),
				borderWidth,
				new float[] {0.0f, 1.0f},
				new Color[] {
						new Color(borderEffectColor.getRed(), borderEffectColor.getGreen(),
								borderEffectColor.getBlue(), currentAlpha),
						new Color(borderEffectColor.getRed(), borderEffectColor.getGreen(),
								borderEffectColor.getBlue(), 0)
				}
		);
		g2d.setPaint(bottomLeftGradient);
		g2d.fillRect(0, Game.GAME_HEIGHT - borderWidth, borderWidth, borderWidth);


		RadialGradientPaint bottomRightGradient = new RadialGradientPaint(
				new Point2D.Float(Game.GAME_WIDTH, Game.GAME_HEIGHT),
				borderWidth,
				new float[] {0.0f, 1.0f},
				new Color[] {
						new Color(borderEffectColor.getRed(), borderEffectColor.getGreen(),
								borderEffectColor.getBlue(), currentAlpha),
						new Color(borderEffectColor.getRed(), borderEffectColor.getGreen(),
								borderEffectColor.getBlue(), 0)
				}
		);
		g2d.setPaint(bottomRightGradient);
		g2d.fillRect(Game.GAME_WIDTH - borderWidth, Game.GAME_HEIGHT - borderWidth, borderWidth, borderWidth);


		g2d.setPaint(originalPaint);
		g2d.setComposite(originalComposite);
	}

	private void updateSkillCooldowns() {
		for (int i = 0; i < skillCooldownTimers.length; i++) {
			if (skillCooldowns[i]) {

				skillCooldownTimers[i] -= (int)(deltaTime * 1000);
				if (skillCooldownTimers[i] <= 0) {
					skillCooldowns[i] = false;
					player.notifyCooldownComplete(i);
				}
			}
		}
	}

	public int getRemainingCooldown(int skillIndex) {
		if (skillIndex >= 0 && skillIndex < skillCooldownTimers.length) {
			return Math.max(0, skillCooldownTimers[skillIndex]);
		}
		return 0;
	}

	private void updateDialogue() {
		for (DialogueEffect de : dialogEffects)
			if (de.isActive())
				de.update();
	}

	private void drawDialogue(Graphics g, int xLvlOffset) {
		for (DialogueEffect de : dialogEffects)
			if (de.isActive()) {
				if (de.getType() == QUESTION)
					g.drawImage(questionImgs[de.getAniIndex()], de.getX() - xLvlOffset, de.getY(), DIALOGUE_WIDTH, DIALOGUE_HEIGHT, null);
				else
					g.drawImage(exclamationImgs[de.getAniIndex()], de.getX() - xLvlOffset, de.getY(), DIALOGUE_WIDTH, DIALOGUE_HEIGHT, null);
			}
	}

	public void addDialogue(int x, int y, int type) {
		dialogEffects.add(new DialogueEffect(x, y - (int) (Game.SCALE * 15), type));
		for (DialogueEffect de : dialogEffects)
			if (!de.isActive())
				if (de.getType() == type) {
					de.reset(x, -(int) (Game.SCALE * 15));
					return;
				}
	}

	private void checkCloseToBorder() {
		int playerX = (int) player.getHitbox().x;
		int diff = playerX - xLvlOffset;

		if (diff > rightBorder)
			xLvlOffset += diff - rightBorder;
		else if (diff < leftBorder)
			xLvlOffset += diff - leftBorder;

		xLvlOffset = Math.max(Math.min(xLvlOffset, maxLvlOffsetX), 0);
	}

	@Override
	public void draw(Graphics g) {
		g.drawImage(backgroundImg, 0, 0, Game.GAME_WIDTH, Game.GAME_HEIGHT, null);
		shootingStarManager.draw(g, xLvlOffset);
		drawClouds(g);

		if (!drawRain) {
			starBackground.draw(g, xLvlOffset);
		}

		if (drawRain) {
			rain.draw(g, xLvlOffset);
			lightning.draw(g, xLvlOffset);
		}

		levelManager.draw(g, xLvlOffset);
		objectManager.draw(g, xLvlOffset);
		objectManager.drawBackgroundTorch(g, xLvlOffset);
		drawLevelUpEffect(g);
		enemyManager.draw(g, xLvlOffset);
		player.render(g, xLvlOffset);
		drawDialogue(g, xLvlOffset);

		drawSkillBoxes(g);
		drawBorderEffect(g);

		drawGameInfo(g);

		
		drawMonsterRankDisplay(g);

		if (paused) {
			g.setColor(new Color(0, 0, 0, 150));
			g.fillRect(0, 0, Game.GAME_WIDTH, Game.GAME_HEIGHT);
			pauseOverlay.draw(g);
		} else if (gameOver)
			gameOverOverlay.draw(g);
		else if (lvlCompleted)
			levelCompletedOverlay.draw(g);
		else if (gameCompleted)
			gameCompletedOverlay.draw(g);

		if (fadeInTransition && fadeInAlpha > 0) {
			g.setColor(new Color(0, 0, 0, fadeInAlpha));
			g.fillRect(0, 0, Game.GAME_WIDTH, Game.GAME_HEIGHT);
		}
	}

	private void drawSkillBoxes(Graphics g) {

		Font originalFont = g.getFont();

		try {

			Font pixelFont = Font.createFont(Font.TRUETYPE_FONT,
					new File("res/fonts/pixel_font.ttf")).deriveFont(15f);
			g.setFont(pixelFont);
		} catch (Exception e) {
			e.printStackTrace();

			g.setFont(new Font("Courier New", Font.BOLD, 15));
		}

		int startX = (Game.GAME_WIDTH / 2) - (SKILL_BOX_WIDTH * 3 / 2) - 10;

		for (int i = 0; i < 3; i++) {
			int boxX = startX + (i * (SKILL_BOX_WIDTH + 10));

			g.drawImage(skillBoxImg, boxX, SKILL_BOX_Y, SKILL_BOX_WIDTH, SKILL_BOX_HEIGHT, null);
			g.drawImage(skillIcons[i], boxX + 5, SKILL_BOX_Y + 5, SKILL_BOX_WIDTH - 10, SKILL_BOX_HEIGHT - 10, null);

			g.setColor(Color.WHITE);
			String keyLabel = "";
			switch (i) {
				case 0: keyLabel = "Q"; break;
				case 1: keyLabel = "R"; break;
				case 2: keyLabel = "T"; break;
			}


			FontMetrics fm = g.getFontMetrics();
			int stringWidth = fm.stringWidth(keyLabel);
			g.drawString(keyLabel, boxX + (SKILL_BOX_WIDTH / 2) - (stringWidth / 2), SKILL_BOX_Y + SKILL_BOX_HEIGHT - 2 );

			if (skillCooldowns[i]) {

				g.setColor(new Color(0, 0, 0, 150));
				g.fillRect(boxX + 5, SKILL_BOX_Y + 5, SKILL_BOX_WIDTH - 10, SKILL_BOX_HEIGHT - 10);


				float secondsRemaining = skillCooldownTimers[i] / 1000f;
				String cooldownText = String.format("%.1f", secondsRemaining);


				g.setColor(Color.WHITE);
				FontMetrics fm2 = g.getFontMetrics();
				int textWidth = fm2.stringWidth(cooldownText);
				int textHeight = fm2.getHeight();
				g.drawString(cooldownText,
						boxX + (SKILL_BOX_WIDTH / 2) - (textWidth / 2),
						SKILL_BOX_Y + (SKILL_BOX_HEIGHT / 2) + (textHeight / 4));
			}
		}


		g.setFont(originalFont);
	}

	private void drawClouds(Graphics g) {
		for (int i = 0; i < 4; i++)
			g.drawImage(bigCloud, i * BIG_CLOUD_WIDTH - (int) (xLvlOffset * 0.3), (int) (204 * Game.SCALE), BIG_CLOUD_WIDTH, BIG_CLOUD_HEIGHT, null);

		for (int i = 0; i < smallCloudsPos.length; i++)
			g.drawImage(smallCloud, SMALL_CLOUD_WIDTH * 4 * i - (int) (xLvlOffset * 0.7), smallCloudsPos[i], SMALL_CLOUD_WIDTH, SMALL_CLOUD_HEIGHT, null);
	}



	public void resetGameCompleted() {
		gameCompleted = false;
	}

	public void resetAll() {
		gameOver = false;
		paused = false;
		lvlCompleted = false;
		playerDying = false;
		drawRain = false;

		rain.stopRainSound();

		resetLevelTimer(); 

		
		for (int i = 0; i < skillCooldowns.length; i++) {
			skillCooldowns[i] = false;
			skillCooldownTimers[i] = 0;
		}

		setDrawRainBoolean();

		
		enemyManager.resetAllEnemies();
		objectManager.resetAllObjects();
		dialogEffects.clear();
	}

	public void retry(){
		resetAll();
		player.retryCurrentLevel();
	}



	private void setDrawRainBoolean() {
		if (rnd.nextFloat() >= 0.2f) {
			drawRain = true;
			System.out.println("TRUE: " + Gamestate.state);
			if (rnd.nextFloat() > 0.5f) {
				lightning.stormMode();
			} else {
				lightning.normalMode();
			}
		} else {
			drawRain = false;
			System.out.println("FALZ: " + Gamestate.state);
		}
	}

	public void setGameOver(boolean gameOver) {
		this.gameOver = gameOver;
		if (gameOver) {
			timerRunning = false; 

			if (rain != null && drawRain) {
				rain.stopRainSound();
			}
		}
	}


	public void checkEnemyHit(Rectangle2D.Float attackBox, int damageMultiplier) {
		enemyManager.setIsEnemyHit(false);
		enemyManager.checkEnemyHit(attackBox, damageMultiplier);
	}

	public boolean wasEnemeyHit() {
		return enemyManager.isEnemyHit();
	}

	public void checkPotionTouched(Rectangle2D.Float hitbox) {
		objectManager.checkObjectTouched(hitbox);
	}

	public void checkSpikesTouched(Player p) {
		objectManager.checkSpikesTouched(p);
	}

	@Override
	public void mouseClicked(MouseEvent e) {

	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (!gameOver && !gameCompleted && !lvlCompleted)
			switch (e.getKeyCode()) {
				case KeyEvent.VK_A:
					player.setLeft(true);
					break;
				case KeyEvent.VK_D:
					player.setRight(true);
					break;
				case KeyEvent.VK_SPACE:
					player.setJump(true);
					break;
				case KeyEvent.VK_E:
					player.setAttacking(true);
					break;
				case KeyEvent.VK_Q:
					player.shadowpierce();
					break;
				case KeyEvent.VK_R:
					player.activateShadowwMonarch();
					break;
				case KeyEvent.VK_T:
					player.activateStealth();
					break;
				case KeyEvent.VK_F:
					player.activateShadowBlast();
					break;
				case KeyEvent.VK_P:
					this.setLevelCompleted(true);
					break;
				case KeyEvent.VK_ESCAPE:
					togglePauseGame();
			}
	}

	public void togglePauseGame() {
		paused = !paused;
		this.getGame().getAudioPlayer().playEffect(AudioPlayer.MESSAGE_EFFECT);
		player.handleGamePause(paused);

		if (rain != null && drawRain) {
			rain.stopRainSound();
		}

	}

	public boolean getShadowpierce() { return player.shadowpierceActive; }


	public void setSkillCooldown(int index, int duration) {
		skillCooldowns[index] = true;
		skillCooldownTimers[index] = duration;
	}

	@Override
	public void keyReleased(KeyEvent e) {
		if (!gameOver && !gameCompleted && !lvlCompleted)
			switch (e.getKeyCode()) {
				case KeyEvent.VK_A:
					player.setLeft(false);
					break;
				case KeyEvent.VK_D:
					player.setRight(false);
					break;
				case KeyEvent.VK_SPACE:
					player.setJump(false);
					break;
			}
	}

	public void mouseDragged(MouseEvent e) {
		if (!gameOver && !gameCompleted && !lvlCompleted)
			if (paused)
				pauseOverlay.mouseDragged(e);
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if (gameOver)
			gameOverOverlay.mousePressed(e);
		else if (paused)
			pauseOverlay.mousePressed(e);
		else if (lvlCompleted)
			levelCompletedOverlay.mousePressed(e);
		else if (gameCompleted)
			gameCompletedOverlay.mousePressed(e);

	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (gameOver)
			gameOverOverlay.mouseReleased(e);
		else if (paused)
			pauseOverlay.mouseReleased(e);
		else if (lvlCompleted)
			levelCompletedOverlay.mouseReleased(e);
		else if (gameCompleted)
			gameCompletedOverlay.mouseReleased(e);
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		if (gameOver)
			gameOverOverlay.mouseMoved(e);
		else if (paused)
			pauseOverlay.mouseMoved(e);
		else if (lvlCompleted)
			levelCompletedOverlay.mouseMoved(e);
		else if (gameCompleted)
			gameCompletedOverlay.mouseMoved(e);
	}

	private void saveCompletionData(int roundNumber, String playerName, long completionTime) {
		try {

			File roundDir = new File("data/rounds/round" + roundNumber);
			if (!roundDir.exists()) {
				roundDir.mkdirs();
			}


			File leaderboardFile = new File(roundDir, "leaderboard.txt");
			boolean fileExists = leaderboardFile.exists();


			FileWriter fw = new FileWriter(leaderboardFile, true);
			BufferedWriter bw = new BufferedWriter(fw);


			if (!fileExists) {
				bw.write("Player:Time\n");
			}


			bw.write(playerName + "," + completionTime + "\n");
			bw.close();

			System.out.println("Saved completion data for " + playerName + " in round " + roundNumber);

		} catch (IOException e) {
			System.err.println("Error saving completion data: " + e.getMessage());
			e.printStackTrace();
		}
	}



	public void setLevelCompleted(boolean levelCompleted) {
		System.out.println((getTime()));
		game.getAudioPlayer().lvlCompleted();

		saveCompletionData(levelManager.getLevelIndex() + 1, game.getPlayerName(), currentTime);

		if (levelManager.getLevelIndex() + 1 >= levelManager.getAmountOfLevels()) {
			gameCompleted = true;
			levelManager.setLevelIndex(0);
			levelManager.loadNextLevel();
			resetAll();
			return;
		}
		this.lvlCompleted = levelCompleted;
	}

	public void setMaxLvlOffset(int lvlOffset) {
		this.maxLvlOffsetX = lvlOffset;
	}



	public void unpauseGame() {
		paused = false;
	}

	public Player getPlayer() {
		return player;
	}

	public EnemyManager getEnemyManager() {
		return enemyManager;
	}

	public ObjectManager getObjectManager() {
		return objectManager;
	}

	public LevelManager getLevelManager() {
		return levelManager;
	}

	public void setPlayerDying(boolean playerDying) {
		this.playerDying = playerDying;
	}

	public boolean isPaused() {
		return paused;
	}

	public boolean isLevelCompleted() {
		return lvlCompleted;
	}

	public boolean isGameOver() {
		return gameOver;
	}

	public boolean isGameCompleted() {
		return gameCompleted;
	}

	public boolean isPlayerDying() {
		return playerDying;
	}


}