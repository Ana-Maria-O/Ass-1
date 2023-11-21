public class Robot {
    public static void main(String[] args) {
        // Initialize the robot's arm
        RobotArm arm = new RobotArm();
        Runtime.getRuntime().addShutdownHook(arm.new ShutdownHook());

        // arm.safeState();
    }
}
