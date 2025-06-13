package utilz;

import main.Game;

import java.util.Random;

public class Constants {

	public static final float GRAVITY = 0.04f * Game.SCALE;
	public static final int ANI_SPEED = 25;



	public static class Dialogue {
		public static final int QUESTION = 0;
		public static final int EXCLAMATION = 1;

		public static final int DIALOGUE_WIDTH = (int) (14 * Game.SCALE);
		public static final int DIALOGUE_HEIGHT = (int) (12 * Game.SCALE);

		public static int GetSpriteAmount(int type) {
			switch (type) {
			case QUESTION, EXCLAMATION:
				return 5;
			}

			return 0;
		}
	}

	public static class Projectiles {
		public static final int SKULL_BALL_DEFAULT_WIDTH = 15;
		public static final int SKULL_BALL_DEFAULT_HEIGHT = 15;

		public static final int SKULL_BALL_WIDTH = (int) (Game.SCALE * SKULL_BALL_DEFAULT_WIDTH);
		public static final int SKULL_BALL_HEIGHT = (int) (Game.SCALE * SKULL_BALL_DEFAULT_HEIGHT);
		public static final float SPEED = 1f * Game.SCALE;
	}

	public static class ObjectConstants {

		public static final int RED_POTION = 0;
		public static final int BLUE_POTION = 1;

		public static final int SPIKE = 4;
		public static final int SKULL_LEFT = 5;
		public static final int SKULL_RIGHT = 6;
		public static final int TORCH = 7;

		public static final int RED_POTION_VALUE = 50;
		public static final int BLUE_POTION_VALUE = 100;

		public static final int POTION_WIDTH_DEFAULT = 12;
		public static final int POTION_HEIGHT_DEFAULT = 16;
		public static final int POTION_WIDTH = (int) (Game.SCALE * POTION_WIDTH_DEFAULT);
		public static final int POTION_HEIGHT = (int) (Game.SCALE * POTION_HEIGHT_DEFAULT);

		public static final int SPIKE_WIDTH_DEFAULT = 32;
		public static final int SPIKE_HEIGHT_DEFAULT = 32;
		public static final int SPIKE_WIDTH = (int) (Game.SCALE * SPIKE_WIDTH_DEFAULT);
		public static final int SPIKE_HEIGHT = (int) (Game.SCALE * SPIKE_HEIGHT_DEFAULT);

		public static final int SKULL_WIDTH_DEFAULT = 40;
		public static final int SKULL_HEIGHT_DEFAULT = 26;
		public static final int SKULL_WIDTH = (int) (SKULL_WIDTH_DEFAULT * Game.SCALE);
		public static final int SKULL_HEIGHT = (int) (SKULL_HEIGHT_DEFAULT * Game.SCALE);

		public static int GetSpriteAmount(int object_type) {
			switch (object_type) {
			case RED_POTION, BLUE_POTION, SKULL_LEFT, SKULL_RIGHT:
				return 7;
			}
			return 1;
		}

		public static int GetTorchOffsetX(int torchType) {
			switch (torchType) {
			case TORCH:
				return (Game.TILES_SIZE / 2) - (GetTorchWidth(torchType) / 2);
			}

			return 0;
		}

		public static int GetTorchOffsetY(int torchType) {

			switch (torchType) {
			case TORCH:
				return -GetTorchHeight(torchType) + Game.TILES_SIZE * 1;
			}
			return 0;

		}

		public static int GetTorchWidth(int torchType) {
			switch (torchType) {
			case TORCH:
				return (int) (32 * Game.SCALE);
			}
			return 0;
		}

		public static int GetTorchHeight(int torchType) {
			switch (torchType) {
			case TORCH:
				return (int) (int) (32 * Game.SCALE);
			}
			return 0;
		}
	}

	public static class EnemyConstants {
		public static final int SLORACK = 0;
		public static final int RADOBAAN = 1;
		public static final int VELGORN = 2;
		public static final int HELLFLAME = 3;

		public static final int IDLE = 0;
		public static final int RUNNING = 1;
		public static final int ATTACK = 2;
		public static final int HIT = 3;
		public static final int DEAD = 4;

		public static final int SLORACK_WIDTH_DEFAULT = 72;
		public static final int SLORACK_HEIGHT_DEFAULT = 32;
		public static final int SLORACK_WIDTH = (int) (SLORACK_WIDTH_DEFAULT * Game.SCALE);
		public static final int SLORACK_HEIGHT = (int) (SLORACK_HEIGHT_DEFAULT * Game.SCALE);
		public static final int SLORACK_DRAWOFFSET_X = (int) (28 * Game.SCALE);
		public static final int SLORACK_DRAWOFFSET_Y = (int) (16 * Game.SCALE);

		public static final int RADOBAAN_WIDTH_DEFAULT = 34;
		public static final int RADOBAAN_HEIGHT_DEFAULT = 30;
		public static final int RADOBAAN_WIDTH = (int) (RADOBAAN_WIDTH_DEFAULT * Game.SCALE);
		public static final int RADOBAAN_HEIGHT = (int) (RADOBAAN_HEIGHT_DEFAULT * Game.SCALE);
		public static final int RADOBAAN_DRAWOFFSET_X = (int) (9 * Game.SCALE);
		public static final int RADOBAAN_DRAWOFFSET_Y = (int) (7 * Game.SCALE);

		public static final int VELGORN_WIDTH_DEFAULT = 34;
		public static final int VELGORN_HEIGHT_DEFAULT = 30;
		public static final int VELGORN_WIDTH = (int) (VELGORN_WIDTH_DEFAULT * Game.SCALE);
		public static final int VELGORN_HEIGHT = (int) (VELGORN_HEIGHT_DEFAULT * Game.SCALE);
		public static final int VELGORN_DRAWOFFSET_X = (int) (8 * Game.SCALE);
		public static final int VELGORN_DRAWOFFSET_Y = (int) (6 * Game.SCALE);

		public static final int HELLFLAME_WIDTH_DEFAULT = 288;
		public static final int HELLFLAME_HEIGHT_DEFAULT = 160;
		public static final int HELLFLAME_WIDTH = (int) (HELLFLAME_WIDTH_DEFAULT * Game.SCALE);
		public static final int HELLFLAME_HEIGHT = (int) (HELLFLAME_HEIGHT_DEFAULT * Game.SCALE);
		public static final int HELLFLAME_DRAWOFFSET_X = (int) (119 * Game.SCALE);
		public static final int HELLFLAME_DRAWOFFSET_Y = (int) (93 * Game.SCALE);

		public static int GetSpriteAmount(int enemy_type, int enemy_state) {
			switch (enemy_state) {

			case IDLE: {
				if (enemy_type == SLORACK)
					return 9;
				else if (enemy_type == RADOBAAN || enemy_type == VELGORN)
					return 8;
				else if (enemy_type == HELLFLAME)
					return 6;
			}
			case RUNNING:
				if (enemy_type == HELLFLAME)
					return 12;
				return 6;
			case ATTACK:
				if (enemy_type == VELGORN)
					return 8;
				else if (enemy_type == HELLFLAME)
					return 15;
				return 7;
			case HIT:
				if (enemy_type == HELLFLAME)
					return 5;
				return 4;
			case DEAD:
				if (enemy_type == HELLFLAME)
					return 16;
				return 5;
			}
			return 0;

		}

		public static int GetMaxHealth(int enemy_type) {
			switch (enemy_type) {
				case SLORACK:
					return 150;
				case VELGORN:
					return 250;
				case RADOBAAN:
					return 400;
				case HELLFLAME:
					return 1000;
				default:
					return 0;
			}
		}


		public static int GetEnemyDmg(int enemy_type) {
			switch (enemy_type) {
			case SLORACK:
				return new Random().nextInt(15,25);
			case VELGORN:
				return new Random().nextInt(30,45);
			case RADOBAAN:
				return new Random().nextInt(40,60);
			case HELLFLAME:
				return new Random().nextInt(60,100);
			default:
				return 0;
			}
		}
	}

	public static class Environment {
		public static final int BIG_CLOUD_WIDTH_DEFAULT = 300;
		public static final int BIG_CLOUD_HEIGHT_DEFAULT = 100;
		public static final int SMALL_CLOUD_WIDTH_DEFAULT = 74;
		public static final int SMALL_CLOUD_HEIGHT_DEFAULT = 24;

		public static final int BIG_CLOUD_WIDTH = (int) (BIG_CLOUD_WIDTH_DEFAULT * Game.SCALE);
		public static final int BIG_CLOUD_HEIGHT = (int) (BIG_CLOUD_HEIGHT_DEFAULT * Game.SCALE);
		public static final int SMALL_CLOUD_WIDTH = (int) (SMALL_CLOUD_WIDTH_DEFAULT * Game.SCALE);
		public static final int SMALL_CLOUD_HEIGHT = (int) (SMALL_CLOUD_HEIGHT_DEFAULT * Game.SCALE);
	}

	public static class UI {
		public static class Buttons {
			public static final int B_WIDTH_DEFAULT = 140;
			public static final int B_HEIGHT_DEFAULT = 56;
			public static final int B_WIDTH = (int) (B_WIDTH_DEFAULT * Game.SCALE);
			public static final int B_HEIGHT = (int) (B_HEIGHT_DEFAULT * Game.SCALE);
		}

		public static class PauseButtons {
			public static final int SOUND_SIZE_DEFAULT = 42;
			public static final int SOUND_SIZE = (int) (SOUND_SIZE_DEFAULT * Game.SCALE);
		}

		public static class URMButtons {
			public static final int URM_DEFAULT_SIZE = 56;
			public static final int URM_SIZE = (int) (URM_DEFAULT_SIZE * Game.SCALE);

		}

		public static class VolumeButtons {
			public static final int VOLUME_DEFAULT_WIDTH = 28;
			public static final int VOLUME_DEFAULT_HEIGHT = 44;
			public static final int SLIDER_DEFAULT_WIDTH = 215;

			public static final int VOLUME_WIDTH = (int) (VOLUME_DEFAULT_WIDTH * Game.SCALE);
			public static final int VOLUME_HEIGHT = (int) (VOLUME_DEFAULT_HEIGHT * Game.SCALE);
			public static final int SLIDER_WIDTH = (int) (SLIDER_DEFAULT_WIDTH * Game.SCALE);
		}

		public static class Background {
			public static final int BACKGROUND_WIDTH = 1536;
			public static final int BACKGROUND_HEIGHT = 1024;
		}
	}

	public static class Directions {
		public static final int LEFT = 0;
		public static final int UP = 1;
		public static final int RIGHT = 2;
		public static final int DOWN = 3;
	}

	public static class PlayerConstants {
		public static final int IDLE = 0;
		public static final int RUNNING = 1;
		public static final int JUMP = 2;
		public static final int FALLING = 3;
		public static final int ATTACK = 4;
		public static final int HIT = 5;
		public static final int DEAD = 6;

		public static final int AURA_DAMAGE_MULTIPLIER = 2;

		public static int GetSpriteAmount(int player_action) {
			switch (player_action) {
			case DEAD:
				return 8;
			case RUNNING:
				return 6;
			case IDLE:
				return 5;
			case HIT:
				return 4;
			case JUMP:
			case ATTACK:
				return 3;
			case FALLING:
			default:
				return 1;
			}
		}
	}

}