package src.SmartWarehouse;

import java.util.Scanner;

public class Robot {
    Packet[] storageSpace = new Packet[1];
    WeightSensor weightSensor = new WeightSensor(true, storageSpace);
    Radar radar = new Radar(true);
    RobotArm arm = new RobotArm(storageSpace);
    Position position = new Position(0, 0);
    String robotID;
    String currentTargetPacketRFID;

    public Robot(String robotID) {
        this.robotID = robotID;
    }

    @Override
    public String toString() {
        return robotID;
    }

    public void moveToPosition(Position position) {
        this.position = position;
    }

    public void enqueueForFetching(CBC conveyorBeltController, String packetRFID) {
        currentTargetPacketRFID = packetRFID;
        System.out.println(this + " enqueues for fetching packet " + packetRFID + " at CBC");
        conveyorBeltController.enqueueForFetching(this, packetRFID);
    }

    public void fetchPacketFromConveyorBelt(ConveyorBelt conveyorBelt) {
        // Known values when fetching packets from the conveyor belt
        double FetchHeight = 1;
        double FetchArmExtension = 1;
        String actualFetchedPacketRFID = conveyorBelt.loadingPositionPacketRFID;
        arm.fetchPackage(0, FetchHeight, FetchArmExtension, actualFetchedPacketRFID);
        if (!currentTargetPacketRFID.equals(actualFetchedPacketRFID)) {
            throw new Error("Wrong packet fetched at conveyor belt: " + currentTargetPacketRFID + " != " + actualFetchedPacketRFID);
        }
        if (weightSensor.getReading() <= 0)
            throw new Error("No packet registered by weight sensor");
        System.out.println(this + " weight sensor senses packet");

        conveyorBelt.setObjectAtRobotLoadingPosition(null);
    }

    public void moveToLoadingPositionAtCB(ConveyorBelt conveyorBelt, Position loadingPosition) {
        moveToPosition(loadingPosition);
        Object[] loadingPositionForRobots = conveyorBelt.getRobotLoadingPosition();
        if (radar.hasFreeSpaceScan(loadingPositionForRobots)) {
            System.out.println(this + " radar readings that loading position can be moved to");
            conveyorBelt.setObjectAtRobotLoadingPosition(this);
        } else {
            System.out.println(this + " radar readings that loading position is occupied");
            // handle occupied loading position case
        }
    }

    public static void main(String[] args) {
        Robot robot = new Robot("R1");
        Scanner scanner = new Scanner(System.in);

        // Add the shutdown hook
        Runtime.getRuntime().addShutdownHook(robot.arm.new ShutdownHook());

        // Get packages for the robot
        while (true) {
            double rotation = scanner.nextDouble();
            double height = scanner.nextDouble();
            double length = scanner.nextDouble();
            String rfid = scanner.next();

            robot.arm.fetchPackage(rotation, height, length, rfid);
        }
    }
}
