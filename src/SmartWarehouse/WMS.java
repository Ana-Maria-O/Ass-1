package src.SmartWarehouse;
public class WMS {
    public static void notify(String message) {
        System.out.println(message);
    }

    public static void main(String[] args) {
        CBC conveyorBeltController = new CBC();
        Robot robot = new Robot("R#1");
        Robot robot2 = new Robot("R#2");
        String packetRFID = "P#1";

        robot.enqueueForFetching(conveyorBeltController, packetRFID);
    }
}
