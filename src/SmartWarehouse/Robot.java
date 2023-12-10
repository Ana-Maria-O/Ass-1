package src.SmartWarehouse;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import src.SmartWarehouse.Robot.Point;

public class Robot {
	// Helper class
	public class Point {
		public int x, y;

		public Point(int x, int y) {
			this.x = x;
			this.y = y;
		}
	}

	// Current position of the robot in the warehouse.
	private int currentPosition;
	private int targetPosition;
//	private final Graph graph;
	
	// A map of all paths in the warehouse. The key is a position, and the value is
	// a list of paths (each path is a list of integers representing positions).
	private Map<Integer, List<List<Integer>>> allPaths;

	// Constructor....
	public Robot(int startPosition, Map<Integer, List<List<Integer>>> allPaths) {
		this.currentPosition = startPosition;
		this.allPaths = allPaths;
	}

	// Method to select the optimal path to a target position. The optimal path is
	// the shortest path.
	public List<Integer> selectOptimalPathToTarget(int target) {
		// Retrieve all possible paths from the current position.
		List<List<Integer>> possiblePaths = this.allPaths.getOrDefault(this.getCurrentPosition(), List.of());

		// Choose the shortest path among the possible paths. If no paths are available,
		// return an empty list.
		return possiblePaths.stream().min(Comparator.comparingInt(List::size)).orElse(List.of());
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
			if (position != currentPosition) {
				moveTo(position);
			}
		}
	}

	// Method to generate a line to the target point using Bresenham's line
	// algorithm
//	public List<Point> lineToTarget(Point from, Point to) {
//		// Initialize start and end points, and calculate deltas
//		int x0 = from.x, y0 = from.y, x1 = to.x, y1 = to.y;
//		int dx = Math.abs(x1 - x0), dy = -Math.abs(y1 - y0);
//		// Determine the direction of the line
//		int sx = x0 < x1 ? 1 : -1, sy = y0 < y1 ? 1 : -1;
//		int err = dx + dy;
//
//		List<Point> line = new ArrayList<>();
//		// Generate points for the line
//		while (true) {
//			line.add(new Point(x0, y0)); // Add the current point to the line
//			// Check if the end point is reached
//			if (x0 == x1 && y0 == y1) {
//				break; // Exit the loop if the end/target point is reached
//			}
//			int e2 = 2 * err;
//			// Adjust the points and error based on the direction of the line
//			if (e2 >= dy) {
//				err += dy;
//				x0 += sx;
//			}
//			if (e2 <= dx) {
//				err += dx;
//				y0 += sy;
//			}
//
//			// Check for potential infinite loop and throw exception if so
//			if (line.size() > this.graph.getGridWidth() * this.graph.getGridHeight()) {
//				throw new RuntimeException("lineToTarget generated too many points...something is wrong");
//			}
//		}
//		return line;
//	}

	// Method to update the robot's current position.
	public void setCurrentPosition(int currentPosition) {
		this.currentPosition = currentPosition;
	}

	// Method to get the robot's current position.
	public int getCurrentPosition() {
		return this.currentPosition;
	}
}
