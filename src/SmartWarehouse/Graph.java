package src.SmartWarehouse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

// Graph represents a grid-based graph structure for the WMS environment.
public class Graph {
	// Dimensions of the grid.
	private int gridWidth;
	private int gridHeight;
	private int numVertices;
	private int[][] adjacencyMatrix;

	// Adjacency list to represent the graph. Each node represents a position in the
	// grid.
	private List<List<Node>> adjList;

	// A set of positions that are considered obstacles and cannot be traversed.
	private Set<Integer> obstacles; // static obstacles
	Map<Integer, Object> dynamicObstacles = new HashMap<>(); // VertexNum and corresponding dynamic obstacle

	// Constructor to initialize the graph with dimensions and obstacles.
	public Graph(int gridWidth, int gridHeight, Set<Integer> obstacles, Map<Integer, Object> dynamicObstacles) {
		this.gridWidth = gridWidth;
		this.gridHeight = gridHeight;
		this.obstacles = obstacles;
		this.dynamicObstacles = dynamicObstacles;
		this.numVertices = gridWidth * gridHeight; // Initialize numVertices here.
		this.adjList = new ArrayList<>(numVertices);
		this.adjacencyMatrix = new int[numVertices][numVertices]; // Initialize adjacencyMatrix here.
		initializeGraph();
	} 
	
	// New Dijkstra's algorithm implementation
	public List<Integer> dijkstra(int src, int target) {
	    int[] distances = new int[numVertices];
	    boolean[] visited = new boolean[numVertices];
	    int[] predecessors = new int[numVertices];

	    Arrays.fill(distances, Integer.MAX_VALUE);
	    Arrays.fill(predecessors, -1);

	    distances[src] = 0;

	    for (int count = 0; count < numVertices - 1; count++) {
	        int u = minDistance(distances, visited);

	        // If the target node is picked as the minimum, its shortest path is finalized.
	        if (u == target) {
	            break;
	        }

	        visited[u] = true;

	        for (int v = 0; v < numVertices; v++) {
	            if (!visited[v] && adjacencyMatrix[u][v] != Integer.MAX_VALUE &&
	                distances[u] + adjacencyMatrix[u][v] < distances[v]) {
	                distances[v] = distances[u] + adjacencyMatrix[u][v];
	                predecessors[v] = u;
	            }
	        }
	    }

	    return reconstructPath(src, target, predecessors);
	}

	private int minDistance(int[] distances, boolean[] visited) {
	    int min = Integer.MAX_VALUE, minIndex = -1;

	    for (int i = 0; i < numVertices; i++) {
	        if (!visited[i] && distances[i] <= min) {
	            min = distances[i];
	            minIndex = i;
	        }
	    }

	    return minIndex;
	}

	private List<Integer> reconstructPath(int src, int target, int[] predecessors) {
	    List<Integer> path = new ArrayList<>();
	    int step = target;
	    while (step != -1) {
	        path.add(step);
	        step = predecessors[step];
	        if (step == src) {
	            break;
	        }
	    }

	    // If the target was never reached, lets return an empty list.
	    if (step != src) {
	        return new ArrayList<>();
	    }

	    Collections.reverse(path);
	    return path;
	}


	private void initializeGraph() {
		int gridSize = gridWidth * gridHeight;
		adjacencyMatrix = new int[gridSize][gridSize];

		for (int[] row : adjacencyMatrix) {
			Arrays.fill(row, Integer.MAX_VALUE);
		}

		// Fill in the adjacency matrix with appropriate edge weights.
		// Assuming a cost of 1 for all direct edges between adjacent nodes.
		for (int i = 0; i < gridSize; i++) {
			if (obstacles.contains(i))
				continue;

			int row = i / gridWidth;
			int col = i % gridWidth;

			if (col < gridWidth - 1 && !obstacles.contains(i + 1)) {
				adjacencyMatrix[i][i + 1] = 1;
				adjacencyMatrix[i + 1][i] = 1; // For undirected graph
			}
			if (row < gridHeight - 1 && !obstacles.contains(i + gridWidth)) {
				adjacencyMatrix[i][i + gridWidth] = 1;
				adjacencyMatrix[i + gridWidth][i] = 1; // For undirected graph
			}
			// No need for left and top neighbors as it's undirected, we've already covered
			// them.
		}
	}


	// Check if a given cell has an obstacle.
	public boolean isObstacle(int cell) {
		return obstacles.contains(cell);
	}

	// Add a new obstacle.
	public void addObstacle(int obstaclePosition) {
		this.obstacles.add(obstaclePosition);
	}
	
	public List<List<Integer>> computeAllPathsToTarget(int target) {
	    List<List<Integer>> allPaths = new ArrayList<>();
	    int gridSize = getGridWidth() * getGridHeight();

	    for (int i = 0; i < gridSize; i++) {
	        // Skip if the starting position is an obstacle
	        if (!obstacles.contains(i)) {
	            List<Integer> path = dijkstra(i, target);
	            allPaths.add(path);

	            // Debugging.
//	            System.out.println("Path from position " + (i + 1) + " to target " + (target + 1) + ": " + path);
	        } else {
//	            // Same.
//	            System.out.println("Position " + (i + 1) + " is an obstacle.");
	            allPaths.add(new ArrayList<>()); // Add an empty path for obstacle positions
	        }
	    }
	    return allPaths;
	}


	public HashMap<String, Boolean> getMoveOptions(Point point) {
		HashMap<String, Boolean> direction_map = new HashMap<>();
		direction_map.put("left", isFree(new Point(point.x - 1, point.y)));
		direction_map.put("right", isFree(new Point(point.x + 1, point.y)));
		direction_map.put("up", isFree(new Point(point.x, point.y - 1)));
		direction_map.put("down", isFree(new Point(point.x, point.y + 1)));
		return direction_map;
	}

	private Boolean isFree(Point point) {
		if (point.x >= 0 && point.x < gridWidth && point.y >= 0 && point.y < gridHeight) {
			int vertexNum = pointToVertexNum(point);
			return !isObstacle(vertexNum);
		}
		return false;
	}

	// Convert a vertex ID to a Point object
	public Point vertexToPoint(int vertex) {
		int x = vertex % gridWidth;
		int y = vertex / gridWidth;
		return new Point(x, y);
	}

	// Convert a Point object to a vertex ID
	public int pointToVertexNum(Point point) {
		return point.y * gridWidth + point.x;
	}

	public List<List<Node>> getAdjList() {
		return adjList;
	}

	public void setAdjList(List<List<Node>> adjList) {
		this.adjList = adjList;
	}

	public Set<Integer> getObstacles() {
		return obstacles;
	}

	public void setObstacles(Set<Integer> obstacles) {
		this.obstacles = obstacles;
	}

	public void setGridWidth(int gridWidth) {
		this.gridWidth = gridWidth;
	}

	public void setGridHeight(int gridHeight) {
		this.gridHeight = gridHeight;
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
}