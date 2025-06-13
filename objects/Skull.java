package objects;

import main.Game;

public class Skull extends GameObject {

	private int tileY;
	private boolean canShoot = true;
	private int shootCooldown = 0;
	private final int SHOOT_COOLDOWN_TICKS = 200; 

	public Skull(int x, int y, int objType) {
		super(x, y, objType);
		tileY = y / Game.TILES_SIZE;
		initHitbox(40, 26);

		hitbox.y += (int) (6 * Game.SCALE);
	}

	public void update() {
		if (doAnimation)
			updateAnimationTick();

		
		if (!canShoot) {
			shootCooldown++;
			if (shootCooldown >= SHOOT_COOLDOWN_TICKS) {
				canShoot = true;
				shootCooldown = 0;
			}
		}
	}

	public int getTileY() {
		return tileY;
	}

	public boolean canShoot() {
		return canShoot;
	}

	public void resetShootCooldown() {
		canShoot = false;
		shootCooldown = 0;
	}
}