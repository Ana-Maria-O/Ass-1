package src.SmartWarehouse;

import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class CBC {
    private ConveyorBelt conveyorBelt = new ConveyorBelt();
    private Deque<Robot> waitingQueue = new ConcurrentLinkedDeque<>();
    private Deque<String> packetRFIDFetchingQueue = new ConcurrentLinkedDeque<>();
    private Lock loadingPositionLock = new ReentrantLock();
    private Position loadingPosition = new Position(0, 0);

    public void enqueueForFetching(Robot robot, String packetRFID) {
        waitingQueue.addLast(robot);
        packetRFIDFetchingQueue.addLast(packetRFID);
        logCBCStatus("Robot " + robot + " enqueued for fetching packet " + packetRFID);
        robotPacketFetchSequence();
    }

    private void robotPacketFetchSequence() {
        loadingPositionLock.lock();

        Robot robot = waitingQueue.removeFirst();
        String packetRFID = packetRFIDFetchingQueue.removeFirst();
        logCBCStatus("Packet fetching sequence initiated for robot " + robot + " and packet " + packetRFID);

        // signal robot loading position is vacant
        logCBCStatus("Signal " + robot + " to move to loading position");
        robot.moveToLoadingPositionAtCB(conveyorBelt, loadingPosition);
        logCBCStatus(robot + " has signaled readiness for fetching packet");
        // await robot to signal it is ready to fetch packet

        // prepare packet for robot
        logCBCStatus("signal CB to prepare packet " + packetRFID);
        conveyorBelt.preparePacketForFetching(null);
        logCBCStatus("CB signaled packet is transported to loading position");
        // await signal that packet is transported to the loading position

        // tell robot to fetch packet
        logCBCStatus(robot + " is signaled to fetch packet from conveyor belt");
        try {
            robot.fetchPacketFromConveyorBelt(conveyorBelt);
        } catch (NoPacketException | WrongPacketException e) {
            logCBCStatus("again signals CB to prepare packet " + packetRFID);
        conveyorBelt.preparePacketForFetching(packetRFID);
            logCBCStatus("CB signaled packet is transported to loading position");
            try {
                robot.fetchPacketFromConveyorBelt(conveyorBelt);
            } catch (NoPacketException | WrongPacketException e1) {
                throw new Error("Despite retry, correct packet does not appear to robot at CB.");
            }
        }
        logCBCStatus(robot + " acknowledges packet is fetched. Loading position should be free again");
        // robot acknowledges packet is fetched and moves away

        loadingPositionLock.unlock();
    }

    private void logCBCStatus(String message) {
        System.out.println("\u001B[35mConveyorBeltController: " + message + "\u001B[0m");
    }
}
