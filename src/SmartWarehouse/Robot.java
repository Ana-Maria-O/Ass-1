package src.SmartWarehouse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import src.SmartWarehouse.robotcomponents.RobotArm;

public class Robot {
	private int currentPosition; // Current position of the robot in the warehouse.
	private int targetPosition; // Target position in the warehouse.
	private Graph graph; // Warehouse map
	private int batteryLevel; // Battery level
	List<Integer> currentSelectedPath = new ArrayList<Integer>();
	int currentPathIndex = 0;
	String direction = "right"; // absolute direction on the plane: up, down, left, right
	boolean bug2IsActive = false;
	Robot otherRobotInBug2;
	List<Integer> remainingPathToFindUsingBug2; // the path we should get onto again using bug2
	private boolean stayStoppedToAvoidCollisionBug2;
	String bug2SearchDirection;
	String name = "R1";
	int index;

	// Instantiate an arm for the robot
	RobotArm arm = new RobotArm(new Packet[]{});

	// A map of all paths in the warehouse. The key is a position, and the value is
	// a list of paths (each path is a list of integers representing positions).
	private List<List<Integer>> allPaths;

	// Constructor....
	public Robot(int startPosition, Graph graph) {
		this.currentPosition = startPosition;
		this.graph = graph;
		graph.dynamicObstacles.put(startPosition, this);
		// Set the Battery level to 100%
		this.batteryLevel = 100;
	}

	public Robot(int startPosition, Graph graph, String direction, String name) {
		this(startPosition, graph);
		this.direction = direction;
		this.name = name;
	}

	public void setTarget(int targetPosition, List<List<Integer>> allPaths) {
		this.targetPosition = targetPosition;
		this.allPaths = allPaths;
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

	// Set the current selected path of the robot
	public void setCurrentSelectedPath(List<Integer> path) {
		// Check if the robot has enough battery for the new path. If not, it tells
		// the WMS it needs to go to a charging station and changes its current selected
		// path to a path to the nearest charging station
		if (path.size() > batteryLevel) {
			currentSelectedPath = WMS.abortToCharge(index);
			// Change the target of the robot
			setTargetPosition(currentSelectedPath.get(currentSelectedPath.size() - 1));
		}
		
		// If the robot has enough battery for the new path, then select it
		else {
			currentSelectedPath = path;
		}
	}

	// Function used to set the selected path back to an empty list
	// Used when a robot has finished following a path
	public void clearCurrentSelectedPath() {
		currentSelectedPath = new ArrayList<Integer>();
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
			System.out.println("Direction: " + direction);
		} else if (stepDir.equals("right")) {
			turnRight();
			System.out.println("Robot turning right");
			System.out.println("Direction: " + direction);
		} else if (!stepDir.equals("forward"))
			throw new Error("Invalid step direction: " + stepDir);

		System.out.println("Robot moving forward from " + (this.getCurrentPosition()) + " to " + (newPosition));
		// Update the robot's current position.
		graph.dynamicObstacles.remove(currentPosition, this);
		setCurrentPosition(newPosition);
		graph.dynamicObstacles.put(currentPosition, this);
	}

	// moves forward given the current absolute plane direction
	public void moveForward() {
		int newVertexNumPosition = getForwardVertexNum();
		moveTo(newVertexNumPosition, "forward");
	}

	private Integer getForwardVertexNum() {
		Point point = vertexToPoint(currentPosition);
		if (direction.equals("up"))
			point.y--;
		else if (direction.equals("down"))
			point.y++;
		else if (direction.equals("left"))
			point.x--;
		else // right
			point.x++;

		return pointToVertex(point);
	}

	private Integer getLeftVertexNum() {
		Point point = vertexToPoint(currentPosition);
		if (direction.equals("up"))
			point.x--;
		else if (direction.equals("down"))
			point.x++;
		else if (direction.equals("left"))
			point.y++;
		else // right
			point.y--;

		return pointToVertex(point);
	}

	private Integer getRightVertexNum() {
		Point point = vertexToPoint(currentPosition);
		if (direction.equals("up"))
			point.x++;
		else if (direction.equals("down"))
			point.x--;
		else if (direction.equals("left"))
			point.y--;
		else // right
			point.y++;

		return pointToVertex(point);
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
		boolean isObstacle = graph.isObstacle(nextVertexNum) || graph.dynamicObstacles.containsKey(nextVertexNum);
		String stepDir = findStepDir(nextVertexNum);
		HashMap<String, Boolean> moveOptions = getMoveOptions();
		return moveOptions.get(stepDir) && !isObstacle;
	}

	public Integer getNextStepOnPath() {
		return currentSelectedPath.get(currentPathIndex + 1);
	}

	public void stepTowardsTarget() {
		if (!stayStoppedToAvoidCollisionBug2) {
			if (bug2IsActive) {
				System.out.println("\n" + name + ": does bug2 step");
				bug2algorithmStep();
			} else {
				System.out.println("\n" + name + ": does path step");
				takeStepOnPath();
			}
		} else {
			System.out.println("\n" + name + ": stays stopped");
		}
	}

	// Tries to step forward on the planned path
	// If it can't, it activates bug2
	private void takeStepOnPath() {
		Integer nextVertexNum = getNextStepOnPath();
		String stepDir = findStepDir(nextVertexNum);

		System.out.println("Direction: " + direction);
		System.out.println("Rel. step direction: " + stepDir);
		System.out.println("Can move to next step: " + canMoveTo(nextVertexNum));

		// graph.dynamicObstacles.containsKey(nextVertexNum)
		if (canMoveTo(nextVertexNum)) {
			currentPathIndex++;
			moveTo(currentSelectedPath.get(currentPathIndex), stepDir);
		} else {
			// make sure we are turned towards the next step before we start bug2
			if (stepDir.equals("backward")) {
				System.out.println("NOTE: turning fully around");
				turnLeft();
				turnLeft();
			} else if (stepDir.equals("left")) {
				turnLeft();
				System.out.println("Turning left for bug2");
			} else if (stepDir.equals("right")) {
				turnRight();
				System.out.println("Turning right for bug2");
			}
			System.out.println("Direction: " + direction);
			Object obstacle = graph.dynamicObstacles.get(getForwardVertexNum());
			activateBug2(obstacle);
		}
	}

	public void stopToAvoidCollisionForBug2() {
		System.out.println(this.name + ": received stop signal");
		stayStoppedToAvoidCollisionBug2 = true;
	}

	public void continueCollsionAvertedBug2() {
		System.out.println(this.name + ": received continue signal");
		stayStoppedToAvoidCollisionBug2 = false;
	}

	private void activateBug2(Object obstacle) {
		System.out.println("bug2 activated");
		bug2IsActive = true;
		remainingPathToFindUsingBug2 = currentSelectedPath.subList(currentPathIndex + 2, currentSelectedPath.size());
		if (obstacle instanceof Robot) {
			System.out.println("telling other robot to stop");
			otherRobotInBug2 = (Robot) obstacle;
			otherRobotInBug2.stopToAvoidCollisionForBug2();

		}
	}

	private void deactivateBug2() {
		currentPathIndex = currentSelectedPath.indexOf(currentPosition);
		bug2IsActive = false;
		bug2SearchDirection = null;
		remainingPathToFindUsingBug2 = null;
		if (otherRobotInBug2 != null) {
			otherRobotInBug2.continueCollsionAvertedBug2();
			otherRobotInBug2 = null;
		}
		System.out.println("BUG2 DEACTIVATED. PATH FOUND");
	}

	public void bug2algorithmStep() {
		if (bug2SearchDirection == null) {
			System.out.println("SEARCH DIRECTION IS NOW LEFT");
			bug2SearchDirection = "left";
			turnRight();
		}
		if (bug2SearchDirection.equals("left")) {
			if (remainingPathToFindUsingBug2.contains(currentPosition)) {
				// robot should be back on the path
				deactivateBug2();
			}
			// robot should try to hug the obstacle on its left
			else if (canMoveTo(getLeftVertexNum())) {
				System.out.println("bug2 move left");
				turnLeft();
				moveForward();
			} else if (canMoveTo(getForwardVertexNum())) {
				System.out.println("bug2 move forward");
				moveForward();
			} else {
				System.out.println("SEARCH DIRECTION IS NOW RIGHT");
				bug2SearchDirection = "right";
				turnRight();
				turnRight();
			}
		} else if (bug2SearchDirection.equals("right")) {
			if (remainingPathToFindUsingBug2.contains(currentPosition)) {
				// robot should be back on the path
				deactivateBug2();
			}
			// robot should try to hug the obstacle on its right
			else if (canMoveTo(getRightVertexNum())) {
				turnRight();
				moveForward();
			} else if (canMoveTo(getForwardVertexNum())) {
				moveForward();
			} else {
				throw new Error("bug2 is unable to find a path");
			}
		}
	}

	public boolean pathIsComplete() {
		return currentPathIndex >= currentSelectedPath.size() - 1;
	}

	// Getter function for the battery level
	public int getBatteryLevel() {
		return batteryLevel;
	}

	// Setter function for the battery level
	public void setBatteryLevel(int newBatteryLevel) {
		batteryLevel = newBatteryLevel;
	}

	// Function to fetch package from shelf and into the storage
	public void fetchPackageFromShelf(int rotation, int height, int length,String rfid) {
		arm.fetchPackage(rotation, height, length, rfid);
	}

	// Dummy function to fake placing a package on a conveyor belt
	public void putPackageOnConveyorBelt(String rfid) {
		arm.removePackageFromRobotStorage();
		System.out.println(rfid + " placed on conveyor belt");
	}
}
