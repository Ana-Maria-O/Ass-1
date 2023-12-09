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

	public Graph(int gridWidth, int gridHeight, Set<Integer> obstacles) {
		this.gridWidth = gridWidth;
		this.gridHeight = gridHeight;
		this.obstacles = obstacles;
		adjList = new ArrayList<>(gridWidth * gridHeight);
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

	public boolean isObstacle(int cell) {
		return obstacles.contains(cell);
	}

	private void initializeGraph() {
		// initialize the adjacency list for all vertices in the graph.
		for (int i = 0; i < gridWidth * gridHeight; i++) {
			adjList.add(new ArrayList<>());
		}

		for (int i = 0; i < gridWidth * gridHeight; i++) {
			if (obstacles.contains(i))
				continue;

			// determine the row and column of the current cell in the grid.
			int row = i / gridWidth;
			int col = i % gridWidth; 

			// check and add an edge to the --right-- neighbor
			if (col < gridWidth - 1 && !obstacles.contains(i + 1)) {
				adjList.get(i).add(new Node(i + 1, 1));
			}

			// check and add an edge to the --bottom-- neighbor
			if (row < gridHeight - 1 && !obstacles.contains(i + gridWidth)) {
				adjList.get(i).add(new Node(i + gridWidth, 1));
			}

			// check and add an edge to the --top-- neighbor
			if (row > 0 && !obstacles.contains(i - gridWidth)) {
				adjList.get(i).add(new Node(i - gridWidth, 1));
			}

			// Check and add an edge to the --left-- neighbor
			if (col > 0 && !obstacles.contains(i - 1)) {
				adjList.get(i).add(new Node(i - 1, 1));
			}
		}
	}

	public List<Integer> dijkstra(int start, int end) {
		int numVertices = gridWidth * gridHeight;
		int[] distances = new int[numVertices];
		Arrays.fill(distances, Integer.MAX_VALUE);

		predecessors = new int[numVertices];
		Arrays.fill(predecessors, -1);
		distances[start] = 0;
		PriorityQueue<Node> queue = new PriorityQueue<>();
		queue.add(new Node(start, 0));
		while (!queue.isEmpty()) {
			Node current = queue.poll();
			for (Node neighbor : adjList.get(current.getVertex())) {
				int newDist = distances[current.getVertex()] + neighbor.getCost();
				if (newDist < distances[neighbor.getVertex()]) {
					distances[neighbor.getVertex()] = newDist;
					predecessors[neighbor.getVertex()] = current.getVertex();
					queue.add(new Node(neighbor.getVertex(), newDist));
				}
			}
		}

		return reconstructPath(end);
	}

	public Map<Integer, List<Integer>> computeOptimalLocalPaths(List<Integer> globalPath, int target) {
		Map<Integer, List<Integer>> optimalLocalPathsMap = new HashMap<>();

		for (int i = 0; i < globalPath.size(); i++) {
			int currentCell = globalPath.get(i);

			// for the last cell in the global path, the local path is the cell itself
			if (currentCell == target) {
				break;
			}

			// start the local path calculation from the next cell in the global path
			int nextCellIndex = i + 1;
			if (nextCellIndex < globalPath.size()) {
				int nextCell = globalPath.get(nextCellIndex);
				List<Integer> localPath = dijkstra(nextCell, target);
				optimalLocalPathsMap.put(currentCell, localPath);
			}
		}

		return optimalLocalPathsMap;
	}

	private List<Integer> reconstructPath(int end) {
		List<Integer> path = new ArrayList<>();
		for (int at = end; at != -1; at = predecessors[at]) {
			path.add(at);
		}
		Collections.reverse(path);
		return path;
	}
}
