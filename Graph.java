package src.SmartWarehouse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

public class Graph {
	private int gridWidth;
	private int gridHeight;
	private List<List<Node>> adjList;
	private Set<Integer> obstacles;
	private int[] predecessors;

	// Constructor
	public Graph(int gridWidth, int gridHeight, Set<Integer> obstacles) {
		this.gridWidth = gridWidth;
		this.gridHeight = gridHeight;
		this.obstacles = obstacles;
		this.adjList = new ArrayList<>(gridWidth * gridHeight);
		initializeGraph();
	}

	public int getGridWidth() {
		return this.gridWidth;
	}

	public int getGridHeight() {
		return this.gridHeight;
	}

	public int getGridSize() {
		return gridWidth * gridHeight;
	}

	// Check if a given cell has an obstacle.
	public boolean isObstacle(int cell) {
		return obstacles.contains(cell);
	}

	// Add a new obstacle.
	public void addObstacle(int obstaclePosition) {
		this.obstacles.add(obstaclePosition);
	}

	// Initialize the graph, setting up adjacency lists for each cell.
	private void initializeGraph() {
		// Initialize the adjacency list for all vertices in the graph.
		for (int i = 0; i < gridWidth * gridHeight; i++) {
			adjList.add(new ArrayList<>());
		}

		// Iterate through each cell to set up edges considering obstacles.
		for (int i = 0; i < gridWidth * gridHeight; i++) {
			if (obstacles.contains(i))
				continue; // Skip if the cell is an obstacle.

			// Determine the row and column of the current cell.
			int row = i / gridWidth;
			int col = i % gridWidth;

			// Add edges to adjacent cells if they are not obstacles.
			if (col < gridWidth - 1 && !obstacles.contains(i + 1)) {
				adjList.get(i).add(new Node(i + 1, 1)); // Right neighbor.
			}
			if (row < gridHeight - 1 && !obstacles.contains(i + gridWidth)) {
				adjList.get(i).add(new Node(i + gridWidth, 1)); // Bottom neighbor.
			}
			if (row > 0 && !obstacles.contains(i - gridWidth)) {
				adjList.get(i).add(new Node(i - gridWidth, 1)); // Top neighbor.
			}
			if (col > 0 && !obstacles.contains(i - 1)) {
				adjList.get(i).add(new Node(i - 1, 1)); // Left neighbor.
			}
		}
	}

	// Dijkstra's algorithm to find the shortest path from start to end.
	public List<Integer> dijkstra(int start, int end) {
		int numVertices = gridWidth * gridHeight;
		int[] distances = new int[numVertices];
		Arrays.fill(distances, Integer.MAX_VALUE);

		predecessors = new int[numVertices];
		Arrays.fill(predecessors, -1);
		distances[start] = 0;
		PriorityQueue<Node> queue = new PriorityQueue<>();
		queue.add(new Node(start, 0));

		// Process each node in the queue.
		while (!queue.isEmpty()) {
			Node current = queue.poll();
			for (Node neighbor : this.adjList.get(current.getVertex())) {
				int newDist = distances[current.getVertex()] + neighbor.getCost();
				if (newDist < distances[neighbor.getVertex()]) {
					distances[neighbor.getVertex()] = newDist;
					predecessors[neighbor.getVertex()] = current.getVertex();
					queue.add(new Node(neighbor.getVertex(), newDist));
				}
			}
		}
		// Reconstruct and return the shortest path.
		return reconstructPath(end);
	}

	// Find the optimal local paths for each cell in the global path.
	public Map<Integer, List<Integer>> computeOptimalLocalPaths(List<Integer> globalPath, int target) {
		Map<Integer, List<Integer>> optimalLocalPathsMap = new HashMap<>();

		for (int i = 0; i < globalPath.size(); i++) {
			int currentCell = globalPath.get(i);

			// For the last cell, the local path is the cell itself.
			if (currentCell == target) {
				break;
			}

			// Calculate the local path from the next cell in the global path.
			int nextCellIndex = i + 1;
			if (nextCellIndex < globalPath.size()) {
				int nextCell = globalPath.get(nextCellIndex);
				List<Integer> localPath = dijkstra(nextCell, target);
				optimalLocalPathsMap.put(currentCell, localPath);
			}
		}

		return optimalLocalPathsMap;
	}

	// Method to reconstruct the path from the end cell to the start cell.
	private List<Integer> reconstructPath(int end) {
		List<Integer> path = new ArrayList<>();
		for (int at = end; at != -1; at = predecessors[at]) {
			path.add(at);
		}
		Collections.reverse(path); // Reverse the path to start-to-end order.
		return path;
	}
}
