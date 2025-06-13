package levels;

import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import entities.HellFlame;
import entities.Slorack;
import entities.Radobaan;
import entities.Velgorn;
import main.Game;
import objects.BackgroundTorch;
import objects.Skull;
import objects.Witchgrass;
import objects.Potion;
import objects.Spike;

import static utilz.Constants.EnemyConstants.*;
import static utilz.Constants.ObjectConstants.*;

public class Level {

	private BufferedImage img;
	private int[][] lvlData;

	private ArrayList<Slorack> sloracks = new ArrayList<>();
	private ArrayList<Radobaan> radobaans = new ArrayList<>();
	private ArrayList<HellFlame> hellflame = new ArrayList<>();
	private ArrayList<Velgorn> velgorns = new ArrayList<>();
	private ArrayList<Potion> potions = new ArrayList<>();
	private ArrayList<Spike> spikes = new ArrayList<>();
	private ArrayList<Skull> skulls = new ArrayList<>();
	private ArrayList<BackgroundTorch> torch = new ArrayList<>();
	private ArrayList<Witchgrass> grass = new ArrayList<>();

	private int lvlTilesWide;
	private int maxTilesOffset;
	private int maxLvlOffsetX;
	private Point playerSpawn;

	public Level(BufferedImage img) {
		this.img = img;
		lvlData = new int[img.getHeight()][img.getWidth()];
		loadLevel();
		calcLvlOffsets();
	}

	private void loadLevel() {

		for (int y = 0; y < img.getHeight(); y++)
			for (int x = 0; x < img.getWidth(); x++) {
				Color c = new Color(img.getRGB(x, y));
				int red = c.getRed();
				int green = c.getGreen();
				int blue = c.getBlue();

				loadLevelData(red, x, y);
				loadEntities(green, x, y);
				loadObjects(blue, x, y);
			}
	}

	private void loadLevelData(int redValue, int x, int y) {
		if (redValue >= 50)
			lvlData[y][x] = 0;
		else
			lvlData[y][x] = redValue;
		switch (redValue) {
		case 0, 1, 2, 3, 30, 31, 36, 37, 38, 39 ->
		grass.add(new Witchgrass((int) (x * Game.TILES_SIZE), (int) (y * Game.TILES_SIZE) - Game.TILES_SIZE, getRndGrassType(x)));
		}
	}

	private int getRndGrassType(int xPos) {
		return xPos % 2;
	}

	private void loadEntities(int greenValue, int x, int y) {
		switch (greenValue) {
		case SLORACK -> sloracks.add(new Slorack(x * Game.TILES_SIZE, y * Game.TILES_SIZE));
		case RADOBAAN -> radobaans.add(new Radobaan(x * Game.TILES_SIZE, y * Game.TILES_SIZE));
		case VELGORN -> velgorns.add(new Velgorn(x * Game.TILES_SIZE, y * Game.TILES_SIZE));
		case HELLFLAME -> hellflame.add(new HellFlame(x * Game.TILES_SIZE, y * Game.TILES_SIZE));
		case 100 -> playerSpawn = new Point(x * Game.TILES_SIZE, y * Game.TILES_SIZE);
		}
	}

	private void loadObjects(int blueValue, int x, int y) {
		switch (blueValue) {
		case RED_POTION, BLUE_POTION -> potions.add(new Potion(x * Game.TILES_SIZE, y * Game.TILES_SIZE, blueValue));
		case SPIKE -> spikes.add(new Spike(x * Game.TILES_SIZE, y * Game.TILES_SIZE, SPIKE));
		case SKULL_LEFT, SKULL_RIGHT -> skulls.add(new Skull(x * Game.TILES_SIZE, y * Game.TILES_SIZE, blueValue));
		case TORCH  -> torch.add(new BackgroundTorch(x * Game.TILES_SIZE, y * Game.TILES_SIZE, blueValue));
		}
	}

	private void calcLvlOffsets() {
		lvlTilesWide = img.getWidth();
		maxTilesOffset = lvlTilesWide - Game.TILES_IN_WIDTH;
		maxLvlOffsetX = Game.TILES_SIZE * maxTilesOffset;
	}

	public int getSpriteIndex(int x, int y) {
		return lvlData[y][x];
	}

	public int[][] getLevelData() {
		return lvlData;
	}

	public int getLvlOffset() {
		return maxLvlOffsetX;
	}

	public Point getPlayerSpawn() {
		return playerSpawn;
	}

	public ArrayList<Slorack> getSloracks() {
		return sloracks;
	}

	public ArrayList<HellFlame> getHellFlame() {
		return hellflame;
	}

	public ArrayList<Velgorn> getVelgorns() {
		return velgorns;
	}

	public ArrayList<Potion> getPotions() {
		return potions;
	}

	public ArrayList<Spike> getSpikes() {
		return spikes;
	}

	public ArrayList<Skull> getCannons() {
		return skulls;
	}

	public ArrayList<Radobaan> getRadobaans() {
		return radobaans;
	}

	public ArrayList<BackgroundTorch> getTorch() {
		return torch;
	}

	public ArrayList<Witchgrass> getGrass() {
		return grass;
	}

}
