package Tests;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import org.junit.Test;

import src.SmartWarehouse.Packet;
import src.SmartWarehouse.robotcomponents.MotorsAndSensors;
import src.SmartWarehouse.robotcomponents.RobotArm;
import src.SmartWarehouse.robotcomponents.MotorsAndSensors.MotorsSensors;


public class MotorsAndSensorsTests {

    // Instantiate a RobotArm object
    RobotArm robotArm = new RobotArm(new Packet[0]);

    // Instantiate a MotorsSensors object
    MotorsAndSensors motorsAndSensors = new MotorsAndSensors(robotArm);

    // Instantiate motors and sensors
    MotorsAndSensors.rotationMotor rotationMotor = motorsAndSensors.new rotationMotor();
    MotorsAndSensors.verticalMotor verticalMotor = motorsAndSensors.new verticalMotor();
    MotorsAndSensors.horizontalMotor horizontalMotor = motorsAndSensors.new horizontalMotor();
    MotorsAndSensors.gripMotor gripMotor = motorsAndSensors.new gripMotor();
    MotorsAndSensors.rotationSensor rotationSensor = motorsAndSensors.new rotationSensor();
    MotorsAndSensors.verticalSensor verticalSensor = motorsAndSensors.new verticalSensor();
    MotorsAndSensors.horizontalSensor horizontalSensor = motorsAndSensors.new horizontalSensor();
    MotorsAndSensors.gripSensor gripSensor = motorsAndSensors.new gripSensor();
    MotorsAndSensors.RFIDReader rfidReader = motorsAndSensors.new RFIDReader();


    // Test the health() function
    @Test
    public void healthTest() {
        MotorsSensors motorsSensors= motorsAndSensors.new MotorsSensors();
        assertEquals(true, motorsSensors.health());
    }
    
    // Test the changeHealth() function
    @Test
    public void changeHealthTest() {
        MotorsSensors motorsSensors= motorsAndSensors.new MotorsSensors();
        motorsSensors.changeHealth();
        assertEquals(false, motorsSensors.health());
    }
    
    // Test the rotateArm() function
    @Test
    public void rotateArmTest() {
        int value = 10;

        rotationMotor.rotateArm(value);
        assertEquals(value, robotArm.rotation, 0);
    }

    // Test the rotateArmFaulty() function
    @Test
    public void  rotateArmFaultyTest() {
        int value = 10;
        robotArm.rotation = 0;

        rotationMotor.rotateArmFaulty(value);
        assertNotEquals(value, rotationSensor.read(), 0);
    } 

    // Test the moveArm() functions
    @Test
    public void moveArmTest() {
        int value = 20;
        robotArm.height = 0;
        robotArm.length = 0;

        // Vertical motor
        verticalMotor.moveArm(value);
        assertEquals(value, robotArm.height, 0);

        // Horizontal motor
        horizontalMotor.moveArm(value);
        assertEquals(value, robotArm.length, 0);
    }

    // Test the moveArmFaulty() functions
    @Test
    public void moveArmFaultyTest() {
        int value = 20;
        robotArm.height = 0;
        robotArm.length = 0;

        // Vertical motor
        verticalMotor.moveArmFaulty(value);
        assertNotEquals(value, robotArm.height, 0);

        // Horizontal motor
        horizontalMotor.moveArmFaulty(value);
        assertNotEquals(value, robotArm.length, 0);
    }

    // Test the grip() function
    @Test
    public void gripTest() {
        String rfid = "abc123";
        // Grip with RFID
        gripMotor.grip(rfid);
        assertEquals(true, robotArm.gripped);
        assertEquals(rfid, robotArm.currentRFID);

        // Reset robotArm.gripped
        robotArm.gripped = false;

        // Grip without RFID
        gripMotor.grip();
        assertEquals(true, robotArm.gripped);
        assertEquals("abc123", robotArm.currentRFID);
    }

    // Test the ungrip() function
    @Test
    public void ungripTest() {
        // Set robotArm.gripped to true
        robotArm.gripped = true;

        // Ungrip
        gripMotor.ungrip();
        assertEquals(false, robotArm.gripped);
        assertEquals(false, robotArm.sensGripped);
        assertEquals("", robotArm.currentRFID);
    }

    // Test the gripFaulty() function
    @Test
    public void gripFaultyTest() {
        // Set robotArm.gripped to false
        robotArm.gripped = false;
        // Set robotArm.sensGripped to false
        robotArm.sensGripped = false;
        // Set the current RFID to an empty string
        robotArm.currentRFID = "";

        // Grip
        gripMotor.gripFaulty();
        assertEquals(false, robotArm.gripped);
        assertEquals(false, robotArm.sensGripped);
        assertEquals("", robotArm.currentRFID);
    }

    // Test the ungripFaulty() function
    @Test
    public void ungripFaultyTest() {
        // Set robotArm.gripped to true
        robotArm.gripped = true;
        // Set robotArm.sensGripped to true
        robotArm.sensGripped = true;
        // Set the current RFID to an some string
        robotArm.currentRFID = "abc123";

        // Ungrip
        gripMotor.ungripFaulty();
        assertEquals(true, robotArm.gripped);
        assertEquals(true, robotArm.sensGripped);
        assertEquals("abc123", robotArm.currentRFID);
    }

    // Test the read() functions
    @Test
    public void readTest() {
        // Set an RFID
        robotArm.currentRFID = "abc123";


        // Rotation sensor
        rotationSensor.read();
        assertEquals(robotArm.rotation, robotArm.sensRotation, 0.5);

        // Vertical sensor
        verticalSensor.read();
        assertEquals(robotArm.height, robotArm.sensHeight, 0.5);

        // Horizontal sensor
        horizontalSensor.read();
        assertEquals(robotArm.length, robotArm.sensLength, 0.5);

        // Grip sensor
        gripSensor.read();
        assertEquals(robotArm.gripped, robotArm.sensGripped);

        // RFID reader
        rfidReader.read();
        assertEquals(robotArm.sensCurrentRFID, robotArm.currentRFID);
    }

    // Test the readFaulty() functions
    @Test
    public void readFaultyTest() {
        // Set an RFID
        robotArm.currentRFID = "abc123";

        // Rotation sensor
        rotationSensor.readFaulty();
        assertNotEquals(robotArm.rotation, robotArm.sensRotation, 0.5);

        // Vertical sensor
        verticalSensor.readFaulty();
        assertNotEquals(robotArm.height, robotArm.sensHeight, 0.5);

        // Horizontal sensor
        horizontalSensor.readFaulty();
        assertNotEquals(robotArm.length, robotArm.sensLength, 0.5);

        // Grip sensor
        gripSensor.readFaulty();
        assertNotEquals(robotArm.gripped, robotArm.sensGripped);

        // RFID reader
        rfidReader.readFaulty();
        assertNotEquals(robotArm.sensCurrentRFID, robotArm.currentRFID);
    }
}
