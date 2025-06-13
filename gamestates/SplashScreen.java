package gamestates;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.util.ArrayList;
import java.util.Random;

import effects.FloatingParticle;
import main.Game;
import utilz.LoadSave;

public class SplashScreen extends State implements Statemethods {

    private BufferedImage background;
    private Font titleFont, subtitleFont;


    private float alpha = 0f;
    private final float FADE_IN_SPEED = 0.01f;
    private final float FADE_OUT_SPEED = 0.02f;
    private int displayTime = 0;
    private final int DISPLAY_DURATION = 5 * 120;
    private boolean fadingIn = true;
    private boolean fadingOut = false;


    private float yOffset = 0;
    private final float FLOAT_AMPLITUDE = 10.0f;
    private final float FLOAT_SPEED = 0.05f;


    private ArrayList<FloatingParticle> particles;
    private final int MAX_PARTICLES = 50;
    private final Random random = new Random();

    public SplashScreen(Game game) {
        super(game);
        loadResources();
        initializeParticles();
    }

    private void loadResources() {
        background = LoadSave.GetSpriteAtlas(LoadSave.SPLASH);


        
        try {
            
            titleFont = Font.createFont(Font.TRUETYPE_FONT, new File("res/fonts/PressStart2P.ttf")).deriveFont(Font.BOLD, (int)(40 * Game.SCALE));
            
            
            

            subtitleFont = Font.createFont(Font.TRUETYPE_FONT, new File("res/fonts/PressStart2P.ttf")).deriveFont(Font.BOLD, (int)(12 * Game.SCALE));
        } catch (Exception e) {
            e.printStackTrace();
            titleFont = new Font("Arial", Font.BOLD, (int)(50 * Game.SCALE));
            subtitleFont = new Font("Arial", Font.PLAIN, (int)(16 * Game.SCALE));
        }
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

    @Override
    public void update() {

        yOffset = (float) Math.sin(System.currentTimeMillis() * FLOAT_SPEED / 1000) * FLOAT_AMPLITUDE;


        updateParticles();


        if (fadingIn) {
            alpha += FADE_IN_SPEED;
            if (alpha >= 1.0f) {
                alpha = 1.0f;
                fadingIn = false;
            }
        } else if (fadingOut) {
            alpha -= FADE_OUT_SPEED;
            if (alpha <= 0.0f) {
                alpha = 0.0f;

                Gamestate.state = Gamestate.MENU;

                game.getMenu().startFadeInTransition();
            }
        } else {

            displayTime++;
            if (displayTime >= DISPLAY_DURATION) {
                fadingOut = true;
            }
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

    @Override
    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;


        Composite originalComposite = g2d.getComposite();


        g2d.drawImage(background, 0, 0, Game.GAME_WIDTH, Game.GAME_HEIGHT, null);


        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha * 0.5f));
        ArrayList<FloatingParticle> particlesCopy = new ArrayList<>(particles);
        for (FloatingParticle p : particlesCopy) {
            if (p != null)
                p.draw(g2d);
        }


        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));


        g2d.setFont(titleFont);
        g2d.setColor(new Color(0, 0, 0, 150)); 
        drawCenteredString(g2d, "SOLO LEVELING", Game.GAME_WIDTH / 2 + 3, Game.GAME_HEIGHT / 2 + 3 + (int)yOffset);
        g2d.setColor(new Color(0, 162, 232)); 
        drawCenteredString(g2d, "SOLO LEVELING", Game.GAME_WIDTH / 2, Game.GAME_HEIGHT / 2 + (int)yOffset);


        g2d.setFont(subtitleFont);
        g2d.setColor(new Color(173, 216, 230)); 
        drawCenteredString(g2d, "Your journey begins now", Game.GAME_WIDTH / 2, Game.GAME_HEIGHT / 2 + 60);
    }

    private void drawCenteredString(Graphics g, String text, int centerX, int centerY) {
        FontMetrics metrics = g.getFontMetrics();
        int x = centerX - metrics.stringWidth(text) / 2;
        int y = centerY;
        g.drawString(text, x, y);
    }


    @Override
    public void mousePressed(MouseEvent e) {
        skipToMenu();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        skipToMenu();
    }

    private void skipToMenu() {
        if (!fadingOut) {
            fadingOut = true;
            fadingIn = false;
        }
    }


    @Override
    public void mouseReleased(MouseEvent e) {}

    @Override
    public void mouseMoved(MouseEvent e) {}

    @Override
    public void mouseClicked(MouseEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {}
}