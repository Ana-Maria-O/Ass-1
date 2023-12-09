package src.SmartWarehouse;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Robot {
	public class Point {
		public int x, y;

		public Point(int x, int y) {
			this.x = x;
			this.y = y;
		}
	}

	static boolean flag = false; // Still testing with the flag....Bug2 thingy
	private int currentPosition; // Stores the current position of the robot
	private List<Integer> globalPath; // Stores the global path for the robot as a list of integers
	private Map<Integer, List<Integer>> localPaths; // Stores local paths with their corresponding starting points
	private Graph graph; // An instance of Graph class used for navigating
	private int target; // The target position the robot aims to reach

	public Robot(int startPosition, List<Integer> globalPath, Map<Integer, List<Integer>> localPaths, Graph graph,
			int target) {
		this.currentPosition = startPosition;
		this.globalPath = globalPath;
		this.localPaths = localPaths;
		this.graph = graph;
		this.target = target;
	}

	// Method to find adjacent cells for a given line
	public void findAdjacentCellsForLine(List<Point> line) {
		for (int i = 0; i < line.size(); i++) { // Loop through each point in the line
			Point point = line.get(i); // Get the current point from the line
			Set<Point> adjacentCells = getAdjacentNonObstacleCells(point); // Get non-obstacle adjacent cells

			// Print the non-obstacle adjacent cells for the current point
			System.out.println("Non-obstacle adjacent cells for point (" + point.x + ", " + point.y + "):");
			for (Point adjacentPoint : adjacentCells) { // Loop through each adjacent cell
				int cellId = pointToVertex(adjacentPoint); // Convert point to vertex ID
				System.out.println("Cell (" + adjacentPoint.x + ", " + adjacentPoint.y + ") with ID: " + (cellId + 1));
			}
		}
	}

	// Method to get adjacent non-obstacle cells for a given point
	private Set<Point> getAdjacentNonObstacleCells(Point point) {
		Set<Point> neighbors = new HashSet<>();
		// Check and add each adjacent point if it's not an obstacle
		addIfNonObstacle(neighbors, new Point(point.x - 1, point.y)); // Check left
		addIfNonObstacle(neighbors, new Point(point.x + 1, point.y)); // Check right
		addIfNonObstacle(neighbors, new Point(point.x, point.y - 1)); // Check up
		addIfNonObstacle(neighbors, new Point(point.x, point.y + 1)); // Check down
		return neighbors;
	}

	// Method to add a point to the set of neighbors if it is not an obstacle
	private void addIfNonObstacle(Set<Point> neighbors, Point point) {
		// Check if the point is within the grid boundaries
		if (point.x >= 0 && point.x < this.graph.getGridWidth() && point.y >= 0
				&& point.y < this.graph.getGridHeight()) {
			int cellId = pointToVertex(point);
			// Add the point to neighbors if it is not an obstacle
			if (!graph.isObstacle(cellId)) {
				neighbors.add(point);
			}
		}
	}

	// Method to get adjacent cells for a given point
	private Set<Point> getAdjacentCells(Point point) {
		Set<Point> neighbors = new HashSet<>();
		// Check and add each adjacent point
		if (point.x > 0)
			neighbors.add(new Point(point.x - 1, point.y)); // Check left
		if (point.x < this.graph.getGridWidth() - 1)
			neighbors.add(new Point(point.x + 1, point.y)); // Check right
		if (point.y > 0)
			neighbors.add(new Point(point.x, point.y - 1)); // Check up
		if (point.y < this.graph.getGridHeight() - 1)
			neighbors.add(new Point(point.x, point.y + 1)); // Check down
		return neighbors;
	}

	// Method to generate a line to the target point using Bresenham's line algorithm
	public List<Point> lineToTarget(Point from, Point to) {
		// Initialize start and end points, and calculate deltas
		int x0 = from.x, y0 = from.y, x1 = to.x, y1 = to.y;
		int dx = Math.abs(x1 - x0), dy = -Math.abs(y1 - y0);
		// Determine the direction of the line
		int sx = x0 < x1 ? 1 : -1, sy = y0 < y1 ? 1 : -1;
		int err = dx + dy;

		List<Point> line = new ArrayList<>();
		// Generate points for the line
		while (true) {
			line.add(new Point(x0, y0)); // Add the current point to the line
			// Check if the end point is reached
			if (x0 == x1 && y0 == y1) {
				break; // Exit the loop if the end/target point is reached
			}
			int e2 = 2 * err;
			// Adjust the points and error based on the direction of the line
			if (e2 >= dy) {
				err += dy;
				x0 += sx;
			}
			if (e2 <= dx) {
				err += dx;
				y0 += sy;
			}

			// Check for potential infinite loop and throw exception if so
			if (line.size() > this.graph.getGridWidth() * this.graph.getGridHeight()) {
				throw new RuntimeException("lineToTarget generated too many points...something is wrong");
			}
		}
		return line;
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

	// Recalculate the local path for the robot
	public void recalculateLocalPath() {
		// Find the shortest path from the current position to the target
		List<Integer> newLocalPath = this.graph.dijkstra(this.currentPosition, this.target);
		// Update the local paths map with the new path
		this.localPaths.put(this.currentPosition, newLocalPath);
	}

	// Method to navigate around an obstacle
	private void circumnavigateObstacle() {
		System.out.println("Circumnavigating obstacle...");

		// Convert the current position and target to points
		Point currentPoint = vertexToPoint(currentPosition);
		Point targetPoint = vertexToPoint(target);
		// Generate a line to the target
		List<Point> mLine = lineToTarget(currentPoint, targetPoint);

		boolean isBackOnLine = false; // Flag to check if the robot is back on the intended line
		// Loop until the robot is back on the line
		while (!isBackOnLine) {
			// Get non-obstacle adjacent cells for the current position
			Set<Point> adjacentCells = getAdjacentNonObstacleCells(currentPoint);
			// Choose the best adjacent point to move towards
			Point nextPoint = chooseBestAdjacentPoint(adjacentCells, mLine);

			// Check if no viable path is found
			if (nextPoint == null) {
				System.out.println("No viable path found around the obstacle.");
				return;
			}

			// Move to the next point and update the current position
			moveTo(pointToVertex(nextPoint));
			currentPoint = nextPoint;

			// Check if the robot is back on the intended line and not on an obstacle
			if (mLine.contains(currentPoint) && !graph.isObstacle(pointToVertex(currentPoint))) {
				isBackOnLine = true;
			}
		}
	}

	// Method to choose the best point from a set of adjacent points based on its proximity to a given line.
	private Point chooseBestAdjacentPoint(Set<Point> adjacentCells, List<Point> mLine) {
	    Point bestPoint = null;
	    double minDistanceToLine = Double.MAX_VALUE;

	    // Iterate through each point in the set of adjacent cells.
	    for (Point cell : adjacentCells) {
	        // Calculate the distance of the current point from the mLine.
	        double distance = distanceToLine(cell, mLine);
	        // Check if the current point's distance to the line is less than the minimum distance found so far.
	        if (distance < minDistanceToLine) {
	            // Update the minimum distance to the current point's distance.
	            minDistanceToLine = distance;
	            // Update the best point to the current point.
	            bestPoint = cell;
	        }
	    }
	    return bestPoint;
	}


	public void moveTo(int newPosition) {
	    // Check if the new position is valid (within bounds) and not an obstacle.
	    if (isValidPosition(newPosition) && !graph.isObstacle(newPosition)) {
	        // If valid, show the movement. 
	        System.out.println("Moving from " + (currentPosition + 1) + " to " + (newPosition + 1));
	        currentPosition = newPosition; // Update the current position.
	    } else {
	        // If not valid, show that the move cannot be made.
	        System.out.println(
	                "Cannot move to position: " + (newPosition + 1) + ". It's either an obstacle or out of bounds.");
	    }
	}


	private boolean isValidPosition(int position) {
	    // Convert the position to x and y coordinates based on grid dimensions.
	    int x = position % this.graph.getGridHeight(), y = position / this.graph.getGridWidth();
	    // Check if the x and y coordinates are within the bounds of the grid.
	    return x >= 0 && x < this.graph.getGridWidth() && y >= 0 && y < this.graph.getGridHeight();
	}


	private double distanceToLine(Point point, List<Point> line) {
	    double minDistance = Double.MAX_VALUE;
	    // Iterate through each point in the line.
	    for (Point linePoint : line) {
	        // Calculate the Euclidean distance from the point to the current line point.
	        double distance = Math.sqrt(Math.pow(linePoint.x - point.x, 2) + Math.pow(linePoint.y - point.y, 2));
	        // Update minDistance if the current distance is smaller.
	        if (distance < minDistance) {
	            minDistance = distance;
	        }
	    }
	    return minDistance;
	}


	public void moveToNext() {
	        recalculateLocalPath();

	    // Load the local path for the current position.
	    List<Integer> currentLocalPath = this.localPaths.get(this.currentPosition);

	    // Check if there is a valid local path.
	    if (currentLocalPath != null && !currentLocalPath.isEmpty()) {
	        // Iterate through each cell in the local path.
	        for (int i = 0; i < currentLocalPath.size(); i++) {
	            int nextPosition = currentLocalPath.get(i);

	            // Check for obstacles at the next cell.
	            if (this.graph.isObstacle(nextPosition)) {
	                System.out.println("Obstacle detected at position: " + (nextPosition + 1) + " now will navigate!");
	                circumnavigateObstacle();
	                return;
	            }

	            // Move to the next cell if it is different from the current position.
	            if (nextPosition != this.currentPosition) {
	                System.out.println("Robot moving from " + (this.currentPosition + 1) + " to " + (nextPosition + 1));
	                this.currentPosition = nextPosition;
	                break;
	            }
	        }
	    } else {
	        System.out.println("No valid path available from current position...rip");
	    }
	}

	public Map<Integer, List<Integer>> getLocalPaths() {
	    Map<Integer, List<Integer>> sortedLocalPaths = new LinkedHashMap<>();
	    // Iterate through each cell in the global path.
	    for (int cell : this.globalPath) {
	        // Retrieve the local path for the cell.
	        List<Integer> localPath = this.localPaths.get(cell);
	        // Add the local path to the sorted map if it exists.
	        if (localPath != null) {
	            sortedLocalPaths.put(cell, new ArrayList<>(localPath));
	        }
	    }
	    return sortedLocalPaths;
	}

	public int getCurrentPosition() {
		return this.currentPosition;
	}

	public void setCurrentPosition(int currentPosition) {
		this.currentPosition = currentPosition;
	}

	public List<Integer> getGlobalPath() {
		return this.globalPath;
	}
}
