package src.SmartWarehouse;

public class ConveyorBelt {

    String loadingPositionPacketRFID;
    Object[] robotLoadingPosition = new Object[1];
    boolean isStopped = true;

    public void preparePacketForFetching(String packetRFID) {
        isStopped = false;
        System.out.println("\u001B[32m" + "ConveyorBelt is on and moves packets" + "\u001B[0m");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        loadingPositionPacketRFID = packetRFID;
        isStopped = true;
        System.out.println("\u001B[32m" + "ConveyorBelt is stopped" + "\u001B[0m");
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
