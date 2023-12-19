package src.SmartWarehouse;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Random;

import static org.junit.Assert.assertNull;

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
	private static final int SAFETY_TIMER = 5; // How many untits of time in between checks from the safety monitor

	// List of robots
	private static List<Robot> robots = new ArrayList<Robot>();
	// Map with all the shortest paths in the warehouse
	private static List<Integer>[][] allPaths;
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
		int target = 15;
		// Target location of the shelf
		int shelfLocation = 56;
		// RFID of the package
		String RFID = "abc123";
		// Package coordinates on shelf (for the robot arm movements)
		int[] packageShelfCoord = new int[] { 2, 10, 2 };

		// Decide which robot to send to pick up the package from the shelf to the
		// conveyor belt
		Object[] mission = chooseRobotForMission(shelfLocation, target);

		// Give the chosen robot the first path and add it to the active robots list
		if (mission.length > 0) {
			// Get the mission elements
			int robotIndex = ((Integer) mission[0]).intValue();
			List<Integer> path1 = (List<Integer>) mission[1];

			// Get the robot selected for the mission
			Robot missionRobot = robots.get(robotIndex);

			missionRobot.setCurrentSelectedPath(path1);
			// Set the robot's target
			missionRobot.setTargetPosition(shelfLocation);
			// Add the robot to the active robots list
			activeRobots.add(robotIndex);
			// Timer for the safety monitor
			int safety = 0;

			// While loop until the mission is complete
			while (missionRobot.getCurrentPosition() != target) {
				// Update the safety timer
				safety += 1;
				Iterator<Integer> iterator = activeRobots.iterator();
				// All robots make a step, and we check if any robot reached a destination
				while (iterator.hasNext()) {
					StatusDisplay.printGraph(graph, robots);
					int robot = iterator.next();
					Robot selectedRobot = robots.get(robot);
					selectedRobot.stepTowardsTarget();
					// If the robot reached its destination, we clear the selected path and we take
					// it out of the active robots list
					// NOTE: This would probably change depending on the scenario
					if (selectedRobot.getCurrentPosition() == selectedRobot.getTargetPosition()) {
						// Clear the selected path
						selectedRobot.clearCurrentSelectedPath();
						// Remove this robot's index from the active robots list
						iterator.remove();
					}
				}

				// We check if the robot's reached the destination of path1 while walking path1
				if (missionRobot.getCurrentPosition() == shelfLocation
						&& missionRobot.getTargetPosition() == shelfLocation) {
					// Get the package off the shelf
					missionRobot.fetchPackageFromShelf(packageShelfCoord[0], packageShelfCoord[1], packageShelfCoord[2],
							RFID);
					// Get a route to the conveyor belt and give it to the robot
					// We can't use the one we had before because the other robots' routes may
					// differ from when we computed it
					missionRobot.setCurrentSelectedPath(computePathICA(shelfLocation, target, robotIndex));
					// Set the robot's new target
					missionRobot.setTargetPosition(target);
					// Put the robot back into the active robots list
					activeRobots.add(robotIndex);
				}

				// We check if the robot reached the conveyor belt and the conveyor belt is the
				// target
				if (missionRobot.getCurrentPosition() == target && missionRobot.getTargetPosition() == target) {
					// The robot places the package on the conveyor belt
					missionRobot.putPackageOnConveyorBelt(RFID);
				}

				// If it is time for the safety monitor to analyze the robots, we call it and
				// reset the timer
				if (safety == SAFETY_TIMER) {
					safetyMonitor();
					safety = 0;
				}
			}
		}

		// TODO: Create scenarios for deviations
	}

	// Method to abort the current task an emergency stop command, a critical error,
	// or other operational constraints..
	private static void abortTask(int robotIndex) {
		Robot robot = robots.get(robotIndex);
		robot.clearCurrentSelectedPath(); // Clear the current path
		activeRobots.remove(Integer.valueOf(robotIndex)); // Remove from active robots list
		notify("Task aborted for Robot " + robotIndex);
	}

// Method to check and avoid collisions
	private static void checkAndAvoidCollision() {
		for (Integer robotIndex : activeRobots) {
			Robot robot = robots.get(robotIndex);
			int nextPosition = robot.getNextStepPosition();
			for (Integer otherRobotIndex : activeRobots) {
				if (!otherRobotIndex.equals(robotIndex)) {
					Robot otherRobot = robots.get(otherRobotIndex);
					if (otherRobot.getCurrentPosition() == nextPosition) {
						// Collision detected, re-route one of the robots
						notify("Collision detected. Rerouting Robot " + robotIndex);
						List<Integer> newPath = computePathICA(robot.getCurrentPosition(), robot.getTargetPosition(),
								robotIndex);
						robot.setCurrentSelectedPath(newPath);
						break;
					}
				}
			}
		}
	}

// Method hecks the battery level for the robot and takes it to the CS if its below a specific level.
	private static void checkBatteryLevels() {
		for (Integer robotIndex : activeRobots) {
			Robot robot = robots.get(robotIndex);
			if (robot.getBatteryLevel() < 10) { // 10%?
				notify("Battery low. Robot " + robotIndex + " heading to charging station.");
				List<Integer> chargingPath = computePathToNearestChargingStation(robot.getCurrentPosition());
				robot.setCurrentSelectedPath(chargingPath);
				robot.setTargetPosition(chargingPath.get(chargingPath.size() - 1));
			}
		}
	}

// Method to compute the path to the nearest charging station from the robot's current position.
	private static List<Integer> computePathToNearestChargingStation(int currentRobotPosition) {
		int nearestCSPosition = -1;
		int shortestDistance = Integer.MAX_VALUE;

		// Iterate through all charging stations to find the nearest one.
		for (int csPosition : csPositions) {
			List<Integer> pathToCS = graph.dijkstra(currentRobotPosition, csPosition);
			if (pathToCS.size() < shortestDistance) {
				shortestDistance = pathToCS.size();
				nearestCSPosition = csPosition;
			}
		}

		// If a nearest charging station is found, return the path to it.
		if (nearestCSPosition != -1) {
			return graph.dijkstra(currentRobotPosition, nearestCSPosition);
		} else {
			// Return an empty path if no charging station is found or reachable.
			return new ArrayList<>();
		}
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
			Robot robot = new Robot(i + 11, graph);
			robot.index = i;
			robots.add(robot);
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
			if (!activeRobots.contains(i) && !csPositions.contains(robot.getCurrentPosition())) {
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
					return new Object[] { i, tempPath1 };
				}
				// If it doesn't, send the robot to charge
				else {
					sendRobotToCharge(i);
				}
			}
		}
		// If no robot is available, return an empty array
		return new Object[] {};
	}

	// TODO: Algorithm which, for each point on the grid, computes the shortest path
	// to each other point
	// The paths should be of class List<Integer>
	// Store all of them the variable allMaps, where allMaps[x][y] is the shortest
	// path from x to y
	private static void computePathsForGrid() {
		int gridSize = graph.getGridWidth() * graph.getGridHeight();
		allPaths = new ArrayList[gridSize][gridSize];

		for (int i = 0; i < gridSize; i++) {
			for (int j = 0; j < gridSize; j++) {
				if (i == j) {
					allPaths[i][j] = new ArrayList<>(Arrays.asList(i)); // Path from a node to itself.
				} else {
					allPaths[i][j] = graph.dijkstra(i, j); // Compute the shortest path from i to j.
				}
			}
		}
	}

	// ICA path planning algorithm - TODO: PLEASE TEST EVERYTHING HERE
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
		chromosomes.add(new ArrayList<Integer>());

		// List of all the fitness levels
		List<Double> fitnesses = new ArrayList<Double>();

		// Get the shortest path between start and end
		List<Integer> currentPath = allPaths[start][end];

		// If there is no other active robot, then return this one
		if (activeRobots.size() == 0) {

			return currentPath;
		}

		// If there are other active robots, continue the ICA

		// Add the waypoint of currentPath to the first chromosome if the robot is not
		// active
		if (!activeRobots.contains(rIndex)) {
			chromosomes.get(0).add(currentPath.get(currentPath.size() / 2));
		}

		// For each active robot, get the shortest path between its target and current
		// position, as well as that path's waypoint
		for (Integer robotIndex : activeRobots) {
			// Get the robot's current position
			Integer robotPos = robots.get(robotIndex).getCurrentPosition();

			// Get the robot's target
			Integer robotTarget = robots.get(robotIndex).getTargetPosition();

			// Get the shortest path between the target and current position
			List<Integer> robotPath = allPaths[robotPos][robotTarget];

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

						// Get the index of the cross point in each paths. In the warehouse, the index
						// represents the time when the robot is expected to reach the waypoint.
						// Compute the difference between the indexes, aka the time difference between
						// when the robots will reach the waypoint
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

		// The child of the chromosomes
		List<Integer> child = new ArrayList<Integer>();

		// For each element in the chromosome, randomly select the parent it will
		// inherit from or if it will be a crossover between the parents
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

	// TODO: Test
	private static List<Integer> pathFromWaypoint(int robotIndex, int waypoint) {
		List<Integer> newPath = new ArrayList<Integer>();
		// Select and add the path from the robot to the waypoint
		newPath.addAll(allPaths[robots.get(robotIndex).getCurrentPosition()][waypoint]);
		// Remove the last element because it repeats in the next path
		newPath.remove(newPath.size() - 1);
		// Select and add the path from the waypoint to the robot's target
		newPath.addAll(allPaths[waypoint][robots.get(robotIndex).getTargetPosition()]);

		return newPath;
	}

	// TODO: Test
	// Should check the positions of all robots, and recompute all paths if it
	// detects safety problems
	private static void safetyMonitor() {
		// Check every active robot's current position and see if it is in the robot's
		// current selected path
		for (Integer robotIndex : activeRobots) {
			// Current robot
			Robot robot = robots.get(robotIndex);
			// If the robot's current position is not in the robot's path, recompute the
			// paths of all robots
			List<Integer> a = robot.getCurrentSelectedPath();
			int b = robot.getCurrentPosition();
			if (!a.contains(b)) {
				// The function returns the path of the robot, but because it is already in the
				// activeRobots list its path is already changed by the algorithm so we don't
				// need to take more actions here
				computePathICA(robot.getCurrentPosition(), robot.getTargetPosition(), robotIndex);
			}
		}
	}

	// Send the robot to the nearest charger to charge
	// TODO: Test
	private static void sendRobotToCharge(Integer robotIndex) {
		// Use the abortToCharge function to get the path to the nearest charging
		// station
		List<Integer> csPath = abortToCharge(robotIndex);

		// Set the new path of the robot. If the path to the charging station is too
		// long for its battery level, the setter function will call abortToCharge again
		// and set the path and target. We won't check for this.
		robots.get(robotIndex).setCurrentSelectedPath(csPath);

		// Set the target of the robot to the nearest charging station
		robots.get(robotIndex).setTargetPosition(csPath.get(csPath.size() - 1));

		// If the robot is not on the list of active robots, add it
		if (!activeRobots.contains(robotIndex)) {
			activeRobots.add(robotIndex);
		}
	}

	// Function called by a robot when it decides to abort its current mission and
	// wants to go charge
	// Returns the path for the robot to the nearest charging station
	// TODO: Test
	// TODO: Resume the mission after charging if robot has package
	public static List<Integer> abortToCharge(Integer robotIndex) {
		// Get the position of the robot
		int position = robots.get(robotIndex).getCurrentPosition();
		// Position of the nearest charging station
		int csPosition;

		// Decide the position of the nearest charging station
		// The robot is in the top left quarter of the grid
		if (position < 5 || (position > 9 && position < 15) || (position > 19 && position < 25)
				|| (position > 29 && position < 35) || (position > 39 && position < 45)) {
			// The closest charging station is in the top left corner of the grid
			csPosition = 0;
			// The robot is in the top right quarter of the grid
		} else if ((position > 4 && position < 10)
				|| (position > 14 && position < 20) && (position > 24 && position < 30)
				|| (position > 34 && position < 40) || (position > 44 && position < 50)) {
			// The closest charging station is in the top right corner of the grid
			csPosition = 9;
			// The robot is in the bottom left quarter of the grid
		} else if ((position > 49 && position < 55) || (position > 59 && position < 65)
				|| (position > 69 && position < 75) || (position > 79 && position < 85)
				|| (position > 89 && position < 95)) {
			// The closest charhing station is in the bottom left corner of the grid
			csPosition = 90;
			// The robot is in the bottom right quarter of the grid
		} else {
			// The closest charging station is in the bottom right of the grid
			csPosition = 99;
		}

		// Return the path to the charging station and reroute the other robots
		return computePathICA(position, csPosition, robotIndex);
	}

	// Dummy function for notifications send to WMS
	public static void notify(String message) {
		System.out.println(message);
	}

	// Class that represents a charging station
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
