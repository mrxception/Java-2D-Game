package effects;

import java.awt.*;
import java.util.Random;

import main.Game;

public class ShootingStar {
    private float x, y;
    private float startX, startY;
    private float endX, endY;
    private float speed;
    private float progress;
    private float length;
    private float thickness;
    private Color color;
    private boolean active;
    private long spawnTime;
    private int duration;
    private Random random;

    public ShootingStar() {
        this.random = new Random();
        this.active = false;
        reset();
    }

    public void reset() {
        boolean moveRight = random.nextFloat() > 0.3f;

        startX = moveRight ?
                random.nextInt((int)(Game.GAME_WIDTH * 0.5f)) - Game.GAME_WIDTH * 0.2f :
                random.nextInt((int)(Game.GAME_WIDTH * 0.8f)) + Game.GAME_WIDTH * 0.2f;

        startY = random.nextInt((int)(Game.GAME_HEIGHT * 0.3f));

        endX = moveRight ?
                startX + random.nextInt((int)(Game.GAME_WIDTH * 0.7f)) + Game.GAME_WIDTH * 0.3f :
                startX - random.nextInt((int)(Game.GAME_WIDTH * 0.7f)) - Game.GAME_WIDTH * 0.3f;

        endY = startY + random.nextInt((int)(Game.GAME_HEIGHT * 0.5f)) + Game.GAME_HEIGHT * 0.1f;

        x = startX;
        y = startY;

        progress = 0;
        speed = random.nextFloat() * 0.009f + 0.003f;
        length = random.nextFloat() * 60 + 40;
        thickness = random.nextFloat() * 2.5f + 1.0f;

        int blue = random.nextInt(55) + 200;
        int red = random.nextInt(55) + 200;
        int green = random.nextInt(55) + 200;
        color = new Color(red, green, blue);

        duration = random.nextInt(1000) + 1000;

        active = false;
    }

    public void activate() {
        active = true;
        progress = 0;
        spawnTime = System.currentTimeMillis();
    }

    public void update() {
        if (!active) return;

        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - spawnTime;

        progress = Math.min(1.0f, (float)elapsedTime / duration);

        x = startX + (endX - startX) * progress;
        y = startY + (endY - startY) * progress;

        if (progress >= 1.0f) {
            active = false;
            reset();
        }
    }

    public void draw(Graphics g, int xLvlOffset) {
        if (!active) return;

        Graphics2D g2d = (Graphics2D) g;

        Composite originalComposite = g2d.getComposite();
        Stroke originalStroke = g2d.getStroke();
        Color originalColor = g2d.getColor();

        float adjustedX = x - xLvlOffset * 0.05f;
        float adjustedY = y;

        float trailX = adjustedX - (endX - startX) * speed * length;
        float trailY = adjustedY - (endY - startY) * speed * length;

        GradientPaint gradient = new GradientPaint(
                adjustedX, adjustedY, color,
                trailX, trailY, new Color(color.getRed(), color.getGreen(), color.getBlue(), 0)
        );

        g2d.setPaint(gradient);
        g2d.setStroke(new BasicStroke(thickness, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.drawLine((int)adjustedX, (int)adjustedY, (int)trailX, (int)trailY);

        g2d.setColor(Color.WHITE);
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.8f));
        g2d.fillOval((int)(adjustedX - thickness), (int)(adjustedY - thickness),
                (int)(thickness * 2), (int)(thickness * 2));

        g2d.setComposite(originalComposite);
        g2d.setStroke(originalStroke);
        g2d.setColor(originalColor);
    }

    public boolean isActive() {
        return active;
    }
}