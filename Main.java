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
    static final Set<Integer> OBSTACLES = new HashSet<>(Arrays.asList(7, 8, 28, 34, 20)); // Added an obstacle at cell 21

    public static void main(String[] args) {
        Graph graph = new Graph(GRID_WIDTH, GRID_HEIGHT, OBSTACLES);
        int start = 0; // start position is cell 1 (index 0)
        int end = 39; // end position is cell 40 (index 39)

        // create robot
        List<Integer> globalPath = graph.dijkstra(start, end);
        Map<Integer, List<Integer>> localPaths = new HashMap<>();
      Robot robot = new Robot(start, globalPath, localPaths, graph, end);

        // Simulate robot movement
        while (robot.getCurrentPosition() != end) {
            robot.moveToNext();
        }

        System.out.println("Robot movement complete.");
    }
}
