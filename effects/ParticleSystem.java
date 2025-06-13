package effects;

import java.awt.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

public class ParticleSystem {
    private CopyOnWriteArrayList<DashParticle> particles = new CopyOnWriteArrayList<>();
    private Random random = new Random();

    public void addParticle(float x, float y, float speedX, float speedY, float size, int lifetime, Color color) {
        particles.add(new DashParticle(x, y, speedX, speedY, size, lifetime, color));
    }

    public void createMovementParticles(float x, float y, boolean isMovingRight, boolean isSpeedBoosted) {
        Color[] fireColors = {
                new Color(20, 20, 20, 200),
                new Color(40, 40, 40, 180),
                new Color(60, 60, 60, 160),
                new Color(80, 80, 80, 140)
        };

        int particleCount = random.nextInt(6) + 15;
        int direction = isMovingRight ? -1 : 1;

        for (int i = 0; i < particleCount; i++) {
            float particleX = x + random.nextFloat() * 15 * direction;
            float particleY = y + random.nextFloat() * 10 - 5;

            float speedX = (random.nextFloat() * 0.8f + 0.5f) * direction;
            float speedY = random.nextFloat() * 0.6f - 0.3f;

            float size = random.nextFloat() * 6 + 4;
            int lifetime = random.nextInt(20) + 15;

            Color baseColor = fireColors[random.nextInt(fireColors.length)];
            Color particleColor = new Color(
                    baseColor.getRed(),
                    baseColor.getGreen(),
                    baseColor.getBlue(),
                    baseColor.getAlpha()
            );

            particles.add(new DashParticle(
                    particleX, particleY,
                    speedX, speedY,
                    size, lifetime,
                    particleColor,
                    true
            ));
        }
    }


    public void createPowerDashParticles(float x, float y, boolean facingRight) {
        Color[] powerColors = new Color[]{
                new Color(220, 240, 255, 160),
                new Color(30, 144, 255, 140),
                new Color(0, 0, 255, 120),
                new Color(65, 105, 225, 100)
        };

        int direction = facingRight ? -1 : 1;

        for (int i = 0; i < 15; i++) {
            float particleX = x + random.nextFloat() * 20 - 10;
            float particleY = y + random.nextFloat() * 30;
            float speedX = (random.nextFloat() * 2 + 1) * direction;
            float speedY = random.nextFloat() * 2 - 1;
            float size = random.nextFloat() * 4 + 2;
            int lifetime = random.nextInt(20) + 10;
            Color color = powerColors[random.nextInt(powerColors.length)];

            addParticle(particleX, particleY, speedX, speedY, size, lifetime, color);
        }
    }

    public void createShadowAuraParticles(float centerX, float centerY, float width) {
        Color[] shadowAuraColors = {
                new Color(102, 51, 153, 160),  
                new Color(72, 61, 139, 140),
                new Color(50, 50, 70, 160),
                new Color(120, 81, 169, 120)
        };

        int particleCount = random.nextInt(6) + 15;

        for (int i = 0; i < particleCount; i++) {
            float angle = random.nextFloat() * 2 * (float)Math.PI;
            float distance = random.nextFloat() * (width/1.5f);

            float particleX = centerX + (float)Math.cos(angle) * distance;
            float particleY = centerY + (float)Math.sin(angle) * distance;

            float speedX = (particleX - centerX) * 0.03f;
            float speedY = -0.3f - random.nextFloat() * 0.7f;

            float size = random.nextFloat() * 15 + 3;
            int lifetime = random.nextInt(30) + 15;

            Color color = shadowAuraColors[random.nextInt(shadowAuraColors.length)];

            particles.add(new AuraParticle(
                    particleX, particleY,
                    speedX, speedY,
                    size, lifetime,
                    color
            ));
        }
    }

    public void update() {
        ArrayList<DashParticle> particlesToRemove = new ArrayList<>();

        for (DashParticle particle : particles) {
            particle.update();
            if (!particle.isActive()) {
                particlesToRemove.add(particle);
            }
        }

        particles.removeAll(particlesToRemove);
    }

    public void render(Graphics g, int lvlOffset) {
        for (DashParticle particle : particles) {
            particle.render(g, lvlOffset);
        }
    }


    public void createLevelUpParticles(float x, float y) {
        Color[] goldColors = {
                new Color(255, 215, 0, 220),
                new Color(255, 255, 150, 200),
                new Color(255, 165, 0, 180),
                new Color(255, 255, 0, 160),
                new Color(255, 200, 0, 140)
        };

        for (int i = 0; i < 30; i++) {
            float speedX = (float)(Math.random() * 8 - 4);
            float speedY = (float)(Math.random() * 8 - 4);
            float size = (float)(Math.random() * 8 + 4);
            int lifetime = (int)(Math.random() * 30 + 20);
            Color color = goldColors[random.nextInt(goldColors.length)];

            particles.add(new DashParticle(
                    x, y,
                    speedX, speedY,
                    size, lifetime,
                    color,
                    true
            ));
        }

        for (int i = 0; i < 20; i++) {
            float offsetX = (float)(Math.random() * 40 - 20);
            float speedX = (float)(Math.random() * 0.8 - 0.4);
            float speedY = (float)(Math.random() * -3 - 2);
            float size = (float)(Math.random() * 6 + 3);
            int lifetime = (int)(Math.random() * 60 + 40);
            Color color = goldColors[random.nextInt(goldColors.length)];

            particles.add(new DashParticle(
                    x + offsetX, y,
                    speedX, speedY,
                    size, lifetime,
                    color,
                    true
            ));
        }

        for (int i = 0; i < 36; i++) {
            float angle = (float)(i * 10 * Math.PI / 180);
            float speed = 1.5f + (float)Math.random() * 0.5f;
            float speedX = (float)Math.cos(angle) * speed;
            float speedY = (float)Math.sin(angle) * speed;
            float size = (float)(Math.random() * 10 + 5);
            int lifetime = (int)(Math.random() * 40 + 30);
            Color color = goldColors[random.nextInt(goldColors.length)];

            particles.add(new DashParticle(
                    x, y,
                    speedX, speedY,
                    size, lifetime,
                    color,
                    true
            ));
        }

        for (int i = 0; i < 15; i++) {
            float speedX = (float)(Math.random() * 4 - 2);
            float speedY = (float)(Math.random() * -3 - 1);
            float size = (float)(Math.random() * 12 + 8);
            int lifetime = (int)(Math.random() * 50 + 40);
            Color color = goldColors[random.nextInt(goldColors.length)];

            particles.add(new DashParticle(
                    x, y,
                    speedX, speedY,
                    size, lifetime,
                    color,
                    false
            ));
        }
    }

    public void createBlastParticles(float x, float y) {
        Color particleColor = new Color(100, 0, 180);
        float size = 3f + random.nextFloat() * 5f;
        float velocityX = -2f + random.nextFloat() * 4f;
        float velocityY = -2f + random.nextFloat() * 4f;
        int lifetime = (int)(20f + random.nextFloat() * 30f);

        float particleX = x + random.nextFloat() * 15;
        float particleY = y + random.nextFloat() * 10 - 5;
        particles.add(new AuraParticle(
                particleX, particleY,
                velocityX, velocityY,
                size, lifetime,
                particleColor
        ));
    }
}