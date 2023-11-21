public class StatusDisplay {
    public static void main(String[] args) {
        Thread displayThread = new Thread(new Runnable() {
            @Override
            public void run() {
                double previousRotation = RobotArm.rotation;
                double previousHeight = RobotArm.height;
                double previousLength = RobotArm.length;
                boolean previousGrip = RobotArm.gripped;
                String previousRFID = RobotArm.currentRFID;
                double previousSensRotation = RobotArm.sensRotation;
                double previousSensHeight = RobotArm.sensHeight;
                double previousSensLength = RobotArm.sensLength;
                boolean previousSensGrip = RobotArm.sensGripped;
                String previousSensCurrentRFID = RobotArm.sensCurrentRFID;

                display();

                while (true) {
                    // Check for changes in variables
                    if (RobotArm.rotation != previousRotation || RobotArm.height != previousHeight
                            || RobotArm.length != previousLength || RobotArm.gripped != previousGrip
                            || RobotArm.currentRFID != previousRFID ||
                            RobotArm.sensRotation != previousSensRotation || RobotArm.sensHeight != previousSensHeight
                            ||
                            RobotArm.sensLength != previousSensLength || RobotArm.sensGripped != previousSensGrip
                            || RobotArm.sensCurrentRFID != previousSensCurrentRFID) {
                        display();
                    }

                }
            }
        });

        displayThread.start();
    }

    private static void display() {
        System.out.print(String.format("%-40s", "Current rotation (rad): " + RobotArm.rotation));
        System.out.println("Sensed rotation (rad): " + RobotArm.sensRotation);
        System.out.print(String.format("%-40s", "Current height (cm): " + RobotArm.height));
        System.out.println("Sensed height (cm): " + RobotArm.sensHeight);
        System.out.print(String.format("%-40s", "Current length (cm): " + RobotArm.length));
        System.out.println("Sensed length (cm): " + RobotArm.sensLength);
        System.out.print(String.format("%-40s", "Gripped: " + RobotArm.gripped));
        System.out.println("Sensed grip: " + RobotArm.sensGripped);
        System.out.print(String.format("%-40s", "Current RFID: " + RobotArm.currentRFID));
        System.out.println("Sensed RFID: " + RobotArm.sensCurrentRFID);
    }
}
