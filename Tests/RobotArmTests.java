package Tests;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.contrib.java.lang.system.SystemOutRule;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import org.mockito.Spy;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.stubbing.Answer;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;

import src.SmartWarehouse.MotorsAndSensors;
import src.SmartWarehouse.RobotArm;
import src.SmartWarehouse.MotorsAndSensors.rotationMotor;
import src.SmartWarehouse.WMS;
import src.SmartWarehouse.Packet;

@RunWith(MockitoJUnitRunner.class)
public class RobotArmTests {
    private double value = 20;
    private String rfid = "abc123";
    private RobotArm robotArm;
    private RobotArm spy;
    // Rules
    @Rule
    public final SystemOutRule systemOutRule = new SystemOutRule().enableLog();

    // @Rule
    // public final ExpectedSystemExit exit = ExpectedSystemExit.none();

    // @Spy
    // private RobotArm robotArmSpy = spy(new RobotArm(new Packet[0]));

    // @InjectMocks
    // private MotorsAndSensors motorsAndSensors = new MotorsAndSensors(robotArm);

    // @Mock
    // private MotorsAndSensors.rotationMotor rotationMotorMock;

    @Test
    public void fetchPackageTest() {
        // Instantiante a robot arm
        robotArm = new RobotArm(new Packet[0]);
        // Create a spy for robotArm
        spy = spy(robotArm);

        // Call the fetchPackage() function an spy on it
        spy.fetchPackage(value, value, value, rfid);

        // Verify that all the necessary functions for fetching the package have been
        // called
        verify(spy).checkMotorHealth();
        verify(spy).checkSensorHealth();
        verify(spy, times(2)).checkSensorMotorValues(any(Double.class), any(Double.class), eq("rotation"));
        verify(spy, times(2)).checkSensorMotorValues(any(Double.class), any(Double.class), eq("height"));
        verify(spy, times(2)).checkSensorMotorValues(any(Double.class), any(Double.class), eq("length"));
        verify(spy).checkSensorMotorValues(any(String.class), eq(rfid));
        verify(spy, times(2)).checkSensorMotorValues(any(String.class), eq(""));
        verify(spy).checkSensorMotorValues(any(Boolean.class), eq(true));
        verify(spy, times(2)).checkSensorMotorValues(any(Boolean.class), eq(false));
        verify(spy).takePackageFromShelf(value, value, value, rfid);
        // verify(spy).placePackageInRobotStorage();

        // Verify that the system was never put in a safe state
        verify(spy, never()).safeState();

        // Check if the successful fetch message is output
        assertTrue(systemOutRule.getLog().contains("Package " + rfid + " fetched successfully"));

    }

    // Test takePackageFromShelf()

    // Test placePackageInRobotStorage()

    // Test computeNeededMovement()

    // Test moveArm()

    // Test gripPackage()

    // Test ungripPackage()

    // Test getArmPosition()

    // Test checkSensorHealth()

    // Test checkMotorHealth()

    // Test checkSensorMotorValues()

    // Test safeState()
    // @Test
    // public void safeStateTest() {
    // final int[] exitStatus = new int[1];
    // int expectedExitStatus = 0;
    // // Set up a security manager to catch the System.exit() call
    // SecurityManager originalSecurityManager = System.getSecurityManager();

    // System.setSecurityManager(new SecurityManager() {
    // @Override
    // public void checkExit(int status) {
    // super.checkExit(status);
    // exitStatus[0] = status; // Store the exit status
    // throw new SecurityException("System.exit(" + status + ") called");
    // }
    // });

    // try {
    // robotArm.safeState();
    // fail("Should have thrown SecurityException");
    // } catch (SecurityException e) {
    // assertEquals(expectedExitStatus, exitStatus[0]);
    // } finally {
    // // Restore the original security manager
    // System.setSecurityManager(originalSecurityManager);
    // }
    // }

    // Test the robot arm's behaviour when the rotation motor is faulty
    @Test
    public void faultyRotationMotorTest() {
        value = 20;
        rfid = "abc123";
        // Instantiate the robot arm mock
        RobotArm robotArmSpy = spy(new RobotArm(new Packet[0]));

        // Instantiate the rotation motor mock
        MotorsAndSensors.rotationMotor rotationMotorMock = mock(MotorsAndSensors.rotationMotor.class);

        // Inject the rotation motor mock into the robotArmMock
        robotArmSpy.rotationMotor = rotationMotorMock;

        // Mimick the motor's real actions
        doCallRealMethod().when(robotArmSpy.rotationMotor).health();

        // Test the robot arm's behaviour when the rotation motor is faulty the first
        // time it's used
        doAnswer(invocation -> {
            Boolean firstTime = true;
            if (firstTime) {
                robotArmSpy.rotationMotor.rotateArmFaulty(value);
                firstTime = false;
            } else {
                invocation.callRealMethod();
            }
            return null;
        }).when(robotArmSpy.rotationMotor).rotateArm(anyDouble());

        // assertTrue(robotArmSpy.gripMotor.health());
        assertFalse(robotArmSpy.rotationMotor.health());
        // Call the robot arm to rotate
        // robotArmSpy.fetchPackage(value, value, value, rfid);
        // verify(rotationMotorMock, times(1)).rotateArm(value); //
        // assertTrue(systemOutRule.getLog().contains("The rotation sensor value does not match the expected value"));
        // verify(robotArmSpy).safeState();

        // // Test the robot arm's behaviour when the rotation motor is faulty the second
        // // time it's used
        // doAnswer(invocation -> {
        //     Boolean secondTime = false;
        //     if (secondTime) {
        //         robotArmSpy.rotationMotor.rotateArmFaulty(value);
        //         secondTime = false;
        //     } else {
        //         invocation.callRealMethod();
        //         secondTime = true;
        //     }
        //     return null;
        // }).when(robotArmSpy.rotationMotor).rotateArm(anyDouble());

        // robotArmSpy.fetchPackage(value, value, value, rfid);
        // verify(rotationMotorMock, times(2)).rotateArm(value); //
        // assertTrue(systemOutRule.getLog().contains("The rotation sensor value does not match the expected value"));
        // verify(robotArmSpy).safeState();
    }

    // Test the robot arm's behaviour when the vertical motor is faulty
    @Test
    public void faultyVerticalMotorTest() {
        value = 20;
        rfid = "abc123";

        // Instantiate the robot arm mock
        RobotArm robotArmSpy = spy(new RobotArm(new Packet[0]));

        // Instantiate the vertical motor mock
        MotorsAndSensors.verticalMotor verticalMotorMock = mock(MotorsAndSensors.verticalMotor.class);

        // Inject the vertical motor mock into the robotArmMock
        robotArmSpy.verticalMotor = verticalMotorMock;

        // Test the robot arm's behaviour when the vertical motor is faulty the first
        // time it's used
        doAnswer(invocation -> {
            Boolean firstTime = true;
            if (firstTime) {
                robotArmSpy.verticalMotor.moveArmFaulty(value);
                firstTime = false;
            } else {
                invocation.callRealMethod();
            }
            return null;
        }).when(robotArmSpy.verticalMotor).moveArm(anyDouble());

        // Call the robot arm to move
        robotArmSpy.fetchPackage(value, value, value, rfid);
        verify(verticalMotorMock, times(1)).moveArm(value); //
        assertTrue(systemOutRule.getLog().contains("The height sensor value does not match the expected value"));
        verify(robotArmSpy).safeState();

        // Test the robot arm's behaviour when the vertical motor is faulty the second
        // time it's used
        doAnswer(invocation -> {
            Boolean secondTime = false;
            if (secondTime) {
                robotArmSpy.verticalMotor.moveArmFaulty(value);
                secondTime = false;
            } else {
                invocation.callRealMethod();
                secondTime = true;
            }
            return null;
        }).when(robotArmSpy.verticalMotor).moveArm(anyDouble());

        robotArmSpy.fetchPackage(value, value, value, rfid);
        verify(verticalMotorMock, times(2)).moveArm(value); //
        assertTrue(systemOutRule.getLog().contains("The height sensor value does not match the expected value"));
        verify(robotArmSpy).safeState();
    }

    @Test
    public void faultyHorizontalMotorTest() {
        value = 20;
        rfid = "abc123";

        // Instantiate the robot arm mock
        RobotArm robotArmSpy = spy(new RobotArm(new Packet[0]));

        // Instantiate the horizontal motor mock
        MotorsAndSensors.horizontalMotor horizontalMotorMock = mock(MotorsAndSensors.horizontalMotor.class);

        // Inject the horizontal motor mock into the robotArmMock
        robotArmSpy.horizontalMotor = horizontalMotorMock;

        // Test the robot arm's behaviour when the horizontal motor is faulty the first
        // time it's used
        doAnswer(invocation -> {
            Boolean firstTime = true;
            if (firstTime) {
                robotArmSpy.horizontalMotor.moveArmFaulty(value);
                firstTime = false;
            } else {
                invocation.callRealMethod();
            }
            return null;
        }).when(robotArmSpy.horizontalMotor).moveArm(anyDouble());

        // Call the robot arm to move
        robotArmSpy.fetchPackage(value, value, value, rfid);
        verify(horizontalMotorMock, times(1)).moveArm(value); //
        assertTrue(systemOutRule.getLog().contains("The length sensor value does not match the expected value"));
        verify(robotArmSpy).safeState();

        // Test the robot arm's behaviour when the horizontal motor is faulty the second
        // time it's used
        doAnswer(invocation -> {
            Boolean secondTime = false;
            if (secondTime) {
                robotArmSpy.horizontalMotor.moveArmFaulty(value);
                secondTime = false;
            } else {
                invocation.callRealMethod();
                secondTime = true;
            }
            return null;
        }).when(robotArmSpy.horizontalMotor).moveArm(anyDouble());

        robotArmSpy.fetchPackage(value, value, value, rfid);
        verify(horizontalMotorMock, times(2)).moveArm(value); //
        assertTrue(systemOutRule.getLog().contains("The length sensor value does not match the expected value"));
        verify(robotArmSpy).safeState();
    }

    @Test
    public void faultyGripMotorTest() {
        value = 20;
        rfid = "abc123";

        // Instantiate the robot arm mock
        RobotArm robotArmSpy = spy(new RobotArm(new Packet[0]));

        // Instantiate the grip motor mock
        MotorsAndSensors.gripMotor gripMotorMock = mock(MotorsAndSensors.gripMotor.class);

        // Inject the grip sensor mock into the robotArmMock
        robotArmSpy.gripMotor = gripMotorMock;

        // Test the robot arm's behaviour when the grip motor can't grip
        doAnswer(invocation -> {
            robotArmSpy.gripMotor.gripFaulty();
            return null;
        }).when(robotArmSpy.gripMotor).grip(rfid);

        // Call the robot arm to move
        robotArmSpy.fetchPackage(value, value, value, rfid);
        verify(gripMotorMock, times(1)).grip(rfid); //
        assertTrue(systemOutRule.getLog().contains("The grip sensor value does not match the expected value"));
        assertEquals(robotArmSpy.sensCurrentRFID, "");
        verify(robotArmSpy).safeState();

        doCallRealMethod().when(robotArmSpy.gripMotor).grip();

        // Test the robot arm's behaviour when the grip motor can't ungrip
        doAnswer(invocation -> {
            robotArmSpy.gripMotor.ungripFaulty();
            return null;
        }).when(robotArmSpy.gripMotor).ungrip();

        // Call the robot arm to move
        robotArmSpy.fetchPackage(value, value, value, rfid);
        verify(gripMotorMock, times(1)).ungrip(); //
        assertTrue(systemOutRule.getLog().contains("The grip sensor value does not match the expected value"));
        assertEquals(robotArmSpy.sensCurrentRFID, rfid);
        verify(robotArmSpy).safeState();
    }

    // Test the robot arm's behaviour when the rotation sensor is faulty
    @Test
    public void faultyRotationSensorTest() {
        value = 20;
        rfid = "abc123";

        // Instantiate the robot arm mock
        RobotArm robotArmSpy = spy(new RobotArm(new Packet[0]));
        // Instantiate the rotation sensor mock
        MotorsAndSensors.rotationSensor rotationSensorMock = mock(MotorsAndSensors.rotationSensor.class);
        // Inject the rotation sensor mock into the robotArmMock
        robotArmSpy.rotationSensor = rotationSensorMock;

        // Test the robot arm's behaviour when the rotation sensor is faulty
        doAnswer(invocation -> {
            robotArmSpy.rotationSensor.readFaulty();
            return null;
        }).when(robotArmSpy.rotationSensor).read();

        // Call the robot arm to move
        robotArmSpy.fetchPackage(value, value, value, rfid);
        verify(rotationSensorMock, times(1)).read(); //
        assertTrue(systemOutRule.getLog().contains("The rotation sensor value does not match the expected value"));
        verify(robotArmSpy).safeState();
    }

    // Test the robot arm's behaviour when the height sensor is faulty
    @Test
    public void faultyHeightSensorTest() {
        value = 20;
        rfid = "abc123";

        // Instantiate the robot arm mock
        RobotArm robotArmSpy = spy(new RobotArm(new Packet[0]));
        // Instantiate the height sensor mock
        MotorsAndSensors.verticalSensor verticalSensorMock = mock(MotorsAndSensors.verticalSensor.class);
        // Inject the height sensor mock into the robotArmMock
        robotArmSpy.verticalSensor = verticalSensorMock;

        // Test the robot arm's behaviour when the height sensor is faulty
        doAnswer(invocation -> {
            robotArmSpy.verticalSensor.readFaulty();
            return null;
        }).when(robotArmSpy.verticalSensor).read();

        // Call the robot arm to move
        robotArmSpy.fetchPackage(value, value, value, rfid);
        verify(verticalSensorMock, times(1)).read(); //
        assertTrue(systemOutRule.getLog().contains("The height sensor value does not match the expected value"));
        verify(robotArmSpy).safeState();
    }

    // Test the robot arm's behaviour when the horizontal sensor is faulty
    @Test
    public void faultyHorizontalSensorTest() {
        value = 20;
        rfid = "abc123";

        // Instantiate the robot arm mock
        RobotArm robotArmSpy = spy(new RobotArm(new Packet[0]));
        // Instantiate the horizontal sensor mock
        MotorsAndSensors.horizontalSensor horizontalSensorMock = mock(MotorsAndSensors.horizontalSensor.class);
        // Inject the horizontal sensor mock into the robotArmMock
        robotArmSpy.horizontalSensor = horizontalSensorMock;

        // Test the robot arm's behaviour when the horizontal sensor is faulty
        doAnswer(invocation -> {
            robotArmSpy.horizontalSensor.readFaulty();
            return null;
        }).when(robotArmSpy.horizontalSensor).read();

        // Call the robot arm to move
        robotArmSpy.fetchPackage(value, value, value, rfid);
        verify(horizontalSensorMock, times(1)).read(); //
        assertTrue(systemOutRule.getLog().contains("The length sensor value does not match the expected value"));
        verify(robotArmSpy).safeState();
    }

    // Test the robot arm's behaviour when the grip sensor is faulty
    @Test
    public void faultyGripSensorTest() {
        value = 20;
        rfid = "abc123";

        // Instantiate the robot arm mock
        RobotArm robotArmSpy = spy(new RobotArm(new Packet[0]));
        // Instantiate the grip sensor mock
        MotorsAndSensors.gripSensor gripSensorMock = mock(MotorsAndSensors.gripSensor.class);
        // Inject the grip sensor mock into the robotArmMock
        robotArmSpy.gripSensor = gripSensorMock;

        // Test the robot arm's behaviour when the grip sensor is faulty
        doAnswer(invocation -> {
            robotArmSpy.gripSensor.readFaulty();
            return null;
        }).when(robotArmSpy.gripSensor).read();

        // Call the robot arm to move
        robotArmSpy.fetchPackage(value, value, value, rfid);
        verify(gripSensorMock, times(1)).read(); //
        assertTrue(systemOutRule.getLog().contains("The grip sensor value does not match the expected value"));
        verify(robotArmSpy).safeState();

    }

    // Test the robot arm's behaviour when the RFID reader is faulty
    @Test
    public void faultyRFIDReaderTest() {
        value = 20;
        rfid = "abc123";

        // Instantiate the robot arm mock
        RobotArm robotArmSpy = spy(new RobotArm(new Packet[0]));
        // Instantiate the RFID reader mock
        MotorsAndSensors.RFIDReader rfidReaderMock = mock(MotorsAndSensors.RFIDReader.class);
        // Inject the RFID reader mock into the robotArmMock
        robotArmSpy.rfidReader = rfidReaderMock;

        // Test the robot arm's behaviour when the RFID reader is faulty
        doAnswer(invocation -> {
            robotArmSpy.rfidReader.readFaulty();
            return null;
        }).when(robotArmSpy.rfidReader).read();

        // Call the robot arm to move
        robotArmSpy.fetchPackage(value, value, value, rfid);
        verify(rfidReaderMock, times(1)).read(); //
        assertTrue(systemOutRule.getLog().contains("The read RFID value does not match the expected value"));
        verify(robotArmSpy).safeState();
    }

    // Test the robot arm's behaviour when the rotation motor and sensor are faulty
    @Test
    public void faultyRotationMotorAndSensorTest() {
        value = 20;
        rfid = "abc123";

        // Instantiate the robot arm mock
        RobotArm robotArmSpy = spy(new RobotArm(new Packet[0]));
        // Instantiate the rotation motor and sensor mock
        MotorsAndSensors.rotationMotor rotationMotorMock = mock(MotorsAndSensors.rotationMotor.class);
        MotorsAndSensors.rotationSensor rotationSensorMock = mock(MotorsAndSensors.rotationSensor.class);
        // Inject the rotation motor and sensor mock into the robotArmMock
        robotArmSpy.rotationMotor = rotationMotorMock;
        robotArmSpy.rotationSensor = rotationSensorMock;

        // Test the robot arm's behaviour when the rotation motor and sensor are faulty
        doAnswer(invocation -> {
            robotArmSpy.rotationMotor.rotateArmFaulty(value);
            return null;
        }).when(robotArmSpy.rotationMotor).rotateArm(value);

        doAnswer(invocation -> {
            robotArmSpy.rotationSensor.readFaulty();
            return null;
        }).when(robotArmSpy.rotationSensor).read();

        // Call the robot arm to move
        robotArmSpy.fetchPackage(value, value, value, rfid);
        verify(rotationMotorMock, times(1)).rotateArm(value); //
        verify(rotationSensorMock, times(1)).read(); //
        assertTrue(systemOutRule.getLog().contains("The rotation sensor value does not match the expected value"));
        verify(robotArmSpy).safeState();
    }

    // Test the robot arm's behaviour when the vertical motor and height sensor are
    // faulty
    @Test
    public void faultyVerticalMotorAndSensorTest() {
        value = 20;
        rfid = "abc123";

        // Instantiate the robot arm mock
        RobotArm robotArmSpy = spy(new RobotArm(new Packet[0]));
        // Instantiate the vertical motor and sensor mock
        MotorsAndSensors.verticalMotor verticalMotorMock = mock(MotorsAndSensors.verticalMotor.class);
        MotorsAndSensors.verticalSensor verticalSensorMock = mock(MotorsAndSensors.verticalSensor.class);
        // Inject the vertical motor and sensor mock into the robotArmMock
        robotArmSpy.verticalMotor = verticalMotorMock;
        robotArmSpy.verticalSensor = verticalSensorMock;

        // Test the robot arm's behaviour when the vertical motor and height sensor are
        // faulty
        doAnswer(invocation -> {
            robotArmSpy.verticalMotor.moveArmFaulty(value);
            return null;
        }).when(robotArmSpy.verticalMotor).moveArm(value);

        doAnswer(invocation -> {
            robotArmSpy.verticalSensor.readFaulty();
            return null;
        }).when(robotArmSpy.verticalSensor).read();

        // Call the robot arm to move
        robotArmSpy.fetchPackage(value, value, value, rfid);
        verify(verticalMotorMock, times(1)).moveArm(value); //
        verify(verticalSensorMock, times(1)).read(); //
        assertTrue(systemOutRule.getLog().contains("The height sensor value does not match the expected value"));
        verify(robotArmSpy).safeState();
    }

    // Test the robot arm's behaviour when the horizontal motor and length sensor
    // are faulty
    @Test
    public void faultyHorizontalMotorAndSensorTest() {
        value = 20;
        rfid = "abc123";

        // Instantiate the robot arm mock
        RobotArm robotArmSpy = spy(new RobotArm(new Packet[0]));
        // Instantiate the horizontal motor and sensor mock
        MotorsAndSensors.horizontalMotor horizontalMotorMock = mock(MotorsAndSensors.horizontalMotor.class);
        MotorsAndSensors.horizontalSensor horizontalSensorMock = mock(MotorsAndSensors.horizontalSensor.class);
        // Inject the horizontal motor and sensor mock into the robotArmMock
        robotArmSpy.horizontalMotor = horizontalMotorMock;
        robotArmSpy.horizontalSensor = horizontalSensorMock;

        // Test the robot arm's behaviour when the horizontal motor and length sensor
        // are faulty
        doAnswer(invocation -> {
            robotArmSpy.horizontalMotor.moveArmFaulty(value);
            return null;
        }).when(robotArmSpy.horizontalMotor).moveArm(value);

        doAnswer(invocation -> {
            robotArmSpy.horizontalSensor.readFaulty();
            return null;
        }).when(robotArmSpy.horizontalSensor).read();

        // Call the robot arm to move
        robotArmSpy.fetchPackage(value, value, value, rfid);
        verify(horizontalMotorMock, times(1)).moveArm(value); //
        verify(horizontalSensorMock, times(1)).read(); //
        assertTrue(systemOutRule.getLog().contains("The length sensor value does not match the expected value"));
        verify(robotArmSpy).safeState();
    }

    // Test the robot arm's behaviour when the grip motor and sensor are faulty
    @Test
    public void faultyGripMotorAndSensorTest() {
        value = 20;
        rfid = "abc123";

        // Instantiate the robot arm mock
        RobotArm robotArmSpy = spy(new RobotArm(new Packet[0]));
        // Instantiate the grip motor and sensor mock
        MotorsAndSensors.gripMotor gripMotorMock = mock(MotorsAndSensors.gripMotor.class);
        MotorsAndSensors.gripSensor gripSensorMock = mock(MotorsAndSensors.gripSensor.class);
        // Inject the grip motor and sensor mock into the robotArmMock
        robotArmSpy.gripMotor = gripMotorMock;
        robotArmSpy.gripSensor = gripSensorMock;

        // Test the robot arm's behaviour when the grip motor and sensor are faulty
        doAnswer(invocation -> {
            robotArmSpy.gripMotor.gripFaulty();
            return null;
        }).when(robotArmSpy.gripMotor).grip(rfid);

        doAnswer(invocation -> {
            robotArmSpy.gripSensor.readFaulty();
            return null;
        }).when(robotArmSpy.gripSensor).read();

        // Call the robot arm to move
        robotArmSpy.fetchPackage(value, value, value, rfid);
        verify(gripMotorMock, times(1)).grip(); //
        verify(gripSensorMock, times(1)).read(); //
        assertEquals(robotArmSpy.currentRFID, "");
        assertTrue(systemOutRule.getLog().contains("The read RFID value does not match the expected value"));
        verify(robotArmSpy).safeState();

        doCallRealMethod().when(robotArmSpy.gripMotor).grip(rfid);

        doAnswer(invocation -> {
            robotArmSpy.gripMotor.ungripFaulty();
            return null;
        }).when(robotArmSpy.gripMotor).ungrip();

        // Call the robot arm to move
        robotArmSpy.fetchPackage(value, value, value, rfid);
        assertTrue(systemOutRule.getLog().contains("The grip sensor value does not match the expected value"));
        verify(robotArmSpy).safeState();
    }

    // Test the robot arm's behaviour when the grip motor and the RFID reader are
    // faulty
    @Test
    public void faultyGripMotorAndRFIDReaderTest() {
        value = 20;
        rfid = "abc123";

        // Instantiate the robot arm mock
        RobotArm robotArmSpy = spy(new RobotArm(new Packet[0]));
        // Instantiate the grip motor and RFID reader mock
        MotorsAndSensors.gripMotor gripMotorMock = mock(MotorsAndSensors.gripMotor.class);
        MotorsAndSensors.RFIDReader rfidReaderMock = mock(MotorsAndSensors.RFIDReader.class);
        // Inject the grip motor and RFID reader mock into the robotArmMock
        robotArmSpy.gripMotor = gripMotorMock;
        robotArmSpy.rfidReader = rfidReaderMock;

        // Test the robot arm's behaviour when the grip motor and the RFID reader are
        // faulty
        doAnswer(invocation -> {
            robotArmSpy.gripMotor.gripFaulty();
            return null;
        }).when(robotArmSpy.gripMotor).grip(rfid);

        doAnswer(invocation -> {
            robotArmSpy.rfidReader.readFaulty();
            return null;
        }).when(robotArmSpy.rfidReader).read();

        // Call the robot arm to move
        robotArmSpy.fetchPackage(value, value, value, rfid);
        assertTrue(systemOutRule.getLog().contains("The read RFID value does not match the expected value"));
        verify(robotArmSpy).safeState();
    }

    // Test the robot arm's behaviour when the grip sensor and RFID reader are
    // faulty
    @Test
    public void faultyGripSensorAndRFIDReaderTest() {
        value = 20;
        rfid = "abc123";

        // Instantiate the robot arm mock
        RobotArm robotArmSpy = spy(new RobotArm(new Packet[0]));
        // Instantiate the grip sensor and RFID reader mock
        MotorsAndSensors.gripSensor gripSensorMock = mock(MotorsAndSensors.gripSensor.class);
        MotorsAndSensors.RFIDReader rfidReaderMock = mock(MotorsAndSensors.RFIDReader.class);
        // Inject the grip sensor and RFID reader mock into the robotArmMock
        robotArmSpy.gripSensor = gripSensorMock;
        robotArmSpy.rfidReader = rfidReaderMock;

        // Test the robot arm's behaviour when the grip sensor and the RFID reader are
        // faulty
        doAnswer(invocation -> {
            robotArmSpy.gripSensor.readFaulty();
            return null;
        }).when(robotArmSpy.gripSensor).read();

        doAnswer(invocation -> {
            robotArmSpy.rfidReader.readFaulty();
            return null;
        }).when(robotArmSpy.rfidReader).read();

        // Call the robot arm to move
        robotArmSpy.fetchPackage(value, value, value, rfid);
        assertEquals(robotArmSpy.currentRFID, "");
        assertTrue(systemOutRule.getLog().contains("The grip sensor value does not match the expected value"));
        verify(robotArmSpy).safeState();

    }

    // Test the robot arm's behaviour when the grip sensor and reader and the RFID reader are faulty

    @Test
    public void faultyGripMotorSensorAndRFIDReaderTest() {
        value = 20;
        rfid = "abc123";

        // Instantiate the robot arm mock
        RobotArm robotArmSpy = spy(new RobotArm(new Packet[0]));
        // Instantiate the grip motor, sensor and RFID reader mock
        MotorsAndSensors.gripMotor gripMotorMock = mock(MotorsAndSensors.gripMotor.class);
        MotorsAndSensors.gripSensor gripSensorMock = mock(MotorsAndSensors.gripSensor.class);
        MotorsAndSensors.RFIDReader rfidReaderMock = mock(MotorsAndSensors.RFIDReader.class);
        // Inject the grip motor, sensor and RFID reader mock into the robotArmMock
        robotArmSpy.gripMotor = gripMotorMock;
        robotArmSpy.gripSensor = gripSensorMock;
        robotArmSpy.rfidReader = rfidReaderMock;

        // Test the robot arm's behaviour when the grip motor, sensor and the RFID reader are faulty
        doAnswer(invocation -> {
            robotArmSpy.gripMotor.gripFaulty();
            return null;
        }).when(robotArmSpy.gripMotor).grip(rfid);

        doAnswer(invocation -> {
            robotArmSpy.gripSensor.readFaulty();
            return null;
        }).when(robotArmSpy.gripSensor).read();

        doAnswer(invocation -> {
            robotArmSpy.rfidReader.readFaulty();
            return null;
        }).when(robotArmSpy.rfidReader).read();

        // Call the robot arm to move
        robotArmSpy.fetchPackage(value, value, value, rfid);
        assertTrue(systemOutRule.getLog().contains("The grip sensor value does not match the expected value"));
        verify(robotArmSpy).safeState();
    }
}