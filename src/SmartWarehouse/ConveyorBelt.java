package src.SmartWarehouse;

public class ConveyorBelt {

    String loadingPositionPacketRFID;
    Object[] robotLoadingPosition = new Object[1];

    public void preparePacketForFetching(String packetRFID) {
        loadingPositionPacketRFID = packetRFID;
    }

    public Object[] getRobotLoadingPosition() {
        return robotLoadingPosition;
    }

    public void setObjectAtRobotLoadingPosition(Object objectAtLoadingPosition) {
        this.robotLoadingPosition[0] = objectAtLoadingPosition;
        if (objectAtLoadingPosition == null)
            System.out.println("\u001B[32m" + "ConveyorBelt loading position got freed\u001B[0m");
        else
            System.out.println("\u001B[32m" + "ConveyorBelt loading position got occupied\u001B[0m");
    }
}
