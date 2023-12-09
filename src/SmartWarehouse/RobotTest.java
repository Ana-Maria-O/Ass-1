package src.SmartWarehouse;

import java.util.*;

public class RobotTest {
    public static void main(String[] args) {
        // Initialize a set of obstacles.
        Set<Integer> OBSTACLES = new HashSet<>(Arrays.asList(7, 8, 28, 34));
        Graph graph = new Graph(6, 7, OBSTACLES);

        // Define start and end positions for the robot's path.
        int start = 0, end = 39;

        // Calculate the global path from start to end using Dijkstra's algorithm.
        List<Integer> globalPath = graph.dijkstra(start, end);
        Map<Integer, List<Integer>> localPaths = new HashMap<>();
        // Create a new Robot instance with initial settings.
        Robot robot = new Robot(start, globalPath, localPaths, graph, end);
        // Create starting and ending points for the line to target.
        Robot.Point startPoint = robot.new Point(0, 0);
        Robot.Point endPoint = robot.new Point(5, 6);
        // Generate a line from the start point to the end point.
        List<Robot.Point> line = robot.lineToTarget(startPoint, endPoint);
        
        // Print the line to the target.
        System.out.print("Line to Target: ");
        for (Robot.Point p : line) {
            System.out.print("(" + p.x + ", " + p.y + ") ");
        }
        System.out.println();
        
        // Find adjacent cells for each point in the line.
        robot.findAdjacentCellsForLine(line);

        System.out.println();
        
        // Print the robot's global path.
        System.out.print("Robot's Global Path: ");
        for (int position : robot.getGlobalPath()) {
            System.out.print((position + 1) + " ");
        }
        System.out.println();
        
        // counter.
        int steps = 0;
        
        // Loop to move the robot until it reaches the end position.
        while (robot.getCurrentPosition() != end) {
            // Add an obstacle at position 19 after one step.
            if (steps == 1) { 
                graph.addObstacle(19);
                System.out.println("Added");
            }

            // Move the robot to the next position.
            robot.moveToNext();
            steps++;
            // Pause for 1 second between each move.
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("Robot has reached the target");
    }
}
