package src.SmartWarehouse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

// Graph represents a grid-based graph structure for the WMS environment.
public class Graph {
    // Dimensions of the grid.
    private int gridWidth;
    private int gridHeight;

    // Adjacency list to represent the graph. Each node represents a position in the grid.
    private List<List<Node>> adjList;

    // A set of positions that are considered obstacles and cannot be traversed.
    private Set<Integer> obstacles;

    // Constructor to initialize the graph with dimensions and obstacles.
    public Graph(int gridWidth, int gridHeight, Set<Integer> obstacles) {
        this.gridWidth = gridWidth;
        this.gridHeight = gridHeight;
        this.obstacles = obstacles;
        this.adjList = new ArrayList<>(gridWidth * gridHeight);
        initializeGraph();
    }

	// Initialize the graph, setting up adjacency lists for each cell.
	private void initializeGraph() {
		// Initialize the adjacency list for all vertices in the graph.
		for (int i = 0; i < this.gridWidth * this.gridHeight; i++) {
			this.adjList.add(new ArrayList<>());
		}

		// Iterate through each cell to set up edges considering obstacles.
		for (int i = 0; i < this.gridWidth *  this.gridHeight; i++) {
			if (obstacles.contains(i))
				continue; // Skip if the cell is an obstacle.

			// Determine the row and column of the current cell.
			int row = i / this.gridWidth;
			int col = i % this.gridWidth;

			// Add edges to adjacent cells if they are not obstacles.
			if (col < this.gridWidth - 1 && !obstacles.contains(i + 1)) {
				this.adjList.get(i).add(new Node(i + 1, 1)); // Right neighbor.
			}
			if (row <  this.gridHeight - 1 && !obstacles.contains(i + this.gridWidth)) {
				this.adjList.get(i).add(new Node(i + this.gridWidth, 1)); // Bottom neighbor.
			}
			if (row > 0 && !obstacles.contains(i - this.gridWidth)) {
				this.adjList.get(i).add(new Node(i - this.gridWidth, 1)); // Top neighbor.
			}
			if (col > 0 && !obstacles.contains(i - 1)) {
				this.adjList.get(i).add(new Node(i - 1, 1)); // Left neighbor.
			}
		}
	}

    // Dijkstra's algorithm to find the shortest path between two nodes.
    public List<Integer> dijkstra(int start, int end) {
        int numVertices = this.gridWidth *  this.gridHeight;
        int[] distances = new int[numVertices];
        Arrays.fill(distances, Integer.MAX_VALUE);
        int[] predecessors = new int[numVertices];
        Arrays.fill(predecessors, -1);

        distances[start] = 0;
        PriorityQueue<Node> queue = new PriorityQueue<>();
        queue.add(new Node(start, 0));

		// Process each node in the queue.
        while (!queue.isEmpty()) {
            Node current = queue.poll();

            // Update distances and predecessors for each neighbor.
            for (Node neighbor : this.adjList.get(current.getVertex())) {
                int newDist = distances[current.getVertex()] + neighbor.getCost();
                if (newDist < distances[neighbor.getVertex()]) {
                    distances[neighbor.getVertex()] = newDist;
                    predecessors[neighbor.getVertex()] = current.getVertex();
                    queue.add(new Node(neighbor.getVertex(), newDist));
                }
            }
        }

        // Reconstruct the shortest path from end to start.
        return reconstructPath(end, predecessors);
    }

    // Reconstruct the path from the end node to the start node.
    private List<Integer> reconstructPath(int end, int[] predecessors) {
        List<Integer> path = new ArrayList<>();
        // Build the path by following the predecessors array.
        for (int at = end; at != -1; at = predecessors[at]) {
            path.add(at);
        }
        Collections.reverse(path); // Reverse to get the path from start to end.
        return path;
    }

    // Compute all paths to a target for each node in the graph.
    public Map<Integer, List<List<Integer>>> computeAllPathsToTarget(int target) {
        Map<Integer, List<List<Integer>>> allPathsMap = new HashMap<>();
        // Iterate over all nodes, avoiding obstacles.
        for (int i = 0; i < this.gridWidth * this.gridHeight; i++) {
            if (!obstacles.contains(i)) {
                List<List<Integer>> paths = new ArrayList<>();
                // Find all paths from node 'i' to the target.
                findAllPaths(i, target, paths, new ArrayList<>(), new HashSet<>());
                allPathsMap.put(i, paths);
            }
        }
        return allPathsMap;
    }

    // Recursive method to find all paths from a current node to the target.
    private void findAllPaths(int current, int target, List<List<Integer>> paths, List<Integer> currentPath, Set<Integer> visited) {
        visited.add(current);
        currentPath.add(current);

        // Base case: if current is the target, add the path to the list.
        if (current == target) {
            paths.add(new ArrayList<>(currentPath));
        } else {
            // Explore all unvisited neighbors.
            for (Node neighbor : this.adjList.get(current)) {
                if (!visited.contains(neighbor.getVertex())) {
                    findAllPaths(neighbor.getVertex(), target, paths, currentPath, visited);
                }
            }
        }

        // Remove the current node from the path and mark it as unvisited.
        currentPath.remove(currentPath.size() - 1);
        visited.remove(current);
    }
}
