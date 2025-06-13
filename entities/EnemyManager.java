package entities;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import java.util.Random;

import audio.AudioPlayer;
import gamestates.Playing;
import levels.Level;
import levels.LevelManager;
import utilz.LoadSave;
import static utilz.Constants.EnemyConstants.*;

public class EnemyManager {

	private Playing playing;
	private LevelManager lvlManager;
	private BufferedImage[][] slorackArr, radobaanArr, velgornArr, hellFlameArr;
	private Level currentLevel;

	public EnemyManager(Playing playing) {
		this.playing = playing;
		loadEnemyImgs();
	}

	public void loadEnemies(Level level) {
		this.currentLevel = level;
	}

	public void update(int[][] lvlData) {
		boolean isAnyActive = false;
		boolean isBossActive = false;
		for (Slorack c : currentLevel.getSloracks())
			if (c.isActive()) {
				c.update(lvlData, playing);
				isAnyActive = true;
			}

		for (Radobaan p : currentLevel.getRadobaans())
			if (p.isActive()) {
				p.update(lvlData, playing);
				isAnyActive = true;
			}

		for (Velgorn s : currentLevel.getVelgorns())
			if (s.isActive()) {
				s.update(lvlData, playing);
				isAnyActive = true;
			}

		for (HellFlame h : currentLevel.getHellFlame())
			if (h.isActive()) {
				h.update(lvlData, playing);
				isAnyActive = true;
				isBossActive = true;
			}
		if (!isAnyActive || (!isBossActive && playing.getLevelManager().getLevelIndex() == 2))
			playing.setLevelCompleted(true);
	}

	public void draw(Graphics g, int xLvlOffset) {
		drawSlorack(g, xLvlOffset);
		drawRadobaans(g, xLvlOffset);
		drawVelgorns(g, xLvlOffset);
		drawHellFlame(g, xLvlOffset);
	}

	public void checkEnemiesInRadius(float centerX, float centerY, int radius, float damageMultiplier) {
		for (Slorack e : currentLevel.getSloracks()) {
			if (e.getCurrentHealth() <= 0)
				continue;
			float enemyCenterX = e.getHitbox().x + e.getHitbox().width / 2;
			float enemyCenterY = e.getHitbox().y + e.getHitbox().height / 2;

			float deltaX = enemyCenterX - centerX;
			float deltaY = enemyCenterY - centerY;
			float distance = (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY);

			if (distance <= radius) {
				float damageRatio = 1.0f - (distance / radius);
				int damage = (int) (30 * damageRatio * damageMultiplier);

				damage = Math.max(10, damage);

				e.hurt(damage, playing);
			}
		}

		for (Velgorn e : currentLevel.getVelgorns()) {
			if (e.getCurrentHealth() <= 0)
				continue;
			float enemyCenterX = e.getHitbox().x + e.getHitbox().width / 2;
			float enemyCenterY = e.getHitbox().y + e.getHitbox().height / 2;

			float deltaX = enemyCenterX - centerX;
			float deltaY = enemyCenterY - centerY;
			float distance = (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY);

			if (distance <= radius) {
				float damageRatio = 1.0f - (distance / radius);
				int damage = (int) (30 * damageRatio * damageMultiplier);

				damage = Math.max(10, damage);

				e.hurt(damage, playing);
			}
		}

		for (Radobaan e : currentLevel.getRadobaans()) {
			if (e.getCurrentHealth() <= 0)
				continue;
			float enemyCenterX = e.getHitbox().x + e.getHitbox().width / 2;
			float enemyCenterY = e.getHitbox().y + e.getHitbox().height / 2;

			float deltaX = enemyCenterX - centerX;
			float deltaY = enemyCenterY - centerY;
			float distance = (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY);

			if (distance <= radius) {
				float damageRatio = 1.0f - (distance / radius);
				int damage = (int) (30 * damageRatio * damageMultiplier);

				damage = Math.max(10, damage);

				e.hurt(damage, playing);
			}
		}

		for (HellFlame e : currentLevel.getHellFlame()) {
			if (e.getCurrentHealth() <= 0)
				continue;
			float enemyCenterX = e.getHitbox().x + e.getHitbox().width / 2;
			float enemyCenterY = e.getHitbox().y + e.getHitbox().height / 2;

			float deltaX = enemyCenterX - centerX;
			float deltaY = enemyCenterY - centerY;
			float distance = (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY);

			if (distance <= radius) {
				float damageRatio = 1.0f - (distance / radius);
				int damage = (int) (30 * damageRatio * damageMultiplier);

				damage = Math.max(10, damage);

				e.hurt(damage, playing);
			}
		}
	}


	private void drawVelgorns(Graphics g, int xLvlOffset) {
		for (Velgorn s : currentLevel.getVelgorns())
			if (s.isActive()) {
				g.drawImage(velgornArr[s.getState()][s.getAniIndex()], (int) s.getHitbox().x - xLvlOffset - VELGORN_DRAWOFFSET_X + s.flipX(),
						(int) s.getHitbox().y - VELGORN_DRAWOFFSET_Y + (int) s.getPushDrawOffset(), VELGORN_WIDTH * s.flipW(), VELGORN_HEIGHT, null);

				s.drawHealthBar(g, s, xLvlOffset, VELGORN_DRAWOFFSET_Y);

				s.drawFloatingText(g, xLvlOffset);

			}
	}

	private void drawRadobaans(Graphics g, int xLvlOffset) {
		for (Radobaan p : currentLevel.getRadobaans())
			if (p.isActive()) {
				g.drawImage(radobaanArr[p.getState()][p.getAniIndex()], (int) p.getHitbox().x - xLvlOffset - RADOBAAN_DRAWOFFSET_X + p.flipX(),
						(int) p.getHitbox().y - RADOBAAN_DRAWOFFSET_Y + (int) p.getPushDrawOffset(), RADOBAAN_WIDTH * p.flipW(), RADOBAAN_HEIGHT, null);

				p.drawHealthBar(g, p, xLvlOffset, RADOBAAN_DRAWOFFSET_Y);

				p.drawFloatingText(g, xLvlOffset);


				
			}
	}

	private void drawSlorack(Graphics g, int xLvlOffset) {
		for (Slorack c : currentLevel.getSloracks())
			if (c.isActive()) {

				g.drawImage(slorackArr[c.getState()][c.getAniIndex()], (int) c.getHitbox().x - xLvlOffset - SLORACK_DRAWOFFSET_X + c.flipX(),
						(int) c.getHitbox().y - SLORACK_DRAWOFFSET_Y + (int) c.getPushDrawOffset(), SLORACK_WIDTH * c.flipW(), SLORACK_HEIGHT, null);


				c.drawHealthBar(g, c, xLvlOffset, SLORACK_DRAWOFFSET_Y);

				c.drawFloatingText(g, xLvlOffset);


//				c.drawHitbox(g, xLvlOffset);
//				c.drawAttackBox(g, xLvlOffset);

			}

	}

	private void drawHellFlame(Graphics g, int xLvlOffset) {
		for (HellFlame c : currentLevel.getHellFlame())
			if (c.isActive()) {

				g.drawImage(hellFlameArr[c.getState()][c.getAniIndex()], (int) c.getHitbox().x - xLvlOffset - HELLFLAME_DRAWOFFSET_X + c.flipX(),
						(int) c.getHitbox().y - HELLFLAME_DRAWOFFSET_Y + (int) c.getPushDrawOffset(), HELLFLAME_WIDTH * c.flipW(), HELLFLAME_HEIGHT, null);


				c.drawHealthBar(g, c, xLvlOffset, SLORACK_DRAWOFFSET_Y);

				c.drawFloatingText(g, xLvlOffset);

//				c.drawHitbox(g, xLvlOffset);
//				c.drawAttackBox(g, xLvlOffset);

			}
	}

	private boolean isEnemyHit;

	public boolean isEnemyHit() { return isEnemyHit; }
	public void setIsEnemyHit(boolean isHit) { this.isEnemyHit = isHit; };
	public void checkEnemyHit(Rectangle2D.Float attackBox, int damageMultiplier) {
		for (Slorack c : currentLevel.getSloracks())
			if (c.isActive()) {
				if (c.getState() != DEAD && c.getState() != HIT) {
					if (attackBox.intersects(c.getHitbox())) {
						playing.getGame().getAudioPlayer().playEffect(AudioPlayer.SLORACK_HIT);

						int ran = (playing.getShadowpierce()) ? new Random().nextInt(50, 80) : new Random().nextInt(20, 30);
						int exp = new Random().nextInt(100, 200);

						c.hurt(ran * damageMultiplier, playing);
						if (c.getState() == DEAD) {
							playing.getPlayer().addExp(exp);
						}
						isEnemyHit = true;
						return;
					}
				}
			}



		for (Velgorn s : currentLevel.getVelgorns())
			if (s.isActive()) {
				if (s.getState() != DEAD && s.getState() != HIT) {
					if (attackBox.intersects(s.getHitbox())) {
						playing.getGame().getAudioPlayer().playEffect(AudioPlayer.VELGORN_HIT);

						int ran = (playing.getShadowpierce()) ? new Random().nextInt(50, 80) : new Random().nextInt(20, 30);
						int exp = new Random().nextInt(30, 50);

						s.hurt(ran * damageMultiplier, playing);
						if (s.getState() == DEAD) {
							playing.getPlayer().addExp(exp);
						}
						isEnemyHit = true;
						return;
					}
				}
			}

		for (Radobaan p : currentLevel.getRadobaans())
			if (p.isActive()) {
				if (p.getState() == ATTACK && p.getAniIndex() >= 3)
					return;
				else {
					if (p.getState() != DEAD && p.getState() != HIT)
						if (attackBox.intersects(p.getHitbox())) {
							playing.getGame().getAudioPlayer().playEffect(AudioPlayer.RADOBAAN_HIT);

							int ran = (playing.getShadowpierce()) ? new Random().nextInt(50, 80) : new Random().nextInt(20, 30);
							int exp = new Random().nextInt(50, 80);

							p.hurt(ran * damageMultiplier, playing);
							if (p.getState() == DEAD) {
								playing.getPlayer().addExp(exp);
							}
							isEnemyHit = true;
							return;
						}
				}
			}

		for (HellFlame c : currentLevel.getHellFlame())
			if (c.isActive()) {
				if (c.getState() != DEAD && c.getState() != HIT) {
					if (attackBox.intersects(c.getHitbox())) {
						playing.getGame().getAudioPlayer().playEffect(AudioPlayer.SLORACK_HIT);

						int ran = (playing.getShadowpierce()) ? new Random().nextInt(50, 80) : new Random().nextInt(20, 30);
						int exp = new Random().nextInt(200, 500);

						c.hurt(ran * damageMultiplier, playing);
						if (c.getState() == DEAD) {
							playing.getPlayer().addExp(exp);
						}
						isEnemyHit = true;
						return;
					}
				}
			}
	}

	private void loadEnemyImgs() {
		slorackArr = getImgArr(LoadSave.GetSpriteAtlas(LoadSave.SLORACK_SPRITE), 9, 5, SLORACK_WIDTH_DEFAULT, SLORACK_HEIGHT_DEFAULT);
		radobaanArr = getImgArr(LoadSave.GetSpriteAtlas(LoadSave.RADOBAAN_ATLAS), 8, 5, RADOBAAN_WIDTH_DEFAULT, RADOBAAN_HEIGHT_DEFAULT);
		velgornArr = getImgArr(LoadSave.GetSpriteAtlas(LoadSave.VELGORN_ATLAS), 8, 5, VELGORN_WIDTH_DEFAULT, VELGORN_HEIGHT_DEFAULT);
		hellFlameArr = getImgArr(LoadSave.GetSpriteAtlas(LoadSave.BOSS), 22, 5, HELLFLAME_WIDTH_DEFAULT, HELLFLAME_HEIGHT_DEFAULT);
	}

	private BufferedImage[][] getImgArr(BufferedImage atlas, int xSize, int ySize, int spriteW, int spriteH) {
		BufferedImage[][] tempArr = new BufferedImage[ySize][xSize];
		for (int j = 0; j < tempArr.length; j++)
			for (int i = 0; i < tempArr[j].length; i++)
				tempArr[j][i] = atlas.getSubimage(i * spriteW, j * spriteH, spriteW, spriteH);
		return tempArr;
	}

	public void resetAllEnemies() {
		for (Slorack c : currentLevel.getSloracks())
			c.resetEnemy();
		for (HellFlame c : currentLevel.getHellFlame())
			c.resetEnemy();
		for (Radobaan p : currentLevel.getRadobaans())
			p.resetEnemy();
		for (Velgorn s : currentLevel.getVelgorns())
			s.resetEnemy();
	}

}
