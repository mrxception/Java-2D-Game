package gamestates;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Random;

import audio.AudioPlayer;
import effects.FloatingParticle;
import main.Game;
import utilz.LoadSave;

import static utilz.Constants.UI.Background.BACKGROUND_HEIGHT;
import static utilz.Constants.UI.Background.BACKGROUND_WIDTH;

public class StoryLine extends State implements Statemethods {


    private ArrayList<String> storyPages;
    private int currentPage = 0;


    private Rectangle skipButton;
    private Font storyFont;
    private Font storyFont2;
    private Font buttonFont;
    private Font otherFont;


    private Color overlayColor = new Color(0, 0, 0, 180);


    private int textCharIndex = 0;
    private final int CHAR_DISPLAY_SPEED = 2;
    private int charDisplayTick = 0;
    private boolean pageFullyDisplayed = false;


    private boolean initialTransition = true;
    private int fadeAlpha = 255;
    private final int FADE_SPEED = 5;


    private int leftCurtainPos = 0;
    private int rightCurtainPos = 0;
    private final int CURTAIN_SPEED = 3;


    private final int CONTINUE_BUTTON_WIDTH = 260;
    private final int SKIP_BUTTON_WIDTH = 130;
    private final int BUTTON_HEIGHT = 50;
    private final int BUTTON_MARGIN = 20;

    private BufferedImage[] backgroundFrames;
    private int aniTick, aniIndex;
    private final int ANIMATION_SPEED = 50;
    private final int FRAME_COUNT = 3;

    private ArrayList<FloatingParticle> particles;
    private final int MAX_PARTICLES = 50;
    private final Random random = new Random();


    private boolean showNameInput = false;
    private NameInputField nameInputField;
    private Rectangle continueButton;


    private float hintOpacity = 1.0f;
    private boolean hintFadingOut = true;
    private final float HINT_FADE_SPEED = 0.003f;
    private final float MIN_HINT_OPACITY = 0.6f;
    private final float MAX_HINT_OPACITY = 1.0f;

    private boolean fadeOutTransition = false;
    private int fadeOutAlpha = 0;
    private final int FADE_OUT_SPEED = 5;


    private boolean nameInputTransition = false;
    private int nameInputFadeAlpha = 0;
    private final int NAME_FADE_SPEED = 3;
    private int nameLeftCurtainPos = 0;
    private int nameRightCurtainPos = 0;


    private boolean skipButtonHovered = false;
    private boolean continueButtonHovered = false;


    private boolean skipButtonPressed = false;
    private boolean continueButtonPressed = false;


    private boolean cursorChanged = false;


    public StoryLine(Game game) {
        super(game);
        initializeStory();
        loadAssets();
        initializeParticles();
        initializeUI();

        nameInputField.setText(game.getPlayerName());


        leftCurtainPos = Game.GAME_WIDTH / 2;
        rightCurtainPos = Game.GAME_WIDTH / 2;
    }

    private void initializeParticles() {
        particles = new ArrayList<>();
        for (int i = 0; i < MAX_PARTICLES; i++) {
            addNewParticle();
        }
    }

    private void addNewParticle() {
        int x = random.nextInt(Game.GAME_WIDTH);
        int y = random.nextInt(Game.GAME_HEIGHT);
        float size = 1 + random.nextFloat() * 3;
        float speed = 0.2f + random.nextFloat() * 0.8f;
        int lifetime = 100 + random.nextInt(200);
        particles.add(new FloatingParticle(x, y, size, speed, lifetime));
    }

    private void initializeStory() {
        storyPages = new ArrayList<>();
        storyPages.add("In a world where mysterious dungeons had appeared, filled with formidable monsters and unsolved mysteries, a new class of warriors emerged.");
        storyPages.add("These warriors, known as Hunters, possessed supernatural abilities that allowed them to delve into these dungeons and combat the threats within.");
        storyPages.add("Among them was a young Hunter, once renowned as the weakest in the world. His name was whispered in disdain by his peers, and his abilities were often questioned. However, fate had other plans for him. ");
        storyPages.add("After a perilous encounter in a particularly treacherous dungeon, he narrowly escaped death and awakened to a unique power known as \"The System.\" This System granted him the ability to level up by completing quests and defeating enemies, allowing him to grow stronger with each victory.");
        storyPages.add("As he began his journey, his ultimate goal was clear: to become the most powerful Hunter and uncover the true purpose behind his newfound abilities.");
        storyPages.add("With each level gained, he grew stronger, but powerful enemies watched his rise with growing concern. They saw him as a potential threat, a force that could disrupt the balance of power in the world.");
        storyPages.add("His journey was not just about personal growth; it was about becoming a legend. He was destined to become the Shadow Monarch, a figure feared and respected by all.");
        storyPages.add("The path ahead was fraught with danger, but he was determined to uncover the secrets behind his powers and the mysterious dungeons that had become a part of his world.");
        storyPages.add("In this world of shadows and strength, only the bravest and most cunning would survive. The young Hunter's story was just beginning, and the world awaited its Shadow Monarch. Would he rise to the challenge and claim his rightful place, or would the shadows consume him? Only time would tell.");
    }

    private void loadAssets() {
        loadBackground();
        try {
            storyFont = Font.createFont(Font.TRUETYPE_FONT, new File("res/fonts/pixel_font.ttf"))
                    .deriveFont(Font.PLAIN, (int)(24 * Game.SCALE));
            storyFont2 = Font.createFont(Font.TRUETYPE_FONT, new File("res/fonts/PressStart2P.ttf"))
                    .deriveFont(Font.BOLD, (int)(20 * Game.SCALE));
            buttonFont = Font.createFont(Font.TRUETYPE_FONT, new File("res/fonts/PressStart2P.ttf"))
                    .deriveFont(Font.PLAIN, (int)(12 * Game.SCALE));
            otherFont = Font.createFont(Font.TRUETYPE_FONT, new File("res/fonts/pixel_font.ttf"))
                    .deriveFont(Font.PLAIN, (int)(16 * Game.SCALE));
        } catch (Exception e) {
            e.printStackTrace();
            storyFont = new Font("Courier New", Font.BOLD, (int)(24 * Game.SCALE));
            buttonFont = new Font("Courier New", Font.BOLD, (int)(16 * Game.SCALE));
        }
    }

    private void initializeUI() {
        skipButton = new Rectangle(
                Game.GAME_WIDTH - SKIP_BUTTON_WIDTH - BUTTON_MARGIN,
                Game.GAME_HEIGHT - BUTTON_HEIGHT - BUTTON_MARGIN,
                SKIP_BUTTON_WIDTH,
                BUTTON_HEIGHT
        );

        nameInputField = new NameInputField(
                Game.GAME_WIDTH / 2 - 150,
                Game.GAME_HEIGHT / 2 - 20,
                300,
                40
        );

        continueButton = new Rectangle(
                Game.GAME_WIDTH / 2 - CONTINUE_BUTTON_WIDTH / 2,
                Game.GAME_HEIGHT / 2 + 50,
                CONTINUE_BUTTON_WIDTH,
                BUTTON_HEIGHT
        );
    }

    @Override
    public void update() {
        updateBackgroundAnimation();
        updateParticles();
        updateHintAnimation();

        if (fadeOutTransition) {
            fadeOutAlpha += FADE_OUT_SPEED;
            if (fadeOutAlpha >= 255) {
                fadeOutAlpha = 255;
                fadeOutTransition = false;

                Gamestate.state = Gamestate.PLAYING;
                game.getPlaying().startGame();
                game.getPlaying().startFadeInTransition();
                game.getAudioPlayer().setLevelSong(game.getPlaying().getLevelManager().getLevelIndex());
            }
            return;
        }


        if (nameInputTransition) {

            if (nameLeftCurtainPos < Game.GAME_WIDTH / 2 || nameRightCurtainPos < Game.GAME_WIDTH / 2) {
                nameLeftCurtainPos += CURTAIN_SPEED;
                if (nameLeftCurtainPos > Game.GAME_WIDTH / 2)
                    nameLeftCurtainPos = Game.GAME_WIDTH / 2;

                nameRightCurtainPos += CURTAIN_SPEED;
                if (nameRightCurtainPos > Game.GAME_WIDTH / 2)
                    nameRightCurtainPos = Game.GAME_WIDTH / 2;
            }

            else if (nameInputFadeAlpha < 255) {
                nameInputFadeAlpha += NAME_FADE_SPEED;
                if (nameInputFadeAlpha >= 255) {
                    nameInputFadeAlpha = 255;

                    showNameInput = true;
                    nameInputField.setSelected(true);


                    nameLeftCurtainPos = Game.GAME_WIDTH / 2;
                    nameRightCurtainPos = Game.GAME_WIDTH / 2;
                    nameInputTransition = false;


                    nameInputFadeAlpha = 255;
                }
            }
            return;
        }


        if (showNameInput && nameInputFadeAlpha > 0) {

            if (nameLeftCurtainPos > 0) {
                nameLeftCurtainPos -= CURTAIN_SPEED;
                if (nameLeftCurtainPos < 0) nameLeftCurtainPos = 0;
            }

            if (nameRightCurtainPos > 0) {
                nameRightCurtainPos -= CURTAIN_SPEED;
                if (nameRightCurtainPos < 0) nameRightCurtainPos = 0;
            }


            if (nameLeftCurtainPos <= 0 && nameRightCurtainPos <= 0) {
                nameInputFadeAlpha -= NAME_FADE_SPEED;
                if (nameInputFadeAlpha < 0) nameInputFadeAlpha = 0;
            }

            nameInputField.update();
            return;
        }

        if (showNameInput) {
            nameInputField.update();
            return;
        }


        if (initialTransition) {
            updateInitialTransition();
        } else if (!pageFullyDisplayed) {
            charDisplayTick++;
            if (charDisplayTick >= CHAR_DISPLAY_SPEED) {
                charDisplayTick = 0;
                textCharIndex++;
                if (textCharIndex >= storyPages.get(currentPage).length()) {
                    pageFullyDisplayed = true;
                }
            }
        }
    }

    private void updateHintAnimation() {
        if (hintFadingOut) {
            hintOpacity -= HINT_FADE_SPEED;
            if (hintOpacity <= MIN_HINT_OPACITY) {
                hintOpacity = MIN_HINT_OPACITY;
                hintFadingOut = false;
            }
        } else {
            hintOpacity += HINT_FADE_SPEED;
            if (hintOpacity >= MAX_HINT_OPACITY) {
                hintOpacity = MAX_HINT_OPACITY;
                hintFadingOut = true;
            }
        }
    }

    private void updateBackgroundAnimation() {
        aniTick++;
        if (aniTick >= ANIMATION_SPEED) {
            aniTick = 0;
            aniIndex = (aniIndex + 1) % FRAME_COUNT;
        }
    }

    private void updateParticles() {
        for (int i = particles.size() - 1; i >= 0; i--) {
            FloatingParticle p = particles.get(i);
            p.update();
            if (p.isExpired()) {
                particles.remove(i);
                addNewParticle();
            }
        }
    }

    private void updateInitialTransition() {
        if (leftCurtainPos > 0) {
            leftCurtainPos -= CURTAIN_SPEED;
            if (leftCurtainPos < 0) leftCurtainPos = 0;
        }

        if (rightCurtainPos > 0) {
            rightCurtainPos -= CURTAIN_SPEED;
            if (rightCurtainPos < 0) rightCurtainPos = 0;
        }

        fadeAlpha -= FADE_SPEED;
        if (fadeAlpha <= 0) {
            fadeAlpha = 0;
            if (leftCurtainPos <= 0 && rightCurtainPos <= 0) {
                initialTransition = false;
            }
        }
    }

    @Override
    public void draw(Graphics g) {
        g.drawImage(backgroundFrames[aniIndex], 0, 0, Game.GAME_WIDTH, Game.GAME_HEIGHT, null);
        g.setColor(overlayColor);
        g.fillRect(0, 0, Game.GAME_WIDTH, Game.GAME_HEIGHT);

        ArrayList<FloatingParticle> particlesCopy = new ArrayList<>(particles);
        for (FloatingParticle p : particlesCopy) {
            if (p != null)
                p.draw(g);
        }

        if (showNameInput) {
            drawNameInputScreen(g);
        } else {
            if (!initialTransition || fadeAlpha < 128) {
                drawSkipButton(g);
                drawStoryText(g);
                drawNavigationHints(g);
                drawPageCounter(g);
            }
            drawTransitionEffects(g);
        }


        if (nameInputTransition || (showNameInput && nameInputFadeAlpha > 0)) {
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, nameLeftCurtainPos, Game.GAME_HEIGHT);
            g.fillRect(Game.GAME_WIDTH - nameRightCurtainPos, 0, nameRightCurtainPos, Game.GAME_HEIGHT);

            if (nameInputFadeAlpha > 0) {
                g.setColor(new Color(0, 0, 0, nameInputFadeAlpha));
                g.fillRect(0, 0, Game.GAME_WIDTH, Game.GAME_HEIGHT);
            }
        }


        if (fadeOutTransition && fadeOutAlpha > 0) {
            g.setColor(new Color(0, 0, 0, fadeOutAlpha));
            g.fillRect(0, 0, Game.GAME_WIDTH, Game.GAME_HEIGHT);
        }
    }

    private void drawStylizedButton(Graphics g, Rectangle button, String text, boolean hovered, boolean pressed) {
        Graphics2D g2d = (Graphics2D) g.create();

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        
        Color SL_BUTTON_BASE = new Color(80, 40, 140);
        Color SL_BUTTON_HOVER = new Color(100, 50, 170);
        Color SL_BUTTON_PRESS = new Color(60, 30, 110);
        Color SL_BUTTON_GLOW = new Color(140, 100, 255);
        Color SL_BUTTON_BORDER = new Color(130, 90, 220);

        
        Color topColor, bottomColor;
        if (pressed) {
            topColor = SL_BUTTON_PRESS;
            bottomColor = SL_BUTTON_BASE;
        } else if (hovered) {
            topColor = SL_BUTTON_HOVER;
            bottomColor = new Color(90, 45, 160);
        } else {
            topColor = SL_BUTTON_BASE;
            bottomColor = new Color(40, 20, 80);
        }

        GradientPaint gradient = new GradientPaint(
                button.x, button.y, topColor,
                button.x, button.y + button.height, bottomColor
        );

        
        g2d.setPaint(gradient);
        g2d.fillRoundRect(button.x, button.y, button.width, button.height, 10, 10);

        
        g2d.setColor(SL_BUTTON_BORDER);
        g2d.setStroke(new BasicStroke(1));
        g2d.drawRoundRect(button.x, button.y, button.width, button.height, 10, 10);

        
        drawButtonCorners(g2d, button);

        
        if (hovered && !pressed) {
            float glowIntensity = 0.5f + (0.5f * (float)Math.sin(System.currentTimeMillis() * 0.003));

            
            g2d.setColor(new Color(SL_BUTTON_GLOW.getRed(), SL_BUTTON_GLOW.getGreen(),
                    SL_BUTTON_GLOW.getBlue(), (int)(60 * glowIntensity)));
            g2d.setStroke(new BasicStroke(3));
            g2d.drawRoundRect(button.x - 3, button.y - 3, button.width + 6, button.height + 6, 12, 12);

            
            g2d.setColor(new Color(SL_BUTTON_GLOW.getRed(), SL_BUTTON_GLOW.getGreen(),
                    SL_BUTTON_GLOW.getBlue(), (int)(120 * glowIntensity)));
            g2d.setStroke(new BasicStroke(1.5f));
            g2d.drawRoundRect(button.x - 1, button.y - 1, button.width + 2, button.height + 2, 11, 11);
        }

        
        g2d.setFont(buttonFont);
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        int textHeight = fm.getHeight();
        int textY = button.y + (button.height + textHeight) / 2;

        if (pressed) {
            textY += 1;
        }

        
        g2d.setColor(new Color(0, 0, 0, 120));
        g2d.drawString(text, button.x + (button.width - textWidth) / 2 + 1, textY + 1);

        
        g2d.setColor(Color.white);
        g2d.drawString(text, button.x + (button.width - textWidth) / 2, textY);

        g2d.dispose();
    }

    private void drawButtonCorners(Graphics2D g2d, Rectangle button) {
        final int CORNER_SIZE = 8;
        Color cornerColor = new Color(160, 120, 255);
        g2d.setColor(cornerColor);

        
        g2d.drawLine(button.x, button.y + CORNER_SIZE, button.x + CORNER_SIZE, button.y);

        
        g2d.drawLine(button.x + button.width - CORNER_SIZE, button.y,
                button.x + button.width, button.y + CORNER_SIZE);

        
        g2d.drawLine(button.x, button.y + button.height - CORNER_SIZE,
                button.x + CORNER_SIZE, button.y + button.height);

        
        g2d.drawLine(button.x + button.width - CORNER_SIZE, button.y + button.height,
                button.x + button.width, button.y + button.height - CORNER_SIZE);

        
        int offset = 3;

        
        g2d.drawLine(button.x + offset, button.y, button.x, button.y + offset);

        
        g2d.drawLine(button.x + button.width - offset, button.y,
                button.x + button.width, button.y + offset);

        
        g2d.drawLine(button.x, button.y + button.height - offset,
                button.x + offset, button.y + button.height);

        
        g2d.drawLine(button.x + button.width - offset, button.y + button.height,
                button.x + button.width, button.y + button.height - offset);
    }


    private void drawNameInputScreen(Graphics g) {
        g.setFont(storyFont2);
        g.setColor(new Color(230, 230, 255)); 
        String title = "HUNTER, WHAT IS YOUR NAME?";
        FontMetrics fm = g.getFontMetrics();
        int titleWidth = fm.stringWidth(title);
        g.drawString(title, (Game.GAME_WIDTH - titleWidth) / 2, Game.GAME_HEIGHT / 2 - 80);

        nameInputField.draw(g);

        
        drawStylizedButton(g, continueButton, "CONTINUE", continueButtonHovered, continueButtonPressed);
    }

    private void drawTransitionEffects(Graphics g) {
        if (initialTransition) {
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, leftCurtainPos, Game.GAME_HEIGHT);
            g.fillRect(Game.GAME_WIDTH - rightCurtainPos, 0, rightCurtainPos, Game.GAME_HEIGHT);

            if (fadeAlpha > 0) {
                g.setColor(new Color(0, 0, 0, fadeAlpha));
                g.fillRect(0, 0, Game.GAME_WIDTH, Game.GAME_HEIGHT);
            }
        }
    }

    private void drawStoryText(Graphics g) {
        g.setFont(storyFont);
        g.setColor(Color.WHITE);
        String fullText = storyPages.get(currentPage);
        String displayText = pageFullyDisplayed ? fullText : fullText.substring(0, textCharIndex);
        drawWrappedText(g, displayText, 100, 150, Game.GAME_WIDTH - 200);
    }

    private void drawWrappedText(Graphics g, String text, int x, int y, int maxWidth) {
        FontMetrics fm = g.getFontMetrics();
        int lineHeight = fm.getHeight();
        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();
        int curY = y;

        for (String word : words) {
            if (fm.stringWidth(currentLine + word) < maxWidth) {
                currentLine.append(word).append(" ");
            } else {
                g.drawString(currentLine.toString(), x, curY);
                curY += lineHeight;
                currentLine = new StringBuilder(word + " ");
            }
        }

        if (currentLine.length() > 0) {
            g.drawString(currentLine.toString(), x, curY);
        }
    }

    private void drawNavigationHints(Graphics g) {

        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setFont(otherFont);
        g2d.setColor(Color.LIGHT_GRAY);


        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, hintOpacity));

        String hint = pageFullyDisplayed ?
                "Press SPACE or click to continue" :
                "Press SPACE to show all text";
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(hint);
        int hintX = (Game.GAME_WIDTH - textWidth) / 2;
        int hintY = Game.GAME_HEIGHT - 80;


        g2d.drawString(hint, hintX, hintY);

        g2d.dispose();
    }

    private void drawPageCounter(Graphics g) {
        g.setFont(otherFont);
        g.setColor(Color.LIGHT_GRAY);
        String pageCounter = (currentPage + 1) + "/" + storyPages.size();
        g.drawString(pageCounter, 20, Game.GAME_HEIGHT - 20);
    }

    private void drawSkipButton(Graphics g) {
        drawStylizedButton(g, skipButton, "SKIP", skipButtonHovered, skipButtonPressed);
    }

    private void advanceStory() {
        if (initialTransition || nameInputTransition) return;

        if (!pageFullyDisplayed) {
            pageFullyDisplayed = true;
            textCharIndex = storyPages.get(currentPage).length();
        } else {
            boolean finished = false;
            if (currentPage != storyPages.size() - 1)
                currentPage++;
            else
                finished = true;

            System.out.println(currentPage + " " + (storyPages.size() - 1));
            if (currentPage >= storyPages.size() - 1) {
                if (finished)
                    startNameInputTransition();
            } else {
                textCharIndex = 0;
                pageFullyDisplayed = false;
            }
        }
    }

    private void startNameInputTransition() {
        nameInputTransition = true;
        nameInputFadeAlpha = 0;
        nameLeftCurtainPos = 0;
        nameRightCurtainPos = 0;
    }

    private void startGame() {
        if (nameInputField.getText().trim().isEmpty()) {
            nameInputField.generateRandomName();
        }
        game.setPlayerName(nameInputField.getText());

        startFadeOutTransition();
    }

    private void startFadeOutTransition() {
        fadeOutTransition = true;
        fadeOutAlpha = 0;
    }

    private void loadBackground() {
        backgroundFrames = new BufferedImage[FRAME_COUNT];
        BufferedImage atlas = LoadSave.GetSpriteAtlas(LoadSave.STORY_BACKGROUND_IMG);
        for (int i = 0; i < FRAME_COUNT; i++) {
            backgroundFrames[i] = atlas.getSubimage(
                    i * BACKGROUND_WIDTH,
                    0,
                    BACKGROUND_WIDTH,
                    BACKGROUND_HEIGHT
            );
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            if (nameInputTransition) return;

            if (showNameInput) {
                if (continueButton.contains(e.getX(), e.getY())) {
                    startGame();
                } else if (nameInputField.getBounds().contains(e.getPoint())) {
                    nameInputField.setSelected(true);
                } else {
                    nameInputField.setSelected(false);
                }
            } else if (!initialTransition && skipButton.contains(e.getX(), e.getY())) {
                startNameInputTransition();
            } else if (!initialTransition) {
                advanceStory();
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            if (showNameInput) {
                if (continueButton.contains(e.getX(), e.getY())) {
                    continueButtonPressed = true;
                }
            } else if (!initialTransition) {
                if (skipButton.contains(e.getX(), e.getY())) {
                    skipButtonPressed = true;
                }
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        continueButtonPressed = false;
        skipButtonPressed = false;

        exitState();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        boolean mouseOverButton = false;


        if (showNameInput) {
            continueButtonHovered = continueButton.contains(e.getX(), e.getY());
            mouseOverButton = continueButtonHovered;
        } else if (!initialTransition) {
            skipButtonHovered = skipButton.contains(e.getX(), e.getY());
            mouseOverButton = skipButtonHovered;
        }


        if (mouseOverButton && !cursorChanged) {
            game.getCanvas().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            cursorChanged = true;
        } else if (!mouseOverButton && cursorChanged) {
            game.getCanvas().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            cursorChanged = false;
        }
    }


    public void exitState() {
        game.getCanvas().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        cursorChanged = false;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (nameInputTransition) return;

        if (showNameInput) {
            if (nameInputField.isSelected()) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    startGame();
                } else {
                    nameInputField.keyPressed(e);
                }
            }
            return;
        }

        if (initialTransition) return;

        switch (e.getKeyCode()) {
            case KeyEvent.VK_SPACE:
            case KeyEvent.VK_ENTER:
                advanceStory();
                break;
            case KeyEvent.VK_ESCAPE:
                startNameInputTransition();
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    private class NameInputField {
        private int x, y, width, height;
        private String text = "";
        private boolean selected = false;
        private java.awt.Rectangle bounds;
        private long lastBlinkTime = 0;
        private boolean showCursor = true;
        private final int CURSOR_BLINK_RATE = 500;
        private final int MAX_CHARS = 20;
        private final int TEXT_PADDING = 10;

        
        private final Color SL_BORDER = new Color(103, 58, 183);
        private final Color SL_GLOW = new Color(140, 100, 255);
        private final Color SL_INNER_GLOW = new Color(80, 200, 255);
        private final Color SL_TEXT = new Color(230, 230, 255);

        
        private final int CORNER_SIZE = 10;
        private BufferedImage decorationImg;
        private float pulseEffect = 0.0f;
        private boolean pulseIncreasing = true;
        private final float PULSE_SPEED = 0.02f;

        public NameInputField(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.bounds = new java.awt.Rectangle(x, y, width, height);

            
            
            decorationImg = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = decorationImg.createGraphics();
            g2d.setColor(new Color(140, 100, 255));
            g2d.fillRect(0, 0, 32, 32);
            g2d.dispose();
        }

        public void update() {
            if (selected && System.currentTimeMillis() - lastBlinkTime > CURSOR_BLINK_RATE) {
                showCursor = !showCursor;
                lastBlinkTime = System.currentTimeMillis();
            }

            
            if (pulseIncreasing) {
                pulseEffect += PULSE_SPEED;
                if (pulseEffect >= 1.0f) {
                    pulseEffect = 1.0f;
                    pulseIncreasing = false;
                }
            } else {
                pulseEffect -= PULSE_SPEED;
                if (pulseEffect <= 0.0f) {
                    pulseEffect = 0.0f;
                    pulseIncreasing = true;
                }
            }
        }

        public void draw(Graphics g) {
            Graphics2D g2d = (Graphics2D) g.create();

            
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            
            GradientPaint bgGradient = new GradientPaint(
                    x, y, new Color(30, 20, 50),
                    x, y + height, new Color(15, 10, 25)
            );
            g2d.setPaint(bgGradient);
            g2d.fillRoundRect(x, y, width, height, 10, 10);

            
            if (selected) {
                
                float alpha = 0.5f + 0.5f * pulseEffect;
                Color outerGlow = new Color(
                        SL_GLOW.getRed(),
                        SL_GLOW.getGreen(),
                        SL_GLOW.getBlue(),
                        (int)(150 * alpha)
                );

                
                g2d.setStroke(new BasicStroke(4));
                g2d.setColor(new Color(outerGlow.getRed(), outerGlow.getGreen(), outerGlow.getBlue(), 40));
                g2d.drawRoundRect(x-4, y-4, width+8, height+8, 12, 12);

                g2d.setStroke(new BasicStroke(2));
                g2d.setColor(new Color(outerGlow.getRed(), outerGlow.getGreen(), outerGlow.getBlue(), 80));
                g2d.drawRoundRect(x-2, y-2, width+4, height+4, 12, 12);

                g2d.setStroke(new BasicStroke(1));
                g2d.setColor(SL_INNER_GLOW);
            } else {
                g2d.setColor(SL_BORDER);
            }
            g2d.drawRoundRect(x, y, width, height, 10, 10);

            
            drawCornerDecorations(g2d);

            
            g2d.setColor(new Color(10, 5, 20));
            g2d.fillRoundRect(x + 5, y + 5, width - 10, height - 10, 5, 5);

            
            g2d.setColor(SL_TEXT);
            g2d.setFont(new Font("Arial", Font.BOLD, 16));
            FontMetrics fm = g2d.getFontMetrics();

            
            g2d.setClip(x + TEXT_PADDING, y, width - (TEXT_PADDING * 2), height);

            int textY = y + (height + fm.getAscent() - fm.getDescent()) / 2;
            int textWidth = fm.stringWidth(text);
            int visibleWidth = width - (TEXT_PADDING * 2);
            int textX = x + TEXT_PADDING;

            if (textWidth > visibleWidth) {
                textX = x + TEXT_PADDING - (textWidth - visibleWidth);
            }

            
            if (text.length() > 0) {
                
                g2d.setColor(new Color(100, 50, 200, 80));
                g2d.drawString(text, textX + 1, textY + 1);

                
                g2d.setColor(SL_TEXT);
                g2d.drawString(text, textX, textY);
            }

            
            if (text.isEmpty() && !selected) {
                g2d.setColor(new Color(150, 150, 200, 100));
                String placeholder = "Enter your hunter name...";
                g2d.drawString(placeholder, textX, textY);
            }

            
            if (selected && showCursor) {
                int cursorX = x + TEXT_PADDING + (textWidth > visibleWidth ? visibleWidth : textWidth);
                g2d.setClip(null);
                g2d.setColor(SL_INNER_GLOW);
                g2d.drawLine(cursorX, y + 8, cursorX, y + height - 8);
            }

            g2d.dispose();
        }

        
        private void drawCornerDecorations(Graphics2D g2d) {
            
            drawCorner(g2d, x, y, 0);

            
            drawCorner(g2d, x + width - CORNER_SIZE, y, 1);

            
            drawCorner(g2d, x, y + height - CORNER_SIZE, 2);

            
            drawCorner(g2d, x + width - CORNER_SIZE, y + height - CORNER_SIZE, 3);
        }

        private void drawCorner(Graphics2D g2d, int x, int y, int position) {
            
            Color cornerColor = selected ?
                    new Color(SL_INNER_GLOW.getRed(), SL_INNER_GLOW.getGreen(), SL_INNER_GLOW.getBlue(),
                            (int)(255 * (0.7f + 0.3f * pulseEffect))) :
                    new Color(SL_BORDER.getRed(), SL_BORDER.getGreen(), SL_BORDER.getBlue(), 200);

            g2d.setColor(cornerColor);

            
            switch (position) {
                case 0: 
                    g2d.drawLine(x, y + CORNER_SIZE/2, x + CORNER_SIZE/2, y);
                    g2d.drawLine(x, y + CORNER_SIZE, x + CORNER_SIZE, y);
                    break;
                case 1: 
                    g2d.drawLine(x + CORNER_SIZE/2, y, x + CORNER_SIZE, y + CORNER_SIZE/2);
                    g2d.drawLine(x, y, x + CORNER_SIZE, y + CORNER_SIZE);
                    break;
                case 2: 
                    g2d.drawLine(x, y + CORNER_SIZE/2, x + CORNER_SIZE/2, y + CORNER_SIZE);
                    g2d.drawLine(x, y, x + CORNER_SIZE, y + CORNER_SIZE);
                    break;
                case 3: 
                    g2d.drawLine(x + CORNER_SIZE/2, y + CORNER_SIZE, x + CORNER_SIZE, y + CORNER_SIZE/2);
                    g2d.drawLine(x, y + CORNER_SIZE, x + CORNER_SIZE, y);
                    break;
            }
        }

        public void generateRandomName() {
            Random rand = new Random();
            String[] hunterNames = {"SungJin", "Hunter", "Shadow", "Monarch", "Igris", "Beru", "Tusk", "Iron"};
            text = hunterNames[rand.nextInt(hunterNames.length)] + (100 + rand.nextInt(900));
        }

        
        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE && !text.isEmpty()) {
                text = text.substring(0, text.length() - 1);
            } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                selected = false;
            } else {
                char c = e.getKeyChar();
                if ((Character.isLetterOrDigit(c) || c == '_' || c == ' ' || c == '-') && text.length() < MAX_CHARS) {
                    text += c;
                }
            }
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
            if (selected) {
                showCursor = true;
                lastBlinkTime = System.currentTimeMillis();
            }
        }

        public boolean isSelected() {
            return selected;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public java.awt.Rectangle getBounds() {
            return bounds;
        }
    }
}
