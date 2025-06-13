package entities;

import static utilz.Constants.Directions.DOWN;
import static utilz.Constants.Directions.LEFT;
import static utilz.Constants.Directions.UP;
import static utilz.Constants.EnemyConstants.*;
import static utilz.HelpMethods.CanMoveHere;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import main.Game;

public abstract class Entity {
	protected float x, y;
	protected int width, height;
	protected Rectangle2D.Float hitbox;
	protected int aniTick, aniIndex;
	protected int state;
	protected float airSpeed;
	protected boolean inAir = false;
	protected Rectangle2D.Float attackBox;
	protected float walkSpeed;

	private int maxHealth;
	private int currentHealth;

	protected int pushBackDir;
	protected float pushDrawOffset;
	protected int pushBackOffsetDir = UP;

	protected List<FloatingText> floatingTexts = new ArrayList<>();

	public Entity(float x, float y, int width, int height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

	protected void updatePushBackDrawOffset() {
		float speed = 0.95f;
		float limit = -30f;

		if (pushBackOffsetDir == UP) {
			pushDrawOffset -= speed;
			if (pushDrawOffset <= limit)
				pushBackOffsetDir = DOWN;
		} else {
			pushDrawOffset += speed;
			if (pushDrawOffset >= 0)
				pushDrawOffset = 0;
		}
	}

	public int getMaxHealth() {
		return maxHealth;
	}

	public void setMaxHealth(int maxHealth) {
		this.maxHealth = maxHealth;
	}

	public void setCurrentHealth(int currentHealth) {
		this.currentHealth = currentHealth;
	}

	protected void pushBack(int pushBackDir, int[][] lvlData, float speedMulti) {
		float xSpeed = 0;
		if (pushBackDir == LEFT)
			xSpeed = -walkSpeed;
		else
			xSpeed = walkSpeed;

		if (CanMoveHere(hitbox.x + xSpeed * speedMulti, hitbox.y, hitbox.width, hitbox.height, lvlData))
			hitbox.x += xSpeed * speedMulti;
	}



	protected void drawAttackBox(Graphics g, int xLvlOffset) {
		g.setColor(Color.red);
		g.drawRect((int) (attackBox.x - xLvlOffset), (int) attackBox.y, (int) attackBox.width, (int) attackBox.height);
	}

	protected void drawHitbox(Graphics g, int xLvlOffset) {
		g.setColor(Color.PINK);
		g.drawRect((int) hitbox.x - xLvlOffset, (int) hitbox.y, (int) hitbox.width, (int) hitbox.height);
	}

	protected void initHitbox(int width, int height) {
		hitbox = new Rectangle2D.Float(x, y, (int) (width * Game.SCALE), (int) (height * Game.SCALE));
	}

	public Rectangle2D.Float getHitbox() {
		return hitbox;
	}

	public int getState() {
		return state;
	}

	public int getAniIndex() {
		return aniIndex;
	}

	protected void newState(int state) {
		this.state = state;
		aniTick = 0;
		aniIndex = 0;
	}

	public class FloatingText {
		public String text;
		public int offsetX;
		public int offsetY;
		public int initialY;
		public long startTime;
		public Color color;
		public boolean withStroke = false;
		public static final int DISPLAY_DURATION = 2500;
		public static final float FLOAT_DISTANCE = 40.0f;
		public float fixedX;
		private static Font pixelFont; 

		
		static {
			try {
				
				pixelFont = Font.createFont(Font.TRUETYPE_FONT, new File("res/fonts/pixel_font.ttf"))
						.deriveFont(12f);
			} catch (Exception e) {
				e.printStackTrace();
				
				pixelFont = new Font("Courier New", Font.BOLD, (int)(32 * Game.SCALE));
			}
		}

		public FloatingText(String text, int initialY, float fixedX) {
			this(text, initialY, fixedX, Color.RED);
		}

		public FloatingText(String text, int initialY, float fixedX, Color color) {
			this.text = text;
			this.initialY = initialY;
			this.offsetX = (int) (Math.random() * 80) - 40;
			this.offsetY = 0;
			this.startTime = System.currentTimeMillis();
			this.color = color;
			this.fixedX = fixedX;
		}

		public boolean isActive() {
			return System.currentTimeMillis() - startTime < DISPLAY_DURATION;
		}

		public float getCurrentYOffset() {
			float progress = (float)(System.currentTimeMillis() - startTime) / DISPLAY_DURATION;
			progress = 1 - (1 - progress) * (1 - progress);
			return -progress * FLOAT_DISTANCE;
		}

		public void render(Graphics g, int xLvlOffset) {
			if (!isActive()) return;


			Graphics2D g2d = (Graphics2D)g;

			
			Font originalFont = g2d.getFont();
			Color originalColor = g2d.getColor();

			
			g2d.setFont(pixelFont);

			
			float progress = (float)(System.currentTimeMillis() - startTime) / DISPLAY_DURATION;
			int alpha = (int)(255 * (1 - progress * 0.7f)); 
			Color textColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);

			
			int textX = (int)(fixedX - xLvlOffset + offsetX);
			int textY = initialY + (int)getCurrentYOffset();

			
			g2d.setColor(new Color(0, 0, 0, alpha/2));
			g2d.drawString(text, textX+1, textY+1);

			
			if (withStroke) {
				
				g2d.setColor(new Color(255, 255, 255, alpha));
				g2d.drawString(text, textX-1, textY-1);
				g2d.drawString(text, textX+1, textY-1);
				g2d.drawString(text, textX-1, textY+1);
				g2d.drawString(text, textX+1, textY+1);
			}

			
			g2d.setColor(textColor);
			g2d.drawString(text, textX, textY);

			
			g2d.setFont(originalFont);
			g2d.setColor(originalColor);
		}
	}

	public List<FloatingText> getFloatingTexts() {
		Iterator<FloatingText> iterator = floatingTexts.iterator();
		while (iterator.hasNext()) {
			FloatingText text = iterator.next();
			if (!text.isActive()) {
				iterator.remove();
			}
		}
		return floatingTexts;
	}

	public void drawFloatingText(Graphics g, int xLvlOffset) {
		
		getFloatingTexts(); 

		try{
			for (FloatingText ft : floatingTexts) {
				ft.render(g, xLvlOffset);
			}
		} catch (Exception e){
			e.printStackTrace();
		}

	}

	public int getCurrentHealth() {
		return currentHealth;
	}

	public void showText(String text, int initialY) {
		floatingTexts.add(new FloatingText(text, initialY, this.hitbox.x));
	}

	public void showText(String text, int initialY, Color color) {
		floatingTexts.add(new FloatingText(text, initialY, this.hitbox.x, color));
	}

	public void showText(String text, int initialY, Color color, boolean withStroke) {
		FloatingText ft = new FloatingText(text, initialY, this.hitbox.x, color);
		ft.withStroke = withStroke;
		floatingTexts.add(ft);
	}

	public void drawHealthBar(Graphics g, Enemy e, int xLvlOffset, int yOffset) {

		if (e.getState() == DEAD) {
			return;
		}
		int width = e.getEnemyType() == HELLFLAME ? 100 : 40;
		int height = 6;

		int x = (int)(e.getHitbox().x - xLvlOffset) + (int)(e.getHitbox().width/2) - width/2;
		int y = (int)e.getHitbox().y - yOffset - 15;

		g.setColor(new Color(150, 0, 0)); 
		g.fillRect(x, y, width, height);

		float healthPercentage = (float)e.getCurrentHealth() / e.getMaxHealth();
		int currentWidth = (int)(width * healthPercentage);

		Color healthColor;
		if (healthPercentage > 0.6f) {
			healthColor = Color.GREEN;
		} else if (healthPercentage > 0.3f) {
			healthColor = Color.YELLOW;
		} else {
			healthColor = Color.RED;
		}

		g.setColor(healthColor);
		g.fillRect(x, y, currentWidth, height);

		g.setColor(Color.BLACK);
		g.drawRect(x, y, width, height);
	}
}