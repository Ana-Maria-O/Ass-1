package src.SmartWarehouse;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;

public class WMS {

    // Constants
    // Number of robots
    private static final int ROBOTNUMBER = 3;
    // Number of conveyor belts
    private final int CBNUMBER = cbPositions.size() / 2;
    // Number of charging stations
    private final int CSNUMBER = csPositions.size();
    // Size of warehouse
    private static final int WAREHOUSE_LENGTH = 10;
    private static final int WAREHOUSE_WIDTH = 10;

    // List of robots
    private static List<Robot> robots = new ArrayList<Robot>();

    // The graph repesenting the warehouse
    private static Graph graph;
    // Set with all the known obstacles in the warehouse
    private static Set<Integer> obstacles = new HashSet<Integer>();
    // Map with all the shortest paths in the warehouse
    private static Map<Integer, Map<Integer, List<Integer>>> allPaths = new HashMap<Integer, Map<Integer, List<Integer>>>();

    // Positions of the charging stations
    // There's one in each corner basically
    private static List<Integer> csPositions = Arrays.asList(0, 9, 90, 99);
    // Positions of the conveyor belts
    // A conveyor belt occupies two nodes
    // There's a conveyor belt in the middle of the bottom and top row
    private static List<Integer> cbPositions = Arrays.asList(4, 5, 94, 95);
    // Positions of the shelves
    // A shelf occupies two nodes
    private static List<Integer> shelfPositions = Arrays.asList(41, 51, 44, 54, 47, 57);

    public static void main(String[] args) {

        // Initiate the warehouse
        initWarehouse();

        // Compute all paths
        computePathsForGrid();

        // Target location of the conveyor
        int target = 4;
        // Target location of the shelf
        int shelfLocation = 57;
        // RFID of the package
        // String RFID = "abc123";

        // Decide which robot to send to pick up the package from the shelf to the
        // conveyor belt
        Object[] mission = chooseRobotForMission(shelfLocation, target);

        // TODO: start the mission

        // TODO: figure out the safety monitor
        // not sure how to have that running in parallel without threads. Probably
        // another while loop where we call the monitor after every step of the main
        // robot

        // TODO: Create scenarios for deviations

    }

    private static void initWarehouse() {
        // Compile a set with all the known obstacles in the warehouse
        // The charging stations
        obstacles.addAll(csPositions);
        // The conveyor belts
        obstacles.addAll(cbPositions);
        // The shelves
        obstacles.addAll(shelfPositions);

        // Create the graph
        graph = new Graph(WAREHOUSE_WIDTH, WAREHOUSE_LENGTH, obstacles, new HashMap<Integer, Object>());

        // Robots
        for (int i = 0; i < ROBOTNUMBER; i++) {
            // These positions only work for the default values of this class
            // If you change the grid size or the positions of anything else, you may need
            // to change the robots' positions as well
            robots.add(new Robot(i + 11, graph));
        }

    }

    // Takes the location of the shelf and the position of the conveyor belt for the
    // mission, selects a robot and returns the index of the robot as well as the 2
    // paths it has to travel
    private static Object[] chooseRobotForMission(int shelfLocation, int target) {

        // Check what robots are available
        for (int i = 0; i < ROBOTNUMBER; i++) {
            Robot robot = robots.get(i);
            // If the robot does not have a current chosen path nor is it in the position of
            // a charging station, its path is computed
            if (robot.getCurrentSelectedPath().size() == 0
                    && !csPositions.contains(robot.getCurrentPosition())) {
                // Compute path for the robot to the shelf
                List<Integer> tempPath1 = computePathICA(robot.getCurrentPosition(), shelfLocation);
                // Compute path for the robot to the conveyor belt
                List<Integer> tempPath2 = computePathICA(shelfLocation, target);
                // TODO Optional?: make algo to find path to the nearest conveyor belt and add
                // that too

                // Check if the robot has enough battery to reach the conveyor
                // Doubled the minimum necessary battery to account for possible deviations from
                // the path
                if (robot.getBatteryLevel() >= 2 * (tempPath1.size() + tempPath2.size())) {
                    // If the robot can reach the conveyor, return the robot and the paths
                    return new Object[] { i, tempPath1, tempPath2 };
                }
            }
        }
        // If no robot is available, return an empty list
        return new Object[] {};
    }

    // TODO: Algorithm which, for each point on the grid, computes the shortest path to each other point
    // The paths should be of class List<Integer>
    // Store all of them the variable allMaps, where allMaps[x][y] is the shortest path from x to y 
    private static void computePathsForGrid() {}

    // TODO ICA path planning algorithm
    // Takes the start and the end positions of a path and returns the path
    // It takes into account all the current paths being travelled by robots
    private static List<Integer> computePathICA(int start, int end) {
        // Get path
        return new ArrayList<Integer>();
    }

    // TODO: Safety monitor
    // Should check the positions of all robots, and recompute all paths if it
    // detects safety problems
    private static void safetyMonitor() {}

    // Dummy function for notifications send to WMS
    public static void notify(String message) {
        System.out.println(message);
    }

    // Clas that represents a charging station
    public class ChargingStation {
        // True if the charging station is currently in use, false otherwise
        private boolean used = false;
        // Position of the charging station on the grid
        private int position;

        // Initialize the charging station on a position
        public ChargingStation(int position) {
            this.position = position;
        }

        // Returns if the charging station is currently in use
        public boolean getUsed() {
            return used;
        }

        // Sets the usage status of the charging station (if it is currently in use or
        // not)
        public void setUsed(boolean newUsed) {
            used = newUsed;
        }

        // Get the position of the charging station
        public int getPosition() {
            return position;
        }
    }
}
