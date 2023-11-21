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
            RobotArm.currentRFID = rfid;
        }

        public static void grip() {
            RobotArm.gripped = true;
        }

        public static void ungrip() {
            RobotArm.gripped = false;
            RobotArm.currentRFID = "";
        }

        public static void gripFaulty() {}
        public static void ungripFaulty() {}
    }

    public class rotationSensor extends MotorsSensors {

        public static double read() {
            return RobotArm.rotation + (Math.random() * 2  - 1);
        }

        public static double readFaulty() {
            return RobotArm.rotation + (random.nextInt((MAX - MIN) + 1) + MIN);
        }
    }

    public class verticalSensor extends MotorsSensors {

        public static double read() {
            return RobotArm.height + (Math.random() * 2  - 1);
        }

        public static double readFaulty() {
            return RobotArm.height + (random.nextInt((MAX - MIN) + 1) + MIN);
        }
    }

    public class horizontalSensor extends MotorsSensors {

        public static double read() {
            return RobotArm.length + (Math.random() * 2  - 1);
        }

        public static double readFaulty() {
            return RobotArm.length + (random.nextInt((MAX - MIN) + 1) + MIN);
        }
    }

    public class gripSensor extends MotorsSensors {

        public static boolean read() {
            return RobotArm.gripped;
        }

        public static boolean readFaulty() {
            return !RobotArm.gripped;
        }
    }

    public class RFIDReader extends MotorsSensors {
        
        public static String read() {
            return RobotArm.currentRFID;
        }

        public static String readFaulty() {
            return "";
        }
    }
}
