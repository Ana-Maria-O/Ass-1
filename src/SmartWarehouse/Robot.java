package src.SmartWarehouse;

import java.util.Comparator;
import java.util.HashMap;
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
	String direction = "right"; // absolute direction on the plane: up, down, left, right
	boolean bug2IsActive = false;
	String bug2SearchDirection;

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
	public void moveTo(int newPosition, String stepDir) {
		if (stepDir.equals("left")) {
			turnLeft();
			System.out.println("Robot turning left");
		}
		else if (stepDir.equals("right")) {
			turnRight();
			System.out.println("Robot turning right");
		}
		else if (!stepDir.equals("forward"))
			throw new Error("Invalid step direction: " + stepDir);
			
		System.out.println("Robot moving forward from " + (this.getCurrentPosition()) + " to " + (newPosition));
		// Update the robot's current position.
		setCurrentPosition(newPosition);
	}

	// moves forward given the current absolute plane direction
	public void moveForward() {
		Point point = vertexToPoint(currentPosition);
		if (direction.equals("up"))
			point.y--;
		else if (direction.equals("down"))
			point.y++;
		else if (direction.equals("left"))
			point.x--;
		else // right
			point.x++;
		
		int newVertexNumPosition = pointToVertex(point);
		moveTo(newVertexNumPosition, "forward");
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

	private HashMap<String, Boolean> translateMoveOptions(boolean forward, boolean left, boolean right) {
		HashMap<String, Boolean> moveOptions = new HashMap<>();
		moveOptions.put("forward", forward);
		moveOptions.put("left", left);
		moveOptions.put("right", right);
		return moveOptions;
	}

	// Returns a map of the robot's move options. Keys are "forward", "left", and
	// "right".
	// values are booleans representing whether the robot can move in that
	// direction.
	public HashMap<String, Boolean> getMoveOptions() {
		Point point = vertexToPoint(currentPosition);
		HashMap<String, Boolean> graphMoveOptions = graph.getMoveOptions(point);
		HashMap<String, Boolean> moveOptions;
		boolean up = graphMoveOptions.get("up");
		boolean down = graphMoveOptions.get("down");
		boolean left = graphMoveOptions.get("left");
		boolean right = graphMoveOptions.get("right");
		if (direction.equals("up"))
			moveOptions = translateMoveOptions(up, left, right);
		else if (direction.equals("down"))
			moveOptions = translateMoveOptions(down, right, left);
		else if (direction.equals("left"))
			moveOptions = translateMoveOptions(left, down, up);
		else // right
			moveOptions = translateMoveOptions(right, up, down);
		
		moveOptions.put("backward", false);
		return moveOptions;
	}

	public void turnLeft() {
		direction = shiftLeft(direction);
	}

	public void turnRight() {
		direction = shiftRight(direction);
	}

	public String shiftLeft(String direction) {
		if (direction.equals("up"))
			return "left";
		else if (direction.equals("left"))
			return "down";
		else if (direction.equals("down"))
			return "right";
		else // right
			return "up";
	}

	public String shiftRight(String direction) {
		if (direction.equals("up"))
			return "right";
		else if (direction.equals("right"))
			return "down";
		else if (direction.equals("down"))
			return "left";
		else // left
			return "up";
	}

	private int stringDirToDegree(String direction) {
		if (direction.equals("up"))
			return 90;
		else if (direction.equals("left"))
			return 180;
		else if (direction.equals("down"))
			return 270;
		else // right
			return 0;
	}

	// returns the relative direction of some vertex
	// main use is the find the direction of the next step on the path
	public String findStepDir(Integer nextVertexNum) {
		Point point = vertexToPoint(nextVertexNum);
		Point robotPos = vertexToPoint(currentPosition);
		// plane direction is the fixed direction of the plane
		String planeDirection;
		if (point.x < robotPos.x)
			planeDirection = "left";
		else if (point.x > robotPos.x)
			planeDirection = "right";
		else if (point.y < robotPos.y)
			planeDirection = "up";
		else // point.y > robotPos.y
			planeDirection = "down";

		int robotDirdegree = stringDirToDegree(direction);
		int planeDirDegree = stringDirToDegree(planeDirection);
		int degreeDiff = planeDirDegree - robotDirdegree;
		String stepDir;
		if (degreeDiff == 0)
			stepDir = "forward";
		else if (degreeDiff == 90 || degreeDiff == -270)
			stepDir = "left";
		else if (degreeDiff == 180 || degreeDiff == -180)
			stepDir = "backward";
		else if (degreeDiff == 270 || degreeDiff == -90)
			stepDir = "right";
		else
			throw new Error("Invalid degree");
		
		return stepDir;
	}

	// checks: if the vertex is an obstacle and if the robot can turn into it
	public boolean canMoveTo(Integer nextVertexNum) {
		boolean isObstacle = graph.isObstacle(nextVertexNum) || dynamicObstacles.contains(nextVertexNum);
		String stepDir = findStepDir(nextVertexNum);
		HashMap<String, Boolean> moveOptions = getMoveOptions();
		return moveOptions.get(stepDir) && !isObstacle;
	}

	public Integer getNextStepOnPath() {
		return currentSelectedPath.get(currentPathIndex + 1);
	}

	public void stepTowardsTarget() {
		takeStepOnPath();
	}

	public void takeStepOnPath() {
		Integer nextVertexNum = getNextStepOnPath();
		String stepDir = findStepDir(nextVertexNum);

		System.out.println("Robot:");
		System.out.println("Next step: " + nextVertexNum);
		System.out.println("Direction: " + direction);
		System.out.println("Rel. step direction: " + stepDir);
		System.out.println("Move options: " + getMoveOptions());
		System.out.println("Can move to next step: " + canMoveTo(nextVertexNum));
		
		if (graph.isObstacle(currentSelectedPath.get(currentPathIndex + 1))) {
			// get move options
			// can move
			// true: --> hug border
			// false error
		}

		if (currentPathIndex < currentSelectedPath.size()) {
			currentPathIndex++;
			moveTo(currentSelectedPath.get(currentPathIndex), stepDir);
		}
	}

	public boolean pathIsComplete() {
		return currentPathIndex >= currentSelectedPath.size() - 1;
	}
}
