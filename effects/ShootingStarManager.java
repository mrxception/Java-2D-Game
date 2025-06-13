package effects;

import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Random;

import main.Game;

public class ShootingStarManager {
    private ArrayList<ShootingStar> stars;
    private Random random;
    private long lastSpawnTime;
    private int minSpawnDelay;    
    private int maxSpawnDelay;    
    private int maxActiveStars;   

    public ShootingStarManager(int starCount) {
        stars = new ArrayList<>();
        random = new Random();

        
        for (int i = 0; i < starCount; i++) {
            stars.add(new ShootingStar());
        }

        
        minSpawnDelay = 3000;     
        maxSpawnDelay = 12000;    
        maxActiveStars = 3;       
        lastSpawnTime = System.currentTimeMillis();
    }

    public void update() {
        
        for (ShootingStar star : stars) {
            if (star.isActive()) {
                star.update();
            }
        }

        
        long currentTime = System.currentTimeMillis();
        long timeElapsed = currentTime - lastSpawnTime;
        int spawnDelay = random.nextInt(maxSpawnDelay - minSpawnDelay) + minSpawnDelay;

        if (timeElapsed > spawnDelay) {
            
            int activeCount = 0;
            for (ShootingStar star : stars) {
                if (star.isActive()) {
                    activeCount++;
                }
            }

            
            if (activeCount < maxActiveStars) {
                
                for (ShootingStar star : stars) {
                    if (!star.isActive()) {
                        star.reset();
                        star.activate();
                        break;
                    }
                }

                lastSpawnTime = currentTime;
            }
        }
    }

    public void draw(Graphics g, int xLvlOffset) {
        
        for (ShootingStar star : stars) {
            if (star.isActive()) {
                star.draw(g, xLvlOffset);
            }
        }
    }
}