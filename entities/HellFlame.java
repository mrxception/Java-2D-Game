package entities;

import static utilz.Constants.EnemyConstants.*;
import static utilz.HelpMethods.IsFloor;
import static utilz.Constants.Dialogue.*;

import audio.AudioPlayer;
import gamestates.Playing;

public class HellFlame extends Enemy {

    public HellFlame(float x, float y) {
        super(x, y, HELLFLAME_WIDTH, HELLFLAME_HEIGHT, HELLFLAME);
        initHitbox(62, 66);
        initAttackBox(88, 66, 62);
    }

    public void update(int[][] lvlData, Playing playing) {
        updateBehavior(lvlData, playing);
        updateAnimationTick();
        updateAttackBox();
        updateAttackBoxFlip();
    }

    private void updateBehavior(int[][] lvlData, Playing playing) {
        if (firstUpdate)
            firstUpdateCheck(lvlData);

        if (inAir) {
            inAirChecks(lvlData, playing);
        } else {
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

                    if (inAir)
                        playing.addDialogue((int) hitbox.x, (int) hitbox.y, EXCLAMATION);

                    break;
                case ATTACK:
                    if (aniIndex == 0)
                        setAttackChecked(false);
                    if (aniIndex == 10 && !isAttackChecked()) {
                        playing.getGame().getAudioPlayer().playEffect(AudioPlayer.SLORACK_ATTACK);
                        checkPlayerHit(attackBox, playing.getPlayer());
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

}