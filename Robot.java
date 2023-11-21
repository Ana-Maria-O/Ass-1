import java.util.Scanner;

public class Robot {
    public static void main(String[] args) {
        // Initialize scanner
        Scanner scanner = new Scanner(System.in);
        // Initialize the robot's arm
        RobotArm arm = new RobotArm();

        //Add the shutdown hook
        Runtime.getRuntime().addShutdownHook(arm.new ShutdownHook());
        
        // Get packages for the robot
        while (true) {
            double rotation = scanner.nextDouble();
            double height = scanner.nextDouble();
            double length = scanner.nextDouble();
            String rfid = scanner.next();

            arm.fetchPackage(rotation, height, length, rfid);
            
        }

    }
}
