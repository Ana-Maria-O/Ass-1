package src.SmartWarehouse;

import java.util.*;

public class Bug2Robot {

	private int currentPosition;
	private int goalPosition;
	private Graph graph;
	private List<Integer> pathToGoal;
	private boolean isObstacleEncountered;
	private int startPoint;

	public Bug2Robot(int startPosition, int goalPosition, Graph graph, List<Integer> pathToGoal) {
		this.currentPosition = startPosition;
		this.goalPosition = goalPosition;
		this.graph = graph;
		this.pathToGoal = pathToGoal;
		this.isObstacleEncountered = false;
		this.startPoint = -1;
	}

	public void start() {
		while (currentPosition != goalPosition) {
			if (!isObstacle(currentPosition)) {
				currentPosition = getNextPositionOnDirectPath();
				System.out.println("Robot moved to " + currentPosition);
			} else {
				if (!isObstacleEncountered) {
					startPoint = currentPosition;
					isObstacleEncountered = true;
					System.out.println("Obstacle encountered at " + currentPosition);
				}
				circumnavigateObstacle();
			}
		}
		System.out.println("Goal " + goalPosition + " reached.");
	}

	private void circumnavigateObstacle() {
		do {
			currentPosition = (currentPosition + 1) % graph.getGridSize();
			System.out.println("Circumnavigating, moved to " + currentPosition);

			if (isOnDirectPath(currentPosition)
					&& calculateDistance(currentPosition, goalPosition) < calculateDistance(startPoint, goalPosition)) {
				System.out.println("Closer to goal on direct path at " + currentPosition);
				break;
			}

			if (currentPosition == startPoint) {
				System.out.println("Returned to start point " + startPoint + ". Goal is unreachable.");
				break;
			}
		} while (true);
	}

	private boolean isObstacle(int cell) {
		return graph.isObstacle(cell);
	}

	private int getNextPositionOnDirectPath() {
		return currentPosition + 1;
	}

	private boolean isOnDirectPath(int currentPosition) {
		return currentPosition / graph.getGridWidth() == goalPosition / graph.getGridWidth();
	}

	private int calculateDistance(int cellA, int cellB) {
		// manhattan distance between two cells
		int rowA = cellA / graph.getGridWidth();
		int colA = cellA % graph.getGridWidth();
		int rowB = cellB / graph.getGridWidth();
		int colB = cellB % graph.getGridWidth();
		return Math.abs(rowA - rowB) + Math.abs(colA - colB);
	}
}
