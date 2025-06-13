package gamestates;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.Font;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.AlphaComposite;
import java.io.File;

import audio.AudioPlayer;
import main.Game;
import utilz.LoadSave;

import static utilz.Constants.UI.Background.BACKGROUND_HEIGHT;
import static utilz.Constants.UI.Background.BACKGROUND_WIDTH;

public class Menu extends State implements Statemethods {

	private TextButton[] buttons = new TextButton[4];
	private BufferedImage[] backgroundFrames;
	private Font pixelFont;
	private final int LEFT_MARGIN = 80;
	private final int STARTING_Y = 90;
	private final int TEXT_SPACING = 80;
	private boolean cursorChanged = false;

	private int aniTick, aniIndex;
	private final int ANIMATION_SPEED = 50;
	private final int FRAME_COUNT = 3;

	
	private boolean fadeOutTransition = false;
	private int fadeOutAlpha = 0;
	private final int FADE_OUT_SPEED = 5;
	private Gamestate transitionTarget = null;

	
	private boolean fadeInTransition = true;
	private int fadeInAlpha = 255;
	private final int FADE_IN_SPEED = 5;

	public Menu(Game game) {
		super(game);
		loadFont();
		loadButtons();
		loadBackground();

		
		fadeInTransition = true;
		fadeInAlpha = 255;
	}

	private void loadFont() {
		try {
			pixelFont = Font.createFont(Font.TRUETYPE_FONT, new File("res/fonts/pixel_font.ttf")).deriveFont(Font.PLAIN, (int)(20 * Game.SCALE));

		} catch (Exception e) {
			e.printStackTrace();
			pixelFont = new Font("Courier New", Font.BOLD, (int)(32 * Game.SCALE));
		}
	}

	private void loadBackground() {
		backgroundFrames = new BufferedImage[FRAME_COUNT];
		BufferedImage atlas = LoadSave.GetSpriteAtlas(LoadSave.MENU_BACKGROUND_IMG);
		for (int i = 0; i < FRAME_COUNT; i++) {
			backgroundFrames[i] = atlas.getSubimage(
					i * BACKGROUND_WIDTH,
					0,
					BACKGROUND_WIDTH,
					BACKGROUND_HEIGHT
			);
		}
	}

	private void updateAnimation() {
		aniTick++;
		if (aniTick >= ANIMATION_SPEED) {
			aniTick = 0;
			aniIndex = (aniIndex + 1) % FRAME_COUNT;
		}
	}

	private void loadButtons() {
		int x = LEFT_MARGIN;

		buttons[0] = new TextButton("PLAY", x, STARTING_Y, Gamestate.PLAYING);
		buttons[1] = new TextButton("LEADERBOARD", x, STARTING_Y + TEXT_SPACING, Gamestate.LEADERBOARD);
		buttons[2] = new TextButton("OPTIONS", x, STARTING_Y + (2 * TEXT_SPACING), Gamestate.OPTIONS);
		buttons[3] = new TextButton("QUIT", x, STARTING_Y + (3 * TEXT_SPACING), Gamestate.QUIT);
	}

	@Override
	public void update() {
		updateAnimation();

		
		if (fadeInTransition) {
			fadeInAlpha -= FADE_IN_SPEED;
			if (fadeInAlpha <= 0) {
				fadeInAlpha = 0;
				fadeInTransition = false;
			}
			return;
		}

		
		if (fadeOutTransition) {
			fadeOutAlpha += FADE_OUT_SPEED;
			if (fadeOutAlpha >= 255) {
				fadeOutAlpha = 255;
				fadeOutTransition = false;

				Gamestate.state = transitionTarget;
				if (transitionTarget == Gamestate.STORY) {
					game.getAudioPlayer().playSong(AudioPlayer.STORY);
				}
				transitionTarget = null;
			}
			return;
		}

		for (TextButton tb : buttons)
			tb.update();
	}

	@Override
	public void draw(Graphics g) {
		g.drawImage(backgroundFrames[aniIndex], 0, 0, Game.GAME_WIDTH, Game.GAME_HEIGHT, null);

		
		Graphics2D g2d = (Graphics2D) g.create();
		float buttonAlpha = fadeInTransition ? 1.0f - (fadeInAlpha / 255.0f) : 1.0f;
		g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, buttonAlpha));

		g2d.setFont(pixelFont);

		for (TextButton tb : buttons)
			tb.draw(g2d);

		g2d.dispose();

		
		if (fadeOutTransition && fadeOutAlpha > 0) {
			g.setColor(new Color(0, 0, 0, fadeOutAlpha));
			g.fillRect(0, 0, Game.GAME_WIDTH, Game.GAME_HEIGHT);
		}

		if (fadeInTransition && fadeInAlpha > 0) {
			g.setColor(new Color(0, 0, 0, fadeInAlpha));
			g.fillRect(0, 0, Game.GAME_WIDTH, Game.GAME_HEIGHT);
		}
	}

	private void startFadeOutTransition(Gamestate target) {
		fadeOutTransition = true;
		fadeOutAlpha = 0;
		transitionTarget = target;
		game.getLeaderboard().resetTransitions();
		game.getLeaderboard().initScores();
	}

	
	public void startFadeInTransition() {
		fadeInTransition = true;
		fadeInAlpha = 255;
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if (fadeInTransition || fadeOutTransition) return;

		for (TextButton tb : buttons) {
			if (isIn(e, tb)) {
				tb.setMousePressed(true);
			}
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (fadeInTransition || fadeOutTransition) return;

		for (TextButton tb : buttons) {
			if (isIn(e, tb)) {
				if (tb.isMousePressed()) {
					exitState();

					if (tb.getState() == Gamestate.PLAYING) {
						startFadeOutTransition(Gamestate.STORY);
					} else if (tb.getState() == Gamestate.LEADERBOARD) {
						startFadeOutTransition(Gamestate.LEADERBOARD);
					} else {
						tb.applyGamestate();
					}

					if (tb.getState() == Gamestate.QUIT) {
						game.getCanvas().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
						cursorChanged = false;
					}
				}
				break;
			}
		}
		resetButtons();
	}

	public void exitState() {
		game.getCanvas().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}

	private void resetButtons() {
		for (TextButton tb : buttons)
			tb.resetBools();
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		if (fadeInTransition || fadeOutTransition) return;

		boolean mouseOverButton = false;

		for (TextButton tb : buttons)
			tb.setMouseOver(false);

		for (TextButton tb : buttons) {
			if (isIn(e, tb)) {
				tb.setMouseOver(true);
				mouseOverButton = true;
				break;
			}
		}

		if (mouseOverButton && !cursorChanged) {
			game.getCanvas().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			cursorChanged = true;
		} else if (!mouseOverButton && cursorChanged) {
			game.getCanvas().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			cursorChanged = false;
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {

	}

	@Override
	public void mouseClicked(MouseEvent e) {

	}

	@Override
	public void keyReleased(KeyEvent e) {

	}

	private boolean isIn(MouseEvent e, TextButton tb) {
		return tb.getBounds().contains(e.getX(), e.getY());
	}

	private class TextButton {
		private int x, y;
		private String text;
		private Gamestate state;
		private boolean mouseOver, mousePressed;

		
		private Color defaultColor = new Color(0, 180, 216); 
		private Color hoverColor = new Color(65, 234, 255);  
		private Color pressedColor = new Color(0, 102, 153); 
		private Color glowColor = new Color(120, 230, 255);  
		private Color accentColor = new Color(130, 60, 200); 

		private java.awt.Rectangle bounds;

		
		private float opacity = 1.0f;
		private boolean fadingOut = true;
		private final float FADE_SPEED = 0.005f;
		private final float MIN_OPACITY = 0.6f;
		private final float MAX_OPACITY = 1.0f;

		
		private float energyEffect = 0.0f;
		private boolean energyIncreasing = true;
		private final float ENERGY_SPEED = 0.01f;

		
		private float glowIntensity = 0.0f;
		private final float GLOW_SPEED = 0.02f;

		public TextButton(String text, int x, int y, Gamestate state) {
			this.text = text;
			this.x = x;
			this.y = y;
			this.state = state;
			calculateBounds();
		}

		private void calculateBounds() {
			BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
			Graphics g = img.getGraphics();
			g.setFont(pixelFont);
			FontMetrics fm = g.getFontMetrics();

			String displayText = "> " + text;
			int width = fm.stringWidth(displayText);
			int height = fm.getHeight();

			bounds = new java.awt.Rectangle(x, y - height / 2, width, height);
			g.dispose();
		}

		public void update() {
			
			if (mouseOver) {
				if (fadingOut) {
					opacity -= FADE_SPEED;
					if (opacity <= MIN_OPACITY) {
						opacity = MIN_OPACITY;
						fadingOut = false;
					}
				} else {
					opacity += FADE_SPEED;
					if (opacity >= MAX_OPACITY) {
						opacity = MAX_OPACITY;
						fadingOut = true;
					}
				}

				
				glowIntensity += GLOW_SPEED;
				if (glowIntensity > 1.0f) glowIntensity = 1.0f;
			} else {
				opacity = MAX_OPACITY;
				fadingOut = true;

				
				glowIntensity -= GLOW_SPEED;
				if (glowIntensity < 0.0f) glowIntensity = 0.0f;
			}

			
			if (energyIncreasing) {
				energyEffect += ENERGY_SPEED;
				if (energyEffect >= 1.0f) {
					energyEffect = 1.0f;
					energyIncreasing = false;
				}
			} else {
				energyEffect -= ENERGY_SPEED;
				if (energyEffect <= 0.0f) {
					energyEffect = 0.0f;
					energyIncreasing = true;
				}
			}
		}

		public void draw(Graphics g) {
			Graphics2D g2d = (Graphics2D) g.create();
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

			Color textColor;
			if (mousePressed) {
				textColor = pressedColor;
			} else if (mouseOver) {
				textColor = hoverColor;
			} else {
				textColor = defaultColor;
			}

			
			g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));

			FontMetrics fm = g2d.getFontMetrics(pixelFont);
			int textX = x;
			int textY = y + fm.getAscent() / 2;
			String displayText = mouseOver ? "> " + text : text;

			
			if (glowIntensity > 0) {
				
				drawEnergyEffect(g2d, textX, textY, displayText, fm);
			}

			
			g2d.setFont(pixelFont);
			g2d.setColor(new Color(10, 10, 30, 100));
			g2d.drawString(displayText, textX + 2, textY + 2);

			
			g2d.setColor(textColor);
			g2d.drawString(displayText, textX, textY);

			
			if (mouseOver) {
				drawDynamicUnderline(g2d, textX, textY, displayText, fm);
			}

			
			if (mouseOver) {
				drawAccentMarker(g2d, textX, textY, fm);
			}

			g2d.dispose();
		}

		private void drawEnergyEffect(Graphics2D g2d, int textX, int textY, String text, FontMetrics fm) {
			
			int textWidth = fm.stringWidth(text);
			int textHeight = fm.getHeight();

			
			Color glow = new Color(
					glowColor.getRed(),
					glowColor.getGreen(),
					glowColor.getBlue(),
					(int)(120 * glowIntensity)
			);

			
			int lineY = textY + 5;
			int lineHeight = 1;
			float lineAlpha = 0.6f * glowIntensity;

			g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, lineAlpha));

			
			for (int i = 0; i < 3; i++) {
				int offsetY = i * 2;
				int lineLength = textWidth + (int)(20 * energyEffect);

				
				GradientPaint gradient = new GradientPaint(
						textX, lineY + offsetY, new Color(glowColor.getRed(), glowColor.getGreen(), glowColor.getBlue(), 0),
						textX + textWidth/2, lineY + offsetY, glow,
						true
				);

				g2d.setPaint(gradient);
				g2d.fillRect(textX, lineY + offsetY, lineLength, lineHeight);
			}

			
			g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));

			
			for (int i = 0; i < 3; i++) {
				float blurAlpha = (0.3f - (i * 0.1f)) * glowIntensity;
				g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, blurAlpha));
				g2d.setColor(glow);
				g2d.drawString(text, textX - i, textY);
				g2d.drawString(text, textX + i, textY);
				g2d.drawString(text, textX, textY - i);
				g2d.drawString(text, textX, textY + i);
			}

			
			g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
		}

		private void drawDynamicUnderline(Graphics2D g2d, int textX, int textY, String text, FontMetrics fm) {
			int textWidth = fm.stringWidth(text);
			int underlineY = textY + 5;

			
			
			GradientPaint gradient = new GradientPaint(
					textX, underlineY, new Color(0, 0, 0, 0),
					textX + textWidth * energyEffect, underlineY, hoverColor,
					false
			);

			g2d.setPaint(gradient);
			g2d.setStroke(new BasicStroke(2));
			g2d.drawLine(textX, underlineY, textX + textWidth, underlineY);
		}

		private void drawAccentMarker(Graphics2D g2d, int textX, int textY, FontMetrics fm) {
			
			
			int markerSize = 8;
			int markerX = textX - markerSize - 2;
			int markerY = textY - fm.getAscent()/2;

			g2d.setColor(accentColor);

			
			int[] xPoints = {markerX, markerX - markerSize/2, markerX};
			int[] yPoints = {markerY - markerSize/2, markerY, markerY + markerSize/2};
			g2d.fillPolygon(xPoints, yPoints, 3);

			
			if (glowIntensity > 0) {
				g2d.setColor(new Color(
						glowColor.getRed(),
						glowColor.getGreen(),
						glowColor.getBlue(),
						(int)(200 * glowIntensity)
				));
				g2d.fillOval(markerX - markerSize/4 - 1, markerY - markerSize/4, markerSize/2, markerSize/2);
			}
		}

		public void applyGamestate() {
			Gamestate.state = state;
		}

		public void resetBools() {
			mouseOver = false;
			mousePressed = false;
			opacity = MAX_OPACITY;
			fadingOut = true;
		}

		public Gamestate getState() {
			return state;
		}

		public void setMouseOver(boolean mouseOver) {
			this.mouseOver = mouseOver;
		}

		public void setMousePressed(boolean mousePressed) {
			this.mousePressed = mousePressed;
		}

		public boolean isMousePressed() {
			return mousePressed;
		}

		public java.awt.Rectangle getBounds() {
			return bounds;
		}
	}
}