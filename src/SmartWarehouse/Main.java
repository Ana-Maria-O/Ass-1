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

	public static void main(String[] args) {
		boolean flag = true;
		Graph graph = new Graph(GRID_WIDTH, GRID_HEIGHT, OBSTACLES);
		int start = 0; // Start position is 1 in 1-indexed
		int target = 39; // Target position is 40 in 1-indexed

		Map<Integer, List<List<Integer>>> allpaths = graph.computeAllPathsToTarget(target);
		Robot robot = new Robot(start, allpaths, graph);
		// Initialize the robot at the start position
//		Robot robot = new Robot(start, graph.computeAllPathsToTarget(target), graph);

		// Get the initial path for the robot
		List<Integer> robotPath = robot.selectPathToTarget(start, target, false); // false for optimal path
		System.out.println("Robot moved to cell " + (robotPath.get(0) + 1));
		Scanner scanner = new Scanner(System.in);
		for (int i = 1; i < robotPath.size(); i++) {
			int currentStep = robotPath.get(i);

			if (currentStep != target) {
				// Comment if you DONT want to ask the user
				System.out.println("Do you want to place an obstacle? (Y/N)");
				String response = scanner.nextLine();
				if (response.equalsIgnoreCase("Y")) {
					System.out.println("Enter the cell ID to place an obstacle:");
					int obstacleCellId = scanner.nextInt() - 1; // Adjust for 1-indexed input
					scanner.nextLine();
					if (!graph.isObstacle(obstacleCellId)) {
						graph.addObstacle(obstacleCellId);
						flag = false;
					}
				}

				// Check if the next step is an obstacle
				if ((i + 1) < robotPath.size() && graph.isObstacle(robotPath.get(i + 1))
						|| graph.isObstacle(robotPath.get(i))) {
					if (graph.isObstacle(robotPath.get(i + 1))) {
						System.out.println("Obstacle detected at cell " + (robotPath.get(i + 1) + 1));
					} else {
						System.out.println("Obstacle detected at cell " + (robotPath.get(i) + 1));
					}

					if (!robot.moveToAdjacentNonObstacleCell(graph)) {
						System.out.println("No adjacent non-obstacle cells available. Unable to proceed.");
						break;
					} else {
						// Recompute the path from the robot's new current position considering the new
						// obstacle
						robotPath = robot.selectPathToTargetObstacle(robot.getCurrentPosition(), target, false, graph);
						i = robotPath.indexOf(robot.getCurrentPosition()) - 1; // Update the loop index
						continue;
					}
				}
				robot.setCurrentPosition(currentStep);
				System.out.println("Robot moved to cell " + (currentStep + 1));
			} else if (currentStep == target)  {
				robot.setCurrentPosition(currentStep); // Move to the final cell/target.
				System.out.println();
				System.out.println("Robot moved to cell " + (currentStep + 1));
				System.out.println();
				System.out.println("Robot reached its destination.");
			}
//			if (flag) {
////			if (response.equalsIgnoreCase("Y")) {
////				System.out.println("Enter the cell ID to place an obstacle:");
////			int obstacleCellId = scanner.nextInt() - 1; // Adjust for 1-indexed input
//				int obstacleCellId = 9;
////			scanner.nextLine();
//
//				if (!graph.isObstacle(obstacleCellId)) {
//					graph.addObstacle(obstacleCellId);
//					flag = false;
//				}
//			}
		}

//        // Iterate through the path
//        for (int i = 0; i < robotPath.size(); i++) {
//            int currentStep = robotPath.get(i);
//            robot.setCurrentPosition(currentStep);
//            System.out.println("Robot moved to cell " + (currentStep + 1));
//
//            System.out.println("Do you want to place an obstacle? (Y/N)");
//            String response = scanner.nextLine();
//            if (response.equalsIgnoreCase("Y")) {
//                System.out.println("Enter the cell ID to place an obstacle:");
//                int obstacleCellId = scanner.nextInt() - 1; // Adjust for 1-indexed input
//                scanner.nextLine(); // Consume the newline
//
//                if (obstacleCellId == currentStep) {
//                    if (!robot.moveToAdjacentNonObstacleCell(graph)) {
//                        System.out.println("No adjacent non-obstacle cells available. Unable to proceed.");
//                        break;
//                    }
//                } else if (!graph.isObstacle(obstacleCellId)) {
//                    graph.addObstacle(obstacleCellId);
//                }
//
//                // Recompute the path from the robot's current position
//                robotPath = robot.selectPathToTarget(target, false); // false for optimal path
//                i = robotPath.indexOf(robot.getCurrentPosition()) - 1; // Update the loop index
//            }
//        }

		scanner.close();
	}
}

//public class Main {
//
//	public static void main(String[] args) {
//		int GRID_WIDTH = 6;
//		int GRID_HEIGHT = 7;
//		Set<Integer> OBSTACLES = new HashSet<>(Arrays.asList(1, 7, 8, 20, 26, 28, 34));
//
//		Graph graph = new Graph(GRID_WIDTH, GRID_HEIGHT, OBSTACLES);
//		int start = 0; // Start position is 1 in 1-indexed
//		int target = 39; // Target position is 40 in 1-indexed
//
//		// Compute all possible paths from each cell in the map to the target
//		Map<Integer, List<List<Integer>>> allPaths = graph.computeAllPathsToTarget(target);
//
//		// Initialize the robot at the start position
//		Robot robot = new Robot(start, allPaths);
//
//		boolean flag = false; // Set to true if you want to have random path to the target
//
//		// Let the robot select the path to the target...will be changed to the WMS
//		List<Integer> optimalPath = robot.selectPathToTarget(target, flag);
//		System.out.print("Optimal Path from Cell " + (start + 1) + " to Target: ");
//		optimalPath.forEach(position -> System.out.print((position + 1) + " "));
//		System.out.println();
//
//		// The robot moves following the optimal path
//		robot.followPath(optimalPath);
//	}
//}
