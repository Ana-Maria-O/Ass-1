package src.SmartWarehouse;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
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
	static List<Integer> activeRobots = new ArrayList<Integer>();

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
		int cbLocation1 = 15;
		int cbLocation2 = 84;
		// Target location of the shelf
		int shelfLocation1 = 56;
		int shelfLocation2 = 43;
		// RFID of the package
		String RFID = "abc123";
		// Package coordinates on shelf (for the robot arm movements)
		int[] packageShelfCoord = new int[] { 2, 10, 2 };

		// uncomment to show robot being sent to recharge
		// robots.get(0).setBatteryLevel(5);

		// Decide which robot to send to pick up the package from the shelf to the
		// conveyor belt
		Object[] mission = chooseRobotForMission(shelfLocation1, cbLocation1);
		int robotIndex = ((Integer) mission[0]).intValue();
		List<Integer> pathToShelf = (List<Integer>) mission[1];
		Task task1 = new Task(robots.get(robotIndex), pathToShelf, shelfLocation1, cbLocation1, packageShelfCoord,
				RFID);

		/*
		 * Uncomment the below uncommented lines to see failing test case
		 * (uncomment choosing mission2 + tasks with two tasks)
		 */

		// Object[] mission2 = chooseRobotForMission(shelfLocation2, cbLocation2);
		// int robotIndex2 = ((Integer) mission2[0]).intValue();
		// List<Integer> pathToShelf2 = (List<Integer>) mission2[1];
		// Task task2 = new Task(robots.get(robotIndex2), pathToShelf2, shelfLocation2,
		// cbLocation2, packageShelfCoord, RFID);

		StatusDisplay.printGraph(graph, robots);

		// Timer to detect when to run the safety monitor
		int safety = 0;

		while (true) {
			boolean allDone = true;
			// Artificially lower robot's battery level to test aborting task to charge
			if (robots.get(0).getCurrentPosition() == 21) {
			robots.get(0).setBatteryLevel(5);
			}
			for (Robot robot : robots) {
				if (!robot.taskIsDone()) {
					allDone = false;
				}
				robot.timeStep();
				safety += 1;

				// If it is time for the safety monitor to analyze the robots, we call it and
				// reset the timer
				if (safety == SAFETY_TIMER) {
					safetyMonitor();
					safety = 0;
				}
			}
			StatusDisplay.printGraph(graph, robots);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if (allDone)
				break;
		}

		System.exit(0);
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

	// Method hecks the battery level for the robot and takes it to the CS if its
	// below a specific level.
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

	// Method to compute the path to the nearest charging station from the robot's
	// current position.
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

		// Add a dynamic obstacle
		Map<Integer, Object> dynamicObstacles = new HashMap<>();
		int obstaclePosition = 31;
		dynamicObstacles.put(obstaclePosition, new Object());

		// Create the graph
		graph = new Graph(WAREHOUSE_WIDTH, WAREHOUSE_LENGTH, obstacles, dynamicObstacles);

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
				List<Integer> tempPath3 = computePathICA(target, csPositionToCsLoadingPosition(chargingStation), i);

				// Check if the robot has enough battery to reach the conveyor
				// Doubled the minimum necessary battery to account for possible deviations from
				// the path
				if (robot.getBatteryLevel() >= 2 * (tempPath1.size() + tempPath2.size() + tempPath3.size())) {
					// If the robot can reach the conveyor, return the robot and the paths
					return new Object[] { i, tempPath1 };
				}
				// If it doesn't, send the robot to charge
				else {
					abortToCharge(i);
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
	static List<Integer> computePathICA(int start, int end, int rIndex) {
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

		// Set the target of the current robot
		robots.get(rIndex).setTargetPosition(end);

		// If there is no other active robot, then return this one
		if (activeRobots.size() == 0) {

			return currentPath;
		}

		// If there are other active robots, continue the ICA

		// Add the waypoint of currentPath to the first chromosome if the robot is not
		// active
		if (!activeRobots.contains(rIndex)) {
			// Add the waypoint
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
			try {
				fitnesses.add(Fitness(chromosomes.get(chromosomeIndex), robotIndexes));
			} catch (Error err) {
				System.out.println("Mutation failed, chromosome: " + chromosomes.get(chromosomeIndex));
			}
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
			for (int j = 0; j < fitnesses.size(); j++) {
				if (j != goodChromIndex[0] && j != goodChromIndex[3]) {
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
				try {
					double newFitness = Fitness(child, robotIndexes);
					// True if some chromosome was replaced with the current child
					boolean replaced = false;

					// See if it can replace an old chromosome. The for loop starts with the
					// smallest fitness value
					for (int j = 0; j < MUTATIONS && !replaced; j++) {
						// If the new chromosome has lower fitness value than the old chromosome, insert
						// it
						if (newFitness < goodChrom[j]) {
							// Save the index of the chromosome that will be replaced (aka the biggest one)
							int replacedIndex = goodChromIndex[MUTATIONS - 1];

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

							// Set replaced to true
							replaced = true;

							// Wow I can't believe i just did that. I hate myself lol
						}
					}
				} catch (Error err) {
					if (children.indexOf(child) == 0) {
						System.err.println("Crossover failed, chromosome: " + child);
					} else {
						System.err.println("Mutation of children failed, chromosome: " + child);
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
				try {
					robots.get(robotIndexes.get(i))
							.setCurrentSelectedPath(pathFromWaypoint(robotIndexes.get(i), winning.get(i)));
				} catch (Error error) {
					System.out.println("Chromosome: " + winning.get(i));
					System.out.println(error.getMessage());
				}
			}
			// Otherwise, only replace the paths of the active robots
		} else {
			for (int i = 1; i < winning.size(); i++) {
				// Replace the robot's old path with a newly computed one
				try {
					robots.get(robotIndexes.get(i))
							.setCurrentSelectedPath(pathFromWaypoint(robotIndexes.get(i), winning.get(i)));
				} catch (Error error) {
					System.out.println("Chromosome: " + winning.get(i));
					System.out.println(error.getMessage());
				}
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
		int mutatedItem = rand.nextInt(chromosome.size());

		// Create the mutated chromosome
		List<Integer> mutation = new ArrayList<Integer>();
		for (int i = 0; i < chromosome.size(); i++) {
			// If the current item was the one chosen for mutation, we mutate it
			if (i == mutatedItem) {
				// Make sure the mutation isn't 0 or on an occupied position and that the
				// mutated value can still be in the grid
				int mutatedValue = rand.nextInt(2 * MAX_MUTATION + 1) - MAX_MUTATION;

				// Keep computing the mutation until all the conditions in the comment above are
				// satisfied
				while (mutatedValue == 0 || chromosome.get(i) + mutatedValue < 0
						|| chromosome.get(i) + mutatedValue >= WAREHOUSE_LENGTH * WAREHOUSE_WIDTH
						|| csPositions.contains(chromosome.get(i) + mutatedValue)
						|| cbPositions.contains(chromosome.get(i) + mutatedValue)
						|| shelfPositions.contains(chromosome.get(i) + mutatedValue)) {

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
	private static double Fitness(List<Integer> chromosome, List<Integer> robotIndexes) throws Error {
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
			try {
				List<Integer> newPath = pathFromWaypoint(robotIndexes.get(i), chromosome.get(i));
				// Add it to the list of paths
				chPaths.add(newPath);

				// Compute the length of the path and add it to the distance metric
				distanceMetrics += newPath.size();
			} catch (Error error) {
				throw new Error("Bad chromosome " + chromosome.get(i));
			}
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
					int average = (parent1.get(i) + parent2.get(i)) / 2;
					// If the average is an invalid position, we pick a spot next to the one pointed
					// to by the average
					if (csPositions.contains(average) || cbPositions.contains(average)
							|| shelfPositions.contains(average) || average < 0
							|| average >= WAREHOUSE_LENGTH * WAREHOUSE_WIDTH) {
						// Modify the position of the average if the new position meets the criteria
						// Move average to the right
						if (csPositions.contains(average + 1) || cbPositions.contains(average + 1)
								|| shelfPositions.contains(average + 1) || average + 1 < 0
								|| average + 1 >= WAREHOUSE_LENGTH * WAREHOUSE_WIDTH) {
							average += 1;
						}
						// Move average to the left
						else if (csPositions.contains(average - 1) || cbPositions.contains(average - 1)
								|| shelfPositions.contains(average - 1) || average - 1 < 0
								|| average - 1 >= WAREHOUSE_LENGTH * WAREHOUSE_WIDTH) {
							average -= 1;
						}
						// Move average up
						else if (csPositions.contains(average - 10) || cbPositions.contains(average - 10)
								|| shelfPositions.contains(average - 10) || average - 10 < 0
								|| average - 10 >= WAREHOUSE_LENGTH * WAREHOUSE_WIDTH) {
							average -= 10;
							// Move average down
						} else if (csPositions.contains(average + 10) || cbPositions.contains(average + 10)
								|| shelfPositions.contains(average + 10) || average + 10 < 0
								|| average + 10 >= WAREHOUSE_LENGTH * WAREHOUSE_WIDTH) {
							average += 10;
						} else {
							throw new Error("Cell " + average + " is blocked on all sides.");
						}
					}

					child.add(average);
			}
		}

		return child;
	}

	// TODO: Test
	private static List<Integer> pathFromWaypoint(int robotIndex, int waypoint) throws Error {
		List<Integer> newPath = new ArrayList<Integer>();
		// Select and add the path from the robot to the waypoint
		newPath.addAll(allPaths[robots.get(robotIndex).getCurrentPosition()][waypoint]);
		// Remove the last element because it repeats in the next path
		if (newPath.size() < 1) {
			throw new Error("Wrong waypoint " + waypoint);
		} else {
			newPath.remove(newPath.size() - 1);
			// Select and add the path from the waypoint to the robot's target
			int target = robots.get(robotIndex).getTargetPosition();
			newPath.addAll(allPaths[waypoint][target]);
		}

		return newPath;
	}

	// TODO: Test
	// Should check the positions of all robots, and recompute all paths if it
	// detects safety problems
	private static void safetyMonitor() {
		// Check every active robot's current position and see if it is in the robot's
		// current selected path
		for (int robotIndex = 0; robotIndex < robots.size(); robotIndex++) {
			// Current robot
			if (activeRobots.contains(robotIndex)) {
				Robot robot = robots.get(robotIndex);
				// If the robot's current position is not in the robot's path, recompute the
				// paths of all robots
				List<Integer> a = robot.getCurrentSelectedPath();
				int b = robot.getCurrentPosition();
				if (!a.contains(b)) {
					System.out.println("SAFETY MONITOR ACTIVATED. RECOMPUTING PATHS");
					// The function returns the path of the robot, but because it is already in the
					// activeRobots list its path is already changed by the algorithm so we don't
					// need to take more actions here
					computePathICA(robot.getCurrentPosition(), robot.getTargetPosition(), robotIndex);
				}
			}
		}
	}

	// Function called by a robot when it decides to abort its current mission and
	// wants to go charge
	// Returns the path for the robot to the nearest charging station
	// TODO: Test
	// TODO: Resume the mission after charging if robot has package
	public static void abortToCharge(Integer robotIndex) {
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
			// The closest charging station is in the bottom left corner of the grid
			csPosition = 90;
			// The robot is in the bottom right quarter of the grid
		} else {
			// The closest charging station is in the bottom right of the grid
			csPosition = 99;
		}

		// Return the path to the charging station and reroute the other robots
		int csLoadingPosition = csPositionToCsLoadingPosition(csPosition);
		List<Integer> csPath = computePathICA(position, csLoadingPosition, robotIndex);
		new ChargeTask(robots.get(robotIndex), csLoadingPosition, csPath);
	}

	/*
	 * We can't calculate a path directly into a charging station because it's seen
	 * as an obstacle
	 * To solve it we can convert charging station positions to loading position
	 */
	public static int csPositionToCsLoadingPosition(int csPosition) {
		if (csPosition == 0)
			return 10;
		else if (csPosition == 9)
			return 19;
		else if (csPosition == 90)
			return 80;
		else // csPosition = 99
			return 89;
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
