public class StatusDisplay {
    public static void main(String[] args){
        Thread displayThread = new Thread(new Runnable() {
            @Override
            public void run() {
                double previousRotation = RobotArm.rotation;
                double previousHeight = RobotArm.height;
                double previousLength = RobotArm.length;
                boolean previousGrip = RobotArm.gripped;

                while (true) {
                    // Check for changes in variables
                    if (RobotArm.rotation != previousRotation || RobotArm.height!= previousHeight || RobotArm.length!= previousLength || RobotArm.gripped!= previousGrip) {
                        System.out.println("Current rotation (rad): " + RobotArm.rotation);
                        System.out.println("Current height (cm): " + RobotArm.height);
                        System.out.println("Current length (cm): " + RobotArm.length);
                        System.out.println("Gripped: " + RobotArm.gripped);
                    }
                    
                }
            }
        });

        displayThread.start();
    }
}
