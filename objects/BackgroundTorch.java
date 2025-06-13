package objects;

import java.util.Random;

public class BackgroundTorch {

	private int x, y, type, aniIndex, aniTick;

	public BackgroundTorch(int x, int y, int type) {
		this.x = x;
		this.y = y;
		this.type = type;

		
		
		Random r = new Random();
		aniIndex = r.nextInt(6);

	}

	public void update() {
		aniTick++;
		if (aniTick >= 35) {
			aniTick = 0;
			aniIndex++;
			if (aniIndex >= 6)
				aniIndex = 0;
		}
	}

	public int getAniIndex() {
		return aniIndex;
	}

	public void setAniIndex(int aniIndex) {
		this.aniIndex = aniIndex;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}
}
