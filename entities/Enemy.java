package entities;

import static utilz.Constants.ANI_SPEED;
import static utilz.Constants.EnemyConstants.*;
import static utilz.Constants.GRAVITY;
import static utilz.HelpMethods.*;

import java.awt.geom.Rectangle2D;

import audio.AudioPlayer;
import gamestates.Playing;

import static utilz.Constants.Directions.*;

import main.Game;

public abstract class Enemy extends Entity {
	private int enemyType;
	private boolean attackChecked;

	protected boolean firstUpdate = true;
	protected int walkDir = LEFT;
	protected int tileY;
	protected float attackDistance = Game.TILES_SIZE;
	protected boolean active = true;
	protected int attackBoxOffsetX;

	public Enemy(float x, float y, int width, int height, int enemyType) {
		super(x, y, width, height);
		this.enemyType = enemyType;

		setMaxHealth(GetMaxHealth(enemyType));
		setCurrentHealth(getMaxHealth());
		walkSpeed = Game.SCALE * 0.35f;
	}

	protected void updateAttackBox() {
		attackBox.x = hitbox.x - attackBoxOffsetX;
		attackBox.y = hitbox.y;
	}

	protected void updateAttackBoxFlip() {
		if (walkDir == RIGHT)
			attackBox.x = hitbox.x + hitbox.width;
		else
			attackBox.x = hitbox.x - attackBoxOffsetX;

		attackBox.y = hitbox.y;
	}

	protected void initAttackBox(int w, int h, int attackBoxOffsetX) {
		attackBox = new Rectangle2D.Float(x, y, (int) (w * Game.SCALE), (int) (h * Game.SCALE));
		this.attackBoxOffsetX = (int) (Game.SCALE * attackBoxOffsetX);
	}

	protected void firstUpdateCheck(int[][] lvlData) {
		if (!IsEntityOnFloor(hitbox, lvlData))
			inAir = true;
		firstUpdate = false;
	}

	protected void inAirChecks(int[][] lvlData, Playing playing) {
		if (state != HIT && state != DEAD) {
			updateInAir(lvlData);
			playing.getObjectManager().checkSpikesTouched(this);
			if (IsEntityInWater(hitbox, lvlData)) {
				hurt(getCurrentHealth(), playing);
			}
		}
	}

	protected void updateInAir(int[][] lvlData) {
		if (CanMoveHere(hitbox.x, hitbox.y + airSpeed, hitbox.width, hitbox.height, lvlData)) {
			hitbox.y += airSpeed;
			airSpeed += GRAVITY;
		} else {
			inAir = false;
			hitbox.y = GetEntityYPosUnderRoofOrAboveFloor(hitbox, airSpeed);
			tileY = (int) (hitbox.y / Game.TILES_SIZE);
		}
	}

	protected void move(int[][] lvlData) {
		float xSpeed = 0;

		if (walkDir == LEFT)
			xSpeed = -walkSpeed;
		else
			xSpeed = walkSpeed;

		if (CanMoveHere(hitbox.x + xSpeed, hitbox.y, hitbox.width, hitbox.height, lvlData))
			if (IsFloor(hitbox, xSpeed, lvlData)) {
				hitbox.x += xSpeed;
				return;
			}

		changeWalkDir();
	}

	protected void turnTowardsPlayer(Player player) {
		if (player.hitbox.x > hitbox.x)
			walkDir = RIGHT;
		else
			walkDir = LEFT;
	}

	protected boolean canSeePlayer(int[][] lvlData, Player player) {
		if (player.isStealthActive()) {
			return false;
		}

		int playerTileY = (int) (player.getHitbox().y / Game.TILES_SIZE);
		int playerBottomTileY = (int) ((player.getHitbox().y + player.getHitbox().height) / Game.TILES_SIZE);

		if (playerTileY == tileY || playerBottomTileY == tileY ||
				(player.getHitbox().y < hitbox.y + hitbox.height &&
						player.getHitbox().y + player.getHitbox().height > hitbox.y)) {

			if (isPlayerInRange(player)) {
				if (IsSightClear(lvlData, hitbox, player.hitbox))
					return true;
			}
		}
		return false;
	}



	protected boolean isPlayerInRange(Player player) {
		int absValue = (int) Math.abs(player.hitbox.x - hitbox.x);
		return absValue <= attackDistance * 5;
	}

	protected boolean isPlayerCloseForAttack(Player player) {
		int absValue = (int) Math.abs(player.hitbox.x - hitbox.x);
		switch (enemyType) {
			case SLORACK -> {
				return absValue <= attackDistance;
				}
			case VELGORN, HELLFLAME -> {
				return absValue <= attackDistance * 2;
				}
		}
		return false;
	}

	public int getDrawOffsetY() {
		switch (enemyType) {
			case SLORACK:
				return SLORACK_DRAWOFFSET_Y;
			case RADOBAAN:
				return RADOBAAN_DRAWOFFSET_Y;
			case VELGORN:
				return VELGORN_DRAWOFFSET_Y;
			case HELLFLAME:
				return HELLFLAME_DRAWOFFSET_Y;
			default:
				return 0;
		}
	}

	public int getEnemyType() {
		return enemyType;
	}

	public void setEnemyType(int enemyType) {
		this.enemyType = enemyType;
	}

	public void setAttackChecked(boolean attackChecked) {
		this.attackChecked = attackChecked;
	}

	public boolean isAttackChecked() {
		return attackChecked;
	}

	public void hurt(int amount, Playing playing) {
		int textY = (int)hitbox.y - getDrawOffsetY();
		int tempHealth = getCurrentHealth();

		setCurrentHealth(getCurrentHealth()-amount);
		if (getCurrentHealth() <= 0) {
			newState(DEAD);

			showText("-" + tempHealth, enemyType == HELLFLAME ? textY + 150 : textY);
			switch (enemyType){
				case SLORACK -> {
					playing.getGame().getAudioPlayer().playEffect(AudioPlayer.SLORACK_DIE);
				}
				case VELGORN -> {
					playing.getGame().getAudioPlayer().playEffect(AudioPlayer.VELGORN_DIE);
				}
				case RADOBAAN -> {
					playing.getGame().getAudioPlayer().playEffect(AudioPlayer.RADOBAAN_DIE);
				}
				case HELLFLAME -> {
					playing.getGame().getAudioPlayer().playEffect(AudioPlayer.RADOBAAN_DIE);
				}
			}
		}
		else {
			newState(HIT);

			showText("-" + amount, enemyType == HELLFLAME ? textY + 150 : textY);

			if (walkDir == LEFT)
				pushBackDir = RIGHT;
			else
				pushBackDir = LEFT;
			pushBackOffsetDir = UP;
			pushDrawOffset = 0;
		}
	}

	protected void checkPlayerHit(Rectangle2D.Float attackBox, Player player) {
		if (attackBox.intersects(player.hitbox))
			player.changeHealth(-GetEnemyDmg(enemyType), this);
		else {
			if (enemyType == VELGORN)
				return;
		}
		attackChecked = true;
	}

	protected void updateAnimationTick() {
		aniTick++;
		if (aniTick >= ANI_SPEED) {
			aniTick = 0;
			aniIndex++;
			if (aniIndex >= GetSpriteAmount(enemyType, state)) {
				if (enemyType == SLORACK || enemyType == VELGORN || enemyType == HELLFLAME) {
					aniIndex = 0;

					switch (state) {
					case ATTACK, HIT -> state = IDLE;
					case DEAD -> active = false;
					}
				} else if (enemyType == RADOBAAN) {
					if (state == ATTACK)
						aniIndex = 3;
					else {
						aniIndex = 0;
						if (state == HIT) {
							state = IDLE;

						} else if (state == DEAD)
							active = false;
					}
				}
			}
		}
	}

	protected void changeWalkDir() {
		if (walkDir == LEFT)
			walkDir = RIGHT;
		else
			walkDir = LEFT;
	}

	public void resetEnemy() {
		hitbox.x = x;
		hitbox.y = y;
		firstUpdate = true;
		setCurrentHealth(getMaxHealth());
		newState(IDLE);
		active = true;
		airSpeed = 0;

		pushDrawOffset = 0;

	}

	public int flipX() {
		if (walkDir == RIGHT)
			return width;
		else
			return 0;
	}

	public int flipW() {
		if (walkDir == RIGHT)
			return -1;
		else
			return 1;
	}

	public boolean isActive() {
		return active;
	}

	public float getPushDrawOffset() {
		return pushDrawOffset;
	}

}