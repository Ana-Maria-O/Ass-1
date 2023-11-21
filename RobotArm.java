public class RobotArm {
    // The current position and status of the robot arm
    public static double rotation = 0;
    public static double height = 0;
    public static double length = 0;
    public static boolean gripped = false;
    public static String currentRFID = "";

    // Detected position and status of the robot arm
    public static double sensRotation = 0;
    public static double sensHeight = 0;
    public static double sensLength = 0;
    public static boolean sensGripped = false;
    public static String sensCurrentRFID = "";

    // Robot storage location
    public static final double ROBOT_STORAGE_ROTATION = 10;
    public static final double ROBOT_STORAGE_HEIGHT = 0;
    public static final double ROBOT_STORAGE_LENGTH = 5;

    public static void main(String[] args) {

    }

    // Fetch the package with RFID rfid and at the position indicated by rotation,
    // height, length
    public void fetchPackage(double rotation, double height, double length, String rfid) {
        // Check the health of the motors and the sensors
        checkMotorHealth();
        checkSensorHealth();

        // Take a package from the shelf
        takePackageFromShelf(rotation, height, length, rfid);

        // Check if the read RFID of the package is the same as the RFID indicated by
        // rfid
        if (rfid.equals(MotorsAndSensors.RFIDReader.read())) {
            // Place the package in the robot storage
            placePackageInRobotStorage();
        } else {
            // Notify WMS
            WMS.notify("The RFID of the package is not the same as the expected RFID");
            // Bring to robot to a safe state
            safeState();
        }
    }

    // Take a package from a shelf at the position indicated by rotation, height,
    // length
    public void takePackageFromShelf(double rotation, double height, double length, String rfid) {
        // If the grip is turned on, ungrip
        ungripPackage();

        // Compute how many units of distance are needed for the robot arm to move to
        // the desired position
        double movement[] = computeNeededMovement(rotation, height, length);
        // Move the arm to the position indicated by rotation, height, length
        moveArm(movement[0], movement[1], movement[2]);
        // Take the package
        gripPackage(rfid);
    }

    // Place a package in the robot storage
    public void placePackageInRobotStorage() {
        // Compute how many units of distance are needed for the robot storage to move
        // to the storage
        double movement[] = computeNeededMovement(ROBOT_STORAGE_ROTATION, ROBOT_STORAGE_HEIGHT, ROBOT_STORAGE_LENGTH);
        // Move the arm to the position indicated by rotation, height, length
        moveArm(movement[0], movement[1], movement[2]);
        // Ungrip the package
        ungripPackage();
    }

    public double[] computeNeededMovement(double rotation, double height, double length) {
        // Get the current position of the robot arm
        double position[] = getArmPosition();

        // Rotation radians
        double r = rotation - position[0];
        // Height distance
        double h = height - position[1];
        // Length distance
        double l = length - position[2];

        return new double[] { r, h, l };
    }

    // Move the arm to the position indicated by rotation, height, length
    public void moveArm(double rotation, double height, double length) {
        // Rotate the arm
        MotorsAndSensors.rotationMotor.rotateArm(rotation);
        // Extend the arm vertically
        MotorsAndSensors.verticalMotor.moveArm(height);
        // Extend the arm horizontally
        MotorsAndSensors.horizontalMotor.moveArm(length);

        // Get the current position of the robot arm
        double position[] = getArmPosition();

        // Check if the sensor values and the expected values are the same
        checkSensorMotorValues(position[0], rotation, "rotation");
        checkSensorMotorValues(position[1], height, "height");
        checkSensorMotorValues(position[2], length, "length");
    }

    // Grip a package
    public void gripPackage(String rfid) {
        // If the grip is gripping, ungrip
        if (MotorsAndSensors.gripSensor.read() == true) {
            MotorsAndSensors.gripMotor.ungrip();
        }

        // Grip the package
        MotorsAndSensors.gripMotor.grip(rfid);

        // Check if the grip sensor value and its expected value is the same
        checkSensorMotorValues(MotorsAndSensors.gripSensor.read(), true);
        // Check if the read RFID of the package is the same as the expected RFID
        checkSensorMotorValues(MotorsAndSensors.RFIDReader.read(), rfid);
    }

    // Ungrip a package
    public void ungripPackage() {
        // Only ungrip if the grip is already gripping
        if (MotorsAndSensors.gripSensor.read()) {
            // Ungrip the package
            MotorsAndSensors.gripMotor.ungrip();
        }

        // Check if the grip sensor works
        checkSensorMotorValues(MotorsAndSensors.gripSensor.read(), false);
        // Check if the RFID reader does not read an RFID
        checkSensorMotorValues(MotorsAndSensors.RFIDReader.read(), "");
    }

    // Get the current position of the robot arm
    public double[] getArmPosition() {
        // Get the rotation position
        double r = MotorsAndSensors.rotationSensor.read();
        // Get the height position
        double h = MotorsAndSensors.verticalSensor.read();
        // Get the length position
        double l = MotorsAndSensors.horizontalSensor.read();

        return new double[] { r, h, l };
    }

    // Notify WMS and send the robot to a safe state if the sensors are not healthy
    public void checkSensorHealth() {
        if (!MotorsAndSensors.RFIDReader.health() || !MotorsAndSensors.gripSensor.health()
                || !MotorsAndSensors.rotationSensor.health() || !MotorsAndSensors.verticalSensor.health()
                || !MotorsAndSensors.horizontalSensor.health()) {
            // Notify WMS
            WMS.notify("The sensors are not healthy");
            // Bring to robot to a safe state
            safeState();
        }
    }

    // Notify WMS and send the robot to a safe state if the motors are not healthy
    public void checkMotorHealth() {
        if (!MotorsAndSensors.rotationMotor.health() || !MotorsAndSensors.verticalMotor.health()
                || !MotorsAndSensors.horizontalMotor.health() || !MotorsAndSensors.gripMotor.health()) {
            // Notify WMS
            WMS.notify("The motors are not healthy");
            // Bring to robot to a safe state
            safeState();
        }
    }

    // Check if the values of the sensor values are the same as the expected values
    // (integer values)
    public void checkSensorMotorValues(double sensValue, double expValue, String sensor) {
        // Notify wms if they don't match
        if (sensValue!= expValue) {
            // Notify WMS
            WMS.notify("The " + sensor + " sensor value does not match the expected value");
            // Bring to robot to a safe state
            safeState();
        }
    }

    // Check if the values of the sensor values are the same as the expected values
    // (boolean values)
    public void checkSensorMotorValues(boolean sensValue, boolean expValue) {
        // Notify wms if they don't match
        if (sensValue!= expValue) {
            // Notify WMS
            WMS.notify("The grip sensor value does not match the expected value");
            // Bring to robot to a safe state
            safeState();
        }
    }

    // Check if the values of the sensor values are the same as the expected values
    // (String values)
    public void checkSensorMotorValues(String sensValue, String expValue) {
        // Notify wms if they don't match
        if (!sensValue.equals(expValue)) {
            // Notify WMS
            WMS.notify("The read RFID value does not match the expected value");
            // Bring to robot to a safe state
            safeState();
        }
    }

    // Bring to robot to a safe state
    public void safeState() {

        System.out.println("Robot stopping...");
        // Stop the robot
        System.exit(0);

    }

    // Shutdown hook when the robot is shutting down
    class ShutdownHook extends Thread
    {
        public void run()
        {
            System.out.println("Robot is in a safe state.");
        }
    }
}