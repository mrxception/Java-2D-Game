package ui;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.awt.BasicStroke;
import java.awt.GradientPaint;
import java.awt.RenderingHints;

import gamestates.Gamestate;
import gamestates.Playing;
import main.Game;

public class GameCompletedOverlay {
	private Playing playing;
	private TextButton backButton;
	private Font pixelFont, titleFont, textFont;
	private boolean cursorChanged = false;

	
	private int boxX, boxY, boxW, boxH;
	private final Color YELLOW_COLOR = new Color(255, 215, 0);
	private final Color BORDER_COLOR = new Color(255, 215, 0);
	private final int BORDER_THICKNESS = 3;

	
	private String[] message = {
			"QUEST COMPLETED",
			"You've conquered the dungeon alone, proving your strength beyond limits.",
			"Each enemy you've faced has only made you stronger.",
			"The dungeon closes, but your legend continues to rise.",
			"Thank you for playing! the path to becoming the strongest hunter never ends."
	};

	public GameCompletedOverlay(Playing playing) {
		this.playing = playing;
		loadFonts();
		createMessageBox();
		initBackButton();
	}

	private void loadFonts() {
		try {
			pixelFont = Font.createFont(Font.TRUETYPE_FONT, new File("res/fonts/pixel_font.ttf"))
					.deriveFont(Font.PLAIN, (int)(8 * Game.SCALE));
			titleFont = Font.createFont(Font.TRUETYPE_FONT, new File("res/fonts/PressStart2P.ttf"))
					.deriveFont(Font.BOLD, (int)(20 * Game.SCALE));
			textFont = Font.createFont(Font.TRUETYPE_FONT, new File("res/fonts/pixel_font.ttf"))
					.deriveFont(Font.PLAIN, (int)(12 * Game.SCALE));
		} catch (Exception e) {
			e.printStackTrace();
			pixelFont = new Font("Courier New", Font.PLAIN, (int)(16 * Game.SCALE));
			titleFont = new Font("Times New Roman", Font.BOLD, (int)(30 * Game.SCALE));
			textFont = new Font("Arial", Font.PLAIN, (int)(16 * Game.SCALE));
		}
	}

	private void initBackButton() {
		int buttonY = Game.GAME_HEIGHT - 80;
		backButton = new TextButton("BACK TO MENU", 80, buttonY);
	}

	private void createMessageBox() {
		boxW = (int) (770 * Game.SCALE);
		boxH = (int) (250 * Game.SCALE);
		boxX = Game.GAME_WIDTH / 2 - boxW / 2;
		boxY = (int) (80 * Game.SCALE);
	}

	public void draw(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;

		
		Stroke originalStroke = g2d.getStroke();
		Object originalAntialiasing = g2d.getRenderingHint(RenderingHints.KEY_ANTIALIASING);

		
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		
		g.setColor(new Color(0, 0, 0, 200));
		g.fillRect(0, 0, Game.GAME_WIDTH, Game.GAME_HEIGHT);

		
		GradientPaint gradient = new GradientPaint(
				boxX, boxY, new Color(40, 40, 50, 220),
				boxX, boxY + boxH, new Color(20, 20, 30, 220));
		g2d.setPaint(gradient);
		g2d.fillRoundRect(boxX, boxY, boxW, boxH, 15, 15);

		
		g2d.setColor(new Color(60, 60, 70, 200));
		g2d.setStroke(new BasicStroke(2));
		g2d.drawRoundRect(boxX + 4, boxY + 4, boxW - 8, boxH - 8, 12, 12);

		
		g2d.setColor(BORDER_COLOR);
		g2d.setStroke(new BasicStroke(BORDER_THICKNESS, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		g2d.drawRoundRect(boxX, boxY, boxW, boxH, 15, 15);

		
		int titleHeight = 40;
		GradientPaint titleGradient = new GradientPaint(
				boxX, boxY + 10, new Color(60, 60, 80, 180),
				boxX, boxY + 10 + titleHeight, new Color(40, 40, 60, 180));
		g2d.setPaint(titleGradient);
		g2d.fillRoundRect(boxX + 10, boxY + 10, boxW - 20, titleHeight, 10, 10);

		
		g2d.setFont(titleFont);
		FontMetrics fm = g2d.getFontMetrics();
		int titleX = Game.GAME_WIDTH / 2 - fm.stringWidth(message[0]) / 2;
		int titleY = boxY + 80;

		
		for (int i = 2; i > 0; i--) {
			g2d.setColor(new Color(255, 215, 0, 50));
			g2d.drawString(message[0], titleX - i, titleY);
			g2d.drawString(message[0], titleX + i, titleY);
			g2d.drawString(message[0], titleX, titleY - i);
			g2d.drawString(message[0], titleX, titleY + i);
		}

		
		g2d.setColor(YELLOW_COLOR);
		g2d.drawString(message[0], titleX, titleY);

		
		int underlineY = titleY + 40;
		GradientPaint linePaint = new GradientPaint(
				boxX + 20, underlineY, new Color(255, 215, 0, 50),
				boxX + boxW/2, underlineY, new Color(255, 215, 0, 255),
				true);
		g2d.setPaint(linePaint);
		g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		g2d.drawLine(boxX + 20, underlineY, boxX + boxW - 20, underlineY);

		
		g2d.setColor(Color.WHITE);
		g2d.setFont(textFont);
		int lineHeight = (int)(25 * Game.SCALE);
		int messageY = titleY + 135;

		for (int i = 1; i < message.length; i++) {
			g2d.drawString(message[i], centerTextX(g, message[i]), messageY);
			messageY += lineHeight;
		}

		
		backButton.draw(g);

		
		g2d.setStroke(originalStroke);
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, originalAntialiasing);
	}

	private int centerTextX(Graphics g, String text) {
		int textWidth = g.getFontMetrics().stringWidth(text);
		return Game.GAME_WIDTH / 2 - textWidth / 2;
	}

	public void update() {
		backButton.update();
	}

	private boolean isIn(MouseEvent e, TextButton b) {
		return b.getBounds().contains(e.getX(), e.getY());
	}

	public void mouseMoved(MouseEvent e) {
		backButton.setMouseOver(false);

		if (isIn(e, backButton)) {
			backButton.setMouseOver(true);

			
			if (!cursorChanged) {
				playing.getGame().getCanvas().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				cursorChanged = true;
			}
		} else {
			
			if (cursorChanged) {
				playing.getGame().getCanvas().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				cursorChanged = false;
			}
		}
	}

	public void mouseReleased(MouseEvent e) {
		if (backButton.isMousePressed() && isIn(e, backButton)) {
			playing.resetGameCompleted();
			playing.setGamestate(Gamestate.MENU);
			playing.resetAll();
		}

		backButton.resetBools();
	}

	public void mousePressed(MouseEvent e) {
		if (isIn(e, backButton)) {
			backButton.setMousePressed(true);
		}
	}

	
	private class TextButton {
		private int x, y;
		private String text;
		private boolean mouseOver, mousePressed;
		private Color defaultColor = new Color(173, 216, 230);
		private Color hoverColor = new Color(0, 255, 255);
		private Color pressedColor = new Color(0, 102, 204);
		private Rectangle bounds;

		private float opacity = 1.0f;
		private boolean fadingOut = true;
		private final float FADE_SPEED = 0.005f;
		private final float MIN_OPACITY = 0.6f;
		private final float MAX_OPACITY = 1.0f;

		public TextButton(String text, int x, int y) {
			this.text = text;
			this.x = x;
			this.y = y;
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

			
			bounds = new Rectangle(x - 15, y - height / 2 - 10, width + 30, height + 20);
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
			} else {
				opacity = MAX_OPACITY;
				fadingOut = true;
			}
		}

		public void draw(Graphics g) {
			Graphics2D g2d = (Graphics2D) g.create();

			
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			if (mousePressed)
				g2d.setColor(pressedColor);
			else if (mouseOver)
				g2d.setColor(hoverColor);
			else
				g2d.setColor(defaultColor);

			g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
			g2d.setFont(pixelFont);

			FontMetrics fm = g2d.getFontMetrics();
			int textX = x;
			int textY = y + fm.getAscent() / 2;

			String displayText = mouseOver ? "> " + text : text;

			g2d.drawString(displayText, textX, textY);

			g2d.dispose();
		}

		public void resetBools() {
			mouseOver = false;
			mousePressed = false;
			opacity = MAX_OPACITY;
			fadingOut = true;
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

		public Rectangle getBounds() {
			return bounds;
		}
	}
}