package effects;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.AlphaComposite;

public class DashParticle {
    protected float x, y;
    protected float speedX, speedY;
    protected float size;
    protected int lifetime;
    protected int currentLife;
    protected Color color;
    protected boolean active = true;
    protected boolean isCircular;
    protected float scale;

    public DashParticle(float x, float y, float speedX, float speedY, float size, int lifetime, Color color) {
        this(x, y, speedX, speedY, size, lifetime, color, true);
    }

    public DashParticle(float x, float y, float speedX, float speedY, float size, int lifetime, Color color, boolean isCircular) {
        this.x = x;
        this.y = y;
        this.speedX = speedX;
        this.speedY = speedY;
        this.size = size;
        this.lifetime = lifetime;
        this.currentLife = lifetime;
        this.color = color;
        this.isCircular = isCircular;
        this.scale = 1.0f;
    }

    public void update() {
        if (!active) return;

        x += speedX;
        y += speedY;
        currentLife--;

        scale = (float) currentLife / lifetime;
        size *= 0.98f;
        speedX *= 0.95f;
        speedY *= 0.95f;

        if (currentLife <= 0) {
            active = false;
        }
    }

    public void render(Graphics g, int lvlOffset) {
        if (!active) return;

        Graphics2D g2d = (Graphics2D) g;
        float alpha = (float) currentLife / lifetime;

        java.awt.Composite originalComposite = g2d.getComposite();

        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

        Color renderColor = new Color(
                color.getRed(),
                (int)(color.getGreen() * (0.5 + 0.5 * scale)),
                (int)(color.getBlue() * 0.5),
                color.getAlpha()
        );
        g2d.setColor(renderColor);

        float renderSize = size * scale;
        if (isCircular) {
            g2d.fill(new Ellipse2D.Float(
                    x - lvlOffset - renderSize/2,
                    y - renderSize/2,
                    renderSize,
                    renderSize
            ));
        } else {
            g2d.fillRect(
                    (int)(x - lvlOffset - renderSize/2),
                    (int)(y - renderSize/2),
                    (int)renderSize,
                    (int)renderSize
            );
        }

        g2d.setComposite(originalComposite);
    }

    public boolean isActive() {
        return active;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getSpeedX() {
        return speedX;
    }

    public void setSpeedX(float speedX) {
        this.speedX = speedX;
    }

    public float getSpeedY() {
        return speedY;
    }

    public void setSpeedY(float speedY) {
        this.speedY = speedY;
    }

    public float getSize() {
        return size;
    }

    public void setSize(float size) {
        this.size = size;
    }

    public int getLifetime() {
        return currentLife;
    }

    public int getMaxLifetime() {
        return lifetime;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }
}