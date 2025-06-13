package effects;

import java.awt.*;

import java.util.Random;

public class AuraParticle extends DashParticle {
    private float fadeRate;
    private float growthRate;
    private float initialSize;
    private Random random = new Random();

    public AuraParticle(float x, float y, float speedX, float speedY, float size, int lifetime, Color color) {
        super(x, y, speedX, speedY, size, lifetime, color, true);
        this.fadeRate = 255f / lifetime;
        this.growthRate = 0.05f;
        this.initialSize = size;
    }

    @Override
    public void update() {
        super.update();

        if (getLifetime() > getMaxLifetime() * 0.7f) {
            setSize(getSize() * (1 + growthRate));
        } else {
            setSize(getSize() * 0.97f);
        }

        setSpeedX(getSpeedX() + (random.nextFloat() - 0.5f) * 0.1f);

        setSpeedY(getSpeedY() * 1.01f);

        Color currentColor = getColor();
        int alpha = Math.max(0, currentColor.getAlpha() - (int)(fadeRate * 1.5f));
        setColor(new Color(currentColor.getRed(), currentColor.getGreen(),
                currentColor.getBlue(), alpha));
    }

    @Override
    public void render(Graphics g, int lvlOffset) {
        Graphics2D g2d = (Graphics2D) g;
        Composite originalComposite = g2d.getComposite();

        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
        g2d.setColor(new Color(getColor().getRed(), getColor().getGreen(),
                getColor().getBlue(), getColor().getAlpha() / 3));

        int glowSize = (int)(getSize() * 3);

        g2d.fillOval(
                (int)(getX() - glowSize/2 - lvlOffset),
                (int)(getY() - glowSize/2),
                glowSize, glowSize
        );

        g2d.setComposite(originalComposite);

        super.render(g, lvlOffset);
    }

    public int getLifetime() {
        return super.currentLife;
    }

    public int getMaxLifetime() {
        return super.lifetime;
    }

    public float getSize() {
        return super.size;
    }

    public void setSize(float size) {
        super.size = size;
    }

    public float getSpeedX() {
        return super.speedX;
    }

    public void setSpeedX(float speedX) {
        super.speedX = speedX;
    }

    public float getSpeedY() {
        return super.speedY;
    }

    public void setSpeedY(float speedY) {
        super.speedY = speedY;
    }

    public Color getColor() {
        return super.color;
    }

    public void setColor(Color color) {
        super.color = color;
    }

    public float getX() {
        return super.x;
    }

    public float getY() {
        return super.y;
    }
}