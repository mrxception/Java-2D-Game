package gamestates;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.Font;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.BasicStroke;
import java.awt.Cursor;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import main.Game;
import utilz.LoadSave;

import static utilz.Constants.UI.Background.BACKGROUND_HEIGHT;
import static utilz.Constants.UI.Background.BACKGROUND_WIDTH;

public class Leaderboard extends State implements Statemethods {

    private Font pixelFont, titleFont, rankFont, timeFont, textButtonFont;
    private BufferedImage[] backgroundFrames;
    private Color titleColor = new Color(255, 215, 0); 
    private Color borderColor = new Color(255, 215, 0); 
    private Color textColor = new Color(255, 255, 255); 
    private Color rankColor = new Color(255, 223, 0); 

    private TextButton backButton;
    private final int NUM_ROUNDS = 3;
    private final int TOP_SCORES = 5;
    private List<List<ScoreEntry>> roundScores;

    private int aniTick, aniIndex;
    private final int ANIMATION_SPEED = 50;
    private final int FRAME_COUNT = 3;

    private boolean fadeInTransition = true;
    private int fadeAlpha = 255;
    private final int FADE_SPEED = 5;

    
    private final int BOX_WIDTH = 300;
    private final int BOX_HEIGHT = 450;
    private final int BOX_SPACING;
    private final int TOP_MARGIN = 200;

    private boolean cursorChanged = false;

    public Leaderboard(Game game) {
        super(game);
        
        BOX_SPACING = (Game.GAME_WIDTH - (NUM_ROUNDS * BOX_WIDTH)) / (NUM_ROUNDS + 1);

        loadFonts();
        loadBackground();
        initBackButton();

        fadeInTransition = true;
        fadeAlpha = 255;
    }

    private void loadFonts() {
        try {
            textButtonFont = Font.createFont(Font.TRUETYPE_FONT, new File("res/fonts/pixel_font.ttf"))
                    .deriveFont(Font.PLAIN, (int)(8 * Game.SCALE));
            pixelFont = Font.createFont(Font.TRUETYPE_FONT, new File("res/fonts/PressStart2P.ttf"))
                    .deriveFont(Font.PLAIN, (int)(8 * Game.SCALE));
            titleFont = Font.createFont(Font.TRUETYPE_FONT, new File("res/fonts/PressStart2P.ttf"))
                    .deriveFont(Font.BOLD, (int)(24 * Game.SCALE));
            rankFont = Font.createFont(Font.TRUETYPE_FONT, new File("res/fonts/PressStart2P.ttf"))
                    .deriveFont(Font.BOLD, (int)(8 * Game.SCALE));
            timeFont = Font.createFont(Font.TRUETYPE_FONT, new File("res/fonts/PressStart2P.ttf"))
                    .deriveFont(Font.PLAIN, (int)(7 * Game.SCALE));
        } catch (Exception e) {
            e.printStackTrace();
            textButtonFont = new Font("Courier New", Font.PLAIN, (int)(16 * Game.SCALE));
            pixelFont = new Font("Courier New", Font.PLAIN, (int)(16 * Game.SCALE));
            titleFont = new Font("Courier New", Font.BOLD, (int)(24 * Game.SCALE));
            rankFont = new Font("Courier New", Font.BOLD, (int)(16 * Game.SCALE));
            timeFont = new Font("Courier New", Font.PLAIN, (int)(14 * Game.SCALE));
        }
    }

    public void resetTransitions() {
        fadeInTransition = true;
        fadeAlpha = 255;
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

    private void initBackButton() {
        int buttonY = Game.GAME_HEIGHT - 80;
        backButton = new TextButton("BACK TO MENU", 80, buttonY);
    }

    public void initScores() {
        roundScores = new ArrayList<>();

        for (int round = 0; round < NUM_ROUNDS; round++) {
            List<ScoreEntry> scores = loadScoresForRound(round + 1);
            roundScores.add(scores);
        }
    }

    private List<ScoreEntry> loadScoresForRound(int roundNumber) {
        List<ScoreEntry> scores = new ArrayList<>();

        try {
            File leaderboardFile = new File("data/rounds/round" + roundNumber + "/leaderboard.txt");

            
            if (!leaderboardFile.exists()) {
                return scores;
            }

            BufferedReader reader = new BufferedReader(new FileReader(leaderboardFile));

            
            String line = reader.readLine();

            
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 2) {
                    String playerName = parts[0];
                    if (playerName.length() > 7) {
                        playerName = playerName.substring(0, 7) + "...";
                    }
                    long timeInMillis = Long.parseLong(parts[1]);
                    String formattedTime = formatTime(timeInMillis);
                    scores.add(new ScoreEntry(playerName, formattedTime));

                }
            }
            reader.close();

            
            scores.sort((s1, s2) -> {
                return convertTimeStringToMillis(s1.time) - convertTimeStringToMillis(s2.time);
            });

            
            if (scores.size() > TOP_SCORES) {
                scores = scores.subList(0, TOP_SCORES);
            }

        } catch (IOException e) {
            System.err.println("Error loading leaderboard data for round " + roundNumber + ": " + e.getMessage());
            e.printStackTrace();
        }

        
        if (scores.isEmpty()) {
            for (int i = 0; i < TOP_SCORES; i++) {
                int timeInSeconds = 30 + (i * 15) + ((roundNumber-1) * 5);
                String formattedTime = formatTime(timeInSeconds);
                scores.add(new ScoreEntry("Player" + (i + 1), formattedTime));
            }
        }

        return scores;
    }

    private int convertTimeStringToMillis(String timeString) {
        try {
            String[] parts = timeString.split("[:.]");
            int minutes = Integer.parseInt(parts[0]);
            int seconds = Integer.parseInt(parts[1]);
            int hundredths = parts.length > 2 ? Integer.parseInt(parts[2]) : 0;

            return (minutes * 60 * 1000) + (seconds * 1000) + (hundredths * 10);
        } catch (Exception e) {
            return Integer.MAX_VALUE; 
        }
    }

    private String formatTime(long timeInMillis) {

        long minutes = (timeInMillis / 60000) % 60;
        long seconds = (timeInMillis / 1000) % 60;
        long millis = (timeInMillis % 1000) / 10;

        return String.format("%02d:%02d:%02d", minutes, seconds, millis);
    }

    @Override
    public void update() {
        updateAnimation();
        backButton.update();

        
        if (fadeInTransition) {
            fadeAlpha -= FADE_SPEED;
            if (fadeAlpha <= 0) {
                fadeAlpha = 0;
                fadeInTransition = false;
            }
        }
    }

    private void updateAnimation() {
        aniTick++;
        if (aniTick >= ANIMATION_SPEED) {
            aniTick = 0;
            aniIndex = (aniIndex + 1) % FRAME_COUNT;
        }
    }

    @Override
    public void draw(Graphics g) {
        
        g.drawImage(backgroundFrames[aniIndex], 0, 0, Game.GAME_WIDTH, Game.GAME_HEIGHT, null);

        
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRect(0, 0, Game.GAME_WIDTH, Game.GAME_HEIGHT);

        
        drawTitleWithGlow(g);

        
        int totalContentHeight = BOX_HEIGHT + 80; 
        int startY = (Game.GAME_HEIGHT - totalContentHeight) / 2;

        
        for (int round = 0; round < NUM_ROUNDS; round++) {
            int boxX = BOX_SPACING + round * (BOX_WIDTH + BOX_SPACING);
            drawRoundBox(g, boxX, startY, BOX_WIDTH, BOX_HEIGHT, round);
        }

        
        backButton.draw(g);

        
        if (fadeInTransition && fadeAlpha > 0) {
            g.setColor(new Color(0, 0, 0, fadeAlpha));
            g.fillRect(0, 0, Game.GAME_WIDTH, Game.GAME_HEIGHT);
        }
    }

    private void drawTitleWithGlow(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        
        Object originalAntialiasing = g2d.getRenderingHint(RenderingHints.KEY_ANTIALIASING);

        
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.setFont(titleFont);
        String title = "LEADERBOARDS";
        FontMetrics fm = g2d.getFontMetrics();
        int titleX = (Game.GAME_WIDTH - fm.stringWidth(title)) / 2;
        int titleY = TOP_MARGIN / 2;

        
        for (int i = 5; i > 0; i--) {
            float alpha = 0.2f - (i * 0.03f);
            g2d.setColor(new Color(
                    titleColor.getRed()/255f,
                    titleColor.getGreen()/255f,
                    titleColor.getBlue()/255f,
                    alpha));
            g2d.drawString(title, titleX - i, titleY);
            g2d.drawString(title, titleX + i, titleY);
            g2d.drawString(title, titleX, titleY - i);
            g2d.drawString(title, titleX, titleY + i);
        }

        
        g2d.setColor(titleColor);
        g2d.drawString(title, titleX, titleY);

        
        int lineY = titleY + 10;
        int lineWidth = fm.stringWidth(title) + 40;
        int lineX = (Game.GAME_WIDTH - lineWidth) / 2;

        g2d.setStroke(new BasicStroke(3));
        g2d.drawLine(lineX, lineY, lineX + lineWidth, lineY);

        
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, originalAntialiasing);
    }

    private void drawRoundBox(Graphics g, int x, int y, int width, int height, int round) {
        Graphics2D g2d = (Graphics2D) g;

        
        Stroke originalStroke = g2d.getStroke();
        Object originalAntialiasing = g2d.getRenderingHint(RenderingHints.KEY_ANTIALIASING);

        
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        
        GradientPaint gradient = new GradientPaint(
                x, y, new Color(40, 40, 50, 220),
                x, y + height, new Color(20, 20, 30, 220));
        g2d.setPaint(gradient);
        g2d.fillRoundRect(x, y, width, height, 15, 15);

        
        g2d.setColor(new Color(60, 60, 70, 200));
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(x + 4, y + 4, width - 8, height - 8, 12, 12);

        
        g2d.setColor(borderColor);
        g2d.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.drawRoundRect(x, y, width, height, 15, 15);

        
        drawBoxTitle(g2d, x, y + 8, width, round);

        
        GradientPaint linePaint = new GradientPaint(
                x + 20, y + 70, new Color(255, 215, 0, 50),
                x + width/2, y + 70, new Color(255, 215, 0, 255),
                true);
        g2d.setPaint(linePaint);
        g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.drawLine(x + 20, y + 70, x + width - 20, y + 70);

        
        drawScores(g2d, x, y, width, height, round);

        
        g2d.setStroke(originalStroke);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, originalAntialiasing);
    }

    private void drawBoxTitle(Graphics2D g2d, int x, int y, int width, int round) {
        
        int titleHeight = 40;
        GradientPaint titleGradient = new GradientPaint(
                x, y, new Color(60, 60, 80, 180),
                x, y + titleHeight, new Color(40, 40, 60, 180));
        g2d.setPaint(titleGradient);
        g2d.fillRoundRect(x + 10, y + 10, width - 20, titleHeight, 10, 10);

        
        g2d.setFont(rankFont);
        String roundTitle = "ROUND " + (round + 1);
        FontMetrics fm = g2d.getFontMetrics();
        int titleX = x + (width - fm.stringWidth(roundTitle)) / 2;

        
        for (int i = 2; i > 0; i--) {
            g2d.setColor(new Color(255, 215, 0, 50));
            g2d.drawString(roundTitle, titleX - i, y + 38);
            g2d.drawString(roundTitle, titleX + i, y + 38);
            g2d.drawString(roundTitle, titleX, y + 38 - i);
            g2d.drawString(roundTitle, titleX, y + 38 + i);
        }

        
        g2d.setColor(titleColor);
        g2d.drawString(roundTitle, titleX, y + 38);
    }

    private void drawScores(Graphics2D g2d, int x, int y, int width, int height, int round) {
        g2d.setFont(pixelFont);
        int startY = y + 110;
        int entrySpacing = 70; 

        List<ScoreEntry> scores = roundScores.get(round);
        for (int i = 0; i < scores.size(); i++) {
            ScoreEntry entry = scores.get(i);
            int currentY = startY + i * entrySpacing;

            
            drawPlayerEntryBackground(g2d, x + 25, currentY - 25, width - 50, entrySpacing - 10, i);

            
            drawRank(g2d, i + 1, x + 50, currentY + 10);

            
            FontMetrics fmName = g2d.getFontMetrics(pixelFont);
            int nameWidth = fmName.stringWidth(entry.playerName);
            int centerX = x + width/2;
            int nameX = centerX - nameWidth/2;

            
            g2d.setColor(textColor);
            g2d.drawString(entry.playerName, nameX, currentY + 3);

            
            g2d.setFont(timeFont);
            FontMetrics fmTime = g2d.getFontMetrics();
            String timeText = entry.time;

            
            String timeLabel = timeText;
            int timeLabelWidth = fmTime.stringWidth(timeLabel);
            g2d.setColor(new Color(180, 180, 180));
            g2d.drawString(timeLabel, centerX - timeLabelWidth/2, currentY + 27);

            g2d.setFont(pixelFont);
        }
    }

    private void drawPlayerEntryBackground(Graphics2D g2d, int x, int y, int width, int height, int position) {
        
        Color bgColor;
        float alpha = 0.2f;

        switch (position) {
            case 0: bgColor = new Color(255, 215, 0, (int)(alpha * 255)); break; 
            case 1: bgColor = new Color(192, 192, 192, (int)(alpha * 255)); break; 
            case 2: bgColor = new Color(205, 127, 50, (int)(alpha * 255)); break; 
            default: bgColor = new Color(60, 60, 60, (int)(alpha * 255)); 
        }

        
        g2d.setColor(bgColor);
        g2d.fillRoundRect(x, y, width, height, 10, 10);

        
        Color borderColor;
        switch (position) {
            case 0: borderColor = new Color(255, 215, 0, 100); break;
            case 1: borderColor = new Color(192, 192, 192, 100); break;
            case 2: borderColor = new Color(205, 127, 50, 100); break;
            default: borderColor = new Color(100, 100, 100, 80);
        }

        g2d.setColor(borderColor);
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawRoundRect(x, y, width, height, 10, 10);
    }

    private void drawRank(Graphics2D g2d, int rank, int x, int y) {
        
        if (rank <= 3) {
            Color rankBgColor;
            switch (rank) {
                case 1: rankBgColor = new Color(255, 215, 0, 150); break; 
                case 2: rankBgColor = new Color(192, 192, 192, 150); break; 
                case 3: rankBgColor = new Color(205, 127, 50, 150); break; 
                default: rankBgColor = new Color(100, 100, 100, 100);
            }

            
            g2d.setColor(rankBgColor);
            g2d.fillOval(x - 15, y - 20, 30, 30);

            
            g2d.setColor(rankColor);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawOval(x - 15, y - 20, 30, 30);

            
            FontMetrics fm = g2d.getFontMetrics();
            int rankWidth = fm.stringWidth(Integer.toString(rank));
            g2d.setColor(Color.WHITE);
            g2d.drawString(Integer.toString(rank), x - rankWidth/2 + 1, y + 5);
        } else {
            
            g2d.setColor(rankColor);
            g2d.drawString(rank + ".", x - 10, y + 5);
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (isIn(e, backButton)) {
            backButton.setMousePressed(true);
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (backButton.isMousePressed() && isIn(e, backButton)) {
            
            Gamestate.state = Gamestate.MENU;
        }

        backButton.resetBools();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        backButton.setMouseOver(false);

        if (isIn(e, backButton)) {
            backButton.setMouseOver(true);

            
            if (!cursorChanged) {
                game.getCanvas().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                cursorChanged = true;
            }
        } else {
            
            if (cursorChanged) {
                game.getCanvas().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                cursorChanged = false;
            }
        }
    }

    private boolean isIn(MouseEvent e, TextButton b) {
        return b.getBounds().contains(e.getX(), e.getY());
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            Gamestate.state = Gamestate.MENU;
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        
    }

    @Override
    public void keyReleased(KeyEvent e) {
        
    }

    
    private class ScoreEntry {
        String playerName;
        String time; 

        public ScoreEntry(String playerName, String time) {
            this.playerName = playerName;
            this.time = time;
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
            g.setFont(textButtonFont);
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
            g2d.setFont(textButtonFont);

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