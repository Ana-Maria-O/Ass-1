package src.SmartWarehouse;

public class Grid {
	private int width;
	private int height;
	private boolean[][] obstacles;

	public Grid(int width, int height) {
		this.width = width;
		this.height = height;
		this.obstacles = new boolean[height][width];
	}

	public void addObstacle(int row, int column) {
		obstacles[row][column] = true;
	}

	public boolean isFree(int row, int column) {
		if (row >= 0 && row < height && column >= 0 && column < width) {
			return !obstacles[row][column];
		}
		return false;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

}
