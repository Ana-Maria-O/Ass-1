package src.SmartWarehouse.robotcomponents;
import java.util.Random;

public class MotorsAndSensors {
    Random random = new Random();
    final int MIN = -5;
    final int MAX = 5;
    private RobotArm robotArm;

    // Constructor
    public MotorsAndSensors(RobotArm robotArm) {
        this.robotArm = robotArm;
    }

    /**
     * Motors and sensors
     */
    public class MotorsSensors {
        private boolean health = true;

        public boolean health() {
            return health;
        } 

        public void changeHealth() {
            health =!health;
        }
    }

////////////////////////////////////////////////////////////////

    public class rotationMotor extends MotorsSensors {

        public void rotateArm(double rotation) {
            robotArm.rotation += rotation;
        }

        public void rotateArmFaulty(double rotation) {
            robotArm.rotation += rotation/2;
        }
    }
    public class verticalMotor extends MotorsSensors {

        public void moveArm(double height) {
            robotArm.height += height;
        }

        public void moveArmFaulty(double height) {
            robotArm.height += height/2;
        }
    }
    public class horizontalMotor extends MotorsSensors {

        public void moveArm(double length) {
            robotArm.length += length;
        }

        public void moveArmFaulty(double length) {
            robotArm.length += length/2;
        }
    }

    public class gripMotor extends MotorsSensors {

        public void grip(String rfid) {
            robotArm.gripped = true;
            robotArm.sensGripped = true;
            robotArm.currentRFID = rfid;
        }

        public void grip() {
            robotArm.gripped = true;
            robotArm.sensGripped = true;
        }

        public void ungrip() {
            robotArm.gripped = false;
            robotArm.sensGripped = false;
            robotArm.currentRFID = "";
        }

        public void gripFaulty() {}
        public void ungripFaulty() {}
    }

    public class rotationSensor extends MotorsSensors {

        public double read() {
            double value = robotArm.rotation + (Math.random() - 0.5);
            robotArm.sensRotation = value;
            return value;
        }

        public double readFaulty() {
            double value = robotArm.rotation + (random.nextInt((MAX - MIN) + 1) + MIN);
            robotArm.sensRotation = value;
            return value;
        }
    }

    public class verticalSensor extends MotorsSensors {

        public double read() {
            double value = robotArm.height + (Math.random() - 0.5);
            robotArm.sensHeight = value;
            return value;
        }

        public double readFaulty() {
            double value = robotArm.height + (random.nextInt((MAX - MIN) + 1) + MIN);
            robotArm.sensHeight = value;
            return value;
        }
    }

    public class horizontalSensor extends MotorsSensors {

        public double read() {
            double value = robotArm.length + (Math.random() - 0.5);
            robotArm.sensLength = value;
            return value;
        }

        public double readFaulty() {
            double value = robotArm.length + (random.nextInt((MAX - MIN) + 1) + MIN);
            robotArm.sensLength = value;
            return value;
        }
    }

    public class gripSensor extends MotorsSensors {

        public boolean read() {
            boolean value = robotArm.gripped;
            robotArm.sensGripped = value;
            return value;
        }

        public boolean readFaulty() {
            boolean value =!robotArm.gripped;
            robotArm.sensGripped = value;
            return value;
        }
    }

    public class RFIDReader extends MotorsSensors {
        
        public String read() {
            String value = robotArm.currentRFID;
            robotArm.sensCurrentRFID = value;
            return value;
        }

        public String readFaulty() {
            if (robotArm.currentRFID.length() > 0){
            String value = robotArm.currentRFID.substring(0, robotArm.currentRFID.length() - 1);
            robotArm.sensCurrentRFID = value;
            return value;}
            else {return "a";}
        }
    }
}
