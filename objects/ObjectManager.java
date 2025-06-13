package objects;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import audio.AudioPlayer;
import entities.Enemy;
import entities.Player;
import gamestates.Playing;
import levels.Level;
import main.Game;
import utilz.LoadSave;

import static utilz.Constants.ObjectConstants.*;
import static utilz.HelpMethods.CanCannonSeePlayer;
import static utilz.HelpMethods.IsProjectileHittingLevel;
import static utilz.Constants.Projectiles.*;

public class ObjectManager {

	private Playing playing;
	private BufferedImage[][] potionImgs, containerImgs;
	private BufferedImage[] cannonImgs, grassImgs;
	private BufferedImage[] torchImgs;
	private BufferedImage spikeImg, cannonBallImg;
	private ArrayList<Potion> potions;
	private ArrayList<Projectile> projectiles = new ArrayList<>();

	private Level currentLevel;

	public ObjectManager(Playing playing) {
		this.playing = playing;
		currentLevel = playing.getLevelManager().getCurrentLevel();
		loadImgs();
	}

	public void checkSpikesTouched(Player p) {
		boolean alreadyDamaged = false;
		for (Spike s : currentLevel.getSpikes())
			if (s.getHitbox().intersects(p.getHitbox())){
				if (!alreadyDamaged) {
					p.kill();
					alreadyDamaged = true;
					break;
				}
			}
	}

	public void checkSpikesTouched(Enemy e) {
		boolean alreadyDamaged = false;
		for (Spike s : currentLevel.getSpikes()) {
			if (s.getHitbox().intersects(e.getHitbox())) {
				if (!alreadyDamaged) {

					e.hurt(e.getCurrentHealth(), playing);
					alreadyDamaged = true;
					break;
				}
			}
		}
	}

	public void checkObjectTouched(Rectangle2D.Float hitbox) {
		for (Potion p : potions)
			if (p.isActive()) {
				if (hitbox.intersects(p.getHitbox())) {
					p.setActive(false);
					applyEffectToPlayer(p);
				}
			}
	}

	public void applyEffectToPlayer(Potion p) {
		if (p.getObjType() == RED_POTION) {
			playing.getPlayer().changeHealth(RED_POTION_VALUE);
			playing.getPlayer().showText("+50 Health", (int)playing.getPlayer().getInitialY(), Color.ORANGE);
		}
		else {
			playing.getPlayer().changePower(BLUE_POTION_VALUE);
			playing.getPlayer().showText("+100 Mana", (int) playing.getPlayer().getInitialY(), Color.ORANGE);
		}
	}

	public void loadObjects(Level newLevel) {
		currentLevel = newLevel;
		potions = new ArrayList<>(newLevel.getPotions());
		projectiles.clear();
	}

	private void loadImgs() {
		BufferedImage potionSprite = LoadSave.GetSpriteAtlas(LoadSave.POTION_ATLAS);
		potionImgs = new BufferedImage[2][7];

		for (int j = 0; j < potionImgs.length; j++)
			for (int i = 0; i < potionImgs[j].length; i++)
				potionImgs[j][i] = potionSprite.getSubimage(12 * i, 16 * j, 12, 16);

		spikeImg = LoadSave.GetSpriteAtlas(LoadSave.TRAP_ATLAS);

		cannonImgs = new BufferedImage[7];
		BufferedImage temp = LoadSave.GetSpriteAtlas(LoadSave.SKULL_ATLAS);

		for (int i = 0; i < cannonImgs.length; i++)
			cannonImgs[i] = temp.getSubimage(i * 40, 0, 40, 26);

		cannonBallImg = LoadSave.GetSpriteAtlas(LoadSave.SKULL_BALL);
		torchImgs = new BufferedImage[6];
		BufferedImage torchOneImg = LoadSave.GetSpriteAtlas(LoadSave.TORCH_ATLAS);
		for (int i = 0; i < 6; i++)
			torchImgs[i] = torchOneImg.getSubimage(i * 32, 0, 32, 32);

		BufferedImage grassTemp = LoadSave.GetSpriteAtlas(LoadSave.GRASS_ATLAS);
		grassImgs = new BufferedImage[2];
		for (int i = 0; i < grassImgs.length; i++)
			grassImgs[i] = grassTemp.getSubimage(32 * i, 0, 32, 32);
	}

	public void update(int[][] lvlData, Player player) {
		updateBackgroundTorch();
		for (Potion p : potions)
			if (p.isActive())
				p.update();

		updateCannons(lvlData, player);
		updateProjectiles(lvlData, player);

	}

	private void updateBackgroundTorch() {
		for (BackgroundTorch bt : currentLevel.getTorch())
			bt.update();
	}

	private void updateProjectiles(int[][] lvlData, Player player) {
		for (Projectile p : projectiles)
			if (p.isActive()) {
				p.updatePos();
				if (p.getHitbox().intersects(player.getHitbox())) {
					player.changeHealth(-25);
					p.setActive(false);
				} else if (IsProjectileHittingLevel(p, lvlData))
					p.setActive(false);
			}
	}

	private boolean isPlayerInRange(Skull c, Player player) {
		int absValue = (int) Math.abs(player.getHitbox().x - c.getHitbox().x);
		return absValue <= Game.TILES_SIZE * 5;
	}

	private boolean isPlayerInfrontOfCannon(Skull c, Player player) {
		if (c.getObjType() == SKULL_LEFT) {
			if (c.getHitbox().x > player.getHitbox().x)
				return true;

		} else if (c.getHitbox().x < player.getHitbox().x)
			return true;
		return false;
	}

	private void updateCannons(int[][] lvlData, Player player) {
		for (Skull c : currentLevel.getCannons()) {
			
			c.update();

			
			if (!c.doAnimation && c.canShoot()) {
				if (c.getTileY() == player.getTileY())
					if (isPlayerInRange(c, player) && !player.isStealthActive())
						if (isPlayerInfrontOfCannon(c, player))
							if (CanCannonSeePlayer(lvlData, player.getHitbox(), c.getHitbox(), c.getTileY()))
								c.setAnimation(true);
			}

			
			if (c.getAniIndex() == 4 && c.getAniTick() == 0 && c.canShoot())
				shootCannon(c);
		}
	}

	private void shootCannon(Skull c) {
		int dir = 1;
		if (c.getObjType() == SKULL_LEFT)
			dir = -1;

		playing.getGame().getAudioPlayer().playEffect(AudioPlayer.SKULL_FIRE);
		projectiles.add(new Projectile((int) c.getHitbox().x, (int) c.getHitbox().y, dir));

		
		c.resetShootCooldown();
	}

	public void draw(Graphics g, int xLvlOffset) {
		drawPotions(g, xLvlOffset);
		drawTraps(g, xLvlOffset);
		drawCannons(g, xLvlOffset);
		drawProjectiles(g, xLvlOffset);
		drawGrass(g, xLvlOffset);
	}

	private void drawGrass(Graphics g, int xLvlOffset) {
		for (Witchgrass grass : currentLevel.getGrass())
			g.drawImage(grassImgs[grass.getType()], grass.getX() - xLvlOffset, grass.getY(), (int) (32 * Game.SCALE), (int) (32 * Game.SCALE), null);
	}

	public void drawBackgroundTorch(Graphics g, int xLvlOffset) {
		for (BackgroundTorch bt : currentLevel.getTorch()) {
			g.drawImage(torchImgs[bt.getAniIndex()], bt.getX() - xLvlOffset + GetTorchOffsetX(TORCH), (int) (bt.getY() + GetTorchOffsetY(TORCH)), GetTorchWidth(TORCH),
					GetTorchHeight(TORCH), null);
		}
	}

	private void drawProjectiles(Graphics g, int xLvlOffset) {
		for (Projectile p : projectiles)
			if (p.isActive())
				g.drawImage(cannonBallImg, (int) (p.getHitbox().x - xLvlOffset), (int) (p.getHitbox().y), SKULL_BALL_WIDTH, SKULL_BALL_HEIGHT, null);
	}

	private void drawCannons(Graphics g, int xLvlOffset) {
		for (Skull c : currentLevel.getCannons()) {
			int x = (int) (c.getHitbox().x - xLvlOffset);
			int width = SKULL_WIDTH;

			if (c.getObjType() == SKULL_RIGHT) {
				x += width;
				width *= -1;
			}
			g.drawImage(cannonImgs[c.getAniIndex()], x, (int) (c.getHitbox().y), width, SKULL_HEIGHT, null);
		}
	}

	private void drawTraps(Graphics g, int xLvlOffset) {
		for (Spike s : currentLevel.getSpikes())
			g.drawImage(spikeImg, (int) (s.getHitbox().x - xLvlOffset), (int) (s.getHitbox().y - s.getyDrawOffset()), SPIKE_WIDTH, SPIKE_HEIGHT, null);

	}


	private void drawPotions(Graphics g, int xLvlOffset) {
		for (Potion p : potions)
			if (p.isActive()) {
				int type = 0;
				if (p.getObjType() == RED_POTION)
					type = 1;
				g.drawImage(potionImgs[type][p.getAniIndex()], (int) (p.getHitbox().x - p.getxDrawOffset() - xLvlOffset), (int) (p.getHitbox().y - p.getyDrawOffset()), POTION_WIDTH, POTION_HEIGHT,
						null);
			}
	}

	public void resetAllObjects() {
		loadObjects(playing.getLevelManager().getCurrentLevel());
		for (Potion p : potions)
			p.reset();
		for (Skull c : currentLevel.getCannons())
			c.reset();
	}
}
