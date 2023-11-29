package src.SmartWarehouse;
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
}
