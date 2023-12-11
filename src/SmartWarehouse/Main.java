package src.SmartWarehouse;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Scanner;

public class Main {
	static final int GRID_WIDTH = 6;
	static final int GRID_HEIGHT = 7;
	static final Set<Integer> OBSTACLES = new HashSet<>(Arrays.asList(7, 8, 28, 34));

	public static void printGraph(Graph graph, List<Robot> robots, Set<Integer> dynamicObstacles) {
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
				if (graph.isObstacle(vertexNum)) {
					System.out.print("\033[91m" + "X" + "\u001B[0m");
				} else if (dynamicObstacles.contains(vertexNum)) {
					System.out.print("\033[40m" + "X" + "\u001B[0m");
				} else if (robotPositions.contains(vertexNum)) {
					System.out.print("\033[92m" + "R" + "\u001B[0m");
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

	public static void main(String[] args) {
		Graph graph = new Graph(GRID_WIDTH, GRID_HEIGHT, OBSTACLES);
		int start = 0; // Start position is 1 in 1-indexed
		int target = 39; // Target position is 40 in 1-indexed

		List<List<Integer>> allpaths = graph.computeAllPathsToTarget(target);
		Robot robot = new Robot(start, graph);
		robot.setTarget(target, allpaths);
		robot.selectPathToTarget();
		Set<Integer> dynamicObstacles = new HashSet<>(Arrays.asList(6));
		robot.dynamicObstacles = dynamicObstacles;

		List<Integer> robotPath = robot.getCurrentSelectedPath();
		System.out.print("Current path: ");
		System.out.println(robotPath);

		printGraph(graph, Arrays.asList(robot), dynamicObstacles);

		System.out.println(graph.getMoveOptions(new Point(0,0)));

		System.out.println(robot.direction);
		System.out.println(robot.getMoveOptions());

		robot.turnLeft();
		System.out.println(robot.direction);
		System.out.println(robot.getMoveOptions());

		robot.turnLeft();
		System.out.println(robot.direction);
		System.out.println(robot.getMoveOptions());

		robot.turnLeft();
		System.out.println(robot.direction);
		System.out.println(robot.getMoveOptions());

		robot.turnLeft();
		System.out.println(robot.direction);
		System.out.println(robot.getMoveOptions());
		
		robot.turnRight();
		System.out.println(robot.direction);
		System.out.println(robot.getMoveOptions());

		robot.turnRight();
		System.out.println(robot.direction);
		System.out.println(robot.getMoveOptions());

		robot.turnRight();
		System.out.println(robot.direction);
		System.out.println(robot.getMoveOptions());

		robot.turnRight();
		System.out.println(robot.direction);
		System.out.println(robot.getMoveOptions());

		System.exit(0);
		// graph.addObstacle(12);
		// Scanner scanner = new Scanner(System.in);
		while (!robot.pathIsComplete()) {
			// System.out.println("Do you want to place an obstacle? (Y/N)");
			// String response = scanner.nextLine();
			// if (response.equalsIgnoreCase("Y")) {
			// System.out.println("Enter the cell ID to place an obstacle:");
			// int obstacleCellId = scanner.nextInt();
			// scanner.nextLine();
			// if (!graph.isObstacle(obstacleCellId)) {
			// graph.addObstacle(obstacleCellId);
			// }
			// }

			robot.takeStepOnPath();
			printGraph(graph, Arrays.asList(robot), dynamicObstacles);

			System.out.print("curr pos: ");
			System.out.println(robot.getCurrentPosition());
		}
		System.exit(0);
		// Dijkstra example ends here
	}
}
