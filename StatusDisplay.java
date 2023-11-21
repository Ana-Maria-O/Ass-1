public class StatusDisplay {
    public static void main(String[] args) {
        display();
    }

    public static void display() {
        System.out.print(String.format("%-50s", "Current rotation (rad): " + RobotArm.rotation));
        System.out.println("Sensed rotation (rad): " + RobotArm.sensRotation);
        System.out.print(String.format("%-50s", "Current height (cm): " + RobotArm.height));
        System.out.println("Sensed height (cm): " + RobotArm.sensHeight);
        System.out.print(String.format("%-50s", "Current length (cm): " + RobotArm.length));
        System.out.println("Sensed length (cm): " + RobotArm.sensLength);
        System.out.print(String.format("%-50s", "Gripped: " + RobotArm.gripped));
        System.out.println("Sensed grip: " + RobotArm.sensGripped);
        System.out.print(String.format("%-50s", "Current RFID: " + RobotArm.currentRFID));
        System.out.println("Sensed RFID: " + RobotArm.sensCurrentRFID);
        System.out.println();
    }
}
