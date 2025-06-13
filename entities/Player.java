package entities;

import static utilz.Constants.ANI_SPEED;
import static utilz.Constants.GRAVITY;
import static utilz.Constants.PlayerConstants.*;
import static utilz.HelpMethods.*;
import static utilz.Constants.Directions.*;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.Random;

import audio.AudioPlayer;
import effects.ParticleSystem;
import gamestates.Playing;
import main.Game;
import utilz.LoadSave;

public class Player extends Entity {

	private BufferedImage[][] animations;
	private boolean moving = false, attacking = false;
	private boolean left, right, jump;
	private int[][] lvlData;
	private float xDrawOffset = 24 * Game.SCALE;
	private float yDrawOffset = 5 * Game.SCALE;

	private float jumpSpeed = -2.25f * Game.SCALE;
	private float fallSpeedAfterCollision = 0.5f * Game.SCALE;

	private int statusBarWidth = (int) (192 * Game.SCALE);
	private int statusBarHeight = (int) (58 * Game.SCALE);
	private int statusBarX = (int) (10 * Game.SCALE);
	private int statusBarY = (int) (10 * Game.SCALE);

	private int healthBarWidth = (int) (150 * Game.SCALE);
	private int healthWidth = healthBarWidth;

	private int powerBarWidth = (int) (104 * Game.SCALE);
	private int powerBarHeight = (int) (2 * Game.SCALE);
	private int powerBarXStart = (int) (44 * Game.SCALE);
	private int powerBarYStart = (int) (34 * Game.SCALE);
	private int powerWidth = powerBarWidth;
	private int powerMaxValue = 400;
	private int powerValue = powerMaxValue;

	private int flipX = 0;
	private int flipW = 1;

	private boolean attackChecked;
	private Playing playing;

	private int tileY = 0;

	private int comboCount = 0;
	private long lastComboTime = 0;
	private final long COMBO_TIMEOUT = 2000;
	private final int MAX_COMBO = 20;
	private Color comboColor = Color.GREEN;

	public boolean shadowpierceActive;
	private int shadowpierceTick;
	private int powerGrowSpeed = 15;
	private int powerGrowTick;

	private boolean shadowMonarchActive;
	private int shadowMonarchTick;
	private int shadowMonarchDuration = 10 * 1000;
	private float normalWalkSpeed;
	private float shadowMonarchMultiplier = 1.7f;


	private boolean stealthActive;
	private int stealthTick;
	private int stealthDuration = 7 * 1000;
	private float stealthOpacity = 0.5f;

	private ParticleSystem particleSystem;
	private Random random = new Random();

	private boolean auraActive;
	private int auraTick;

	private int shadowMonarchCooldown = 20 * 1000;
	private int stealthCooldown = 13 * 1000;
	private int shadowpierceCooldown = 3 * 1000;

	private boolean shadowMonarchOnCooldown = false;
	private boolean stealthOnCooldown = false;
	private boolean shadowpierceOnCooldown = false;

	private boolean auraEffectPlaying = false;

	private boolean shadowBlastActive;
	private int shadowBlastTick;
	private int shadowBlastDuration = 500;
	private int shadowBlastRadius = (int) (100 * Game.SCALE);
	private int shadowBlastCooldown = 6 * 1000;
	private boolean shadowBlastOnCooldown = false;
	private float shadowBlastDamageMultiplier = 2.0f;

	private long lastUpdateTime;
	private float deltaTime;

	private int playerLevel = 1;
	private int currentExp = 0;
	private int expToNextLevel = 100;
	private final float EXP_GROWTH_RATE = 1.2f;

	private int checkpointPlayerLevel = 1;
	private int checkpointCurrentExp = 0;
	private int checkpointExpToNextLevel = 100;
	private int checkpointMaxHealth = 200;
	private int checkpointPowerMaxValue = 400;

	private final Font LEVEL_FONT = new Font("Arial", Font.BOLD, 20);
	private final Color TEXT_SHADOW = new Color(0, 0, 0, 150);



	public Player(float x, float y, int width, int height, Playing playing) {
		super(x, y, width, height);
		this.playing = playing;
		this.lastUpdateTime = System.currentTimeMillis();
		this.state = IDLE;
		this.setMaxHealth(200);
		this.setCurrentHealth(getMaxHealth());
		this.walkSpeed = Game.SCALE * 0.7f;
		this.particleSystem = new ParticleSystem();
		loadAnimations();
		initHitbox(15, 26);
		initAttackBox();

	}

	public void setSpawn(Point spawn) {
		this.x = spawn.x;
		this.y = spawn.y;
		hitbox.x = x;
		hitbox.y = y;
	}

	private void initAttackBox() {
		attackBox = new Rectangle2D.Float(x, y, (int) (35 * Game.SCALE), (int) (20 * Game.SCALE));
		resetAttackBox();
	}

	public void update() {
		long currentTime = System.currentTimeMillis();
		deltaTime = (currentTime - lastUpdateTime) / 1000f;
		lastUpdateTime = currentTime;

		
		checkComboTimeout(currentTime);

		updateScreenEffects();
		updateHealthBar();
		updatePowerBar();
		particleSystem.update();

		if (getCurrentHealth() <= 0) {
			if (state != DEAD) {
				state = DEAD;
				aniTick = 0;
				aniIndex = 0;
				playing.setPlayerDying(true);
				playing.getGame().getAudioPlayer().playEffect(AudioPlayer.DIE);

				if (auraEffectPlaying) {
					playing.getGame().getAudioPlayer().stopEffect(AudioPlayer.AURA_EFFECT);
					auraEffectPlaying = false;
				}


				if (!IsEntityOnFloor(hitbox, lvlData)) {
					inAir = true;
					airSpeed = 0;
				}
			} else if (aniIndex == GetSpriteAmount(DEAD) - 1 && aniTick >= ANI_SPEED - 1) {
				playing.setGameOver(true);
				playing.getGame().getAudioPlayer().stopSong();
				playing.getGame().getAudioPlayer().playEffect(AudioPlayer.GAMEOVER);
			} else {
				updateAnimationTick();


				if (inAir)
					if (CanMoveHere(hitbox.x, hitbox.y + airSpeed, hitbox.width, hitbox.height, lvlData)) {
						hitbox.y += airSpeed;
						airSpeed += GRAVITY;
					} else
						inAir = false;

			}

			return;
		}

		updateAttackBox();

		if (state == HIT) {
			if (aniIndex <= GetSpriteAmount(state) - 3)
				pushBack(pushBackDir, lvlData, shadowMonarchActive ? .8f : 1.25f);
			updatePushBackDrawOffset();
		} else {
			updatePos();
			createMovementParticles();
		}

		if (moving) {
			checkPotionTouched();
			checkSpikesTouched();
			checkInsideWater();
			tileY = (int) (hitbox.y / Game.TILES_SIZE);

			if (shadowpierceActive) {
				shadowpierceTick++;

				if (shadowpierceTick % 2 == 0) {
					createshadowpierceParticles();
				}
				if (shadowpierceTick >= 35) {
					shadowpierceTick = 0;
					shadowpierceActive = false;
				}
			}
		}


		if (attacking || shadowpierceActive)
			checkAttack();

		if (shadowMonarchActive) {
			shadowMonarchTick += deltaTime * 1000;
			updateAura();
			if (shadowMonarchTick >= shadowMonarchDuration) {
				shadowMonarchTick = 0;
				shadowMonarchActive = false;
				auraActive = false;
				walkSpeed = normalWalkSpeed;


				if (auraEffectPlaying) {
					playing.getGame().getAudioPlayer().stopEffect(AudioPlayer.AURA_EFFECT);
					auraEffectPlaying = false;
				}
			}
		}


		if (stealthActive) {
			stealthTick += deltaTime * 1000;
			if (stealthTick >= stealthDuration) {
				stealthTick = 0;
				stealthActive = false;

				playing.getGame().getAudioPlayer().playEffect(AudioPlayer.STEALTH_EFFECT);
			}
		}

		if (shadowBlastActive) {
			shadowBlastTick += deltaTime * 1000;
			if (shadowBlastTick >= shadowBlastDuration) {
				shadowBlastTick = 0;
				shadowBlastActive = false;
			}
		}

		updateAnimationTick();
		setAnimation();
	}

	
	private void checkComboTimeout(long currentTime) {
		if (comboCount > 0 && currentTime - lastComboTime > COMBO_TIMEOUT) {
			resetCombo();
		}
	}

	
	private void resetCombo() {
		comboCount = 0;
	}

	
	public void incrementCombo() {
		comboCount++;
		if (comboCount > MAX_COMBO) {
			comboCount = MAX_COMBO;
		}

		lastComboTime = System.currentTimeMillis();

		
		String comboText = "Combo " + comboCount + "x";

		
		if (comboCount <= 3) {
			comboColor = Color.GREEN;
		} else if (comboCount <= 6) {
			comboColor = Color.YELLOW;
		} else {
			comboColor = Color.RED;
		}

		
		showText(comboText, (int)hitbox.y - (int)yDrawOffset - 60, comboColor);

		
		if (comboCount == 5) {
			playing.getGame().getAudioPlayer().playEffect(AudioPlayer.JUMP);
		} else if (comboCount == 10) {
			playing.getGame().getAudioPlayer().playEffect(AudioPlayer.LEVEL_UP);
		}
	}

	private void createShadowBlastParticles() {
		float centerX = hitbox.x + hitbox.width / 2;
		float centerY = hitbox.y + hitbox.height / 2;


		double angle = random.nextDouble() * Math.PI * 2;
		double distance = random.nextDouble() * shadowBlastRadius;

		float particleX = (float)(centerX + Math.cos(angle) * distance);
		float particleY = (float)(centerY + Math.sin(angle) * distance);


		particleSystem.createBlastParticles(particleX, particleY);
	}

	private void performShadowBlastDamage() {
		float centerX = hitbox.x + hitbox.width / 2;
		float centerY = hitbox.y + hitbox.height / 2;


		playing.getEnemyManager().checkEnemiesInRadius(centerX, centerY, shadowBlastRadius, shadowBlastDamageMultiplier);
	}

	private void createshadowpierceParticles() {
		float particleX = hitbox.x;
		float particleY = hitbox.y + hitbox.height / 2;


		if (flipW == -1) {
			particleX = hitbox.x + hitbox.width;
		}

		particleSystem.createPowerDashParticles(particleX, particleY, flipW == -1);
	}

	private void checkInsideWater() {
		if (IsEntityInWater(hitbox, playing.getLevelManager().getCurrentLevel().getLevelData())){
			showText("-" + getCurrentHealth(), (int)hitbox.y - (int)yDrawOffset - 20);
			setCurrentHealth(0);
		}
	}

	public boolean isStealthActive() {
		return stealthActive;
	}

	public void shadowpierce() {
		if (shadowpierceActive)
			return;
		if (powerValue >= 60 && !shadowpierceOnCooldown) {
			shadowpierceActive = true;
			shadowpierceOnCooldown = true;
			changePower(-30);

			for (int i = 0; i < 20; i++) {
				createshadowpierceParticles();
			}

			playing.getGame().getPlaying().setSkillCooldown(0, shadowpierceCooldown);


			showText("Shadowpierce Activated!", (int)hitbox.y - (int)yDrawOffset - 40, Color.ORANGE);
		} else if (shadowpierceOnCooldown) {
			float remaining = playing.getGame().getPlaying().getRemainingCooldown(0) / 1000f;
			showText(String.format("%.1fs cooldown left!", remaining), (int)hitbox.y - (int)yDrawOffset - 20, Color.GRAY);
		} else if (powerValue < 60){
			showText("Not enough mana!", (int)hitbox.y - (int)yDrawOffset - 20, Color.GRAY);
		}
	}

	public int getInitialY() { return (int)hitbox.y - (int)yDrawOffset -20; }

	public void activateStealth() {
		if (powerValue >= 50 && !stealthActive && !stealthOnCooldown) {
			stealthActive = true;
			stealthTick = 0;
			stealthOnCooldown = true;
			changePower(-50);


			playing.getGame().getAudioPlayer().playEffect(AudioPlayer.STEALTH_EFFECT);
			playing.getGame().getPlaying().setSkillCooldown(2, stealthCooldown);
			showText("Stealth Mode Activated!", (int)hitbox.y - (int)yDrawOffset - 40, Color.ORANGE);
		} else if (stealthOnCooldown) {
			float remaining = playing.getGame().getPlaying().getRemainingCooldown(2) / 1000f;
			showText(String.format("%.1fs cooldown left!", remaining), (int)hitbox.y - (int)yDrawOffset - 20, Color.GRAY);
		} else if (powerValue < 50){
			showText("Not enough mana!", (int)hitbox.y - (int)yDrawOffset - 20, Color.GRAY);
		}
	}

	public void activateShadowwMonarch() {
		if (powerValue >= 40 && !shadowMonarchActive && !shadowMonarchOnCooldown) {
			shadowMonarchActive = true;
			auraActive = true;
			shadowMonarchTick = 0;
			auraTick = 0;
			shadowMonarchOnCooldown = true;
			normalWalkSpeed = walkSpeed;
			walkSpeed *= shadowMonarchMultiplier;
			changePower(-80);

			playing.getGame().getAudioPlayer().playEffect(AudioPlayer.AURA_EFFECT);
			playing.getGame().getPlaying().setSkillCooldown(1, shadowMonarchCooldown);
			showText("Shadoww Monarch Activated!", (int)hitbox.y - (int)yDrawOffset - 40, Color.ORANGE);
		} else if (shadowMonarchOnCooldown) {
			float remaining = playing.getGame().getPlaying().getRemainingCooldown(1) / 1000f;
			showText(String.format("%.1fs cooldown left!", remaining), (int)hitbox.y - (int)yDrawOffset - 20, Color.GRAY);
		} else if (powerValue < 40){
			showText("Not enough mana!", (int)hitbox.y - (int)yDrawOffset - 20, Color.GRAY);
		}
	}

	public void handleGamePause(boolean isPaused) {
		if (auraEffectPlaying) {
			if (isPaused) {
				playing.getGame().getAudioPlayer().stopEffect(AudioPlayer.AURA_EFFECT);
			} else if (shadowMonarchActive && getCurrentHealth() > 0) {
				playing.getGame().getAudioPlayer().continueEffect(AudioPlayer.AURA_EFFECT);
			}
		}
	}

	private void checkSpikesTouched() {
		playing.checkSpikesTouched(this);
	}

	private void checkPotionTouched() {
		playing.checkPotionTouched(hitbox);
	}

	private void checkAttack() {
		if (attackChecked || aniIndex != 1)
			return;
		attackChecked = true;

		if (shadowpierceActive)
			attackChecked = false;


		int damageMultiplier = shadowMonarchActive ? AURA_DAMAGE_MULTIPLIER : 1;

		if (playerLevel > 1) {
			float levelMultiplier = 1 + (Math.min(playerLevel, 5) - 1) * 0.5f;
			damageMultiplier *= levelMultiplier;
		}

		if (comboCount > 0) {
			float comboMultiplier = 1 + (comboCount * 0.1f);
			damageMultiplier *= comboMultiplier;
		}

		playing.checkEnemyHit(attackBox, damageMultiplier);
		boolean enemyHit = playing.wasEnemeyHit();

		if (enemyHit) {
			incrementCombo();
		}

		playing.getGame().getAudioPlayer().playAttackSound();
	}

	private void setAttackBoxOnRightSide() {
		attackBox.x = hitbox.x + hitbox.width - (int) (Game.SCALE * 5);
	}

	private void setAttackBoxOnLeftSide() {
		attackBox.x = hitbox.x - hitbox.width - (int) (Game.SCALE * 10);
	}

	private void updateAttackBox() {
		if (right && left) {
			if (flipW == 1) {
				setAttackBoxOnRightSide();
			} else {
				setAttackBoxOnLeftSide();
			}

		} else if (right || (shadowpierceActive && flipW == 1))
			setAttackBoxOnRightSide();
		else if (left || (shadowpierceActive && flipW == -1))
			setAttackBoxOnLeftSide();

		attackBox.y = hitbox.y + (Game.SCALE * 10);
	}

	private void updateHealthBar() {
		healthWidth = (int) ((getCurrentHealth() / (float) getMaxHealth()) * healthBarWidth);
	}

	private void updatePowerBar() {
		powerWidth = (int) ((powerValue / (float) powerMaxValue) * powerBarWidth);

		powerGrowTick++;
		if (powerGrowTick >= powerGrowSpeed) {
			powerGrowTick = 0;
			changePower(1);
		}
	}

	public void render(Graphics g, int lvlOffset) {

		particleSystem.render(g, lvlOffset);


		java.awt.Composite originalComposite = ((java.awt.Graphics2D)g).getComposite();

		if (stealthActive) {
			java.awt.AlphaComposite alphaComposite = java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, stealthOpacity);
			((java.awt.Graphics2D)g).setComposite(alphaComposite);
		}

		if (auraActive) {
			Graphics2D g2d = (Graphics2D)g;
			Composite originalComposite2 = g2d.getComposite();


			g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.15f));
			g2d.setColor(new Color(100, 180, 255, 40));

			g2d.setComposite(originalComposite2);
		}

		g.drawImage(animations[state][aniIndex], (int) (hitbox.x - xDrawOffset) - lvlOffset + flipX, (int) (hitbox.y - yDrawOffset + (int) (pushDrawOffset)), width * flipW, height, null);

		((java.awt.Graphics2D)g).setComposite(originalComposite);

		drawFloatingText(g, lvlOffset);
		drawUI(g);


	}

	
	private void drawUI(Graphics g) {
		
		drawCustomStatusBar(g);

		
		if (comboCount > 0) {
			g.setFont(LEVEL_FONT);
			String comboText = "COMBO: " + comboCount + "x";
			g.setColor(TEXT_SHADOW);
			g.drawString(comboText, 20 + 2, statusBarY + powerBarYStart + statusBarHeight + 5 + 2);
			g.setColor(comboColor);
			g.drawString(comboText, 20, statusBarY + powerBarYStart + statusBarHeight + 5);
		}
	}

	private void drawCustomStatusBar(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;

		Composite originalComposite = g2d.getComposite();

		int extendedStatusBarHeight = statusBarHeight + 30; 

		GradientPaint backgroundGradient = new GradientPaint(
				statusBarX, statusBarY,
				new Color(20, 25, 45),
				statusBarX, statusBarY + extendedStatusBarHeight,
				new Color(40, 45, 65)
		);
		g2d.setPaint(backgroundGradient);

		RoundRectangle2D.Float statusBarRect = new RoundRectangle2D.Float(
				statusBarX, statusBarY, statusBarWidth, extendedStatusBarHeight, 15, 15);
		g2d.fill(statusBarRect);

		g2d.setColor(new Color(60, 70, 100, 100));
		g2d.setStroke(new BasicStroke(2f));
		g2d.draw(statusBarRect);

		
		g2d.setColor(new Color(80, 90, 120));
		g2d.setStroke(new BasicStroke(1.5f));
		g2d.draw(new RoundRectangle2D.Float(
				statusBarX, statusBarY, statusBarWidth, extendedStatusBarHeight, 15, 15));

		
		int centerPadding = 20;
		int barWidth = statusBarWidth - (centerPadding * 2);
		int barX = statusBarX + centerPadding;

		
		int baseY = statusBarY + 25; 
		int elementSpacing = 25; 

		
		g2d.setFont(new Font("Arial", Font.BOLD, 12));
		String healthText = "HP: " + getCurrentHealth() + "/" + getMaxHealth();
		g2d.setColor(Color.WHITE);
		g2d.drawString(healthText, barX, baseY);

		
		int barHeight = 8; 
		int textToBarGap = 10; 

		RoundRectangle2D.Float healthBarContainer = new RoundRectangle2D.Float(
				barX, baseY + textToBarGap,
				barWidth, barHeight,
				4, 4
		);
		g2d.setColor(new Color(30, 30, 40));
		g2d.fill(healthBarContainer);

		
		int calculatedHealthWidth = (int)((getCurrentHealth() / (float)getMaxHealth()) * barWidth);

		
		if (calculatedHealthWidth > 0) {
			g2d.setColor(new Color(220, 60, 60));
			RoundRectangle2D.Float healthBarRect = new RoundRectangle2D.Float(
					barX, baseY + textToBarGap,
					calculatedHealthWidth, barHeight,
					4, 4
			);
			g2d.fill(healthBarRect);
		}

		
		int mpY = baseY + textToBarGap + barHeight + elementSpacing;
		String powerText = "MP: " + powerValue + "/" + powerMaxValue;
		g2d.setColor(Color.WHITE);
		g2d.drawString(powerText, barX, mpY);

		
		RoundRectangle2D.Float powerBarContainer = new RoundRectangle2D.Float(
				barX, mpY + textToBarGap,
				barWidth, barHeight,
				4, 4
		);
		g2d.setColor(new Color(30, 30, 40));
		g2d.fill(powerBarContainer);

		
		int calculatedPowerWidth = (int)((powerValue / (float)powerMaxValue) * barWidth);

		
		if (calculatedPowerWidth > 0) {
			g2d.setColor(new Color(65, 105, 225)); 
			RoundRectangle2D.Float powerBarRect = new RoundRectangle2D.Float(
					barX, mpY + textToBarGap,
					calculatedPowerWidth, barHeight,
					4, 4
			);
			g2d.fill(powerBarRect);
		}

		
		int expY = mpY + textToBarGap + barHeight + elementSpacing;

		
		g2d.setFont(new Font("Arial", Font.BOLD, 14));
		String levelText = "Lv: " + playerLevel;
		g2d.setColor(Color.WHITE);
		g2d.drawString(levelText, barX, expY);

		
		String expText = "EXP: " + currentExp + "/" + expToNextLevel;
		g2d.drawString(expText, barX + 70, expY);

		
		RoundRectangle2D.Float expBarContainer = new RoundRectangle2D.Float(
				barX, expY + textToBarGap,
				barWidth, barHeight,
				4, 4
		);
		g2d.setColor(new Color(30, 30, 40));
		g2d.fill(expBarContainer);

		
		int calculatedExpWidth = (int)((currentExp / (float)expToNextLevel) * barWidth);

		
		if (calculatedExpWidth > 0) {
			g2d.setColor(new Color(255, 215, 0)); 
			RoundRectangle2D.Float expBarRect = new RoundRectangle2D.Float(
					barX, expY + textToBarGap,
					calculatedExpWidth, barHeight,
					4, 4
			);
			g2d.fill(expBarRect);
		}

		
		g2d.setComposite(originalComposite);

		
		if (shadowMonarchActive) {
			g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
					0.3f + (float)Math.sin(System.currentTimeMillis() / 300.0) * 0.1f));
			g2d.setColor(new Color(100, 180, 255));
			g2d.setStroke(new BasicStroke(3f));
			g2d.draw(new RoundRectangle2D.Float(
					statusBarX - 3, statusBarY - 3,
					statusBarWidth + 6, extendedStatusBarHeight + 6,
					18, 18));
			g2d.setComposite(originalComposite);
		}

		
		if (playerJustHit && System.currentTimeMillis() - lastHitTime < HIT_EFFECT_DURATION) {
			
			float timeRatio = 1 - ((System.currentTimeMillis() - lastHitTime) / (float)HIT_EFFECT_DURATION);
			float pulseAlpha = 0.4f * timeRatio + (float)Math.sin(System.currentTimeMillis() / 100.0) * 0.1f * timeRatio;

			g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, pulseAlpha));
			g2d.setColor(new Color(255, 0, 0));
			g2d.fill(statusBarRect);
			g2d.setComposite(originalComposite);
		}
	}

	private void updateAnimationTick() {
		aniTick++;
		if (aniTick >= ANI_SPEED) {
			aniTick = 0;
			aniIndex++;
			if (aniIndex >= GetSpriteAmount(state)) {
				aniIndex = 0;
				attacking = false;
				attackChecked = false;
				if (state == HIT) {
					newState(IDLE);
					airSpeed = 0f;
					if (!IsFloor(hitbox, 0, lvlData))
						inAir = true;
				}
			}
		}
	}

	private void setAnimation() {
		int startAni = state;

		if (state == HIT)
			return;

		if (moving)
			state = RUNNING;
		else
			state = IDLE;

		if (inAir) {
			if (airSpeed < 0)
				state = JUMP;
			else
				state = FALLING;
		}

		if (shadowpierceActive) {
			state = ATTACK;
			aniIndex = 1;
			aniTick = 0;
			return;
		}

		if (attacking) {
			state = ATTACK;
			if (startAni != ATTACK) {
				aniIndex = 1;
				aniTick = 0;
				return;
			}
		}
		if (startAni != state)
			resetAniTick();
	}

	private void resetAniTick() {
		aniTick = 0;
		aniIndex = 0;
	}

	private void updatePos() {
		moving = false;

		if (jump)
			jump();

		if (!inAir)
			if (!shadowpierceActive)
				if ((!left && !right) || (right && left))
					return;

		float xSpeed = 0;

		if (left && !right) {
			xSpeed -= walkSpeed;
			flipX = width;
			flipW = -1;
		}
		if (right && !left) {
			xSpeed += walkSpeed;
			flipX = 0;
			flipW = 1;
		}

		if (shadowpierceActive) {
			if ((!left && !right) || (left && right)) {
				if (flipW == -1)
					xSpeed = -walkSpeed;
				else
					xSpeed = walkSpeed;
			}

			xSpeed *= 3;
		}

		if (!inAir)
			if (!IsEntityOnFloor(hitbox, lvlData))
				inAir = true;

		if (inAir && !shadowpierceActive) {
			if (CanMoveHere(hitbox.x, hitbox.y + airSpeed, hitbox.width, hitbox.height, lvlData)) {
				hitbox.y += airSpeed;
				airSpeed += GRAVITY;
				updateXPos(xSpeed);
			} else {
				hitbox.y = GetEntityYPosUnderRoofOrAboveFloor(hitbox, airSpeed);
				if (airSpeed > 0)
					resetInAir();
				else
					airSpeed = fallSpeedAfterCollision;
				updateXPos(xSpeed);
			}

		} else
			updateXPos(xSpeed);
		moving = true;
	}

	private void jump() {
		if (inAir)
			return;
		playing.getGame().getAudioPlayer().playEffect(AudioPlayer.JUMP);
		inAir = true;
		airSpeed = jumpSpeed;
	}

	private void resetInAir() {
		inAir = false;
		airSpeed = 0;
	}

	private void updateXPos(float xSpeed) {
		if (CanMoveHere(hitbox.x + xSpeed, hitbox.y, hitbox.width, hitbox.height, lvlData))
			hitbox.x += xSpeed;
		else {
			hitbox.x = GetEntityXPosNextToWall(hitbox, xSpeed);
			if (shadowpierceActive) {
				shadowpierceActive = false;
				shadowpierceTick = 0;
			}
		}
	}

	private boolean playerJustHit = false;
	private long lastHitTime = 0;
	private final long HIT_EFFECT_DURATION = 500; 

	public void changeHealth(int value) {
		if (value < 0) {
			if (state == HIT)
				return;
			else
				newState(HIT);

			
			playerJustHit = true;
			lastHitTime = System.currentTimeMillis();
			
			resetCombo();
		}

		setCurrentHealth(getCurrentHealth()+value);
		setCurrentHealth(Math.max(Math.min(getCurrentHealth(), getMaxHealth()), 0));
	}



	public void changeHealth(int value, Enemy e) {
		if (state == HIT)
			return;
		changeHealth(value);

		playing.getGame().getAudioPlayer().playEffect(AudioPlayer.HIT);
		showText(String.valueOf(value), (int)hitbox.y - (int)yDrawOffset - 20);

		pushBackOffsetDir = UP;
		pushDrawOffset = 0;

		if (e.getHitbox().x < hitbox.x)
			pushBackDir = RIGHT;
		else
			pushBackDir = LEFT;
	}

	public void kill() {
		showText("-" + getMaxHealth(), (int)hitbox.y - (int)yDrawOffset - 20);
		setCurrentHealth(0);
	}

	public void changePower(int value) {
		powerValue += value;
		powerValue = Math.max(Math.min(powerValue, powerMaxValue), 0);
	}

	private void loadAnimations() {
		BufferedImage img = LoadSave.GetSpriteAtlas(LoadSave.PLAYER_ATLAS);
		animations = new BufferedImage[7][8];
		for (int j = 0; j < animations.length; j++)
			for (int i = 0; i < animations[j].length; i++)
				animations[j][i] = img.getSubimage(i * 64, j * 40, 64, 40);

	}

	public void loadLvlData(int[][] lvlData) {
		this.lvlData = lvlData;
		if (!IsEntityOnFloor(hitbox, lvlData))
			inAir = true;
	}

	public void resetDirBooleans() {
		left = false;
		right = false;
	}

	public void setAttacking(boolean attacking) {
		if (powerValue >= 20) {
			this.attacking = attacking;
			changePower(-20);
		}
	}

	public void setLeft(boolean left) {
		this.left = left;
	}

	public void setRight(boolean right) {
		this.right = right;
	}

	public void setJump(boolean jump) {
		this.jump = jump;
	}


	public void createLevelCheckpoint() {

		checkpointPlayerLevel = playerLevel;
		checkpointCurrentExp = currentExp;
		checkpointExpToNextLevel = expToNextLevel;
		checkpointMaxHealth = getMaxHealth();
		checkpointPowerMaxValue = powerMaxValue;
	}


	public void startNextLevel() {
		
		createLevelCheckpoint();

		
		resetAll();
	}

	
	public void resetAll() {
		resetDirBooleans();

		inAir = false;
		attacking = false;
		moving = false;
		airSpeed = 0f;
		state = IDLE;
		setCurrentHealth(getMaxHealth());
		shadowpierceActive = false;
		shadowpierceTick = 0;
		powerValue = powerMaxValue;

		stealthActive = false;
		stealthTick = 0;

		shadowMonarchActive = false;
		shadowMonarchTick = 0;
		walkSpeed = Game.SCALE * 1.0f;

		shadowBlastActive = false;
		shadowBlastTick = 0;

		resetCooldowns();

		
		particleSystem = new ParticleSystem();

		hitbox.x = x;
		hitbox.y = y;
		resetAttackBox();

		if (!IsEntityOnFloor(hitbox, lvlData))
			inAir = true;
	}

	
	public void retryCurrentLevel() {
		
		playerLevel = checkpointPlayerLevel;
		currentExp = checkpointCurrentExp;
		expToNextLevel = checkpointExpToNextLevel;
		setMaxHealth(checkpointMaxHealth);
		powerMaxValue = checkpointPowerMaxValue;

		
		resetAll();
	}


	private void resetAttackBox() {
		if (flipW == 1)
			setAttackBoxOnRightSide();
		else
			setAttackBoxOnLeftSide();
	}

	public int getTileY() {
		return tileY;
	}



	public void resetCooldowns() {
		shadowMonarchOnCooldown = false;
		stealthOnCooldown = false;
		shadowpierceOnCooldown = false;
		shadowBlastOnCooldown = false;
	}

	public void notifyCooldownComplete(int skillIndex) {
		switch (skillIndex) {
			case 0:
				shadowpierceOnCooldown = false;
				break;
			case 1:
				shadowMonarchOnCooldown = false;
				break;
			case 2:
				stealthOnCooldown = false;
			case 3:
				shadowBlastOnCooldown = false;
				break;
		}
	}

	public void activateShadowBlast() {
		if (powerValue >= 70 && !shadowBlastActive && !shadowBlastOnCooldown) {
			shadowBlastActive = true;
			shadowBlastTick = 0;
			shadowBlastOnCooldown = true;
			changePower(-70);

			
			for (int i = 0; i < 60; i++) {
				createShadowBlastParticles();
			}

			
			performShadowBlastDamage();

			
			playing.getGame().getPlaying().setSkillCooldown(3, shadowBlastCooldown);
			showText("Shadow Blast!", (int)hitbox.y - (int)yDrawOffset - 40, Color.ORANGE);
		} else if (shadowBlastOnCooldown) {
			float remaining = playing.getGame().getPlaying().getRemainingCooldown(3) / 1000f;
			showText(String.format("%.1fs cooldown left!", remaining), (int)hitbox.y - (int)yDrawOffset - 20, Color.GRAY);
		} else if (powerValue < 70) {
			showText("Not enough mana!", (int)hitbox.y - (int)yDrawOffset - 20, Color.GRAY);
		}
	}

	private void createMovementParticles() {
		if (moving && (left || right)) {
			
			float particleX = hitbox.x + (flipW == 1 ? hitbox.width : 0);
			float particleY = hitbox.y + hitbox.height - 2; 

			
			particleSystem.createMovementParticles(
					particleX, particleY,
					flipW == 1,  
					shadowMonarchActive  
			);

			
			if (shadowMonarchActive && random.nextInt(3) == 0) {
				particleSystem.createMovementParticles(
						particleX, particleY - 5,
						flipW == 1,
						true
				);
			}
		}
	}

	private void updateAura() {
		if (shadowMonarchActive) {
			auraTick++;
			if (auraTick >= shadowMonarchDuration) {
				auraActive = false;
			} else {
				
				particleSystem.createShadowAuraParticles(
						hitbox.x + hitbox.width/2,  
						hitbox.y + hitbox.height/2, 
						hitbox.width               
				);
			}
		}
	}

	public void updateScreenEffects() {
		
		float healthPercentage = (float) getCurrentHealth() / getMaxHealth();

		if (shadowMonarchActive) {
			
			playing.getGame().getPlaying().showBorderEffect(true, new Color(0, 0, 0));
			playing.getGame().getPlaying().setPulsatingEffect(false);
			playing.getGame().getPlaying().setBorderWidth(150);
		} else if (healthPercentage <= 0.3f) {  
			
			playing.getGame().getPlaying().showBorderEffect(true, new Color(200, 0, 0));
			playing.getGame().getPlaying().setPulsatingEffect(true);
			playing.getGame().getPlaying().setBorderWidth(150);

			
			if (healthPercentage <= 0.15f) {  
				playing.getGame().getPlaying().setBorderWidth(100);  
			}
		} else {
			
			playing.getGame().getPlaying().showBorderEffect(false, null);
		}
	}

	public void addExp(int amount) {
		currentExp += amount;

		while (currentExp >= expToNextLevel) {
			levelUp();
		}

		showText("+" + amount + " EXP", (int)hitbox.y - (int)yDrawOffset - 40, Color.YELLOW);
	}

	private void levelUp() {
		currentExp -= expToNextLevel;
		playerLevel++;

		setMaxHealth(getMaxHealth() + 20);
		setCurrentHealth(getMaxHealth());
		powerMaxValue += 20;
		powerValue = powerMaxValue;

		
		expToNextLevel = (int)(expToNextLevel * EXP_GROWTH_RATE);

		playing.getGame().getAudioPlayer().playEffect(AudioPlayer.LEVEL_UP);
		//showText("LEVEL UP!", (int)hitbox.y - (int)yDrawOffset - 60, Color.ORANGE);

		playing.showLevelUpEffect(playerLevel);

		for (int i = 0; i < 30; i++) {
			particleSystem.createLevelUpParticles(
					hitbox.x + hitbox.width/2,
					hitbox.y + hitbox.height/2
			);
		}
	}
}