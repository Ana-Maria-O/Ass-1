package code;

public class Node implements Comparable<Node> {
	private int vertex;
	private int cost;

	public Node(int vertex, int cost) {
		this.vertex = vertex;
		this.cost = cost;
	}

	public int getVertex() {
		return this.vertex;
	}

	public int getCost() {
		return this.cost;
	}

	@Override
	public int compareTo(Node other) {
		// https://www.baeldung.com/java-compareto
		return Integer.compare(this.cost, other.cost);
	}
}
