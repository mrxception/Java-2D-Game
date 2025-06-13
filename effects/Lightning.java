package effects;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import javax.sound.sampled.*;
import java.io.IOException;

import audio.AudioPlayer;
import gamestates.Gamestate;
import gamestates.Playing;
import main.Game;

public class Lightning {
    private ArrayList<LightningBolt> bolts;
    private Random random;
    private long lastLightningTime;
    private long lightningFrequency;
    private boolean flashActive;
    private int flashDuration;
    private int flashAlpha;
    private Color flashColor;
    private int flashTimer;
    private boolean thunderPlaying;
    private Clip thunderSound;
    private long thunderDelay;
    private Playing playing;

    public Lightning(Playing playing) {
        bolts = new ArrayList<>();
        random = new Random();
        lastLightningTime = System.currentTimeMillis();
        lightningFrequency = 10000;
        flashActive = false;
        flashDuration = 0;
        flashAlpha = 0;
        flashColor = new Color(255, 255, 255, 0);
        flashTimer = 0;
        thunderPlaying = false;
        thunderDelay = 0;
        this.playing = playing;
    }



    public void update() {
        if (playing.isPaused() || Gamestate.state != Gamestate.PLAYING || playing.isLevelCompleted() || playing.isGameOver() || playing.isGameCompleted())
            return;
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastLightningTime > lightningFrequency + random.nextInt(5000)) {
            createLightning();
            lastLightningTime = currentTime;
        }

        Iterator<LightningBolt> it = bolts.iterator();
        while (it.hasNext()) {
            LightningBolt bolt = it.next();
            bolt.update();
            if (bolt.isFinished()) {
                it.remove();
            }
        }

        if (flashActive) {
            flashTimer++;
            if (flashTimer < flashDuration / 3) {
                flashAlpha = Math.min(180, flashAlpha + 20);
            } else {
                flashAlpha = Math.max(0, flashAlpha - 5);
                if (flashAlpha <= 0) {
                    flashActive = false;
                    flashTimer = 0;
                }
            }
            flashColor = new Color(255, 255, 255, flashAlpha);

            if (thunderDelay > 0) {
                thunderDelay--;
                if (thunderDelay <= 0 && !thunderPlaying && Gamestate.state == Gamestate.PLAYING && !playing.isLevelCompleted() && !playing.isGameOver() && !playing.isGameCompleted()) {
                    playing.getGame().getAudioPlayer().playThunder();
                }
            }
        }
    }

    public void draw(Graphics g, int xLvlOffset) {
        if (flashActive && flashAlpha > 0) {
            Graphics2D g2d = (Graphics2D) g;
            Composite originalComposite = g2d.getComposite();
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, flashAlpha / 255.0f));
            g2d.setColor(flashColor);
            g2d.fillRect(0, 0, Game.GAME_WIDTH, Game.GAME_HEIGHT);
            g2d.setComposite(originalComposite);
        }

        ArrayList<LightningBolt> boltsCopy = new ArrayList<>(bolts);

        try{
            for (LightningBolt bolt : boltsCopy) {
                bolt.draw(g, xLvlOffset);
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }

    private void createLightning() {
        int startX = random.nextInt(Game.GAME_WIDTH);
        LightningBolt mainBolt = new LightningBolt(startX, 0, startX + random.nextInt(400) - 200, Game.GAME_HEIGHT, 3, playing);
        bolts.add(mainBolt);

        if (random.nextFloat() < 0.7f) {
            int branchCount = random.nextInt(3) + 1;
            for (int i = 0; i < branchCount; i++) {
                Point2D.Float branchPoint = mainBolt.getRandomPoint();
                if (branchPoint != null) {
                    float endX = branchPoint.x + random.nextInt(200) - 100;
                    float endY = branchPoint.y + random.nextInt(200) + 50;
                    bolts.add(new LightningBolt(branchPoint.x, branchPoint.y, endX, endY, 2, playing));
                }
            }
        }

        flashActive = true;
        flashDuration = 15 + random.nextInt(10);
        flashTimer = 0;
        flashAlpha = 0;

        thunderDelay = random.nextInt(100) + 20;
    }

    public void stormMode() {
        lightningFrequency = 2000 + random.nextInt(3000);
    }

    public void normalMode() {
        lightningFrequency = 10000 + random.nextInt(5000);
    }

    private class LightningBolt {
        private ArrayList<Point2D.Float> points;
        private int life;
        private int maxLife;
        private float startX, startY, endX, endY;
        private float thickness;
        private Color boltColor;
        private int detail;
        private Playing playing;

        public LightningBolt(float startX, float startY, float endX, float endY, float thickness, Playing playing) {
            this.startX = startX;
            this.startY = startY;
            this.endX = endX;
            this.endY = endY;
            this.thickness = thickness;
            this.detail = (int) (Math.sqrt(Math.pow(endX - startX, 2) + Math.pow(endY - startY, 2)) / 15);
            this.maxLife = 5 + random.nextInt(5);
            this.life = maxLife;
            this.playing = playing;
            int blue = 200 + random.nextInt(55);
            boltColor = new Color(200, 200, blue);

            if (Gamestate.state != Gamestate.PLAYING){
                return;
            }

            if (!playing.isPaused() && !playing.isLevelCompleted() && !playing.isGameOver() && !playing.isGameCompleted()) {
                playing.getGame().getAudioPlayer().playThunder();
            }
            generatePoints();
        }

        private void generatePoints() {
            points = new ArrayList<>();
            points.add(new Point2D.Float(startX, startY));

            float offsetX = (random.nextFloat() - 0.5f) * 80;
            float offsetY = (random.nextFloat() - 0.5f) * 40;

            for (int i = 1; i <= detail; i++) {
                float ratio = (float) i / (detail + 1);
                float x = startX + (endX - startX) * ratio;
                float y = startY + (endY - startY) * ratio;

                if (i == detail / 2) {
                    x += offsetX;
                    y += offsetY;
                } else {
                    x += (random.nextFloat() - 0.5f) * 40;
                    y += (random.nextFloat() - 0.5f) * 20;
                }

                points.add(new Point2D.Float(x, y));
            }

            points.add(new Point2D.Float(endX, endY));
        }

        public void update() {
            life--;
        }

        public void draw(Graphics g, int xLvlOffset) {
            if (life <= 0) return;

            Graphics2D g2d = (Graphics2D) g;
            Stroke originalStroke = g2d.getStroke();
            Color originalColor = g2d.getColor();

            float alpha = (float) life / maxLife;
            g2d.setColor(new Color(
                    boltColor.getRed(),
                    boltColor.getGreen(),
                    boltColor.getBlue(),
                    (int) (255 * alpha)
            ));

            g2d.setStroke(new BasicStroke(thickness * 3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            Point2D.Float prev = null;
            for (Point2D.Float p : points) {
                if (prev != null) {
                    g2d.drawLine(
                            (int) (prev.x - xLvlOffset * 0.2f), (int) prev.y,
                            (int) (p.x - xLvlOffset * 0.2f), (int) p.y
                    );
                }
                prev = p;
            }

            g2d.setColor(Color.WHITE);
            g2d.setStroke(new BasicStroke(thickness, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            prev = null;
            for (Point2D.Float p : points) {
                if (prev != null) {
                    g2d.drawLine(
                            (int) (prev.x - xLvlOffset * 0.2f), (int) prev.y,
                            (int) (p.x - xLvlOffset * 0.2f), (int) p.y
                    );
                }
                prev = p;
            }

            g2d.setStroke(originalStroke);
            g2d.setColor(originalColor);
        }

        public boolean isFinished() {
            return life <= 0;
        }

        public Point2D.Float getRandomPoint() {
            if (points.size() < 3) return null;
            int index = 1 + random.nextInt(points.size() - 2);
            return points.get(index);
        }
    }
}