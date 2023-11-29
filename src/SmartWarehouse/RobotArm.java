package src.SmartWarehouse;
public class RobotArm {
    Packet storageLocation[];
    // The current position and status of the robot arm
    public double rotation = 0;
    public double height = 0;
    public double length = 0;
    public boolean gripped = false;
    public String currentRFID = "";

    // Detected position and status of the robot arm
    public double sensRotation = 0;
    public double sensHeight = 0;
    public double sensLength = 0;
    public boolean sensGripped = false;
    public String sensCurrentRFID = "";

    // Robot storage location
    public final double ROBOT_STORAGE_ROTATION = 10;
    public final double ROBOT_STORAGE_HEIGHT = 0;
    public final double ROBOT_STORAGE_LENGTH = 5;

    // Instantiate the motors and sensors
    public MotorsAndSensors motorsAndSensors = new MotorsAndSensors(this);
    public MotorsAndSensors.rotationMotor rotationMotor = motorsAndSensors.new rotationMotor();
    public MotorsAndSensors.verticalMotor verticalMotor = motorsAndSensors.new verticalMotor();
    public MotorsAndSensors.horizontalMotor horizontalMotor = motorsAndSensors.new horizontalMotor();
    public MotorsAndSensors.gripMotor gripMotor = motorsAndSensors.new gripMotor();
    public MotorsAndSensors.rotationSensor rotationSensor = motorsAndSensors.new rotationSensor();
    public MotorsAndSensors.verticalSensor verticalSensor = motorsAndSensors.new verticalSensor();
    public MotorsAndSensors.horizontalSensor horizontalSensor = motorsAndSensors.new horizontalSensor();
    public MotorsAndSensors.gripSensor gripSensor = motorsAndSensors.new gripSensor();
    public MotorsAndSensors.RFIDReader rfidReader = motorsAndSensors.new RFIDReader();

    public RobotArm(Packet[] storageLocation) {
        this.storageLocation = storageLocation;
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
        if (rfid.equals(rfidReader.read())) {
            // Retract the arm horizontally
            horizontalMotor.moveArm(0);
            // Place the package in the robot storage
            placePackageInRobotStorage(rfid);
        } else {
            // Notify WMS
            WMS.notify("The RFID of the package is not the same as the expected RFID");
            // Bring to robot to a safe state
            safeState();
        }

        System.out.println("Package " + rfid + " fetched successfully");
    }

    // Take a package from a shelf at the position indicated by rotation, height,
    // length
    public void takePackageFromShelf(double rotation, double height, double length, String rfid) {
        // If the grip is turned on, ungrip
        ungripPackage();
        // Move the arm to the position indicated by rotation, height, length
        moveArm(rotation, height, length);
        // Take the package
        gripPackage(rfid);
    }

    // Place a package in the robot storage
    public void placePackageInRobotStorage(String rfid) {
        // Move the arm to the position of the storage
        moveArm(ROBOT_STORAGE_ROTATION, ROBOT_STORAGE_HEIGHT, ROBOT_STORAGE_LENGTH);
        // Ungrip the package
        ungripPackage();
        storageLocation[0] = new Packet(rfid, 1);
        System.out.println(rfid + " placed in robot's storage space");
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
        // Compute how many units of distance are needed for the robot storage to move
        // to the storage
        double movement[] = computeNeededMovement(rotation, height, length);

        // Rotate the arm
        rotationMotor.rotateArm(movement[0]);
        // Extend the arm vertically
        verticalMotor.moveArm(movement[1]);
        // Extend the arm horizontally
        horizontalMotor.moveArm(movement[2]);

        // Get the current position of the robot arm
        double position[] = getArmPosition();

        // Display
        StatusDisplay.display(this);

        // Check if the sensor values and the expected values are the same
        checkSensorMotorValues(position[0], rotation, "rotation");
        checkSensorMotorValues(position[1], height, "height");
        checkSensorMotorValues(position[2], length, "length");
    }

    // Grip a package
    public void gripPackage(String rfid) {
        // If the grip is gripping, ungrip
        if (gripSensor.read() == true) {
            gripMotor.ungrip();
        }

        // Grip the package
        gripMotor.grip(rfid);

        // Display
        StatusDisplay.display(this);

        // Check if the grip sensor value and its expected value is the same
        checkSensorMotorValues(gripSensor.read(), true);
        // Check if the read RFID of the package is the same as the expected RFID
        checkSensorMotorValues(rfidReader.read(), rfid);

        // Display
        StatusDisplay.display(this);
    }

    // Ungrip a package
    public void ungripPackage() {
        // Only ungrip if the grip is already gripping
        if (gripSensor.read()) {
            // Ungrip the package
            gripMotor.ungrip();
        }

        // Display
        StatusDisplay.display(this);

        // Check if the grip sensor works
        checkSensorMotorValues(gripSensor.read(), false);
        // Check if the RFID reader does not read an RFID
        checkSensorMotorValues(rfidReader.read(), "");

        // Display
        StatusDisplay.display(this);
    }

    // Get the current position of the robot arm
    public double[] getArmPosition() {
        // Get the rotation position
        double r = Math.round(rotationSensor.read());
        // Get the height position
        double h = Math.round(verticalSensor.read());
        // Get the length position
        double l = Math.round(horizontalSensor.read());

        return new double[] { r, h, l };
    }

    // Notify WMS and send the robot to a safe state if the sensors are not healthy
    public void checkSensorHealth() {
        if (!rfidReader.health() || !gripSensor.health()
                || !rotationSensor.health() || !verticalSensor.health()
                || !horizontalSensor.health()) {
            // Notify WMS
            WMS.notify("The sensors are not healthy");
            // Bring to robot to a safe state
            safeState();
        }
    }

    // Notify WMS and send the robot to a safe state if the motors are not healthy
    public void checkMotorHealth() {
        if (!rotationMotor.health() || !verticalMotor.health()
                || !horizontalMotor.health() || !gripMotor.health()) {
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
        if (sensValue != expValue) {
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