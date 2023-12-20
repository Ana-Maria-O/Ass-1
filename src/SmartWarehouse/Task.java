package src.SmartWarehouse;

import java.util.List;

public class Task {
    Robot missionRobot;
    int subtask = 0;
    int pathSetForSubtask = -1;
    List<Integer> pathToShelf;
    int shelfLocation;
    int conveyorBeltLocation;
    int[] packageShelfCoord;
    String packetRFID;
    boolean isDone = false;

    public Task(Robot missionRobot, List<Integer> pathToShelf,
            int shelfLocation, int conveyorBeltLocation, int[] packageShelfCoord, String packetRFID) {
        this.missionRobot = missionRobot;
        this.pathToShelf = pathToShelf;
        this.shelfLocation = shelfLocation;
        this.conveyorBeltLocation = conveyorBeltLocation;
        this.packageShelfCoord = packageShelfCoord;
        this.packetRFID = packetRFID;

        missionRobot.setCurrentSelectedPath(pathToShelf);
        missionRobot.setTargetPosition(shelfLocation);
        pathSetForSubtask = subtask;
        WMS.activeRobots.add(missionRobot.index);
        missionRobot.task = this;
    }

    public void timeStep() {
        switch (subtask) {
            case 0:
                // goto shelf
                if (pathSetForSubtask != subtask) {
                    missionRobot.setCurrentSelectedPath(pathToShelf);
                    missionRobot.setTargetPosition(shelfLocation);
                    pathSetForSubtask = subtask;
                }
                if (missionRobot.pathIsComplete()) {
                    subtask++;
                    break;
                }
                System.out.println("curr path: " + missionRobot.currentSelectedPath);
                missionRobot.stepTowardsTarget();
                break;

            case 1:
                // pickup packet
                // We check if the robot's reached the destination of path1 while walking path1
                if (missionRobot.getCurrentPosition() == shelfLocation
                        && missionRobot.getTargetPosition() == shelfLocation) {
                    missionRobot.fetchPackageFromShelf(packageShelfCoord[0], packageShelfCoord[1], packageShelfCoord[2],
                            packetRFID);
                    subtask++;
                }
                break;

            case 2:
                // goto conveyor belt
                if (pathSetForSubtask != subtask) {
                    // Get a route to the conveyor belt and give it to the robot
                    // We can't use the one we had before because the other robots' routes may
                    // differ from when we computed it
                    WMS.activeRobots.remove((Object) missionRobot.index);
                    missionRobot.setCurrentSelectedPath(
                            WMS.computePathICA(shelfLocation, conveyorBeltLocation, missionRobot.index));
                    WMS.activeRobots.add(missionRobot.index);
                    missionRobot.setTargetPosition(conveyorBeltLocation);
                    pathSetForSubtask = subtask;

                }
                if (missionRobot.pathIsComplete()) {
                    subtask++;
                    break;
                }
                missionRobot.stepTowardsTarget();
                break;

            case 3:
                // We check if the robot reached the conveyor belt and the conveyor belt is the
                // target
                if (missionRobot.getCurrentPosition() == conveyorBeltLocation
                        && missionRobot.getTargetPosition() == conveyorBeltLocation) {
                    // The robot places the package on the conveyor belt
                    missionRobot.putPackageOnConveyorBelt(packetRFID);
                    subtask++;

                }
                break;
        }
        if (subtask >= 4) {
            WMS.activeRobots.remove((Object) missionRobot.index);
            isDone = true;
        }
    }
}
