import java.util.Random;

public class MotorsAndSensors {
    static Random random = new Random();
    final static int MIN = -5;
    final static int MAX = 5;
    /**
     * Motors
     */
    public class MotorsSensors {
        private static boolean health = true;

        public static boolean health() {
            return health;
        } 

        public static void changeHealth() {
            health =!health;
        }
    }

////////////////////////////////////////////////////////////////

    public class rotationMotor extends MotorsSensors {

        public static void rotateArm(double rotation) {
            RobotArm.rotation += rotation;
        }

        public static void rotateArmFaulty(double rotation) {
            RobotArm.rotation += rotation/2;
        }
    }
    public class verticalMotor extends MotorsSensors {

        public static void moveArm(double height) {
            RobotArm.height += height;
        }

        public static void moveArmFaulty(double height) {
            RobotArm.height += height/2;
        }
    }
    public class horizontalMotor extends MotorsSensors {

        public static void moveArm(double length) {
            RobotArm.length += length;
        }

        public static void moveArmFaulty(double length) {
            RobotArm.length += length/2;
        }
    }

    public class gripMotor extends MotorsSensors {

        public static void grip(String rfid) {
            RobotArm.gripped = true;
            RobotArm.sensGripped = true;
            RobotArm.currentRFID = rfid;
        }

        public static void grip() {
            RobotArm.gripped = true;
            RobotArm.sensGripped = true;
        }

        public static void ungrip() {
            RobotArm.gripped = false;
            RobotArm.sensGripped = false;
            RobotArm.currentRFID = "";
        }

        public static void gripFaulty() {}
        public static void ungripFaulty() {}
    }

    public class rotationSensor extends MotorsSensors {

        public static double read() {
            double value = RobotArm.rotation + (Math.random() - 0.5);
            RobotArm.sensRotation = value;
            return value;
        }

        public static double readFaulty() {
            double value = RobotArm.rotation + (random.nextInt((MAX - MIN) + 1) + MIN);
            RobotArm.sensRotation = value;
            return value;
        }
    }

    public class verticalSensor extends MotorsSensors {

        public static double read() {
            double value = RobotArm.height + (Math.random() - 0.5);
            RobotArm.sensHeight = value;
            return value;
        }

        public static double readFaulty() {
            double value = RobotArm.height + (random.nextInt((MAX - MIN) + 1) + MIN);
            RobotArm.sensHeight = value;
            return value;
        }
    }

    public class horizontalSensor extends MotorsSensors {

        public static double read() {
            double value = RobotArm.length + (Math.random() - 0.5);
            RobotArm.sensLength = value;
            return value;
        }

        public static double readFaulty() {
            double value = RobotArm.length + (random.nextInt((MAX - MIN) + 1) + MIN);
            RobotArm.sensLength = value;
            return value;
        }
    }

    public class gripSensor extends MotorsSensors {

        public static boolean read() {
            boolean value = RobotArm.gripped;
            RobotArm.sensGripped = value;
            return value;
        }

        public static boolean readFaulty() {
            boolean value =!RobotArm.gripped;
            RobotArm.sensGripped = value;
            return value;
        }
    }

    public class RFIDReader extends MotorsSensors {
        
        public static String read() {
            String value = RobotArm.currentRFID;
            RobotArm.sensCurrentRFID = value;
            return value;
        }

        public static String readFaulty() {
            RobotArm.sensCurrentRFID = "";
            return "";
        }
    }
}
