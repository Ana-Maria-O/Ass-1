package src.SmartWarehouse;

import java.util.Comparator;
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

	// A map of all paths in the warehouse. The key is a position, and the value is
	// a list of paths (each path is a list of integers representing positions).
	private Map<Integer, List<List<Integer>>> allPaths;

	// Constructor....
	public Robot(int startPosition, Map<Integer, List<List<Integer>>> allPaths, Graph graph) {
		this.currentPosition = startPosition;
		this.allPaths = allPaths;
		this.graph = graph;
	}

	// Method to select a path to the target. Flag is used to either generate random
	// or optimal path
	public List<Integer> selectPathToTarget(int start, int target, boolean flag) {
		List<List<Integer>> possiblePaths = getAllPaths().getOrDefault(start, List.of());
		if (flag) {
			// Select a random path
			random = new Random();
			return possiblePaths.isEmpty() ? List.of() : possiblePaths.get(random.nextInt(possiblePaths.size()));
		} else {
			// Select the optimal (shortest) path
			return possiblePaths.stream().min(Comparator.comparingInt(List::size)).orElse(List.of());
		}
	}

	// Method to select a NEW path to the target without including the new obstacle.
	// Flag is used to either generate random or optimal path
	public List<Integer> selectPathToTargetObstacle(int start, int target, boolean flag, Graph graph) {
		List<List<Integer>> possiblePaths = getAllPaths().getOrDefault(start, List.of());
		possiblePaths = possiblePaths.stream().filter(path -> path.stream().noneMatch(graph::isObstacle))
				.collect(Collectors.toList());

		if (flag) {
			// Select a random path
			random = new Random();
			return possiblePaths.isEmpty() ? List.of() : possiblePaths.get(random.nextInt(possiblePaths.size()));
		} else {
			// Select the optimal (shortest) path
			return possiblePaths.stream().min(Comparator.comparingInt(List::size)).orElse(List.of());
		}
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
	// OLD Method to select the optimal path to a target position. The optimal path
	// is
	// the shortest path.
	// public List<Integer> selectOptimalPathToTarget(int target) {
	// // Retrieve all possible paths from the current position.
	// List<List<Integer>> possiblePaths =
	// this.allPaths.getOrDefault(this.getCurrentPosition(), List.of());
	//
	// // Choose the shortest path among the possible paths. If no paths are
	// available,
	// // return an empty list.
	// return
	// possiblePaths.stream().min(Comparator.comparingInt(List::size)).orElse(List.of());
	// }

	public Map<Integer, List<List<Integer>>> getAllPaths() {
		return allPaths;
	}

	public void setAllPaths(Map<Integer, List<List<Integer>>> allPaths) {
		this.allPaths = allPaths;
	}

	// Method to move the robot to a new position.
	public void moveTo(int newPosition) {
		System.out.println("Robot moving from " + (this.getCurrentPosition() + 1) + " to " + (newPosition + 1));
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
}
