package code;

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

	public static void main(String[] args) {
		Graph graph = new Graph(GRID_WIDTH, GRID_HEIGHT, OBSTACLES);
		int start = 0; // start position is cell 1 (index 0)
		int end = 39; // end position is cell 40 (index 39)
//		List<Integer> globalPath = graph.dijkstra(start, end);
//		Map<Integer, List<Integer>> localPaths = graph.computeOptimalLocalPaths(globalPath, end);
//
//		Robot robot = new Robot(start, globalPath, localPaths);

		List<Integer> globalPath = graph.dijkstra(start, end);
		Map<Integer, List<Integer>> localPaths = new HashMap<>();

		Robot_path robot = new Robot_path(start, globalPath, localPaths, graph, end);

		System.out.print("Robot's Global Path: ");
		for (int position : robot.getGlobalPath()) {
			System.out.print((position + 1) + " "); // increase each position by 1 for printing...since it starts from 0
		}

		System.out.println();

//		robot.getLocalPaths().forEach((cell, path) -> {
//			System.out.print("Robot's Local Path from Cell " + (cell + 1) + ": ");
//			path.forEach(position -> System.out.print((position + 1) + " "));
//			System.out.println();
//		});

		Map<Integer, List<Integer>> sortedLocalPaths = robot.getLocalPaths();
		sortedLocalPaths.forEach((cell, path) -> {
			System.out.print("Local Path from Cell " + (cell + 1) + ": ");
			path.forEach(position -> System.out.print((position + 1) + " "));
			System.out.println();
		});

//        while (robot.getCurrentPosition() != end) {
//            robot.moveToNext();
//        }
		while (robot.getCurrentPosition() != end) {
			robot.moveToNext();
		}
		System.out.println("Robot movement complete.");
//		if (globalPath.isEmpty() || globalPath.get(0) == end) {
//			System.out.println("No global path found.");
//		} else {
//			System.out.print("Global Path: ");
//			globalPath.forEach(position -> System.out.print((position + 1) + " "));
//			System.out.println("\n");
//
//
//			Map<Integer, List<Integer>> optimalLocalPathsMap = graph.computeOptimalLocalPaths(globalPath, end);
//
//
//			for (int cell : globalPath) {
//				List<Integer> localPath = optimalLocalPathsMap.get(cell);
//				if (localPath != null && !localPath.isEmpty()) {
//					System.out.print("Optimal Local Path for Cell " + (cell + 1) + ": ");
//					localPath.forEach(position -> System.out.print((position + 1) + " "));
//					System.out.println();
//				}
//			}
//		}

//		List<Integer> globalPath = graph.dijkstra(start, end);
//		Map<Integer, List<Integer>> optimalLocalPathsMap = graph.computeOptimalLocalPaths(globalPath, end);
//		for (Map.Entry<Integer, List<Integer>> entry : optimalLocalPathsMap.entrySet()) {
//			int cell = entry.getKey();
//			List<Integer> localPath = entry.getValue();
//
//			System.out.print("Optimal Local Path for Cell " + (cell + 1) + ": ");
//			localPath.forEach(position -> System.out.print((position + 1) + " "));
//			System.out.println();
//		}
//		if (globalPath.isEmpty() || globalPath.get(0) == end) {
//			System.out.println("No global path found.");
//		} else {
//			System.out.println("Global Path to " + (end + 1) + ":");
//			globalPath.forEach(position -> System.out.print((position + 1) + " "));
//			System.out.println("\n");
//
//			Map<Integer, List<List<Integer>>> localPathsMap = graph.computeLocalPaths(globalPath, end);
//			for (Map.Entry<Integer, List<List<Integer>>> entry : localPathsMap.entrySet()) {
//				int cell = entry.getKey();
//				List<List<Integer>> localPaths = entry.getValue();
//
//				System.out.println("Local Paths for Cell " + (cell + 1) + ":");
//				for (List<Integer> path : localPaths) {
//					System.out.print("Path: ");
//					path.forEach(p -> System.out.print((p + 1) + " "));
//					System.out.println();
//				}
//				System.out.println();
//			}
//		}

//		List<Integer> path = graph.dijkstra(start, end);
//		Map<Integer, List<List<Integer>>> localPathsMap = graph.computeLocalPaths(path, end);
//		
//		if (path.isEmpty() || path.get(0) == end) {
//			System.out.println("No path found.");
//		} else {
//			System.out.println("Path to " + (end + 1) + ":");
//			path.forEach(position -> {
//				System.out.print((position + 1) + " ");
//			});
//			System.out.println();
//
//			Robot robot = new Robot(start);
//			for (int position : path) {
//				robot.moveTo(position);
//			}
//		}
	}

}
