package src.SmartWarehouse;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import java.lang.Math;

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
    // Map with all the shortest paths in the warehouse
    private static Map<Integer, Map<Integer, List<Integer>>> allPaths = new HashMap<Integer, Map<Integer, List<Integer>>>();
    // List with all the currently active robots in the warehouse
    private static List<Integer> activeRobots = new ArrayList<Integer>();

    // The graph repesenting the warehouse
    private static Graph graph;
    // Set with all the known obstacles in the warehouse
    private static Set<Integer> obstacles = new HashSet<Integer>();

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
            // Find the nearest charging station to the conveyor belt location
            Integer chargingStation;
            switch (target) {
                case 4:
                    chargingStation = 0;
                    break;

                case 5:
                    chargingStation = 9;
                    break;

                case 94:
                    chargingStation = 90;
                    break;

                case 95:
                    chargingStation = 99;
                    break;

                default:
                    chargingStation = 0;
            }
            // If the robot is in the active robots list or it isn't in the position of
            // a charging station, its path is computed
            if (!activeRobots.contains(i)
                    && !csPositions.contains(robot.getCurrentPosition())) {
                // Compute path for the robot to the shelf
                List<Integer> tempPath1 = computePathICA(robot.getCurrentPosition(), shelfLocation, i);
                // Compute path for the robot to the conveyor belt
                List<Integer> tempPath2 = computePathICA(shelfLocation, target, i);
                // Compute the path to the closest charging station
                List<Integer> tempPath3 = computePathICA(target, chargingStation, i);

                // Check if the robot has enough battery to reach the conveyor
                // Doubled the minimum necessary battery to account for possible deviations from
                // the path
                if (robot.getBatteryLevel() >= 2 * (tempPath1.size() + tempPath2.size() + tempPath3.size())) {
                    // If the robot can reach the conveyor, return the robot and the paths
                    return new Object[] { i, tempPath1, tempPath2 };
                }
                // If it doesn't, send the robot to charge
                else {
                    sendRobotToCharge(i);
                }
            }
        }
        // If no robot is available, return an empty list
        return new Object[] {};
    }

    // TODO: Algorithm which, for each point on the grid, computes the shortest path
    // to each other point
    // The paths should be of class List<Integer>
    // Store all of them the variable allMaps, where allMaps[x][y] is the shortest
    // path from x to y
    private static void computePathsForGrid() {
    }

    // ICA path planning algorithm - PLEASE TEST EVERYTIHING HERE
    // Takes the start and the end positions of a path and returns a path
    // Also takes the id of a robot, useful for when computing a path for a yet
    // inactive robot
    // It takes into account all the current paths being travelled by robots
    // If rIndex is not of an active robot, then it returns the path of the robot
    // and modifies the path of every other robot
    // If rIndex is of an inactive robot then it returns the new path of the first
    // robot in the activeRobots list and modifies the path of every other robot
    private static List<Integer> computePathICA(int start, int end, int rIndex) {
        // Some constants for the function
        // Number of initial mutations
        final int MUTATIONS = 4;
        // Number of loops of "breeding"
        final int LOOPS = 3;

        // List of all the chromosomes
        List<List<Integer>> chromosomes = new ArrayList<List<Integer>>();

        // List of all the fitness levels
        List<Double> fitnesses = new ArrayList<Double>();

        // Get the shortest path between start and end
        List<Integer> currentPath = allPaths.get(start).get(end);

        // If there is no other active robot, then return this one
        if (activeRobots.size() == 0) {

            return currentPath;
        }

        // If there are other active robots, continue the ICA

        // Add the waypoint of currentPath to the first chromosome
        chromosomes.add(new ArrayList<Integer>());
        chromosomes.get(0).add(currentPath.get(currentPath.size() / 2));

        // For each active robot, get the shortest path between its target and current
        // position, as well as that path's waypoint
        for (Integer robotIndex : activeRobots) {
            // Get the robot's current position
            Integer robotPos = robots.get(robotIndex).getCurrentPosition();

            // Get the robot's target
            Integer robotTarget = robots.get(robotIndex).getTargetPosition();

            // Get the shortest path between the target and current position
            List<Integer> robotPath = allPaths.get(robotPos).get(robotTarget);

            // Get the path's waypoint and add it to the first chromosome
            chromosomes.get(0).add(robotPath.get(robotPath.size() / 2));
        }

        // Perform mutations to obtain more chromosomes
        for (int i = 1; i < MUTATIONS; i++) {
            chromosomes.add(performMutation(chromosomes.get(0)));
        }

        // Make the list of robot indexes for the fitness function
        List<Integer> robotIndexes = new ArrayList<Integer>();

        // Add the current robot's index if it is not active yet
        if (!activeRobots.contains(rIndex)) {
            robotIndexes.add(rIndex);
        }
        // Add all the active robots
        robotIndexes.addAll(activeRobots);

        // Fitness function for each chromosome
        for (int chromosomeIndex = 0; chromosomeIndex < chromosomes.size(); chromosomeIndex++) {
            fitnesses.add(Fitness(chromosomes.get(chromosomeIndex), robotIndexes));
        }

        // While loop for "breeding"
        // I am writing this past midnight, so my code is delving into madness
        // TODO: I NEED TO TEST THIS DESPERATELY
        for (int i = 0; i < LOOPS; i++) {

            // Find the order of chromosomes and their indexes
            int goodChromIndex[] = new int[] { fitnesses.indexOf(Collections.min(fitnesses)), -1, -1,
                    fitnesses.indexOf(Collections.max(fitnesses)) };
            double goodChrom[] = new double[] { fitnesses.get(goodChromIndex[0]), -1, -1,
                    fitnesses.get(goodChromIndex[3]) };

            // Loop through the list of fitness values
            // Only works for MUTATIONS = 4
            for (int j = 1; j < fitnesses.size() && j != goodChromIndex[0] && j != goodChromIndex[3]; j++) {
                // Check if any fitness values have been evaluated, if not then give the current
                // one to goodChrom[1]]
                if (goodChrom[1] == -1) {
                    goodChrom[1] = fitnesses.get(j);
                    goodChromIndex[1] = j;
                    // If the current value is smaller than goodChrom2 then move it to goodChrom[2]
                    // and give the current one to goodChrom[1]
                } else if (fitnesses.get(j) < goodChrom[1]) {
                    goodChrom[2] = goodChrom[1];
                    goodChromIndex[2] = goodChromIndex[1];
                    goodChrom[1] = fitnesses.get(j);
                    goodChromIndex[1] = j;
                    // Otherwise give the current one to goodChrom[2]
                } else {
                    goodChrom[2] = fitnesses.get(j);
                    goodChromIndex[2] = j;
                }
            }

            // Make the list of mutated and crossed fitness values
            List<List<Integer>> children = new ArrayList<List<Integer>>();
            // Crossover the best chromosomes
            children.add(crossOver(chromosomes.get(goodChromIndex[0]), chromosomes.get(goodChromIndex[1])));
            // Mutate the good chromosomes
            children.add(performMutation(chromosomes.get(goodChromIndex[0])));
            children.add(performMutation(chromosomes.get(goodChromIndex[1])));

            // Compute the fitness values for each child and replace the chromosome with the
            // biggest fitness value if the child has a lower fitness value than any
            // chromosome in the population
            for (List<Integer> child : children) {
                // Compute fitness
                double newFitness = Fitness(child, robotIndexes);

                // See if it can replace an old chromosome. The for loop starts with the
                // smallest fitness value
                for (int j = 0; j < MUTATIONS; j++) {
                    // If the new chromosome has lower fitness value than the old chromosome, insert
                    // it
                    if (newFitness < goodChrom[j]) {
                        // Save the index of the chromosome that will be replaced (aka the biggest one)
                        int replacedIndex = goodChromIndex[MUTATIONS - 2];

                        // Shift the arrays with {values for fitness} and {their indexes in the
                        // chromosomes
                        // list} to make space for the new chromosome
                        for (int k = MUTATIONS - 2; k >= j; k--) {
                            goodChrom[k + 1] = goodChrom[k];
                            goodChromIndex[k + 1] = goodChromIndex[k];
                        }

                        // Insert the new chromosome fitness level into the array withordered fitness
                        // levels
                        goodChrom[j] = newFitness;
                        // Insert the new chromosome's index into the array with indexes of the ordered
                        // chromosomes
                        goodChromIndex[j] = replacedIndex;
                        // Replace the chromosome with the previously biggest fitness value with the new
                        // chromosome
                        chromosomes.set(replacedIndex, child);

                        // Wow I can't believe i just did that. I hate myself lol
                    }
                }
            }
        }

        // Choose the chromosome with the lowest fitness level
        double lowestFitness = fitnesses.get(0);
        int lowestFitnessIndex = 0;

        // Run through the list of fitness levels to find the lowest
        for (int i = 1; i < fitnesses.size(); i++) {
            if (fitnesses.get(i) < lowestFitness) {
                lowestFitness = fitnesses.get(i);
                lowestFitnessIndex = i;
            }
        }

        // Get the winning chromosome
        List<Integer> winning = chromosomes.get(lowestFitnessIndex);

        // Recompute the new paths for all the robots and replaces their old paths
        // This condition is true if the robot with index rIndex is currently active
        if (activeRobots.size() == robotIndexes.size()) {

            // Each element in the chromosome corresponds to an active robot
            for (int i = 0; i < winning.size(); i++) {
                // Replace the robot's old path with a newly computed one
                robots.get(robotIndexes.get(i))
                        .setCurrentSelectedPath(pathFromWaypoint(robotIndexes.get(i), winning.get(i)));
            }
            // Otherwise, only replace the paths of the active robots
        } else {
            for (int i = 1; i < winning.size(); i++) {
                // Replace the robot's old path with a newly computed one
                robots.get(robotIndexes.get(i))
                        .setCurrentSelectedPath(pathFromWaypoint(robotIndexes.get(i), winning.get(i)));
            }
        }

        // Return the path for the first robot in the list of robot indexes
        // That is the robot of index rIndex if it not active yet, otherwise it's the
        // first active robot
        return pathFromWaypoint(rIndex, winning.get(0));
    }

    // Helper function for ICA
    // Takes a chromosome as argument and returns a mutation of the passed
    // chromosome
    private static List<Integer> performMutation(List<Integer> chromosome) {
        // What the biggest mutation can be
        // Yes I know it's not the greatest way to get a new position
        final int MAX_MUTATION = 30;

        // Instance of Random
        Random rand = new Random();

        // Choose a random item in the chromosome to mutate
        int mutatedItem = chromosome.get(rand.nextInt(chromosome.size()));

        // Create the mutated chromosome
        List<Integer> mutation = new ArrayList<Integer>();
        for (int i = 0; i < chromosome.size(); i++) {
            // If the current item was the one chosen for mutation, we mutate it
            if (i == mutatedItem) {
                // Make sure the mutation isn't 0 and that the mutated value can still be in the
                // grid
                int mutatedValue = rand.nextInt(2 * MAX_MUTATION + 1) - MAX_MUTATION;

                // Keep computing the mutation until all the conditions in the comment above are
                // satisfied
                while (mutatedValue == 0 || chromosome.get(i) + mutatedValue < 0
                        || chromosome.get(i) + mutatedValue >= WAREHOUSE_LENGTH * WAREHOUSE_WIDTH) {

                    mutatedValue = rand.nextInt(2 * MAX_MUTATION + 1) - MAX_MUTATION;
                }

                mutation.add(chromosome.get(i) + mutatedValue);
            }

            // Else we just add it to the new chromosome as normal
            else {
                mutation.add(chromosome.get(i));
            }
        }

        return mutation;
    }

    // Fitness function for a chromosome
    // Check Lecture 7 slides 42-44 for the function description
    private static double Fitness(List<Integer> chromosome, List<Integer> robotIndexes) {
        // Constants for the function
        final double ALPHA = 0.5 * activeRobots.size();
        final double BETA = Math.sqrt(WAREHOUSE_LENGTH * WAREHOUSE_WIDTH) * activeRobots.size();
        final int SAFTEY_THRESHOLD = 1;

        // Arguments for the function
        int distanceMetrics = 0;
        int crossPoints = 0;
        int safetyLevel = 0; // Maximum value is 3, only adds each level value once

        // List of paths defined by the chromosome
        List<List<Integer>> chPaths = new ArrayList<List<Integer>>();

        // Overall distance of routing

        // For each waypoint in the chromosome, calculate the length of the path
        // represented by the waypoint
        for (int i = 0; i < chromosome.size(); i++) {
            // Path defined by the waypoint
            List<Integer> newPath = pathFromWaypoint(robotIndexes.get(i), chromosome.get(i));
            // Add it to the list of paths
            chPaths.add(newPath);

            // Compute the length of the path and add it to the distance metric
            distanceMetrics += newPath.size();
        }

        // Number and safety level of cross points

        // For each path, check the rest of the list for cross points
        for (int i = 0; i < chPaths.size(); i++) {
            // Transform the list into a set for easier element comparison
            Set<Integer> tempSet1 = new HashSet<Integer>(chPaths.get(i));

            // Check the rest of the list
            for (int j = i + 1; j < chPaths.size(); j++) {
                // Transform the list into a set for easier element comparison
                Set<Integer> tempSet2 = new HashSet<Integer>(chPaths.get(j));

                // Keep only the elements which are common between the 2 paths
                tempSet2.retainAll(tempSet1);

                // Add the number of common elements to the sum of cross points
                crossPoints += tempSet2.size();

                // If there are cross points, check their safety level and add it to the sum of
                // unique safety levels
                if (tempSet2.size() > 0) {
                    // Check each cross point
                    for (int crossPoint : tempSet2) {
                        // Compute the level of safety
                        int level = 2;
                        int difference = Math
                                .abs(chPaths.get(i).indexOf(crossPoint) - chPaths.get(j).indexOf(crossPoint));
                        // If the time difference is greater than the threshold then level is 1,
                        // otherwise it's 2
                        if (difference > SAFTEY_THRESHOLD) {
                            level = 1;
                        }

                        // Add level to the sum if it hasn't been added before
                        if (level == 1) {
                            if (safetyLevel == 0 || safetyLevel == 2) {
                                safetyLevel += 1;
                            }
                        } else {
                            if (safetyLevel == 0 || safetyLevel == 1) {
                                safetyLevel += 2;
                            }
                        }
                    }
                }

            }
        }

        // Return fitness level
        // Uses the formula in Lecture 7 Slide 44
        return distanceMetrics + ALPHA * crossPoints + BETA * safetyLevel;
    }

    // Takes two chromosomes and returns the result of their "breeding"
    private static List<Integer> crossOver(List<Integer> parent1, List<Integer> parent2) {
        // Instance of random
        Random rand = new Random();

        // The child of the parents
        List<Integer> child = new ArrayList<Integer>();

        // For each element in the chromosome, randomly select the parent it will
        // inherit or if it will be a crossover between the parents
        for (int i = 0; i < parent1.size(); i++) {
            int result = rand.nextInt(3);

            switch (result) {
                // If the result is 0, then the child inherits the feature from parent1
                case 0:
                    child.add(parent1.get(i));
                    break;

                // If the result is 1, then the child inherits the feature from parent2
                case 1:
                    child.add(parent2.get(i));
                    break;

                // Otherwise the result is 3, which case the child's feature will be the average
                // of the parents' features
                default:
                    child.add((parent1.get(i) + parent2.get(i)) / 2);
            }
        }

        return child;
    }

    private static List<Integer> pathFromWaypoint(int robotIndex, int waypoint) {
        List<Integer> newPath = new ArrayList<Integer>();
        // Select and add the path from the robot to the waypoint
        newPath.addAll(allPaths.get(robots.get(robotIndex).getCurrentPosition()).get(waypoint));
        // Remove the last element because it repeats in the next path
        newPath.remove(newPath.size() - 1);
        // Select and add the path from the waypoint to the robot's target
        newPath.addAll(allPaths.get(waypoint).get(robots.get(robotIndex).getTargetPosition()));

        return newPath;
    }

    // TODO: Safety monitor
    // Should check the positions of all robots, and recompute all paths if it
    // detects safety problems
    private static void safetyMonitor() {
    }

    // TODO: Send the robot to the nearest charger to charge
    private static void sendRobotToCharge(Integer robotIndex) {
    }

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
