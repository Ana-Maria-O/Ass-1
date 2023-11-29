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

    public void fetchPacketFromConveyorBelt(ConveyorBelt conveyorBelt) throws NoPacketException, WrongPacketException {
        String actualFetchedPacketRFID = conveyorBelt.loadingPositionPacketRFID;
        if (actualFetchedPacketRFID == null) {
            System.out.println(this + " does not detect any packet at CB. Performing short wait");
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (conveyorBelt.loadingPositionPacketRFID == null) {
            System.out.println(this + " does still not detect any packet. Notifying CBC.");
            throw new NoPacketException();
        }
        if (!conveyorBelt.loadingPositionPacketRFID.equals(currentTargetPacketRFID)) {
            System.out.println(this + " is presented with the wrong packet at the conveyor belt");
            throw new WrongPacketException();
        }

        // Known values when fetching packets from the conveyor belt
        double FetchHeight = 1;
        double FetchArmExtension = 1;
        arm.fetchPackage(0, FetchHeight, FetchArmExtension, actualFetchedPacketRFID);
        if (!currentTargetPacketRFID.equals(actualFetchedPacketRFID)) {
            throw new Error("Wrong packet fetched at conveyor belt: " + currentTargetPacketRFID + " != " + actualFetchedPacketRFID);
        }
        if (weightSensor.getReading() <= 0)
            throw new Error("No packet registered by weight sensor");
        System.out.println(this + " weight sensor senses packet");

        conveyorBelt.loadingPositionPacketRFID = null;
        conveyorBelt.setObjectAtRobotLoadingPosition(null);
    }

    public void moveToLoadingPositionAtCB(ConveyorBelt conveyorBelt, Position loadingPosition) {
        moveToPosition(loadingPosition);
        Object[] loadingPositionForRobots = conveyorBelt.getRobotLoadingPosition();
        if (radar.hasFreeSpaceScan(loadingPositionForRobots)) {
            System.out.println(this + " radar reads that loading position can be moved to");
            conveyorBelt.setObjectAtRobotLoadingPosition(this);
        } else {
            System.out.println(this + " radar reads that loading position is occupied");
            System.out.println(this + " performing short wait for space to be freed");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (!radar.hasFreeSpaceScan(loadingPositionForRobots))
                throw new Error(this + ": loading position is still not empty despite wait");
            conveyorBelt.setObjectAtRobotLoadingPosition(this);
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
