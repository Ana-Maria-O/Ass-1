package src.SmartWarehouse;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

public class Robot {
	private int currentPosition; // Current position of the robot in the warehouse.
	private int targetPosition; // Target position in the warehouse.
	private Graph graph; // Warehouse map
	private Random random; // Random number to select a random path
	List<Integer> currentSelectedPath;
	int currentPathIndex = 0;
	Set<Integer> dynamicObstacles = new HashSet<>();

	// A map of all paths in the warehouse. The key is a position, and the value is
	// a list of paths (each path is a list of integers representing positions).
	private List<List<Integer>> allPaths;

	// Constructor....
	public Robot(int startPosition, Graph graph) {
		this.currentPosition = startPosition;
		this.graph = graph;
	}

	public void setTarget(int targetPosition, List<List<Integer>> allPaths) {
		this.targetPosition = targetPosition;
		this.allPaths = allPaths;
	}

	public boolean moveToAdjacentNonObstacleCell(Graph graph) {
		Set<Point> adjacentCells = graph.getAdjacentNonObstacleCells(vertexToPoint(currentPosition));
		for (Point adjacentCell : adjacentCells) {
			int adjacentCellId = pointToVertex(adjacentCell);
			if (!graph.isObstacle(adjacentCellId)) {
				setCurrentPosition(adjacentCellId);
				System.out.println("Moved to adjacent cell (" + (adjacentCell.x + 1) + ", " + (adjacentCell.y + 1)
						+ ") with ID: " + (adjacentCellId + 1));

				return true;
			}
		}
		return false;
	}

	// Method to convert a point to a vertex ID
	private int pointToVertex(Point point) {
		return point.y * this.graph.getGridWidth() + point.x; // Calculate vertex ID based on point coordinates
	}

	// Method to convert a vertex ID to a point
	private Point vertexToPoint(int vertex) {
		int x = vertex % this.graph.getGridWidth(); // Calculate x-coordinate from vertex ID
		int y = vertex / this.graph.getGridWidth(); // Calculate y-coordinate from vertex ID
		return new Point(x, y);
	}

	public List<List<Integer>> getAllPaths() {
		return allPaths;
	}

	public List<Integer> getCurrentSelectedPath() {
		return currentSelectedPath;
	}

	public void selectPathToTarget() {
		currentSelectedPath = allPaths.get(currentPosition);
		currentPathIndex = 0;
	}

	public void setAllPaths(List<List<Integer>> allPaths) {
		this.allPaths = allPaths;
	}

	// Method to move the robot to a new position.
	public void moveTo(int newPosition) {
		System.out.println("Robot moving from " + (this.getCurrentPosition()) + " to " + (newPosition));
		// Update the robot's current position.
		setCurrentPosition(newPosition);
	}

	// Method for the robot to follow a given path.
	public void followPath(List<Integer> path) {
		// Loop through each position in the path.
		for (int position : path) {
			// Move the robot to the next position if it's not already there.
			if (position != getCurrentPosition()) {
				moveTo(position);
			}
		}
	}

	public List<Integer> recalculatePath(int target, Graph graph) {
		return graph.dijkstra(currentPosition, target); // Recalculate path using Dijkstra's algorithm
	}

	// Method to update the robot's current position.
	public void setCurrentPosition(int currentPosition) {
		this.currentPosition = currentPosition;
	}

	// Method to get the robot's current position.
	public int getCurrentPosition() {
		return currentPosition;
	}

	public int getTargetPosition() {
		return targetPosition;
	}

	public void setTargetPosition(int targetPosition) {
		this.targetPosition = targetPosition;
	}

	public void takeStepOnPath() {
		if (graph.isObstacle(currentSelectedPath.get(currentPathIndex + 1))) {
			// get move options
			// can move
			// true: --> hug border
			// false error
		}
		if (currentPathIndex < currentSelectedPath.size()) {
			currentPathIndex++;
			moveTo(currentSelectedPath.get(currentPathIndex));
		}
	}

	public boolean pathIsComplete() {
		return currentPathIndex >= currentSelectedPath.size() - 1;
	}
}
