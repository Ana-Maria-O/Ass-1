package code;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Robot_path {
	private int currentPosition;
	private final List<Integer> globalPath;
	private final Map<Integer, List<Integer>> localPaths;
	private final Graph graph;
	private final int target;

	public Robot_path(int startPosition, List<Integer> globalPath, Map<Integer, List<Integer>> localPaths, Graph graph,
			int target) {
		this.currentPosition = startPosition;
		this.globalPath = globalPath;
		this.localPaths = localPaths;
		this.graph = graph;
		this.target = target;
	}

	public void recalculateLocalPath() {
		List<Integer> newLocalPath = graph.dijkstra(currentPosition, target);
		localPaths.put(currentPosition, newLocalPath);
	}

	public void moveToNext() {
		// if the robot has already reached the target
		if (currentPosition == target) {
			System.out.println("Robot has reached the target.");
			return;
		}

		// recalculate the local path from the current position
		recalculateLocalPath();

		// get the recalculated local path
		List<Integer> currentLocalPath = localPaths.get(currentPosition);

		if (currentLocalPath != null && !currentLocalPath.isEmpty()) {
			int nextPosition = (currentLocalPath.get(0) == currentPosition && currentLocalPath.size() > 1)
					? currentLocalPath.get(1)
					: currentLocalPath.get(0);

			if (nextPosition != currentPosition) {
				System.out.println("Robot moving from " + (currentPosition + 1) + " to " + (nextPosition + 1));
				currentPosition = nextPosition;
			} else {
				System.out.println("Robot is at a deadend.");
			}
		} else {
			System.out.println("No valid path available from current position.");
		}
	}

	public int getCurrentPosition() {
		return this.currentPosition;
	}

	public void moveTo(int newPosition) {
		System.out.println("Robot moving from " + (currentPosition + 1) + " to " + (newPosition + 1));
		this.currentPosition = newPosition;
	}

	public List<Integer> getGlobalPath() {
		return this.globalPath;
	}

	public Map<Integer, List<Integer>> getLocalPaths() {
		Map<Integer, List<Integer>> sortedLocalPaths = new LinkedHashMap<>();
		for (int cell : this.globalPath) {
			List<Integer> localPath = this.localPaths.get(cell);
			if (localPath != null) {
				sortedLocalPaths.put(cell, new ArrayList<>(localPath));
			}
		}
		return sortedLocalPaths;
	}

	public void setCurrentPosition(int currentPosition) {
		this.currentPosition = currentPosition;
	}

}
