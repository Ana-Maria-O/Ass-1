package src.SmartWarehouse;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import src.SmartWarehouse.robotcomponents.RobotArm;

public class StatusDisplay {
    public static void main(String[] args) {
        display(new RobotArm(new Packet[1]));
    }

    public static void display(RobotArm robotArm) {
        System.out.print(String.format("%-50s", "Current rotation (rad): " + robotArm.rotation));
        System.out.println("Sensed rotation (rad): " + robotArm.sensRotation);
        System.out.print(String.format("%-50s", "Current height (cm): " + robotArm.height));
        System.out.println("Sensed height (cm): " + robotArm.sensHeight);
        System.out.print(String.format("%-50s", "Current length (cm): " + robotArm.length));
        System.out.println("Sensed length (cm): " + robotArm.sensLength);
        System.out.print(String.format("%-50s", "Gripped: " + robotArm.gripped));
        System.out.println("Sensed grip: " + robotArm.sensGripped);
        System.out.print(String.format("%-50s", "Current RFID: " + robotArm.currentRFID));
        System.out.println("Sensed RFID: " + robotArm.sensCurrentRFID);
        System.out.println();
    }

    public static void printGraph(Graph graph, List<Robot> robots) {
    	Set<Integer> robotPositions = new HashSet<>();
    	Set<Integer> robotTargets = new HashSet<>();
    	Set<Integer> robotPathsVerticies = new HashSet<>();
    	if (robots != null) {
    		for (Robot robot : robots) {
    			robotPositions.add(robot.getCurrentPosition());
    			robotTargets.add(robot.getTargetPosition());
    			robotPathsVerticies.addAll(robot.getCurrentSelectedPath());
    		}
    	}
    	System.out.println();
    	for (int y = 0; y < graph.getGridHeight(); y++) {
    		for (int x = 0; x < graph.getGridWidth(); x++) {
    			int vertexNum = graph.pointToVertexNum(new Point(x, y));
    			if (robotPositions.contains(vertexNum)) {
    				System.out.print("\033[92m" + "R" + "\u001B[0m");
    			} else if (graph.dynamicObstacles.containsKey(vertexNum)) {
    				System.out.print("\033[40m" + "X" + "\u001B[0m");
    			} else if (graph.isObstacle(vertexNum)) {
    				System.out.print("\033[91m" + "X" + "\u001B[0m");
    			} else if (robotTargets.contains(vertexNum)) {
    				System.out.print("\033[93m" + "T" + "\u001B[0m");
    			} else if (robotPathsVerticies.contains(vertexNum)) {
    				System.out.print("\033[94m" + "*" + "\u001B[0m");
    			} else {
    				System.out.print("*");
    			}
    		}
    		System.out.println();
    	}
    }
}
