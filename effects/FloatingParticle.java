package effects;

import main.Game;

import java.awt.*;
import java.util.Random;

public class FloatingParticle {
    private float x, y;
    private float size;
    private float speed;
    private int lifetime;
    private int currentLife;
    private float angle;
    private Random random;

    public FloatingParticle(float x, float y, float size, float speed, int lifetime) {
        this.x = x;
        this.y = y;
        this.size = size;
        this.speed = speed;
        this.lifetime = lifetime;
        this.currentLife = 0;
        this.random = new Random();
        this.angle = random.nextFloat() * 360;
    }

    public void update() {
        x += Math.sin(angle * Math.PI / 180) * speed;
        y -= 0.5f + (Math.cos(angle * Math.PI / 180) + 1) * speed;

        if (y < -10) y = Game.GAME_HEIGHT + 10;
        if (x < -10) x = Game.GAME_WIDTH + 10;
        if (x > Game.GAME_WIDTH + 10) x = -10;

        currentLife++;

        angle += 0.2f;
    }

    public boolean isExpired() {
        return currentLife >= lifetime;
    }

    public void draw(Graphics g) {
        int opacity = 255;
        if (currentLife < lifetime / 5) {
            opacity = 255 * currentLife / (lifetime / 5);
        } else if (currentLife > lifetime * 4 / 5) {
            opacity = 255 * (lifetime - currentLife) / (lifetime / 5);
        }

        g.setColor(new Color(255, 255, 255, opacity));
        g.fillOval((int)x, (int)y, (int)size, (int)size);
    }


}