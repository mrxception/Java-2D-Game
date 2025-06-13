package effects;

import java.awt.*;
import java.awt.geom.Path2D;
import java.util.Random;
import main.Game;

public class Stars {
    private Star[] stars;
    private ShiningBurstStar[] burstStars;
    private Random random;
    private final int NUM_STARS = 160;
    private final int NUM_BURST_STARS = 10;

    private final Color[] STAR_COLORS = {
            Color.WHITE,                     
            new Color(255, 255, 220),        
            new Color(220, 220, 255),        
            new Color(255, 220, 220),        
            new Color(255, 240, 180)         
    };

    public Stars() {
        stars = new Star[NUM_STARS];
        burstStars = new ShiningBurstStar[NUM_BURST_STARS];
        random = new Random();
        initStars();
        initBurstStars();
    }

    private void initStars() {
        for (int i = 0; i < stars.length; i++) {
            int x = random.nextInt(Game.GAME_WIDTH);
            int y = random.nextInt(Game.GAME_HEIGHT);
            float size = 0.5f + random.nextFloat() * 2.0f; 
            float brightness = 0.3f + random.nextFloat() * 0.7f; 
            float twinkleSpeed = 0.0005f + random.nextFloat() * 0.002f; 
            stars[i] = new Star(x, y, size, brightness, twinkleSpeed);
        }
    }

    private void initBurstStars() {
        for (int i = 0; i < burstStars.length; i++) {
            createNewBurstStar(i);
        }
    }

    private void createNewBurstStar(int index) {
        int x = random.nextInt(Game.GAME_WIDTH);
        int y = random.nextInt(Game.GAME_HEIGHT);
        float maxSize = 3.0f + random.nextFloat() * 3.0f;
        float duration = 3000 + random.nextInt(5000); 
        float delay = random.nextInt(8000); 
        burstStars[index] = new ShiningBurstStar(x, y, maxSize, duration, delay);
    }




    public void update() {
        
        for (Star star : stars) {
            star.update();
        }

        
        for (int i = 0; i < burstStars.length; i++) {
            burstStars[i].update();

            
            if (burstStars[i].isComplete()) {
                createNewBurstStar(i);
            }
        }


    }

    public void draw(Graphics g, int xLvlOffset) {
        Graphics2D g2d = (Graphics2D) g;

        
        Composite originalComposite = g2d.getComposite();
        Color originalColor = g2d.getColor();
        Stroke originalStroke = g2d.getStroke();

        
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        


        
        for (Star star : stars) {
            
            float alpha = star.getCurrentBrightness();
            if (alpha >= 0) {
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            }
            else{
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
            }

            g2d.setColor(STAR_COLORS[star.colorIndex]);

            
            float size = star.size;
            if (size < 1.0f) {
                
                g2d.fillRect(star.x, star.y, 1, 1);
            } else if (size < 1.5f) {
                
                g2d.fillOval(star.x, star.y, 2, 2);
            } else {
                
                int starSize = (int) size;

                
                if (star.getCurrentBrightness() > 0.5f) {
                    float glowStrength = (star.getCurrentBrightness() - 0.5f) * 2.0f; 
                    int glowSize = Math.max(1, (int)(starSize * 3.0f));

                    RadialGradientPaint glowGradient = new RadialGradientPaint(
                            star.x + starSize/2, star.y + starSize/2, glowSize/2.0f,
                            new float[] {0.0f, 0.5f, 1.0f},
                            new Color[] {
                                    new Color(255, 255, 255, (int)(80 * glowStrength)),
                                    new Color(255, 255, 255, (int)(40 * glowStrength)),
                                    new Color(255, 255, 255, 0)
                            }
                    );

                    g2d.setPaint(glowGradient);
                    g2d.fillOval(star.x + starSize/2 - glowSize/2, star.y + starSize/2 - glowSize/2,
                            glowSize, glowSize);

                    
                    g2d.setColor(STAR_COLORS[star.colorIndex]);
                }

                
                g2d.fillOval(star.x, star.y, starSize, starSize);

                
                if (star.getCurrentBrightness() > 0.7f) {
                    float sparkleStrength = (star.getCurrentBrightness() - 0.7f) * 3.33f; 
                    g2d.setStroke(new BasicStroke(1.0f * sparkleStrength));

                    int sparkleLength = (int)(starSize * 2.0f);
                    int centerX = star.x + starSize/2;
                    int centerY = star.y + starSize/2;

                    g2d.drawLine(centerX - sparkleLength, centerY, centerX + sparkleLength, centerY);
                    g2d.drawLine(centerX, centerY - sparkleLength, centerX, centerY + sparkleLength);

                    
                    if (star.getCurrentBrightness() > 0.85f) {
                        int diagLength = (int)(sparkleLength * 0.7f);
                        g2d.drawLine(centerX - diagLength, centerY - diagLength,
                                centerX + diagLength, centerY + diagLength);
                        g2d.drawLine(centerX - diagLength, centerY + diagLength,
                                centerX + diagLength, centerY - diagLength);
                    }
                }
            }
        }

        
        for (ShiningBurstStar burstStar : burstStars) {
            if (burstStar.isActive()) {
                
                float alpha = burstStar.getCurrentAlpha();
                float size = Math.max(0.1f, burstStar.getCurrentSize());
                float raySize = Math.max(0.1f, burstStar.getRaySize());

                
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

                
                RadialGradientPaint glowGradient = new RadialGradientPaint(
                        burstStar.x, burstStar.y, raySize,
                        new float[] {0.0f, 0.5f, 1.0f},
                        new Color[] {
                                new Color(255, 255, 255, 100),
                                new Color(255, 255, 220, 50),
                                new Color(255, 255, 200, 0)
                        }
                );

                g2d.setPaint(glowGradient);
                g2d.fillOval((int)(burstStar.x - raySize), (int)(burstStar.y - raySize),
                        Math.max(1, (int)(raySize * 2)), Math.max(1, (int)(raySize * 2)));

                
                g2d.setColor(Color.WHITE);
                g2d.setStroke(new BasicStroke(1.5f * alpha));

                int centerX = burstStar.x;
                int centerY = burstStar.y;

                
                for (int i = 0; i < 8; i++) {
                    float angle = (float)(i * Math.PI / 4.0);
                    float rayLength = raySize * (0.8f + 0.4f * (float)Math.sin(burstStar.getPhase() * 5 + i));
                    rayLength = Math.max(0.1f, rayLength);

                    int endX = centerX + (int)(Math.cos(angle) * rayLength);
                    int endY = centerY + (int)(Math.sin(angle) * rayLength);

                    g2d.drawLine(centerX, centerY, endX, endY);
                }

                
                g2d.setColor(Color.WHITE);
                g2d.fillOval((int)(centerX - size/2), (int)(centerY - size/2),
                        Math.max(1, (int)size), Math.max(1, (int)size));
            }
        }



        
        g2d.setComposite(originalComposite);
        g2d.setColor(originalColor);
        g2d.setStroke(originalStroke);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_DEFAULT);
    }

    private class Star {
        int x, y;
        float size;
        float baseBrightness;
        float currentBrightness;
        float twinkleSpeed;
        float twinkleOffset;
        int colorIndex; 

        public Star(int x, int y, float size, float brightness, float twinkleSpeed) {
            this.x = x;
            this.y = y;
            this.size = size;
            this.baseBrightness = brightness;
            this.currentBrightness = brightness;
            this.twinkleSpeed = twinkleSpeed;
            this.twinkleOffset = random.nextFloat() * (float)Math.PI * 2; 
            this.colorIndex = random.nextInt(STAR_COLORS.length);
        }

        public void update() {
            
            
            float time = System.currentTimeMillis() / 1000.0f;
            float variation = (float)Math.sin(time * twinkleSpeed + twinkleOffset) * 0.3f;
            currentBrightness = baseBrightness + variation;

            
            if (currentBrightness < 0.1f) currentBrightness = 0.1f;
            if (currentBrightness > 1.0f) currentBrightness = 1.0f;
        }

        public float getCurrentBrightness() {
            return currentBrightness;
        }
    }

    private class ShiningBurstStar {
        int x, y;
        float maxSize;
        float currentSize;
        float currentAlpha;
        float duration; 
        float delay;    
        long startTime; 

        public ShiningBurstStar(int x, int y, float maxSize, float duration, float delay) {
            this.x = x;
            this.y = y;
            this.maxSize = Math.max(0.1f, maxSize);
            this.duration = duration;
            this.delay = delay;
            this.startTime = System.currentTimeMillis();
            this.currentSize = 0;
            this.currentAlpha = 0;
        }

        public void update() {
            
            if (!isActive()) return;

            
            long currentTime = System.currentTimeMillis();
            float elapsed = (currentTime - startTime - delay);

            
            float lifePhase = elapsed / duration;

            if (lifePhase < 0.3f) {
                
                float growPhase = lifePhase / 0.3f; 
                currentSize = maxSize * smoothStep(growPhase);
                currentAlpha = smoothStep(growPhase);
            }
            else if (lifePhase < 0.7f) {
                
                currentSize = maxSize;
                currentAlpha = 1.0f;
            }
            else if (lifePhase < 1.0f) {
                
                float fadePhase = (lifePhase - 0.7f) / 0.3f; 
                currentSize = maxSize * (1.0f - fadePhase * 0.3f); 
                currentAlpha = 1.0f - smoothStep(fadePhase);
            }
        }

        
        private float smoothStep(float x) {
            
            return x * x * (3 - 2 * x);
        }

        public boolean isActive() {
            return System.currentTimeMillis() - startTime > delay;
        }

        public boolean isComplete() {
            return System.currentTimeMillis() - startTime > delay + duration;
        }

        public float getCurrentSize() {
            return Math.max(0.1f, currentSize);
        }

        public float getCurrentAlpha() {
            return currentAlpha;
        }

        public float getRaySize() {
            return Math.max(0.1f, currentSize * 2.0f);
        }

        public float getPhase() {
            if (!isActive()) return 0;

            long currentTime = System.currentTimeMillis();
            float elapsed = (currentTime - startTime - delay);
            return (elapsed / duration);
        }
    }
}