package entities;

import static utilz.Constants.Dialogue.*;
import static utilz.Constants.Directions.LEFT;
import static utilz.Constants.EnemyConstants.*;
import static utilz.HelpMethods.CanMoveHere;
import static utilz.HelpMethods.IsFloor;

import audio.AudioPlayer;
import gamestates.Playing;

public class Velgorn extends Enemy {

	public Velgorn(float x, float y) {
		super(x, y, VELGORN_WIDTH, VELGORN_HEIGHT, VELGORN);
		initHitbox(18, 22);
		initAttackBox(20, 20, 20);
	}

	public void update(int[][] lvlData, Playing playing) {
		updateBehavior(lvlData, playing);
		updateAnimationTick();
		updateAttackBoxFlip();
	}

	private void updateBehavior(int[][] lvlData, Playing playing) {
		if (firstUpdate)
			firstUpdateCheck(lvlData);

		if (inAir)
			inAirChecks(lvlData, playing);
		else {
			switch (state) {
			case IDLE:
				if (IsFloor(hitbox, lvlData))
					newState(RUNNING);
				else
					inAir = true;
				break;
			case RUNNING:
				if (canSeePlayer(lvlData, playing.getPlayer())) {
					turnTowardsPlayer(playing.getPlayer());
					if (isPlayerCloseForAttack(playing.getPlayer()))
						newState(ATTACK);
				}

				move(lvlData);
				break;
			case ATTACK:
				if (aniIndex == 0) {
					playing.getGame().getAudioPlayer().playEffect(AudioPlayer.VELGORN_ATTACK);
					setAttackChecked(false);
				}
				else if (aniIndex == 3) {
					if (!isAttackChecked())
						checkPlayerHit(attackBox, playing.getPlayer());

					attackMove(lvlData, playing);
				}
				break;
	 		case HIT:
				if (aniIndex <= GetSpriteAmount(getEnemyType(), state) - 2)
					pushBack(pushBackDir, lvlData, 2f);
				updatePushBackDrawOffset();
				break;
			}
		}
	}

	protected void attackMove(int[][] lvlData, Playing playing) {
		float xSpeed = 0;

		if (walkDir == LEFT)
			xSpeed = -walkSpeed;
		else
			xSpeed = walkSpeed;

		if (CanMoveHere(hitbox.x + xSpeed * 4, hitbox.y, hitbox.width, hitbox.height, lvlData))
			if (IsFloor(hitbox, xSpeed * 4, lvlData)) {
				hitbox.x += xSpeed * 4;
				return;
			}
		newState(IDLE);
		playing.addDialogue((int) hitbox.x, (int) hitbox.y, EXCLAMATION);
	}
}
