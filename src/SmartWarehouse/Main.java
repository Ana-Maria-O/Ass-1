package src.SmartWarehouse;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Main {

    public static void main(String[] args) {
    	int GRID_WIDTH = 6;
    	int GRID_HEIGHT = 7;
        Set<Integer> OBSTACLES = new HashSet<>(Arrays.asList(1, 7, 8, 20, 26, 28, 34));
        
        Graph graph = new Graph(GRID_WIDTH, GRID_HEIGHT, OBSTACLES);
        int start = 0;  // Start position is 1 in 1-indexed
        int target = 39; // Target position is 40 in 1-indexed

        // Compute all possible paths from each cell in the map to the target
        Map<Integer, List<List<Integer>>> allPaths = graph.computeAllPathsToTarget(target);

        // Initialize the robot at the start position
        Robot robot = new Robot(start, allPaths);

        // Let the robot select the optimal path to the target...will be changed to the WMS
        List<Integer> optimalPath = robot.selectOptimalPathToTarget(target);
        System.out.print("Optimal Path from Cell " + (start + 1) + " to Target: ");
        optimalPath.forEach(position -> System.out.print((position + 1) + " "));
        System.out.println();

        // The robot moves following the optimal path
        robot.followPath(optimalPath);
    }
}
