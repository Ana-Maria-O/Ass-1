package src.SmartWarehouse;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Main {
	static final int GRID_WIDTH = 6;
	static final int GRID_HEIGHT = 7;
	static final Set<Integer> OBSTACLES = new HashSet<>(Arrays.asList(7, 8, 28, 34));

	public static void printGraph(Graph graph, List<Robot> robots, Map<Integer, Object> dynamicObstacles) {
		Set<Integer> robotPositions = new HashSet<>();
		Set<Integer> robotTargets = new HashSet<>();
		Set<Integer> robotPathsVerticies = new HashSet<>();
		if (robots != null) {
			for (Robot robot : robots) {
				robotPositions.add(robot.getCurrentPosition());
				robotTargets.add(robot.getTargetPosition());
				robotPathsVerticies.addAll(robot.getCurrentSelectedPath());
			}
		}
		System.out.println();
		for (int y = 0; y < graph.getGridHeight(); y++) {
			for (int x = 0; x < graph.getGridWidth(); x++) {
				int vertexNum = graph.pointToVertexNum(new Point(x, y));
				if (robotPositions.contains(vertexNum)) {
					System.out.print("\033[92m" + "R" + "\u001B[0m");
				} else if (dynamicObstacles.containsKey(vertexNum)) {
					System.out.print("\033[40m" + "X" + "\u001B[0m");
				} else if (graph.isObstacle(vertexNum)) {
					System.out.print("\033[91m" + "X" + "\u001B[0m");
				} else if (robotTargets.contains(vertexNum)) {
					System.out.print("\033[93m" + "T" + "\u001B[0m");
				} else if (robotPathsVerticies.contains(vertexNum)) {
					System.out.print("\033[94m" + "*" + "\u001B[0m");
				} else {
					System.out.print("*");
				}
			}
			System.out.println();
		}
	}

	// no dynamic obstacles
	public static void scenario1() {
		Map<Integer, Object> dynamicObstacles = new HashMap<>();
		List<Integer> unknownStaticObstacles = Arrays.asList();
		for (Integer vertexNum : unknownStaticObstacles) {
			dynamicObstacles.put(vertexNum, new Object());
		}
		Graph graph = new Graph(GRID_WIDTH, GRID_HEIGHT, OBSTACLES, dynamicObstacles);
		int start = 0; // Start position is 1 in 1-indexed
		int target = 39; // Target position is 40 in 1-indexed

		List<List<Integer>> allpaths = graph.computeAllPathsToTarget(target);
		Robot robot = new Robot(start, graph);
		robot.setTarget(target, allpaths);
		robot.selectPathToTarget();

		List<Integer> robotPath = robot.getCurrentSelectedPath();
		System.out.print("Current path: ");
		System.out.println(robotPath);
		printGraph(graph, Arrays.asList(robot), dynamicObstacles);

		while (!robot.pathIsComplete()) {
			robot.stepTowardsTarget();
			printGraph(graph, Arrays.asList(robot), dynamicObstacles);
		}
	}
	
	// need to round obstacles using bug2
	public static void scenario2() {
		Map<Integer, Object> dynamicObstacles = new HashMap<>();
		List<Integer> unknownStaticObstacles = Arrays.asList(6,31);
		for (Integer vertexNum : unknownStaticObstacles) {
			dynamicObstacles.put(vertexNum, new Object());
		}
		Graph graph = new Graph(GRID_WIDTH, GRID_HEIGHT, OBSTACLES, dynamicObstacles);
		int start = 0; // Start position is 1 in 1-indexed
		int target = 39; // Target position is 40 in 1-indexed

		List<List<Integer>> allpaths = graph.computeAllPathsToTarget(target);
		Robot robot = new Robot(start, graph);
		robot.setTarget(target, allpaths);
		robot.selectPathToTarget();

		List<Integer> robotPath = robot.getCurrentSelectedPath();
		System.out.print("Current path: ");
		System.out.println(robotPath);
		printGraph(graph, Arrays.asList(robot), dynamicObstacles);

		while (!robot.pathIsComplete()) {
			robot.stepTowardsTarget();
			printGraph(graph, Arrays.asList(robot), dynamicObstacles);
		}
	}
	
	// bug2 gets stuck
	public static void scenario3() {
		Map<Integer, Object> dynamicObstacles = new HashMap<>();
		List<Integer> unknownStaticObstacles = Arrays.asList(3,6,31);
		for (Integer vertexNum : unknownStaticObstacles) {
			dynamicObstacles.put(vertexNum, new Object());
		}
		Graph graph = new Graph(GRID_WIDTH, GRID_HEIGHT, OBSTACLES, dynamicObstacles);
		int start = 0; // Start position is 1 in 1-indexed
		int target = 39; // Target position is 40 in 1-indexed

		List<List<Integer>> allpaths = graph.computeAllPathsToTarget(target);
		Robot robot = new Robot(start, graph);
		robot.setTarget(target, allpaths);
		robot.selectPathToTarget();

		List<Integer> robotPath = robot.getCurrentSelectedPath();
		System.out.print("Current path: ");
		System.out.println(robotPath);
		printGraph(graph, Arrays.asList(robot), dynamicObstacles);

		while (!robot.pathIsComplete()) {
			robot.stepTowardsTarget();
			printGraph(graph, Arrays.asList(robot), dynamicObstacles);
		}
	}

	public static void main(String[] args) {
		scenario1();
		scenario2();
		scenario3();
	}
}
